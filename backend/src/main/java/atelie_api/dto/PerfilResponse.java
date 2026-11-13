package atelie_api.dto;

/** Dados da tela de perfil. Os campos do ateliê vêm null para compradores. */
public record PerfilResponse(
        Integer idUsuario,
        String nomeCompleto,
        String cidade,
        String estado,
        String tipoConta,
        long obrasAVenda,       // obras com status 'disponivel'
        long acervoTotal,       // todas as obras já cadastradas (inclusive vendidas)
        boolean temAtelie,
        String nomeAtelie,
        String especialidade
) { }
