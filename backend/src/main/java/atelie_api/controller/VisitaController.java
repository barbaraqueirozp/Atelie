package atelie_api.controller;

import atelie_api.dto.AgendaResponse;
import atelie_api.dto.VisitaRequest;
import atelie_api.model.Atelie;
import atelie_api.repository.AtelieRepository;
import atelie_api.repository.UsuarioRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Agenda e agendamento de visitas ao ateliê de um artista.
 *   GET  /usuarios/{idArtista}/atelie/agenda?inicio=2026-11-01&dias=30
 *   POST /usuarios/{idArtista}/atelie/visitas
 * Cada visita dura 1 hora.
 */
@RestController
@RequestMapping("/usuarios/{idArtista}/atelie")
public class VisitaController {

    /** Antecedência mínima para marcar uma visita. */
    private static final int ANTECEDENCIA_HORAS = 2;
    /** Até quantos dias à frente dá para agendar. */
    private static final int LIMITE_DIAS = 90;

    private final AtelieRepository atelies;
    private final UsuarioRepository usuarios;
    private final JdbcTemplate jdbc;

    public VisitaController(AtelieRepository atelies, UsuarioRepository usuarios, JdbcTemplate jdbc) {
        this.atelies = atelies;
        this.usuarios = usuarios;
        this.jdbc = jdbc;
    }

    @GetMapping("/agenda")
    public AgendaResponse agenda(@PathVariable Integer idArtista,
                                 @RequestParam String inicio,
                                 @RequestParam(defaultValue = "31") int dias) {
        Atelie a = buscarAtelie(idArtista);
        LocalDate primeiro = lerData(inicio);
        dias = Math.max(1, Math.min(dias, 62));

        List<AgendaResponse.DiaLivre> livres = new ArrayList<>();
        if (a.isAceitaVisitas()) {
            Set<LocalDateTime> ocupados = horariosOcupados(a.getIdAtelie(),
                    primeiro.atStartOfDay(), primeiro.plusDays(dias).atStartOfDay());

            for (int i = 0; i < dias; i++) {
                LocalDate dia = primeiro.plusDays(i);
                List<String> horarios = new ArrayList<>();
                for (LocalDateTime horario : horariosDoDia(a, dia)) {
                    if (!ocupados.contains(horario)) {
                        horarios.add(String.format("%02d:00", horario.getHour()));
                    }
                }
                if (!horarios.isEmpty()) {
                    livres.add(new AgendaResponse.DiaLivre(dia.toString(), horarios));
                }
            }
        }
        return new AgendaResponse(a.getNome(), a.isAceitaVisitas(),
                hora(a.getHoraAbertura()), hora(a.getHoraFechamento()), livres);
    }

    @PostMapping("/visitas")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> agendar(@PathVariable Integer idArtista, @RequestBody VisitaRequest req) {
        Atelie a = buscarAtelie(idArtista);

        if (req.idVisitante() == null || !usuarios.existsById(req.idVisitante())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Visitante inválido");
        }
        if (req.idVisitante().equals(idArtista)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Você não pode agendar visita no seu próprio ateliê");
        }
        if (!a.isAceitaVisitas()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este ateliê não está recebendo visitas");
        }

        LocalDateTime quando;
        try {
            quando = LocalDateTime.parse(req.dataHora()).withMinute(0).withSecond(0).withNano(0);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data e hora inválidas");
        }

        // O horário precisa existir na agenda (dia de atendimento, dentro do expediente, no futuro)
        if (!horariosDoDia(a, quando.toLocalDate()).contains(quando)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este horário não está disponível");
        }

        try {
            Integer idVisita = jdbc.queryForObject("""
                    INSERT INTO public.visita (id_atelie, id_visitante, data_hora)
                    VALUES (?, ?, ?)
                    RETURNING id_visita
                    """, Integer.class, a.getIdAtelie(), req.idVisitante(), Timestamp.valueOf(quando));
            return Map.of("idVisita", idVisita, "dataHora", quando.toString(), "status", "pendente");
        } catch (DuplicateKeyException e) {
            // Outra pessoa reservou este horário um instante antes
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este horário acabou de ser reservado. Escolha outro.");
        }
    }

    // =========================
    // AUXILIARES
    // =========================

    /** Horários de início de visita num dia, já descontando dias fechados, passado e antecedência. */
    private List<LocalDateTime> horariosDoDia(Atelie a, LocalDate dia) {
        LocalDateTime agora = LocalDateTime.now();
        if (dia.isBefore(agora.toLocalDate()) || dia.isAfter(agora.toLocalDate().plusDays(LIMITE_DIAS))) {
            return List.of();
        }
        Set<Integer> diasAbertos = new HashSet<>();
        for (String d : a.getDiasAtendimento().split(",")) {
            if (!d.isBlank()) diasAbertos.add(Integer.parseInt(d.trim()));
        }
        if (!diasAbertos.contains(dia.getDayOfWeek().getValue())) {
            return List.of();
        }

        List<LocalDateTime> horarios = new ArrayList<>();
        LocalTime t = a.getHoraAbertura().withMinute(0);
        while (!t.plusHours(1).isAfter(a.getHoraFechamento())) {
            LocalDateTime horario = dia.atTime(t);
            if (horario.isAfter(agora.plusHours(ANTECEDENCIA_HORAS))) {
                horarios.add(horario);
            }
            t = t.plusHours(1);
            if (t.equals(LocalTime.MIDNIGHT)) break;
        }
        return horarios;
    }

    private Set<LocalDateTime> horariosOcupados(Integer idAtelie, LocalDateTime de, LocalDateTime ate) {
        return new HashSet<>(jdbc.query("""
                SELECT data_hora FROM public.visita
                WHERE id_atelie = ? AND data_hora >= ? AND data_hora < ?
                  AND status IN ('pendente', 'confirmada')
                """, (rs, n) -> rs.getTimestamp(1).toLocalDateTime(),
                idAtelie, Timestamp.valueOf(de), Timestamp.valueOf(ate)));
    }

    private Atelie buscarAtelie(Integer idArtista) {
        return atelies.findByIdUsuario(idArtista).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Este artista ainda não cadastrou o ateliê"));
    }

    private static LocalDate lerData(String texto) {
        try {
            return LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data inválida: " + texto);
        }
    }

    private static String hora(LocalTime t) {
        return String.format("%02d:%02d", t.getHour(), t.getMinute());
    }
}
