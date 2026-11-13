package com.example.ateli.api;

/** Resposta de GET /usuarios/{id}/perfil. */
public class PerfilResponse {
    public int idUsuario;
    public String nomeCompleto;
    public String cidade;
    public String estado;
    public long obrasAVenda;   // obras disponíveis agora
    public long acervoTotal;   // todas as obras já cadastradas
}
