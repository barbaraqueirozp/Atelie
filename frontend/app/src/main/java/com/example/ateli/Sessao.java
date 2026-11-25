package com.example.ateli;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.ateli.api.UsuarioResponse;

/** Guarda quem está logado (sem senha). Centraliza as chaves do SharedPreferences. */
public final class Sessao {

    public static final String COMPRADOR = "comprador";
    public static final String VENDEDOR = "vendedor";

    private Sessao() { }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(CadastroActivity.PREFS_USUARIO, Context.MODE_PRIVATE);
    }

    public static void salvar(Context c, UsuarioResponse u) {
        prefs(c).edit()
                .clear()
                .putInt("id_usuario", u.idUsuario)
                .putString("nome", u.nomeCompleto)
                .putString("email", u.email)
                .putString("tipo_conta", u.tipoConta == null ? COMPRADOR : u.tipoConta)
                .putBoolean("tem_atelie", u.temAtelie)
                .apply();
    }

    /** Depois de cadastrar o ateliê, a conta vira vendedor. */
    public static void virouVendedor(Context c) {
        prefs(c).edit().putString("tipo_conta", VENDEDOR).putBoolean("tem_atelie", true).apply();
    }

    public static int idUsuario(Context c) { return prefs(c).getInt("id_usuario", -1); }
    public static String nome(Context c) { return prefs(c).getString("nome", ""); }
    public static boolean ehVendedor(Context c) {
        return VENDEDOR.equals(prefs(c).getString("tipo_conta", COMPRADOR));
    }
    public static boolean temAtelie(Context c) { return prefs(c).getBoolean("tem_atelie", false); }

    public static void sair(Context c) { prefs(c).edit().clear().apply(); }
}
