package com.example.ateli;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.ateli.databinding.ActivityExplorarBinding;

import java.util.List;

public class ExplorarActivity extends AppCompatActivity {

    /** Chave da categoria inicial enviada pela tela principal. */
    public static final String EXTRA_CATEGORIA = "com.example.ateli.EXTRA_CATEGORIA";

    private static final String ESTADO_CATEGORIA = "estado_categoria";

    private ActivityExplorarBinding binding;
    private ObraAdapter adapter;
    private String categoriaAtual = ObraRepositorio.CATEGORIA_TODAS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityExplorarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Prioridade: estado salvo (rotação) > categoria recebida pela Intent > padrão
        if (savedInstanceState != null) {
            categoriaAtual = savedInstanceState.getString(ESTADO_CATEGORIA, categoriaAtual);
        } else {
            String categoriaRecebida = getIntent().getStringExtra(EXTRA_CATEGORIA);
            if (categoriaRecebida != null) {
                categoriaAtual = categoriaRecebida;
            }
        }

        // RecyclerView em grade de 2 colunas
        adapter = new ObraAdapter(
                ObraRepositorio.listarPorCategoria(categoriaAtual), false, this::abrirDetalhe);
        binding.rvObras.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvObras.setHasFixedSize(true);
        binding.rvObras.setAdapter(adapter);

        binding.grupoFiltros.check(chipDaCategoria(categoriaAtual));
        aplicarFiltro(categoriaAtual);

        binding.grupoFiltros.setOnCheckedStateChangeListener((grupo, idsMarcados) -> {
            if (!idsMarcados.isEmpty()) {
                aplicarFiltro(categoriaDoChip(idsMarcados.get(0)));
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ESTADO_CATEGORIA, categoriaAtual);
    }

    private void aplicarFiltro(String categoria) {
        categoriaAtual = categoria;
        List<Obra> obras = ObraRepositorio.listarPorCategoria(categoria);
        adapter.atualizarLista(obras);

        binding.txtCategoria.setText(categoria);
        binding.txtQuantidade.setText(obras.size() == 1 ? "1 obra" : obras.size() + " obras");

        boolean vazio = obras.isEmpty();
        binding.rvObras.setVisibility(vazio ? View.GONE : View.VISIBLE);
        binding.txtVazio.setVisibility(vazio ? View.VISIBLE : View.GONE);
    }

    private void abrirDetalhe(Obra obra) {
        Intent intent = new Intent(this, ObraDetalheActivity.class);
        intent.putExtra(ObraDetalheActivity.EXTRA_OBRA_ID, obra.getId());
        startActivity(intent);
    }

    // if/else em vez de switch: os IDs de recurso não são constantes nas versões novas do Gradle
    private String categoriaDoChip(int idChip) {
        if (idChip == R.id.chipPintura) return ObraRepositorio.PINTURA;
        if (idChip == R.id.chipEscultura) return ObraRepositorio.ESCULTURA;
        if (idChip == R.id.chipFotografia) return ObraRepositorio.FOTOGRAFIA;
        return ObraRepositorio.CATEGORIA_TODAS;
    }

    private int chipDaCategoria(String categoria) {
        if (ObraRepositorio.PINTURA.equals(categoria)) return R.id.chipPintura;
        if (ObraRepositorio.ESCULTURA.equals(categoria)) return R.id.chipEscultura;
        if (ObraRepositorio.FOTOGRAFIA.equals(categoria)) return R.id.chipFotografia;
        return R.id.chipEmAlta;
    }
}
