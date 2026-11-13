package atelie_api.dto;

import java.time.LocalDate;

/** JSON que o app envia na tela de Cadastro. */
public record CadastroRequest(
        String nomeCompleto,
        String cpf,
        LocalDate dataNascimento,   // formato "2000-12-31"
        String email,
        String senha,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String estado,
        String tipoConta            // "comprador" ou "vendedor"
) { }
