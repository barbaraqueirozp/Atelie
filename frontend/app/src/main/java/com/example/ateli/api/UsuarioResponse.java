package com.example.ateli.api;

/** Resposta da API no cadastro e no login (nunca contém a senha). */
public class UsuarioResponse {
    public int idUsuario;
    public String nomeCompleto;
    public String email;
    public String cidade;
    public String estado;
}
