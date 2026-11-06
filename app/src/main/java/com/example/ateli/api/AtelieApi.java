package com.example.ateli.api;

import com.example.ateli.Obra;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Endpoints da API Spring Boot (atelie-api). */
public interface AtelieApi {

    @POST("usuarios")
    Call<UsuarioResponse> cadastrar(@Body CadastroRequest dados);

    @POST("login")
    Call<UsuarioResponse> login(@Body LoginRequest dados);

    /** categoria pode ser null para trazer todas. */
    @GET("obras")
    Call<List<Obra>> listarObras(@Query("categoria") String categoria);

    @GET("obras/{id}")
    Call<Obra> buscarObra(@Path("id") int id);
}
