package com.example.ateli;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ateli.api.ApiClient;
import com.example.ateli.api.LoginRequest;
import com.example.ateli.api.UsuarioResponse;

import com.example.ateli.databinding.ActivityLoginBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SharedPreferences preferences;

    /**
     * Abre o Cadastro e recebe de volta o e-mail cadastrado (Intent de resultado),
     * já preenchendo o campo de login.
     */
    private final ActivityResultLauncher<Intent> cadastroLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String emailCadastrado =
                            result.getData().getStringExtra(CadastroActivity.EXTRA_EMAIL);
                    if (emailCadastrado != null) {
                        binding.edtEmail.setText(emailCadastrado);
                        binding.edtSenha.requestFocus();
                        Toast.makeText(this,
                                "Cadastro realizado! Agora é só digitar sua senha.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferences = getSharedPreferences(CadastroActivity.PREFS_USUARIO, MODE_PRIVATE);

        binding.btnEntrar.setOnClickListener(v -> fazerLogin());

        binding.btnCriarConta.setOnClickListener(v ->
                cadastroLauncher.launch(new Intent(this, CadastroActivity.class)));

        binding.btnEsqueciSenha.setOnClickListener(v ->
                Toast.makeText(this, "Recuperação de senha", Toast.LENGTH_SHORT).show());
    }

    private void fazerLogin() {
        binding.tilEmail.setError(null);
        binding.tilSenha.setError(null);

        String email = texto(binding.edtEmail.getText()).trim();
        String senha = texto(binding.edtSenha.getText());

        if (email.isEmpty()) {
            binding.tilEmail.setError("Digite seu e-mail");
            binding.edtEmail.requestFocus();
            return;
        }

        if (senha.isEmpty()) {
            binding.tilSenha.setError("Digite sua senha");
            binding.edtSenha.requestFocus();
            return;
        }

        // Confere e-mail e senha na API (que compara com o hash salvo no banco)
        binding.btnEntrar.setEnabled(false);
        ApiClient.get().login(new LoginRequest(email, senha)).enqueue(new Callback<UsuarioResponse>() {
            @Override
            public void onResponse(@NonNull Call<UsuarioResponse> call,
                                   @NonNull Response<UsuarioResponse> resposta) {
                if (isFinishing() || isDestroyed()) return;
                binding.btnEntrar.setEnabled(true);

                if (resposta.isSuccessful() && resposta.body() != null) {
                    UsuarioResponse usuario = resposta.body();

                    // Guarda só a sessão (id e nome) — a senha NÃO fica no celular
                    preferences.edit()
                            .clear()
                            .putInt("id_usuario", usuario.idUsuario)
                            .putString("nome", usuario.nomeCompleto)
                            .putString("email", usuario.email)
                            .apply();

                    Toast.makeText(LoginActivity.this, "Login realizado com sucesso!",
                            Toast.LENGTH_SHORT).show();

                    // Intent explícita levando o nome do usuário para a tela principal
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra(MainActivity.EXTRA_NOME_USUARIO, usuario.nomeCompleto);
                    startActivity(intent);
                    finish();
                } else {
                    binding.tilSenha.setError(
                            ApiClient.mensagemDeErro(resposta, "E-mail ou senha incorretos"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<UsuarioResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                binding.btnEntrar.setEnabled(true);
                Toast.makeText(LoginActivity.this,
                        "Sem conexão com o servidor. A API está rodando?",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /** getText() pode retornar null — este método evita NullPointerException. */
    private static String texto(Editable editable) {
        return editable == null ? "" : editable.toString();
    }
}
