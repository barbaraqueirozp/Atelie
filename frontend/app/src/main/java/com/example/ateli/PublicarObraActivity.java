package com.example.ateli;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.ateli.api.ApiClient;
import com.example.ateli.api.PublicarObraResponse;
import com.example.ateli.databinding.ActivityPublicarObraBinding;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** O artista fotografa (ou escolhe da galeria) a obra, preenche os dados e publica. */
public class PublicarObraActivity extends AppCompatActivity {

    private static final String ESTADO_FOTO = "estado_foto";
    private static final String ESTADO_CAMERA = "estado_camera";
    private static final MediaType TEXTO = MediaType.parse("text/plain");
    private static final MediaType JPEG = MediaType.parse("image/jpeg");

    private ActivityPublicarObraBinding binding;
    private final ExecutorService segundoPlano = Executors.newSingleThreadExecutor();

    private int idUsuario;
    /** Foto escolhida (câmera ou galeria). null = ainda sem foto. */
    private Uri fotoUri;
    /** Arquivo que a câmera vai preencher enquanto está aberta. */
    private Uri cameraUri;

    // Câmera: abre o app de câmera do celular, que grava a foto em cameraUri.
    // Não precisa pedir permissão de CAMERA porque quem usa a câmera é o app do sistema.
    private final ActivityResultLauncher<Uri> tirarFoto =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), sucesso -> {
                if (Boolean.TRUE.equals(sucesso) && cameraUri != null) {
                    mostrarFoto(cameraUri);
                }
            });

    // Galeria: seletor de fotos do Android (também não precisa de permissão).
    private final ActivityResultLauncher<PickVisualMediaRequest> escolherDaGaleria =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    mostrarFoto(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(CadastroActivity.PREFS_USUARIO, MODE_PRIVATE);
        idUsuario = prefs.getInt("id_usuario", -1);
        if (idUsuario == -1) {
            Toast.makeText(this, "Faça login para publicar", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        binding = ActivityPublicarObraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.ime());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        // Ciclo de vida: ao girar a tela, recupera a foto já escolhida
        if (savedInstanceState != null) {
            cameraUri = savedInstanceState.getParcelable(ESTADO_CAMERA);
            Uri salva = savedInstanceState.getParcelable(ESTADO_FOTO);
            if (salva != null) {
                mostrarFoto(salva);
            }
        }

        binding.btnVoltar.setOnClickListener(v -> finish());
        binding.areaFoto.setOnClickListener(v -> abrirCamera());
        binding.btnCamera.setOnClickListener(v -> abrirCamera());
        binding.btnGaleria.setOnClickListener(v -> escolherDaGaleria.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()));
        binding.btnPublicar.setOnClickListener(v -> publicar());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ESTADO_FOTO, fotoUri);
        outState.putParcelable(ESTADO_CAMERA, cameraUri);
    }

    @Override
    protected void onDestroy() {
        segundoPlano.shutdown();
        super.onDestroy();
    }

    // =========================
    // FOTO
    // =========================

    private void abrirCamera() {
        try {
            File arquivo = Fotos.novoArquivoCamera(this);
            cameraUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", arquivo);
            tirarFoto.launch(cameraUri);
        } catch (IOException | IllegalArgumentException e) {
            Toast.makeText(this, "Não foi possível abrir a câmera", Toast.LENGTH_SHORT).show();
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, "Nenhum app de câmera encontrado", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarFoto(Uri uri) {
        fotoUri = uri;
        binding.txtSemFoto.setVisibility(View.GONE);
        binding.imgFoto.setVisibility(View.VISIBLE);
        Glide.with(this).load(uri).into(binding.imgFoto);
    }

    // =========================
    // PUBLICAR
    // =========================

    private void publicar() {
        String titulo = texto(binding.edtTitulo);
        String descricao = texto(binding.edtDescricao);
        String dimensoes = ""; // o protótipo pede o tamanho junto da descrição
        String categoria = categoriaSelecionada();
        BigDecimal preco = lerPreco(texto(binding.edtPreco));

        if (fotoUri == null) {
            Toast.makeText(this, "Adicione uma foto da obra", Toast.LENGTH_SHORT).show();
            return;
        }
        if (titulo.isEmpty()) {
            binding.edtTitulo.setError("Digite o título");
            binding.edtTitulo.requestFocus();
            return;
        }
        if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
            binding.edtPreco.setError("Preço inválido");
            binding.edtPreco.requestFocus();
            return;
        }
        if (categoria == null) {
            Toast.makeText(this, "Escolha a categoria", Toast.LENGTH_SHORT).show();
            return;
        }

        carregando(true);

        // Comprimir a foto é pesado: faz em segundo plano e depois envia
        Uri origem = fotoUri;
        segundoPlano.execute(() -> {
            try {
                File foto = Fotos.comprimir(getApplicationContext(), origem);
                runOnUiThread(() -> enviar(foto, titulo, descricao, categoria, preco, dimensoes));
            } catch (IOException e) {
                runOnUiThread(() -> {
                    carregando(false);
                    Toast.makeText(this, "Não foi possível ler a foto", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void enviar(File foto, String titulo, String descricao, String categoria,
                        BigDecimal preco, String dimensoes) {
        if (isFinishing() || isDestroyed()) return;

        MultipartBody.Part parteFoto = MultipartBody.Part.createFormData(
                "foto", foto.getName(), RequestBody.create(JPEG, foto));

        ApiClient.get().publicarObra(
                parte(String.valueOf(idUsuario)),
                parte(titulo),
                parte(descricao),
                parte(categoria),
                parte(preco.toPlainString()),
                parte(dimensoes),
                parteFoto
        ).enqueue(new Callback<PublicarObraResponse>() {
            @Override
            public void onResponse(@NonNull Call<PublicarObraResponse> call,
                                   @NonNull Response<PublicarObraResponse> resposta) {
                foto.delete();
                if (isFinishing() || isDestroyed()) return;
                carregando(false);

                if (resposta.isSuccessful()) {
                    Toast.makeText(PublicarObraActivity.this, "Obra enviada!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish(); // volta para a tela anterior, que recarrega a lista
                } else {
                    Toast.makeText(PublicarObraActivity.this,
                            ApiClient.mensagemDeErro(resposta, "Não foi possível publicar"),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PublicarObraResponse> call, @NonNull Throwable t) {
                foto.delete();
                if (isFinishing() || isDestroyed()) return;
                carregando(false);
                Toast.makeText(PublicarObraActivity.this,
                        "Sem conexão com o servidor. A API está rodando?", Toast.LENGTH_LONG).show();
            }
        });
    }

    // =========================
    // AUXILIARES
    // =========================

    private void carregando(boolean sim) {
        binding.btnPublicar.setEnabled(!sim);
        binding.btnPublicar.setText(sim ? "Enviando..." : "Enviar para curadoria");
        binding.btnCamera.setEnabled(!sim);
        binding.btnGaleria.setEnabled(!sim);
        binding.areaFoto.setEnabled(!sim);
    }

    private String categoriaSelecionada() {
        int id = binding.grupoCategoria.getCheckedChipId();
        if (id == R.id.chipPintura) return ObraRepositorio.PINTURA;
        if (id == R.id.chipEscultura) return ObraRepositorio.ESCULTURA;
        if (id == R.id.chipFotografia) return ObraRepositorio.FOTOGRAFIA;
        if (id == R.id.chipDigital) return ObraRepositorio.DIGITAL;
        return null;
    }

    /** Aceita "1240", "1240,50", "1.240,50" e "1240.50". */
    private static BigDecimal lerPreco(String texto) {
        if (texto.isEmpty()) return null;
        String normalizado = texto.contains(",")
                ? texto.replace(".", "").replace(",", ".")
                : texto;
        try {
            return new BigDecimal(normalizado);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static RequestBody parte(String valor) {
        return RequestBody.create(TEXTO, valor == null ? "" : valor);
    }

    private static String texto(EditText campo) {
        Editable editable = campo.getText();
        return editable == null ? "" : editable.toString().trim();
    }
}
