package com.example.ateli.api;

/** Pedido de visita enviado para POST /usuarios/{id}/atelie/visitas. */
public class VisitaRequest {
    public final int idVisitante;
    public final String dataHora; // "2026-11-10T14:00"

    public VisitaRequest(int idVisitante, String dataHora) {
        this.idVisitante = idVisitante;
        this.dataHora = dataHora;
    }
}
