package com.example.ateli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Fonte de dados das obras. Por enquanto os dados são fixos no app;
 * no futuro esta classe pode buscar as obras de um banco ou API
 * sem que as telas precisem mudar.
 */
public final class ObraRepositorio {

    public static final String CATEGORIA_TODAS = "Em alta";
    public static final String PINTURA = "Pintura";
    public static final String ESCULTURA = "Escultura";
    public static final String FOTOGRAFIA = "Fotografia";

    private static final List<Obra> OBRAS = Collections.unmodifiableList(Arrays.asList(
            new Obra(1, "Brisa Tropical", "Marina Duarte", PINTURA, 1240,
                    "Acrílica sobre tela inspirada nas cores do litoral brasileiro.",
                    R.drawable.brisa_tropical, R.drawable.bg_imagem_obra_1),
            new Obra(2, "Sol de Março", "Marina Duarte", PINTURA, 1680,
                    "Óleo sobre tela que retrata o fim de tarde no cerrado.",
                    R.drawable.sol_de_marco, R.drawable.bg_imagem_obra_2),
            new Obra(3, "Raízes", "Carlos Mendes", ESCULTURA, 2350,
                    "Escultura em madeira de reaproveitamento, peça única.",
                    0, R.drawable.bg_imagem_obra_1),
            new Obra(4, "Forma Livre", "Ana Lúcia Prado", ESCULTURA, 980,
                    "Cerâmica modelada à mão com esmalte artesanal.",
                    0, R.drawable.bg_imagem_obra_2),
            new Obra(5, "Luz da Tarde", "Rafael Torres", FOTOGRAFIA, 560,
                    "Fotografia em impressão fine art, tiragem limitada.",
                    0, R.drawable.bg_imagem_obra_2),
            new Obra(6, "Cidade Azul", "Rafael Torres", FOTOGRAFIA, 720,
                    "Fotografia urbana noturna, papel algodão 300g.",
                    0, R.drawable.bg_imagem_obra_1)
    ));

    private ObraRepositorio() { }

    public static List<Obra> listarTodas() {
        return OBRAS;
    }

    public static List<Obra> listarPorCategoria(String categoria) {
        if (categoria == null || CATEGORIA_TODAS.equals(categoria)) {
            return OBRAS;
        }
        List<Obra> filtradas = new ArrayList<>();
        for (Obra obra : OBRAS) {
            if (obra.getCategoria().equals(categoria)) {
                filtradas.add(obra);
            }
        }
        return filtradas;
    }

    /** Retorna a obra com o id informado, ou null se não existir. */
    public static Obra buscarPorId(int id) {
        for (Obra obra : OBRAS) {
            if (obra.getId() == id) {
                return obra;
            }
        }
        return null;
    }
}
