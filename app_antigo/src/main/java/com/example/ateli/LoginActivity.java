package com.example.ateli;



import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail;
    private EditText edtSenha;
    private TextView btnEntrar;
    private TextView btnCriarConta;
    private TextView txtEsqueciSenha;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ligando os campos do XML
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        btnCriarConta = findViewById(R.id.btnCriarConta);
        txtEsqueciSenha = findViewById(R.id.txtEsqueciSenha);

        // Acessa os dados salvos no cadastro
        preferences = getSharedPreferences("dados_usuario", MODE_PRIVATE);

        // BOTÃO ENTRAR
        btnEntrar.setOnClickListener(v -> {

            String email = edtEmail.getText().toString().trim();
            String senha = edtSenha.getText().toString();

            // Verifica se o e-mail foi preenchido
            if (email.isEmpty()) {
                edtEmail.setError("Digite seu e-mail");
                edtEmail.requestFocus();
                return;
            }

            // Verifica se a senha foi preenchida
            if (senha.isEmpty()) {
                edtSenha.setError("Digite sua senha");
                edtSenha.requestFocus();
                return;
            }

            // Pega o e-mail e a senha cadastrados
            String emailCadastrado =
                    preferences.getString("email", "");

            String senhaCadastrada =
                    preferences.getString("senha", "");

            // Confere os dados
            if (email.equals(emailCadastrado)
                    && senha.equals(senhaCadastrada)) {

                Toast.makeText(
                        LoginActivity.this,
                        "Login realizado com sucesso!",
                        Toast.LENGTH_SHORT
                ).show();

                // Vai para a tela principal
                Intent intent = new Intent(
                        LoginActivity.this,
                        MainActivity.class
                );

                startActivity(intent);
                finish();

            } else {

                Toast.makeText(
                        LoginActivity.this,
                        "E-mail ou senha incorretos",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // BOTÃO CRIAR CONTA
        btnCriarConta.setOnClickListener(v -> {

            Intent intent = new Intent(
                    LoginActivity.this,
                    CadastroActivity.class
            );

            startActivity(intent);
        });

        // ESQUECI MINHA SENHA
        txtEsqueciSenha.setOnClickListener(v -> {

            Toast.makeText(
                    LoginActivity.this,
                    "Recuperação de senha",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }
}