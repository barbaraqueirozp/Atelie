package com.example.ateli;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ateli.databinding.ActivityLoginBinding;

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

        String emailCadastrado = preferences.getString("email", "");
        String senhaCadastrada = preferences.getString("senha", "");

        if (email.equals(emailCadastrado) && senha.equals(senhaCadastrada)) {
            Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();

            // Intent explícita levando o nome do usuário para a tela principal
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_NOME_USUARIO, preferences.getString("nome", ""));
            startActivity(intent);
            finish();
        } else {
            binding.tilSenha.setError("E-mail ou senha incorretos");
        }
    }

    /** getText() pode retornar null — este método evita NullPointerException. */
    private static String texto(Editable editable) {
        return editable == null ? "" : editable.toString();
    }
}
