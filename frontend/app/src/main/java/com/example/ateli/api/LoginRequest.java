package com.example.ateli.api;

/** Dados enviados para POST /login. */
public class LoginRequest {
    public final String email;
    public final String senha;

    public LoginRequest(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }
}
