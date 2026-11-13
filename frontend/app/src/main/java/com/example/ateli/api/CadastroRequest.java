package com.example.ateli.api;

/** Dados enviados para POST /usuarios. Os nomes batem com o JSON da API. */
public class CadastroRequest {
    public String nomeCompleto;
    public String cpf;
    public String dataNascimento; // "yyyy-MM-dd"
    public String email;
    public String senha;
    public String cep;
    public String logradouro;
    public String numero;
    public String complemento;
    public String bairro;
    public String cidade;
    public String estado;
}
