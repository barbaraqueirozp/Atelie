package atelie_api.dto;

import java.math.BigDecimal;

/**
 * Usado nos dois sentidos: o app envia para salvar o ateliê,
 * e a API devolve para preencher a tela.
 * Em GET, quando o ateliê ainda não existe, "existe" vem false
 * e o endereço vem do cadastro do usuário, para facilitar.
 */
public record AtelieDados(
        boolean existe,
        String nome,
        String especialidade,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String estado,
        BigDecimal latitude,
        BigDecimal longitude,
        Boolean aceitaVisitas,
        String diasAtendimento,   // "2,3,4,5,6" (1=segunda ... 7=domingo)
        String horaAbertura,      // "10:00"
        String horaFechamento     // "18:00"
) { }
