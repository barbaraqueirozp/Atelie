package com.example.ateli;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ateli.databinding.ActivityEmBreveBinding;

/** Tela provisória para as funcionalidades do menu que ainda serão implementadas. */
public class EmBreveActivity extends AppCompatActivity {

    private static final String EXTRA_TITULO = "com.example.ateli.EXTRA_TITULO";
    private static final String EXTRA_ICONE = "com.example.ateli.EXTRA_ICONE";
    private static final String EXTRA_DESCRICAO = "com.example.ateli.EXTRA_DESCRICAO";

    /** Monta a Intent já com o conteúdo da tela. */
    public static Intent criar(Context contexto, String titulo, String icone, String descricao) {
        return new Intent(contexto, EmBreveActivity.class)
                .putExtra(EXTRA_TITULO, titulo)
                .putExtra(EXTRA_ICONE, icone)
                .putExtra(EXTRA_DESCRICAO, descricao);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityEmBreveBinding binding = ActivityEmBreveBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Mantém o espaçamento do layout e soma as barras do sistema
        int margem = binding.main.getPaddingStart();
        int topo = binding.main.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(margem + barras.left, topo + barras.top, margem + barras.right, barras.bottom);
            return insets;
        });

        binding.txtTitulo.setText(getIntent().getStringExtra(EXTRA_TITULO));
        binding.txtIcone.setText(getIntent().getStringExtra(EXTRA_ICONE));
        binding.txtDescricao.setText(getIntent().getStringExtra(EXTRA_DESCRICAO));
        binding.btnVoltar.setOnClickListener(v -> finish());
    }
}
