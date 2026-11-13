package atelie_api.dto;

/** Pedido de visita: quem vai e quando ("2026-11-10T14:00"). */
public record VisitaRequest(Integer idVisitante, String dataHora) { }
