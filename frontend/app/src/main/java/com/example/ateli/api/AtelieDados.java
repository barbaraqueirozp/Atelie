package com.example.ateli.api;

/** Dados do ateliê: enviados no PUT e recebidos no GET /usuarios/{id}/atelie. */
public class AtelieDados {
    public boolean existe;
    public String nome;
    public String especialidade;
    public String cep;
    public String logradouro;
    public String numero;
    public String complemento;
    public String bairro;
    public String cidade;
    public String estado;
    public Double latitude;
    public Double longitude;
    public Boolean aceitaVisitas;
    public String diasAtendimento; // "2,3,4,5,6" (1=segunda ... 7=domingo)
    public String horaAbertura;    // "10:00"
    public String horaFechamento;  // "18:00"
}
