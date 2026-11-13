package atelie_api.dto;

/** JSON que o app envia na tela de Login. */
public record LoginRequest(String email, String senha) { }
