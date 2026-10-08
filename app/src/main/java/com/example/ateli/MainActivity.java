package com.example.ateli;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.location.LocationManagerCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ateli.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    /** Chave do nome do usuário enviado pelo Login. */
    public static final String EXTRA_NOME_USUARIO = "com.example.ateli.EXTRA_NOME_USUARIO";

    private static final String ESTADO_CATEGORIA = "estado_categoria";

    private ActivityMainBinding binding;

    /** Categoria escolhida no menu de filtros (enviada para o Explorar). */
    private String categoriaSelecionada = ObraRepositorio.CATEGORIA_TODAS;

    /** Permite cancelar a busca de localização se a tela for fechada. */
    private CancellationSignal sinalLocalizacao;

    private final ActivityResultLauncher<String> pedirLocalizacao =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), permitida -> {
                if (permitida) {
                    obterLocalizacaoAtual();
                } else {
                    Toast.makeText(this, "Permissão de localização não concedida",
                            Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (savedInstanceState != null) {
            categoriaSelecionada = savedInstanceState.getString(
                    ESTADO_CATEGORIA, ObraRepositorio.CATEGORIA_TODAS);
            atualizarDicaBusca();
        }

        mostrarSaudacao();
        configurarDestaques();

        binding.btnLocalizacao.setOnClickListener(v -> abrirMenuLocalizacao());
        binding.btnFiltros.setOnClickListener(v -> abrirMenuFiltros());

        binding.btnExplorar.setOnClickListener(v -> abrirExplorar(categoriaSelecionada));
        binding.btnVerTudo.setOnClickListener(v -> abrirExplorar(ObraRepositorio.CATEGORIA_TODAS));
        binding.btnArte.setOnClickListener(v -> abrirExplorar(ObraRepositorio.CATEGORIA_TODAS));
        binding.btnBusca.setOnClickListener(v -> binding.campoBusca.requestFocus());
        binding.navExplorar.setOnClickListener(v -> abrirExplorar(ObraRepositorio.CATEGORIA_TODAS));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ESTADO_CATEGORIA, categoriaSelecionada);
    }

    @Override
    protected void onDestroy() {
        // Ciclo de vida: cancela a localização pendente para o callback
        // não tentar atualizar uma tela que já foi destruída.
        if (sinalLocalizacao != null) {
            sinalLocalizacao.cancel();
        }
        super.onDestroy();
    }

    // =========================
    // DADOS RECEBIDOS VIA INTENT
    // =========================

    private void mostrarSaudacao() {
        String nome = getIntent().getStringExtra(EXTRA_NOME_USUARIO);
        if (nome == null || nome.trim().isEmpty()) {
            binding.txtSaudacao.setText("Olá!");
        } else {
            String primeiroNome = nome.trim().split("\\s+")[0];
            binding.txtSaudacao.setText("Olá, " + primeiroNome + "!");
        }
    }

    // =========================
    // RECYCLERVIEW DE DESTAQUES
    // =========================

    private void configurarDestaques() {
        ObraAdapter adapter = new ObraAdapter(
                ObraRepositorio.listarTodas(), true, this::abrirDetalhe);
        binding.rvDestaques.setAdapter(adapter);
        binding.rvDestaques.setHasFixedSize(true);
    }

    // =========================
    // NAVEGAÇÃO (Intents explícitas)
    // =========================

    private void abrirDetalhe(Obra obra) {
        Intent intent = new Intent(this, ObraDetalheActivity.class);
        intent.putExtra(ObraDetalheActivity.EXTRA_OBRA_ID, obra.getId());
        startActivity(intent);
    }

    private void abrirExplorar(String categoria) {
        Intent intent = new Intent(this, ExplorarActivity.class);
        intent.putExtra(ExplorarActivity.EXTRA_CATEGORIA, categoria);
        startActivity(intent);
    }

    // =========================
    // MENUS
    // =========================

    private void abrirMenuFiltros() {
        PopupMenu menu = new PopupMenu(this, binding.btnFiltros);
        menu.getMenu().add(ObraRepositorio.CATEGORIA_TODAS);
        menu.getMenu().add(ObraRepositorio.PINTURA);
        menu.getMenu().add(ObraRepositorio.ESCULTURA);
        menu.getMenu().add(ObraRepositorio.FOTOGRAFIA);

        menu.setOnMenuItemClickListener(item -> {
            categoriaSelecionada = String.valueOf(item.getTitle());
            atualizarDicaBusca();
            return true;
        });
        menu.show();
    }

    private void atualizarDicaBusca() {
        if (ObraRepositorio.CATEGORIA_TODAS.equals(categoriaSelecionada)) {
            binding.tilBusca.setHint("Pesquisar artista ou obra");
        } else {
            binding.tilBusca.setHint("Pesquisar em: " + categoriaSelecionada);
        }
    }

    private void abrirMenuLocalizacao() {
        PopupMenu menu = new PopupMenu(this, binding.btnLocalizacao);
        menu.getMenu().add("Usar localização atual");
        menu.getMenu().add("Escolher outra localização");

        menu.setOnMenuItemClickListener(item -> {
            if ("Usar localização atual".contentEquals(item.getTitle())) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    obterLocalizacaoAtual();
                } else {
                    pedirLocalizacao.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
                }
            } else {
                Toast.makeText(this, "A escolha manual da cidade será adicionada em seguida",
                        Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        menu.show();
    }

    // =========================
    // LOCALIZAÇÃO
    // =========================

    private void obterLocalizacaoAtual() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationManager gerenciador = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (gerenciador == null
                || !gerenciador.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(this, "Ative a localização do celular e tente novamente",
                    Toast.LENGTH_LONG).show();
            return;
        }

        binding.btnLocalizacao.setText("Obtendo localização...");

        if (sinalLocalizacao != null) {
            sinalLocalizacao.cancel();
        }
        sinalLocalizacao = new CancellationSignal();

        LocationManagerCompat.getCurrentLocation(
                gerenciador,
                LocationManager.NETWORK_PROVIDER,
                sinalLocalizacao,
                ContextCompat.getMainExecutor(this),
                local -> {
                    if (isFinishing() || isDestroyed()) {
                        return; // tela já fechada: não mexe na interface
                    }
                    if (local == null) {
                        binding.btnLocalizacao.setText("⌖  Artistas próximos  ▾");
                        Toast.makeText(this, "Não foi possível obter a localização",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    binding.btnLocalizacao.setText("⌖  Minha localização  ▾");

                    // Coordenadas disponíveis para a futura busca de artistas.
                    double latitude = local.getLatitude();
                    double longitude = local.getLongitude();
                    Toast.makeText(this, "Localização obtida: " + latitude + ", " + longitude,
                            Toast.LENGTH_LONG).show();
                });
    }
}
