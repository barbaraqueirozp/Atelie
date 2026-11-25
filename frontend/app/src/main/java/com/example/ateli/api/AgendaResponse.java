package com.example.ateli.api;

import java.util.List;

/** Resposta de GET /usuarios/{id}/atelie/agenda. Só vêm os dias com horário livre. */
public class AgendaResponse {
    public String nomeAtelie;
    public boolean aceitaVisitas;
    public String horaAbertura;
    public String horaFechamento;
    public List<DiaLivre> dias;

    public static class DiaLivre {
        public String data;            // "2026-11-10"
        public List<String> horarios;  // ["10:00", "11:00", ...]
    }
}
