package com.example.ateli;

import androidx.annotation.DrawableRes;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Modelo de dados de uma obra de arte exibida no app.
 * Os campos são finais (imutáveis) para evitar alterações acidentais.
 */
public class Obra {

    private final int id;
    private final String titulo;
    private final String artista;
    private final String categoria;
    private final double preco;
    private final String descricao;
    @DrawableRes private final int imagemRes;   // 0 = obra sem foto
    @DrawableRes private final int fundoRes;    // cor usada enquanto não há foto

    public Obra(int id, String titulo, String artista, String categoria,
                double preco, String descricao,
                @DrawableRes int imagemRes, @DrawableRes int fundoRes) {
        this.id = id;
        this.titulo = titulo;
        this.artista = artista;
        this.categoria = categoria;
        this.preco = preco;
        this.descricao = descricao;
        this.imagemRes = imagemRes;
        this.fundoRes = fundoRes;
    }

    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getArtista() { return artista; }
    public String getCategoria() { return categoria; }
    public double getPreco() { return preco; }
    public String getDescricao() { return descricao; }
    public int getImagemRes() { return imagemRes; }
    public int getFundoRes() { return fundoRes; }

    /** Preço formatado no padrão brasileiro, ex.: "R$ 1.240,00". */
    public String getPrecoFormatado() {
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(preco);
    }
}
