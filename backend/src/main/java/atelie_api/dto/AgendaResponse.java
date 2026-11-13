package atelie_api.dto;

import java.util.List;

/**
 * Agenda de visitas de um ateliê num período.
 * "dias" traz SÓ os dias com pelo menos um horário livre;
 * qualquer outro dia do período é indisponível.
 */
public record AgendaResponse(
        String nomeAtelie,
        boolean aceitaVisitas,
        String horaAbertura,
        String horaFechamento,
        List<DiaLivre> dias
) {
    /** data no formato "2026-11-10"; horarios como "10:00", "11:00"... */
    public record DiaLivre(String data, List<String> horarios) { }
}
