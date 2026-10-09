package com.example.ateli;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.ateli.databinding.ActivityObraDetalheBinding;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;

/**
 * Tela de detalhes de uma obra. Recebe apenas o ID da obra pela Intent
 * e busca o restante no repositório (tráfego de dados mínimo e seguro).
 */
public class ObraDetalheActivity extends AppCompatActivity {

    /** Chave do ID da obra enviado pelas telas de listagem. */
    public static final String EXTRA_OBRA_ID = "com.example.ateli.EXTRA_OBRA_ID";

    private static final String TAG_PICKER_VISITA = "picker_visita";
    private static final String ESTADO_DATA_VISITA = "estado_data_visita";

    private ActivityObraDetalheBinding binding;
    private Obra obra;
    private long dataVisitaMillis = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Validação dos dados recebidos ANTES de montar a tela:
        // se a Intent vier sem ID ou com um ID inválido, fechamos sem travar o app.
        int obraId = getIntent().getIntExtra(EXTRA_OBRA_ID, -1);
        obra = ObraRepositorio.buscarPorId(obraId);
        if (obra == null) {
            Toast.makeText(this, "Obra não encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        binding = ActivityObraDetalheBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        preencherTela();

        if (savedInstanceState != null) {
            dataVisitaMillis = savedInstanceState.getLong(ESTADO_DATA_VISITA, -1L);
            if (dataVisitaMillis != -1L) {
                mostrarDataVisita();
            }
        }
        reconectarDatePicker();

        binding.btnVoltar.setOnClickListener(v -> finish());
        binding.btnAgendarVisita.setOnClickListener(v -> abrirDatePicker());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(ESTADO_DATA_VISITA, dataVisitaMillis);
    }

    private void preencherTela() {
        binding.txtCategoria.setText(obra.getCategoria());
        binding.txtTitulo.setText(obra.getTitulo());
        binding.txtArtista.setText(obra.getArtista());
        binding.txtPreco.setText(obra.getPrecoFormatado());
        binding.txtDescricao.setText(obra.getDescricao());

        binding.imgObra.setBackgroundResource(obra.getFundoRes());
        if (obra.getImagemRes() != 0) {
            binding.imgObra.setImageResource(obra.getImagemRes());
        }
        binding.imgObra.setContentDescription("Obra " + obra.getTitulo());
    }

    // =========================
    // MATERIAL DATE PICKER
    // =========================

    private void abrirDatePicker() {
        if (getSupportFragmentManager().findFragmentByTag(TAG_PICKER_VISITA) != null) {
            return; // já está aberto
        }

        CalendarConstraints restricoes = new CalendarConstraints.Builder()
                .setStart(MaterialDatePicker.todayInUtcMilliseconds())
                .setValidator(DateValidatorPointForward.now()) // só hoje ou datas futuras
                .build();

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Data da visita ao ateliê")
                .setSelection(dataVisitaMillis != -1L
                        ? dataVisitaMillis
                        : MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(restricoes)
                .build();

        picker.addOnPositiveButtonClickListener(this::aoEscolherData);
        picker.show(getSupportFragmentManager(), TAG_PICKER_VISITA);
    }

    @SuppressWarnings("unchecked")
    private void reconectarDatePicker() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_PICKER_VISITA);
        if (fragment instanceof MaterialDatePicker) {
            ((MaterialDatePicker<Long>) fragment).addOnPositiveButtonClickListener(this::aoEscolherData);
        }
    }

    private void aoEscolherData(Long selecao) {
        if (selecao == null) {
            return;
        }
        dataVisitaMillis = selecao;
        mostrarDataVisita();
        Toast.makeText(this, "Visita agendada!", Toast.LENGTH_SHORT).show();
    }

    private void mostrarDataVisita() {
        binding.txtDataVisita.setText("Visita marcada para " + Datas.formatarPorExtenso(dataVisitaMillis));
        binding.btnAgendarVisita.setText("Alterar data");
    }
}
