package com.example.ateli;

import android.content.Intent;
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
import com.example.ateli.databinding.ItemMenuPerfilBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Perfil do usuário logado, com duas interfaces:
 *  - ARTISTA (vendedor): contadores, menu, minhas obras e publicar;
 *  - COMPRADOR: menu com compras, favoritas, notificações, conversas e configurações.
 */
public class PerfilActivity extends AppCompatActivity {

    private ActivityPerfilBinding binding;
    private ObraAdapter adapter;
    private int idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // O id foi salvo no login. Sem ele, a pessoa precisa entrar de novo.
        idUsuario = Sessao.idUsuario(this);
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

        adapter = new ObraAdapter(new ArrayList<>(), false, this::abrirDetalhe);
        binding.rvMinhasObras.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvMinhasObras.setAdapter(adapter);

        configurarMenu();

        binding.btnVoltar.setOnClickListener(v -> finish());
        binding.btnSair.setOnClickListener(v -> {
            Sessao.sair(this);
            irParaLogin();
        });
        binding.btnPublicar.setOnClickListener(v ->
                startActivity(new Intent(this, PublicarObraActivity.class)));
        binding.btnCadastrarAtelie.setOnClickListener(v -> abrirCadastroAtelie());
        binding.btnVerComoCliente.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExplorarActivity.class);
            intent.putExtra(ExplorarActivity.EXTRA_CATEGORIA, ObraRepositorio.CATEGORIA_TODAS);
            startActivity(intent);
        });

        // Mostra a interface certa com o que está salvo; a API confirma em seguida
        mostrarNome(Sessao.nome(this));
        aplicarTipoDeConta(Sessao.ehVendedor(this), Sessao.temAtelie(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarrega sempre que a tela aparece (ex.: depois de publicar, vender ou cadastrar o ateliê)
        if (binding != null) {
            carregarPerfil();
        }
    }

    // =========================
    // MENU
    // =========================

    private void configurarMenu() {
        item(binding.menuAtelie, "🎨", "Meu ateliê", "Endereço, especialidade e visitas",
                v -> abrirCadastroAtelie());
        item(binding.menuCompras, "🛍", "Minhas compras", "Obras que você comprou",
                v -> emBreve("Minhas compras", "🛍", "Aqui você vai acompanhar suas compras e entregas."));
        item(binding.menuFavoritas, "♡", "Favoritas", "Obras que você marcou com coração",
                v -> emBreve("Favoritas", "♡", "As obras que você favoritar vão aparecer aqui."));
        item(binding.menuNotificacoes, "🔔", "Notificações", "Vendas, visitas e novidades",
                v -> emBreve("Notificações", "🔔", "Avisos sobre compras, vendas e visitas aos ateliês."));
        item(binding.menuConversas, "💬", "Conversas", "Mensagens com artistas e compradores",
                v -> emBreve("Conversas", "💬", "Converse com artistas e colecionadores sem sair do app."));
        item(binding.menuConfiguracoes, "⚙", "Configurações da conta", "Dados pessoais, senha e privacidade",
                v -> emBreve("Configurações", "⚙", "Edite seus dados, altere a senha e gerencie sua conta."));
        item(binding.menuVender, "✦", "Quero vender minhas obras", "Cadastre seu ateliê e comece a vender",
                v -> abrirCadastroAtelie());
    }

    private void item(ItemMenuPerfilBinding item, String icone, String titulo, String descricao,
                      View.OnClickListener acao) {
        item.txtIcone.setText(icone);
        item.txtTituloMenu.setText(titulo);
        item.txtDescricaoMenu.setText(descricao);
        item.getRoot().setOnClickListener(acao);
    }

    /** Mostra ou esconde as partes da tela conforme o tipo de conta. */
    private void aplicarTipoDeConta(boolean vendedor, boolean temAtelie) {
        // Artista
        binding.grupoEstatisticas.setVisibility(vendedor ? View.VISIBLE : View.GONE);
        binding.secaoObras.setVisibility(vendedor && temAtelie ? View.VISIBLE : View.GONE);
        binding.cardSemAtelie.setVisibility(vendedor && !temAtelie ? View.VISIBLE : View.GONE);
        binding.menuAtelie.getRoot().setVisibility(vendedor && temAtelie ? View.VISIBLE : View.GONE);

        // Comprador
        binding.menuCompras.getRoot().setVisibility(vendedor ? View.GONE : View.VISIBLE);
        binding.menuFavoritas.getRoot().setVisibility(vendedor ? View.GONE : View.VISIBLE);
        binding.menuVender.getRoot().setVisibility(vendedor ? View.GONE : View.VISIBLE);

        // Notificações, Conversas e Configurações aparecem para os dois
    }

    // =========================
    // DADOS
    // =========================

    private void carregarPerfil() {
        ApiClient.get().buscarPerfil(idUsuario).enqueue(new Callback<PerfilResponse>() {
            @Override
            public void onResponse(@NonNull Call<PerfilResponse> call,
                                   @NonNull Response<PerfilResponse> resposta) {
                if (isFinishing() || isDestroyed()) return;
                if (!resposta.isSuccessful() || resposta.body() == null) {
                    Toast.makeText(PerfilActivity.this,
                            ApiClient.mensagemDeErro(resposta, "Erro ao carregar o perfil"),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                PerfilResponse perfil = resposta.body();
                boolean vendedor = Sessao.VENDEDOR.equals(perfil.tipoConta);

                mostrarNome(perfil.nomeCompleto);
                aplicarTipoDeConta(vendedor, perfil.temAtelie);
                String local = perfil.cidade + " · " + perfil.estado;

                if (vendedor && perfil.temAtelie) {
                    Sessao.virouVendedor(PerfilActivity.this); // mantém a sessão em dia
                    binding.txtSubtitulo.setText(perfil.nomeAtelie + "\n" + perfil.especialidade + " · " + local);
                    binding.txtObrasAVenda.setText(String.valueOf(perfil.obrasAVenda));
                    binding.txtAcervo.setText(String.valueOf(perfil.acervoTotal));
                    carregarObras();
                } else if (vendedor) {
                    binding.txtSubtitulo.setText("Artista · " + local);
                    binding.txtObrasAVenda.setText(String.valueOf(perfil.obrasAVenda));
                    binding.txtAcervo.setText(String.valueOf(perfil.acervoTotal));
                } else {
                    binding.txtSubtitulo.setText("Colecionador(a) · " + local);
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

    // =========================
    // NAVEGAÇÃO
    // =========================

    private void abrirDetalhe(Obra obra) {
        Intent intent = new Intent(this, ObraDetalheActivity.class);
        intent.putExtra(ObraDetalheActivity.EXTRA_OBRA_ID, obra.getId());
        startActivity(intent);
    }

    private void abrirCadastroAtelie() {
        startActivity(new Intent(this, CadastroAtelieActivity.class));
    }

    private void emBreve(String titulo, String icone, String descricao) {
        startActivity(EmBreveActivity.criar(this, titulo, icone, descricao));
    }

    /** Abre o Login e apaga as telas anteriores (o "voltar" não retorna ao app logado). */
    private void irParaLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
