package com.example.ateli;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.location.LocationManagerCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ateli.api.ApiClient;
import com.example.ateli.api.AtelieDados;
import com.example.ateli.databinding.ActivityCadastroAtelieBinding;

import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Cadastro (ou edição) do ateliê do vendedor.
 * Aberta logo após o cadastro de quem escolheu "Quero vender",
 * ou pelo perfil / botão "+" de quem ainda não tem ateliê.
 */
public class CadastroAtelieActivity extends AppCompatActivity {

    public static final String EXTRA_ID_USUARIO = "com.example.ateli.EXTRA_ID_USUARIO";
    /** true quando vem direto do cadastro de conta (a pessoa ainda não fez login). */
    public static final String EXTRA_VEIO_DO_CADASTRO = "com.example.ateli.EXTRA_VEIO_DO_CADASTRO";

    private static final String ESTADO_LAT = "estado_lat";
    private static final String ESTADO_LNG = "estado_lng";
    private static final String ESTADO_ABRE = "estado_abre";
    private static final String ESTADO_FECHA = "estado_fecha";
    private static final String TAG_RELOGIO = "relogio_atelie";

    private ActivityCadastroAtelieBinding binding;
    private int idUsuario;
    private boolean veioDoCadastro;
    private Double latitude;
    private Double longitude;
    private CancellationSignal sinalLocalizacao;
    /** Horário de visitas em horas cheias (0–23 e 1–24). */
    private int horaAbre = 10;
    private int horaFecha = 18;

    private final ActivityResultLauncher<String[]> pedirLocalizacao =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), resultado -> {
                if (temPermissao()) {
                    registrarLocalizacao();
                } else {
                    Toast.makeText(this, "Permissão de localização não concedida", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        idUsuario = getIntent().getIntExtra(EXTRA_ID_USUARIO, Sessao.idUsuario(this));
        veioDoCadastro = getIntent().getBooleanExtra(EXTRA_VEIO_DO_CADASTRO, false);
        if (idUsuario == -1) {
            Toast.makeText(this, "Faça login para continuar", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        binding = ActivityCadastroAtelieBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.ime());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        if (veioDoCadastro) {
            binding.txtEtapa.setText("Último passo");
        }

        binding.btnVoltar.setOnClickListener(v -> finish());
        binding.btnEstouNoAtelie.setOnClickListener(v -> {
            if (temPermissao()) {
                registrarLocalizacao();
            } else {
                pedirLocalizacao.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION});
            }
        });
        binding.btnSalvar.setOnClickListener(v -> salvar());
        binding.btnAbertura.setOnClickListener(v -> escolherHora(true));
        binding.btnFechamento.setOnClickListener(v -> escolherHora(false));
        binding.switchVisitas.setOnCheckedChangeListener((botao, marcado) -> atualizarSecaoHorarios());

        // Ciclo de vida: ao girar a tela, os campos de texto se recuperam sozinhos;
        // só a localização precisa ser guardada à mão.
        if (savedInstanceState != null) {
            horaAbre = savedInstanceState.getInt(ESTADO_ABRE, horaAbre);
            horaFecha = savedInstanceState.getInt(ESTADO_FECHA, horaFecha);
            mostrarHorario();
            atualizarSecaoHorarios();
            if (savedInstanceState.containsKey(ESTADO_LAT)) {
                latitude = savedInstanceState.getDouble(ESTADO_LAT);
                longitude = savedInstanceState.getDouble(ESTADO_LNG);
                mostrarLocalizacao();
            }
        } else {
            carregarDados();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ESTADO_ABRE, horaAbre);
        outState.putInt(ESTADO_FECHA, horaFecha);
        if (latitude != null && longitude != null) {
            outState.putDouble(ESTADO_LAT, latitude);
            outState.putDouble(ESTADO_LNG, longitude);
        }
    }

    @Override
    protected void onDestroy() {
        if (sinalLocalizacao != null) {
            sinalLocalizacao.cancel();
        }
        super.onDestroy();
    }

    // =========================
    // CARREGAR (ateliê existente ou endereço do cadastro)
    // =========================

    private void carregarDados() {
        ApiClient.get().buscarAtelie(idUsuario).enqueue(new Callback<AtelieDados>() {
            @Override
            public void onResponse(@NonNull Call<AtelieDados> call, @NonNull Response<AtelieDados> resposta) {
                if (isFinishing() || isDestroyed()) return;
                if (resposta.isSuccessful() && resposta.body() != null) {
                    preencher(resposta.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AtelieDados> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(CadastroAtelieActivity.this, "Sem conexão com o servidor",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void preencher(AtelieDados d) {
        if (d.existe) {
            binding.txtTitulo.setText("Editar ateliê");
            binding.edtNomeAtelie.setText(d.nome);
            binding.grupoEspecialidade.check(chipDaEspecialidade(d.especialidade));
        }
        binding.edtCep.setText(d.cep);
        binding.edtLogradouro.setText(d.logradouro);
        binding.edtNumero.setText(d.numero);
        binding.edtComplemento.setText(d.complemento);
        binding.edtBairro.setText(d.bairro);
        binding.edtCidade.setText(d.cidade);
        binding.edtEstado.setText(d.estado);
        binding.switchVisitas.setChecked(d.aceitaVisitas == null || d.aceitaVisitas);
        marcarDias(d.diasAtendimento);
        horaAbre = lerHora(d.horaAbertura, 10);
        horaFecha = lerHora(d.horaFechamento, 18);
        if (horaFecha == 0) horaFecha = 24;
        mostrarHorario();
        atualizarSecaoHorarios();
        if (d.latitude != null && d.longitude != null) {
            latitude = d.latitude;
            longitude = d.longitude;
            mostrarLocalizacao();
        }
    }

    // =========================
    // LOCALIZAÇÃO ("Estou no meu ateliê")
    // =========================

    private boolean temPermissao() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressWarnings("MissingPermission") // a permissão é conferida em temPermissao()
    private void registrarLocalizacao() {
        if (!temPermissao()) return;

        LocationManager gerenciador = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (gerenciador == null) return;

        // GPS é o mais preciso; se estiver desligado, usa a rede (Wi-Fi / antenas)
        String provedor;
        if (gerenciador.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            provedor = LocationManager.GPS_PROVIDER;
        } else if (gerenciador.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            provedor = LocationManager.NETWORK_PROVIDER;
        } else {
            Toast.makeText(this, "Ative a localização do celular e tente novamente", Toast.LENGTH_LONG).show();
            return;
        }

        binding.btnEstouNoAtelie.setEnabled(false);
        binding.btnEstouNoAtelie.setText("Obtendo localização...");

        if (sinalLocalizacao != null) sinalLocalizacao.cancel();
        sinalLocalizacao = new CancellationSignal();

        LocationManagerCompat.getCurrentLocation(gerenciador, provedor, sinalLocalizacao,
                ContextCompat.getMainExecutor(this), local -> {
                    if (isFinishing() || isDestroyed()) return;
                    binding.btnEstouNoAtelie.setEnabled(true);
                    if (local == null) {
                        binding.btnEstouNoAtelie.setText("📍  Estou no meu ateliê");
                        Toast.makeText(this, "Não foi possível obter a localização", Toast.LENGTH_LONG).show();
                        return;
                    }
                    latitude = local.getLatitude();
                    longitude = local.getLongitude();
                    mostrarLocalizacao();
                });
    }

    private void mostrarLocalizacao() {
        binding.txtLocalizacao.setText(String.format(Locale.US,
                "✓ Localização registrada (%.5f, %.5f)", latitude, longitude));
        binding.btnEstouNoAtelie.setText("📍  Atualizar localização");
    }

    // =========================
    // SALVAR
    // =========================

    private void salvar() {
        AtelieDados d = new AtelieDados();
        d.nome = texto(binding.edtNomeAtelie);
        d.especialidade = especialidadeSelecionada();
        d.cep = texto(binding.edtCep);
        d.logradouro = texto(binding.edtLogradouro);
        d.numero = texto(binding.edtNumero);
        d.complemento = texto(binding.edtComplemento);
        d.bairro = texto(binding.edtBairro);
        d.cidade = texto(binding.edtCidade);
        d.estado = texto(binding.edtEstado).toUpperCase(Locale.ROOT);
        d.latitude = latitude;
        d.longitude = longitude;
        d.aceitaVisitas = binding.switchVisitas.isChecked();
        d.diasAtendimento = diasMarcados();
        d.horaAbertura = String.format(Locale.US, "%02d:00", horaAbre);
        d.horaFechamento = horaFecha == 24 ? "23:59" : String.format(Locale.US, "%02d:00", horaFecha);

        // Validação: primeiro campo vazio recebe o erro
        Map<EditText, String> obrigatorios = new java.util.LinkedHashMap<>();
        obrigatorios.put(binding.edtNomeAtelie, "Digite o nome do ateliê");
        obrigatorios.put(binding.edtCep, "Digite o CEP");
        obrigatorios.put(binding.edtLogradouro, "Digite a rua");
        obrigatorios.put(binding.edtNumero, "Digite o número");
        obrigatorios.put(binding.edtBairro, "Digite o bairro");
        obrigatorios.put(binding.edtCidade, "Digite a cidade");
        for (Map.Entry<EditText, String> campo : obrigatorios.entrySet()) {
            if (texto(campo.getKey()).isEmpty()) {
                campo.getKey().setError(campo.getValue());
                campo.getKey().requestFocus();
                return;
            }
        }
        if (d.cep.length() != 8) {
            binding.edtCep.setError("CEP com 8 números");
            binding.edtCep.requestFocus();
            return;
        }
        if (d.estado.length() != 2) {
            binding.edtEstado.setError("UF");
            binding.edtEstado.requestFocus();
            return;
        }

        if (d.aceitaVisitas && d.diasAtendimento.isEmpty()) {
            Toast.makeText(this, "Escolha pelo menos um dia de visita", Toast.LENGTH_SHORT).show();
            return;
        }
        if (horaFecha <= horaAbre) {
            Toast.makeText(this, "O ateliê precisa fechar depois de abrir", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSalvar.setEnabled(false);
        binding.btnSalvar.setText("Salvando...");

        ApiClient.get().salvarAtelie(idUsuario, d).enqueue(new Callback<AtelieDados>() {
            @Override
            public void onResponse(@NonNull Call<AtelieDados> call, @NonNull Response<AtelieDados> resposta) {
                if (isFinishing() || isDestroyed()) return;
                binding.btnSalvar.setEnabled(true);
                binding.btnSalvar.setText("Salvar ateliê");

                if (resposta.isSuccessful()) {
                    if (veioDoCadastro) {
                        Toast.makeText(CadastroAtelieActivity.this,
                                "Ateliê cadastrado! Agora é só entrar.", Toast.LENGTH_LONG).show();
                    } else {
                        Sessao.virouVendedor(CadastroAtelieActivity.this);
                        Toast.makeText(CadastroAtelieActivity.this, "Ateliê salvo!", Toast.LENGTH_SHORT).show();
                    }
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(CadastroAtelieActivity.this,
                            ApiClient.mensagemDeErro(resposta, "Não foi possível salvar"),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AtelieDados> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                binding.btnSalvar.setEnabled(true);
                binding.btnSalvar.setText("Salvar ateliê");
                Toast.makeText(CadastroAtelieActivity.this,
                        "Sem conexão com o servidor. A API está rodando?", Toast.LENGTH_LONG).show();
            }
        });
    }

    // =========================
    // DIAS E HORÁRIO DE VISITA
    // =========================

    /** Chips na ordem 1=segunda ... 7=domingo (mesma numeração do banco). */
    private com.google.android.material.chip.Chip[] chipsDias() {
        return new com.google.android.material.chip.Chip[]{
                binding.chipSeg, binding.chipTer, binding.chipQua, binding.chipQui,
                binding.chipSex, binding.chipSab, binding.chipDom};
    }

    private String diasMarcados() {
        StringBuilder dias = new StringBuilder();
        com.google.android.material.chip.Chip[] chips = chipsDias();
        for (int i = 0; i < chips.length; i++) {
            if (chips[i].isChecked()) {
                if (dias.length() > 0) dias.append(',');
                dias.append(i + 1);
            }
        }
        return dias.toString();
    }

    private void marcarDias(String dias) {
        if (dias == null) return;
        com.google.android.material.chip.Chip[] chips = chipsDias();
        for (com.google.android.material.chip.Chip chip : chips) chip.setChecked(false);
        for (String parte : dias.split(",")) {
            try {
                int d = Integer.parseInt(parte.trim());
                if (d >= 1 && d <= 7) chips[d - 1].setChecked(true);
            } catch (NumberFormatException ignorada) {
                // valor inválido: ignora
            }
        }
    }

    /** Relógio do Material Design (só horas cheias, já que cada visita dura 1 hora). */
    private void escolherHora(boolean abertura) {
        if (getSupportFragmentManager().findFragmentByTag(TAG_RELOGIO) != null) return;
        com.google.android.material.timepicker.MaterialTimePicker relogio =
                new com.google.android.material.timepicker.MaterialTimePicker.Builder()
                        .setTimeFormat(com.google.android.material.timepicker.TimeFormat.CLOCK_24H)
                        .setHour(abertura ? horaAbre : Math.min(horaFecha, 23))
                        .setMinute(0)
                        .setTitleText(abertura ? "Abre às" : "Fecha às")
                        .build();
        relogio.addOnPositiveButtonClickListener(v -> {
            int hora = relogio.getHour();
            if (abertura) {
                horaAbre = hora;
            } else {
                horaFecha = hora == 0 ? 24 : hora;
            }
            if (relogio.getMinute() != 0) {
                Toast.makeText(this, "Usamos horas cheias: os minutos foram ignorados", Toast.LENGTH_SHORT).show();
            }
            mostrarHorario();
        });
        relogio.show(getSupportFragmentManager(), TAG_RELOGIO);
    }

    private void mostrarHorario() {
        binding.btnAbertura.setText(String.format(Locale.US, "Abre %02d:00", horaAbre));
        binding.btnFechamento.setText(horaFecha == 24 ? "Fecha 24:00"
                : String.format(Locale.US, "Fecha %02d:00", horaFecha));
    }

    /** A seção de dias e horário só faz sentido se o ateliê aceita visitas. */
    private void atualizarSecaoHorarios() {
        boolean ativo = binding.switchVisitas.isChecked();
        binding.secaoHorarios.setAlpha(ativo ? 1f : 0.4f);
        binding.btnAbertura.setEnabled(ativo);
        binding.btnFechamento.setEnabled(ativo);
        for (com.google.android.material.chip.Chip chip : chipsDias()) chip.setEnabled(ativo);
    }

    /** "10:00" ou "10:00:00" -> 10 */
    private static int lerHora(String texto, int padrao) {
        if (texto == null || texto.length() < 2) return padrao;
        try {
            return Integer.parseInt(texto.substring(0, 2));
        } catch (NumberFormatException e) {
            return padrao;
        }
    }

    // =========================
    // AUXILIARES
    // =========================

    private String especialidadeSelecionada() {
        int id = binding.grupoEspecialidade.getCheckedChipId();
        if (id == R.id.chipEscultura) return ObraRepositorio.ESCULTURA;
        if (id == R.id.chipFotografia) return ObraRepositorio.FOTOGRAFIA;
        if (id == R.id.chipDigital) return ObraRepositorio.DIGITAL;
        return ObraRepositorio.PINTURA;
    }

    private int chipDaEspecialidade(String especialidade) {
        if (ObraRepositorio.ESCULTURA.equals(especialidade)) return R.id.chipEscultura;
        if (ObraRepositorio.FOTOGRAFIA.equals(especialidade)) return R.id.chipFotografia;
        if (ObraRepositorio.DIGITAL.equals(especialidade)) return R.id.chipDigital;
        return R.id.chipPintura;
    }

    private static String texto(EditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
