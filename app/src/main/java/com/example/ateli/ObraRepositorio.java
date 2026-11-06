package com.example.ateli;

import androidx.annotation.NonNull;

import com.example.ateli.api.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fonte de dados das obras: agora busca na API (que lê o PostgreSQL).
 * As chamadas são assíncronas — o resultado chega pelo callback,
 * já na thread principal (pode mexer na tela direto).
 */
public final class ObraRepositorio {

    public static final String CATEGORIA_TODAS = "Em alta";
    public static final String PINTURA = "Pintura";
    public static final String ESCULTURA = "Escultura";
    public static final String FOTOGRAFIA = "Fotografia";

    /** Resposta de uma busca: sucesso com o dado, ou erro com uma mensagem. */
    public interface Resultado<T> {
        void sucesso(T dado);
        void erro(String mensagem);
    }

    private ObraRepositorio() { }

    public static void listarTodas(Resultado<List<Obra>> resultado) {
        listarPorCategoria(CATEGORIA_TODAS, resultado);
    }

    public static void listarPorCategoria(String categoria, Resultado<List<Obra>> resultado) {
        String filtro = (categoria == null || CATEGORIA_TODAS.equals(categoria)) ? null : categoria;
        ApiClient.get().listarObras(filtro).enqueue(new Callback<List<Obra>>() {
            @Override
            public void onResponse(@NonNull Call<List<Obra>> call, @NonNull Response<List<Obra>> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    resultado.sucesso(resp.body());
                } else {
                    resultado.erro(ApiClient.mensagemDeErro(resp, "Erro ao carregar obras"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Obra>> call, @NonNull Throwable t) {
                resultado.erro("Sem conexão com o servidor");
            }
        });
    }

    /** Obras à venda de um artista. */
    public static void listarDoVendedor(int idVendedor, Resultado<List<Obra>> resultado) {
        ApiClient.get().listarObrasDoVendedor(idVendedor).enqueue(new Callback<List<Obra>>() {
            @Override
            public void onResponse(@NonNull Call<List<Obra>> call, @NonNull Response<List<Obra>> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    resultado.sucesso(resp.body());
                } else {
                    resultado.erro(ApiClient.mensagemDeErro(resp, "Erro ao carregar obras"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Obra>> call, @NonNull Throwable t) {
                resultado.erro("Sem conexão com o servidor");
            }
        });
    }

    public static void buscarPorId(int id, Resultado<Obra> resultado) {
        ApiClient.get().buscarObra(id).enqueue(new Callback<Obra>() {
            @Override
            public void onResponse(@NonNull Call<Obra> call, @NonNull Response<Obra> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    resultado.sucesso(resp.body());
                } else {
                    resultado.erro(ApiClient.mensagemDeErro(resp, "Obra não encontrada"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Obra> call, @NonNull Throwable t) {
                resultado.erro("Sem conexão com o servidor");
            }
        });
    }
}
