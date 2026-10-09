package com.example.ateli;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.ateli.databinding.ActivityCadastroBinding;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class CadastroActivity extends AppCompatActivity {

    /** Chave do e-mail devolvido para o Login. */
    public static final String EXTRA_EMAIL = "com.example.ateli.EXTRA_EMAIL";
    /** Nome do arquivo de SharedPreferences com os dados do usuário. */
    public static final String PREFS_USUARIO = "dados_usuario";

    private static final String TAG_PICKER_NASCIMENTO = "picker_nascimento";
    private static final String ESTADO_DATA_NASCIMENTO = "estado_data_nascimento";

    private ActivityCadastroBinding binding;
    private SharedPreferences preferences;

    /** Data escolhida no MaterialDatePicker (UTC). -1 = ainda não escolhida. */
    private long dataNascimentoMillis = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferences = getSharedPreferences(PREFS_USUARIO, MODE_PRIVATE);

        // Ciclo de vida: ao girar a tela, a Activity é recriada.
        // Recuperamos a data já escolhida para não perdê-la.
        if (savedInstanceState != null) {
            dataNascimentoMillis = savedInstanceState.getLong(ESTADO_DATA_NASCIMENTO, -1L);
            if (dataNascimentoMillis != -1L) {
                binding.edtDataNascimento.setText(Datas.formatar(dataNascimentoMillis));
            }
        }
        reconectarDatePicker();

        binding.edtDataNascimento.setOnClickListener(v -> abrirDatePicker());
        binding.tilDataNascimento.setEndIconOnClickListener(v -> abrirDatePicker());

        binding.btnCadastrar.setOnClickListener(v -> cadastrar());

        binding.btnJaTenhoConta.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish(); // volta para o Login que já está na pilha
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(ESTADO_DATA_NASCIMENTO, dataNascimentoMillis);
    }

    // =========================
    // MATERIAL DATE PICKER
    // =========================

    private void abrirDatePicker() {
        // Evita abrir dois calendários com toques rápidos
        if (getSupportFragmentManager().findFragmentByTag(TAG_PICKER_NASCIMENTO) != null) {
            return;
        }

        CalendarConstraints restricoes = new CalendarConstraints.Builder()
                .setEnd(MaterialDatePicker.todayInUtcMilliseconds())
                .setValidator(DateValidatorPointBackward.now()) // só datas passadas
                .build();

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Data de nascimento")
                .setSelection(dataNascimentoMillis != -1L
                        ? dataNascimentoMillis
                        : MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(restricoes)
                .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                .build();

        picker.addOnPositiveButtonClickListener(this::aoEscolherData);
        picker.show(getSupportFragmentManager(), TAG_PICKER_NASCIMENTO);
    }

    /** Se o calendário estava aberto quando a tela girou, religa o listener. */
    @SuppressWarnings("unchecked")
    private void reconectarDatePicker() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_PICKER_NASCIMENTO);
        if (fragment instanceof MaterialDatePicker) {
            ((MaterialDatePicker<Long>) fragment).addOnPositiveButtonClickListener(this::aoEscolherData);
        }
    }

    private void aoEscolherData(Long selecao) {
        if (selecao == null) {
            return;
        }
        dataNascimentoMillis = selecao;
        binding.edtDataNascimento.setText(Datas.formatar(selecao));
        binding.tilDataNascimento.setError(null);
    }

    // =========================
    // CADASTRO
    // =========================

    private void cadastrar() {
        limparErros();

        String nome = texto(binding.edtNome);
        String cpf = texto(binding.edtCpf);
        String email = texto(binding.edtEmailCadastro);
        String senha = textoSemTrim(binding.edtSenhaCadastro);
        String confirmarSenha = textoSemTrim(binding.edtConfirmarSenha);

        String cep = texto(binding.edtCep);
        String rua = texto(binding.edtRua);
        String numero = texto(binding.edtNumero);
        String complemento = texto(binding.edtComplemento);
        String bairro = texto(binding.edtBairro);
        String cidade = texto(binding.edtCidade);
        String estado = texto(binding.edtEstado);

        if (nome.isEmpty()) {
            mostrarErro(binding.tilNome, binding.edtNome, "Digite seu nome");
            return;
        }
        if (cpf.length() != 11) {
            mostrarErro(binding.tilCpf, binding.edtCpf, "Digite um CPF válido (11 números)");
            return;
        }
        if (dataNascimentoMillis == -1L) {
            binding.tilDataNascimento.setError("Escolha sua data de nascimento");
            return;
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mostrarErro(binding.tilEmail, binding.edtEmailCadastro, "Digite um e-mail válido");
            return;
        }
        if (senha.length() < 6) {
            mostrarErro(binding.tilSenha, binding.edtSenhaCadastro,
                    "A senha deve ter pelo menos 6 caracteres");
            return;
        }
        if (!senha.equals(confirmarSenha)) {
            mostrarErro(binding.tilConfirmarSenha, binding.edtConfirmarSenha,
                    "As senhas não são iguais");
            return;
        }
        if (cep.length() != 8) {
            mostrarErro(binding.tilCep, binding.edtCep, "Digite um CEP válido (8 números)");
            return;
        }
        if (rua.isEmpty()) {
            mostrarErro(binding.tilRua, binding.edtRua, "Digite sua rua");
            return;
        }
        if (numero.isEmpty()) {
            mostrarErro(binding.tilNumero, binding.edtNumero, "Digite o número");
            return;
        }
        if (bairro.isEmpty()) {
            mostrarErro(binding.tilBairro, binding.edtBairro, "Digite seu bairro");
            return;
        }
        if (cidade.isEmpty()) {
            mostrarErro(binding.tilCidade, binding.edtCidade, "Digite sua cidade");
            return;
        }
        if (estado.length() != 2) {
            mostrarErro(binding.tilEstado, binding.edtEstado, "Digite a sigla do estado (ex.: GO)");
            return;
        }

        // =========================
        // SALVAR USUÁRIO
        // =========================

        preferences.edit()
                .putString("nome", nome)
                .putString("cpf", cpf)
                .putLong("data_nascimento", dataNascimentoMillis)
                .putString("email", email)
                .putString("senha", senha)
                .putString("cep", cep)
                .putString("rua", rua)
                .putString("numero", numero)
                .putString("complemento", complemento)
                .putString("bairro", bairro)
                .putString("cidade", cidade)
                .putString("estado", estado)
                .apply();

        // Devolve o e-mail para o Login (Intent de resultado) e fecha esta tela.
        // Não abrimos um novo Login para não duplicar telas na pilha.
        Intent resultado = new Intent();
        resultado.putExtra(EXTRA_EMAIL, email);
        setResult(RESULT_OK, resultado);
        finish();
    }

    // =========================
    // AUXILIARES
    // =========================

    private void mostrarErro(TextInputLayout campo, TextInputEditText edit, String mensagem) {
        campo.setError(mensagem);
        edit.requestFocus();
    }

    private void limparErros() {
        TextInputLayout[] campos = {
                binding.tilNome, binding.tilCpf, binding.tilDataNascimento, binding.tilEmail,
                binding.tilSenha, binding.tilConfirmarSenha, binding.tilCep, binding.tilRua,
                binding.tilNumero, binding.tilComplemento, binding.tilBairro,
                binding.tilCidade, binding.tilEstado
        };
        for (TextInputLayout campo : campos) {
            campo.setError(null);
        }
    }

    /** Lê o texto com segurança (getText() pode ser null) e remove espaços. */
    private static String texto(TextInputEditText edit) {
        return textoSemTrim(edit).trim();
    }

    private static String textoSemTrim(TextInputEditText edit) {
        Editable editable = edit.getText();
        return editable == null ? "" : editable.toString();
    }
}
