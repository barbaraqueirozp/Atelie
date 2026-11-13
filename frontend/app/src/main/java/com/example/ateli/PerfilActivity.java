package com.example.ateli;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.ateli.api.ApiClient;
import com.example.ateli.api.PerfilResponse;
import com.example.ateli.databinding.ActivityPerfilBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Perfil do usuário logado: dados, contadores e as obras que ele está vendendo. */
public class PerfilActivity extends AppCompatActivity {

    private ActivityPerfilBinding binding;
    private ObraAdapter adapter;
    private int idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // O id foi salvo no login. Sem ele, a pessoa precisa entrar de novo.
        SharedPreferences prefs = getSharedPreferences(CadastroActivity.PREFS_USUARIO, MODE_PRIVATE);
        idUsuario = prefs.getInt("id_usuario", -1);
        if (idUsuario == -1) {
            irParaLogin();
            return;
        }

        EdgeToEdge.enable(this);
        binding = ActivityPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        // Mostra o nome salvo enquanto a API não responde
        mostrarNome(prefs.getString("nome", ""));

        adapter = new ObraAdapter(new ArrayList<>(), false, this::abrirDetalhe);
        binding.rvMinhasObras.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvMinhasObras.setAdapter(adapter);

        binding.btnVoltar.setOnClickListener(v -> finish());
        binding.btnSair.setOnClickListener(v -> sair(prefs));
        binding.btnVerComoCliente.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExplorarActivity.class);
            intent.putExtra(ExplorarActivity.EXTRA_CATEGORIA, ObraRepositorio.CATEGORIA_TODAS);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarrega sempre que a tela aparece (ex.: depois de publicar ou vender uma obra)
        if (binding != null) {
            carregarPerfil();
            carregarObras();
        }
    }

    private void carregarPerfil() {
        ApiClient.get().buscarPerfil(idUsuario).enqueue(new Callback<PerfilResponse>() {
            @Override
            public void onResponse(@NonNull Call<PerfilResponse> call,
                                   @NonNull Response<PerfilResponse> resposta) {
                if (isFinishing() || isDestroyed()) return;
                if (resposta.isSuccessful() && resposta.body() != null) {
                    PerfilResponse perfil = resposta.body();
                    mostrarNome(perfil.nomeCompleto);
                    binding.txtLocal.setText(perfil.cidade + " · " + perfil.estado);
                    binding.txtObrasAVenda.setText(String.valueOf(perfil.obrasAVenda));
                    binding.txtAcervo.setText(String.valueOf(perfil.acervoTotal));
                } else {
                    Toast.makeText(PerfilActivity.this,
                            ApiClient.mensagemDeErro(resposta, "Erro ao carregar o perfil"),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PerfilResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(PerfilActivity.this, "Sem conexão com o servidor",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void carregarObras() {
        ObraRepositorio.listarDoVendedor(idUsuario, new ObraRepositorio.Resultado<List<Obra>>() {
            @Override
            public void sucesso(List<Obra> obras) {
                if (isFinishing() || isDestroyed()) return;
                adapter.atualizarLista(obras);
                boolean vazio = obras.isEmpty();
                binding.rvMinhasObras.setVisibility(vazio ? View.GONE : View.VISIBLE);
                binding.txtSemObras.setVisibility(vazio ? View.VISIBLE : View.GONE);
            }

            @Override
            public void erro(String mensagem) {
                // O erro de conexão já é avisado por carregarPerfil()
            }
        });
    }

    /** Nome completo no cartão e as iniciais no círculo (ex.: "Marina Duarte" → "MD"). */
    private void mostrarNome(String nome) {
        if (nome == null) nome = "";
        binding.txtNome.setText(nome);

        String[] partes = nome.trim().split("\\s+");
        StringBuilder iniciais = new StringBuilder();
        if (partes.length > 0 && !partes[0].isEmpty()) iniciais.append(partes[0].charAt(0));
        if (partes.length > 1) iniciais.append(partes[partes.length - 1].charAt(0));
        binding.txtIniciais.setText(iniciais.toString().toUpperCase());
    }

    private void abrirDetalhe(Obra obra) {
        Intent intent = new Intent(this, ObraDetalheActivity.class);
        intent.putExtra(ObraDetalheActivity.EXTRA_OBRA_ID, obra.getId());
        startActivity(intent);
    }

    private void sair(SharedPreferences prefs) {
        prefs.edit().clear().apply();
        irParaLogin();
    }

    /** Abre o Login e apaga as telas anteriores (o "voltar" não retorna ao app logado). */
    private void irParaLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
