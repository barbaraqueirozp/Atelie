package com.example.ateli.api;

/** Resposta do agendamento. */
public class VisitaResponse {
    public int idVisita;
    public String dataHora;
    public String status; // "pendente" até o artista confirmar
}
