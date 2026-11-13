package com.example.ateli;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CadastroActivity extends AppCompatActivity {

    // =========================
    // CAMPOS
    // =========================

    private EditText edtNome;
    private EditText edtCpf;
    private EditText edtEmail;
    private EditText edtSenha;
    private EditText edtConfirmarSenha;

    private EditText edtCep;
    private EditText edtRua;
    private EditText edtNumero;
    private EditText edtComplemento;
    private EditText edtBairro;
    private EditText edtCidade;
    private EditText edtEstado;

    private TextView btnCadastrar;
    private TextView txtJaTenhoConta;

    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cadastro);


        // =========================
        // CAMPOS
        // =========================

        edtNome = findViewById(R.id.edtNome);
        edtCpf = findViewById(R.id.edtCpf);
        edtEmail = findViewById(R.id.edtEmailCadastro);
        edtSenha = findViewById(R.id.edtSenhaCadastro);
        edtConfirmarSenha = findViewById(R.id.edtConfirmarSenha);

        // Endereço
        edtCep = findViewById(R.id.edtCep);
        edtRua = findViewById(R.id.edtRua);
        edtNumero = findViewById(R.id.edtNumero);
        edtComplemento = findViewById(R.id.edtComplemento);
        edtBairro = findViewById(R.id.edtBairro);
        edtCidade = findViewById(R.id.edtCidade);
        edtEstado = findViewById(R.id.edtEstado);


        // =========================
        // BOTÕES
        // =========================

        btnCadastrar = findViewById(R.id.btnCadastrar);
        txtJaTenhoConta = findViewById(R.id.txtJaTenhoConta);


        // =========================
        // ARMAZENAMENTO LOCAL
        // =========================

        preferences = getSharedPreferences(
                "dados_usuario",
                MODE_PRIVATE
        );


        // =========================
        // BOTÃO CADASTRAR
        // =========================

        btnCadastrar.setOnClickListener(v -> {

            String nome = edtNome.getText().toString().trim();
            String cpf = edtCpf.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String senha = edtSenha.getText().toString();
            String confirmarSenha =
                    edtConfirmarSenha.getText().toString();

            // Endereço
            String cep = edtCep.getText().toString().trim();
            String rua = edtRua.getText().toString().trim();
            String numero = edtNumero.getText().toString().trim();
            String complemento =
                    edtComplemento.getText().toString().trim();
            String bairro = edtBairro.getText().toString().trim();
            String cidade = edtCidade.getText().toString().trim();
            String estado = edtEstado.getText().toString().trim();


            // =========================
            // NOME
            // =========================

            if (nome.isEmpty()) {

                edtNome.setError("Digite seu nome");
                edtNome.requestFocus();
                return;
            }


            // =========================
            // CPF
            // =========================

            if (cpf.isEmpty()) {

                edtCpf.setError("Digite seu CPF");
                edtCpf.requestFocus();
                return;
            }

            if (cpf.length() < 11) {

                edtCpf.setError("Digite um CPF válido");
                edtCpf.requestFocus();
                return;
            }


            // =========================
            // E-MAIL
            // =========================

            if (email.isEmpty()) {

                edtEmail.setError("Digite seu e-mail");
                edtEmail.requestFocus();
                return;
            }


            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                edtEmail.setError("Digite um e-mail válido");
                edtEmail.requestFocus();
                return;
            }


            // =========================
            // SENHA
            // =========================

            if (senha.isEmpty()) {

                edtSenha.setError("Digite uma senha");
                edtSenha.requestFocus();
                return;
            }


            if (senha.length() < 6) {

                edtSenha.setError(
                        "A senha deve ter pelo menos 6 caracteres"
                );

                edtSenha.requestFocus();
                return;
            }


            // =========================
            // CONFIRMAÇÃO DA SENHA
            // =========================

            if (confirmarSenha.isEmpty()) {

                edtConfirmarSenha.setError(
                        "Confirme sua senha"
                );

                edtConfirmarSenha.requestFocus();
                return;
            }


            if (!senha.equals(confirmarSenha)) {

                edtConfirmarSenha.setError(
                        "As senhas não são iguais"
                );

                edtConfirmarSenha.requestFocus();
                return;
            }


            // =========================
            // CEP
            // =========================

            if (cep.isEmpty()) {

                edtCep.setError("Digite seu CEP");
                edtCep.requestFocus();
                return;
            }


            // =========================
            // RUA
            // =========================

            if (rua.isEmpty()) {

                edtRua.setError("Digite sua rua");
                edtRua.requestFocus();
                return;
            }


            // =========================
            // NÚMERO
            // =========================

            if (numero.isEmpty()) {

                edtNumero.setError("Digite o número");
                edtNumero.requestFocus();
                return;
            }


            // =========================
            // BAIRRO
            // =========================

            if (bairro.isEmpty()) {

                edtBairro.setError("Digite seu bairro");
                edtBairro.requestFocus();
                return;
            }


            // =========================
            // CIDADE
            // =========================

            if (cidade.isEmpty()) {

                edtCidade.setError("Digite sua cidade");
                edtCidade.requestFocus();
                return;
            }


            // =========================
            // ESTADO
            // =========================

            if (estado.isEmpty()) {

                edtEstado.setError("Digite seu estado");
                edtEstado.requestFocus();
                return;
            }


            // =========================
            // SALVAR USUÁRIO
            // =========================

            SharedPreferences.Editor editor =
                    preferences.edit();

            editor.putString("nome", nome);
            editor.putString("cpf", cpf);
            editor.putString("email", email);
            editor.putString("senha", senha);

            // Endereço
            editor.putString("cep", cep);
            editor.putString("rua", rua);
            editor.putString("numero", numero);
            editor.putString("complemento", complemento);
            editor.putString("bairro", bairro);
            editor.putString("cidade", cidade);
            editor.putString("estado", estado);

            editor.apply();


            // =========================
            // MENSAGEM
            // =========================

            Toast.makeText(
                    CadastroActivity.this,
                    "Cadastro realizado com sucesso!",
                    Toast.LENGTH_SHORT
            ).show();


            // =========================
            // VOLTA PARA O LOGIN
            // =========================

            Intent intent = new Intent(
                    CadastroActivity.this,
                    LoginActivity.class
            );

            startActivity(intent);

            finish();
        });


        // =========================
        // JÁ TENHO UMA CONTA
        // =========================

        txtJaTenhoConta.setOnClickListener(v -> {

            Intent intent = new Intent(
                    CadastroActivity.this,
                    LoginActivity.class
            );

            startActivity(intent);

            finish();
        });
    }
}