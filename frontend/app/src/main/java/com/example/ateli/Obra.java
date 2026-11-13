package com.example.ateli;

import androidx.annotation.DrawableRes;

import com.example.ateli.api.ApiClient;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Modelo de uma obra de arte, preenchido automaticamente (Gson)
 * com o JSON de GET /obras. Os nomes dos campos são os mesmos do JSON.
 */
public class Obra {

    private int idObra;
    private String titulo;
    private String nomeVendedor;
    private String tecnica;
    private double preco;
    private String descricao;
    private String imagemCapa;   // ex.: "/imagens/brisa_tropical.jpeg" ou null

    public int getId() { return idObra; }
    public String getTitulo() { return titulo; }
    public String getArtista() { return nomeVendedor; }
    public String getCategoria() { return tecnica; }
    public double getPreco() { return preco; }
    public String getDescricao() { return descricao; }

    /** URL completa da imagem de capa, ou null se a obra não tiver foto. */
    public String getImagemUrl() { return ApiClient.urlCompleta(imagemCapa); }

    /** Cor de fundo usada enquanto não há foto (alterna entre duas). */
    @DrawableRes
    public int getFundoRes() {
        return idObra % 2 == 0 ? R.drawable.bg_imagem_obra_2 : R.drawable.bg_imagem_obra_1;
    }

    /** Preço formatado no padrão brasileiro, ex.: "R$ 1.240,00". */
    public String getPrecoFormatado() {
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(preco);
    }
}
