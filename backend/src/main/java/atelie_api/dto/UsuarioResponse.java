package atelie_api.dto;

import atelie_api.model.Usuario;

/** O que a API devolve sobre um usuário (sem a senha!). */
public record UsuarioResponse(
        Integer idUsuario,
        String nomeCompleto,
        String email,
        String cidade,
        String estado,
        String tipoConta,     // "comprador" ou "vendedor"
        boolean temAtelie     // vendedor que já cadastrou o ateliê
) {
    public static UsuarioResponse de(Usuario u, boolean temAtelie) {
        return new UsuarioResponse(u.getIdUsuario(), u.getNomeCompleto(), u.getEmail(),
                u.getCidade(), u.getEstado(), u.getTipoConta(), temAtelie);
    }
}
