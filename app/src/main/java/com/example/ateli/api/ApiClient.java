package com.example.ateli.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Cria (uma única vez) o Retrofit que conversa com a API. */
public final class ApiClient {

    /**
     * Emulador: 10.0.2.2 é o "localhost" do computador.
     * Celular físico: troque pelo IP do Mac na rede Wi-Fi (ex.: http://192.168.0.15:8080/).
     */
    public static final String BASE_URL = "http://10.0.2.2:8080/";

    private static AtelieApi api;

    private ApiClient() { }

    public static synchronized AtelieApi get() {
        if (api == null) {
            api = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(AtelieApi.class);
        }
        return api;
    }

    /** Monta a URL completa de uma imagem ("/imagens/x.jpg" vira "http://10.0.2.2:8080/imagens/x.jpg"). */
    public static String urlCompleta(String caminho) {
        if (caminho == null || caminho.isEmpty()) return null;
        if (caminho.startsWith("http")) return caminho;
        return BASE_URL + (caminho.startsWith("/") ? caminho.substring(1) : caminho);
    }

    /** Lê o campo "message" que a API devolve nos erros; usa o padrão se não houver. */
    public static String mensagemDeErro(Response<?> resposta, String padrao) {
        try {
            if (resposta.errorBody() != null) {
                JsonObject json = JsonParser.parseString(resposta.errorBody().string()).getAsJsonObject();
                if (json.has("message") && !json.get("message").getAsString().isEmpty()) {
                    return json.get("message").getAsString();
                }
            }
        } catch (Exception ignorada) {
            // corpo vazio ou não é JSON: usa a mensagem padrão
        }
        return padrao;
    }
}
