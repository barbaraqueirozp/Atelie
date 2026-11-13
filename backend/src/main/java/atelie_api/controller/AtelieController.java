package atelie_api.controller;

import atelie_api.dto.AtelieDados;
import atelie_api.model.Atelie;
import atelie_api.model.Usuario;
import atelie_api.repository.AtelieRepository;
import atelie_api.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios/{id}/atelie")
public class AtelieController {

    private static final Set<String> ESPECIALIDADES = Set.of("Pintura", "Escultura", "Fotografia", "Digital");

    private final AtelieRepository atelies;
    private final UsuarioRepository usuarios;

    public AtelieController(AtelieRepository atelies, UsuarioRepository usuarios) {
        this.atelies = atelies;
        this.usuarios = usuarios;
    }

    /** GET /usuarios/{id}/atelie — dados do ateliê (ou o endereço do usuário, se ainda não existir). */
    @GetMapping
    public AtelieDados buscar(@PathVariable Integer id) {
        Usuario u = buscarUsuario(id);
        return atelies.findByIdUsuario(id)
                .map(a -> new AtelieDados(true, a.getNome(), a.getEspecialidade(), a.getCep(),
                        a.getLogradouro(), a.getNumero(), a.getComplemento(), a.getBairro(),
                        a.getCidade(), a.getEstado(), a.getLatitude(), a.getLongitude(), a.isAceitaVisitas(),
                        a.getDiasAtendimento(), hora(a.getHoraAbertura()), hora(a.getHoraFechamento())))
                .orElseGet(() -> new AtelieDados(false, null, null, u.getCep(), u.getLogradouro(),
                        u.getNumero(), u.getComplemento(), u.getBairro(), u.getCidade(), u.getEstado(),
                        null, null, true, "2,3,4,5,6", "10:00", "18:00"));
    }

    /** PUT /usuarios/{id}/atelie — cria ou atualiza o ateliê e transforma a conta em vendedor. */
    @PutMapping
    @Transactional
    public AtelieDados salvar(@PathVariable Integer id, @RequestBody AtelieDados dados) {
        Usuario u = buscarUsuario(id);

        if (dados.nome() == null || dados.nome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o nome do ateliê");
        }
        if (!ESPECIALIDADES.contains(dados.especialidade())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Escolha a especialidade");
        }
        if (vazio(dados.cep()) || vazio(dados.logradouro()) || vazio(dados.numero())
                || vazio(dados.bairro()) || vazio(dados.cidade()) || vazio(dados.estado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Preencha o endereço do ateliê");
        }

        Atelie a = atelies.findByIdUsuario(id).orElseGet(Atelie::new);
        a.setIdUsuario(id);
        a.setNome(dados.nome().trim());
        a.setEspecialidade(dados.especialidade());
        a.setCep(dados.cep().trim());
        a.setLogradouro(dados.logradouro().trim());
        a.setNumero(dados.numero().trim());
        a.setComplemento(vazio(dados.complemento()) ? null : dados.complemento().trim());
        a.setBairro(dados.bairro().trim());
        a.setCidade(dados.cidade().trim());
        a.setEstado(dados.estado().trim().toUpperCase());
        a.setLatitude(dados.latitude());
        a.setLongitude(dados.longitude());
        a.setAceitaVisitas(dados.aceitaVisitas() == null || dados.aceitaVisitas());

        // ---------- dias e horário de visita ----------
        if (dados.diasAtendimento() != null) {
            a.setDiasAtendimento(normalizarDias(dados.diasAtendimento()));
        }
        LocalTime abre = lerHora(dados.horaAbertura(), a.getHoraAbertura());
        LocalTime fecha = lerHora(dados.horaFechamento(), a.getHoraFechamento());
        if (!fecha.isAfter(abre.plusMinutes(59))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O horário de fechamento deve ser pelo menos 1 hora depois da abertura");
        }
        if (a.isAceitaVisitas() && a.getDiasAtendimento().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Escolha pelo menos um dia de visita");
        }
        a.setHoraAbertura(abre);
        a.setHoraFechamento(fecha);
        atelies.save(a);

        // Quem cadastra um ateliê passa a ser vendedor
        if (!"vendedor".equals(u.getTipoConta())) {
            u.setTipoConta("vendedor");
            usuarios.save(u);
        }
        return buscar(id);
    }

    private Usuario buscarUsuario(Integer id) {
        return usuarios.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    /** "6,2, 3,9" -> "2,3,6" (só valores de 1 a 7, sem repetir, em ordem). */
    private static String normalizarDias(String dias) {
        TreeSet<Integer> validos = new TreeSet<>();
        for (String parte : dias.split(",")) {
            try {
                int d = Integer.parseInt(parte.trim());
                if (d >= 1 && d <= 7) validos.add(d);
            } catch (NumberFormatException ignorada) {
                // ignora valores inválidos
            }
        }
        return validos.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private static LocalTime lerHora(String texto, LocalTime padrao) {
        if (texto == null || texto.isBlank()) return padrao;
        try {
            return LocalTime.parse(texto.trim()).withSecond(0).withNano(0);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Horário inválido: " + texto);
        }
    }

    private static String hora(LocalTime t) {
        return t == null ? null : String.format("%02d:%02d", t.getHour(), t.getMinute());
    }

    private static boolean vazio(String s) {
        return s == null || s.isBlank();
    }
}
