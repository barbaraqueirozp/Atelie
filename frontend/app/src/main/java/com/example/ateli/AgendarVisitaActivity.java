package com.example.ateli;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.ateli.api.AgendaResponse;
import com.example.ateli.api.ApiClient;
import com.example.ateli.api.VisitaRequest;
import com.example.ateli.api.VisitaResponse;
import com.example.ateli.databinding.ActivityAgendarVisitaBinding;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Agendamento de visita ao ateliê do artista de uma obra.
 * Mostra um calendário com os dias livres e, ao escolher o dia, os horários livres.
 * Devolve para a tela anterior um texto como "quinta-feira, 12 de novembro às 14:00".
 */
public class AgendarVisitaActivity extends AppCompatActivity {

    public static final String EXTRA_ID_ARTISTA = "com.example.ateli.EXTRA_ID_ARTISTA";
    public static final String EXTRA_TITULO_OBRA = "com.example.ateli.EXTRA_TITULO_OBRA";
    public static final String EXTRA_RESULTADO = "com.example.ateli.EXTRA_RESULTADO_VISITA";

    private static final String ESTADO_ANO = "estado_ano";
    private static final String ESTADO_MES = "estado_mes";
    private static final String ESTADO_DIA = "estado_dia";
    private static final String ESTADO_HORA = "estado_hora";

    /** Mesmo limite da API: agendamentos até ~3 meses à frente. */
    private static final int MESES_A_FRENTE = 3;

    private static final Locale PT_BR = new Locale("pt", "BR");

    private ActivityAgendarVisitaBinding binding;
    private DiaCalendarioAdapter adapterDias;
    private int idArtista;
    private int idUsuario;

    /** Primeiro dia do mês que está na tela. */
    private final Calendar mesNaTela = Calendar.getInstance();
    /** Horários livres por dia ("2026-11-10" -> ["10:00", ...]) do mês na tela. */
    private final Map<String, List<String>> horariosPorDia = new HashMap<>();
    private String diaEscolhido;
    private String horaEscolhida;
    /** Evita mostrar a resposta de um mês antigo se a pessoa trocar de mês rápido. */
    private int pedidoAtual = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        idArtista = getIntent().getIntExtra(EXTRA_ID_ARTISTA, -1);
        idUsuario = Sessao.idUsuario(this);
        if (idArtista == -1 || idUsuario == -1) {
            Toast.makeText(this, "Faça login para agendar uma visita", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        binding = ActivityAgendarVisitaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        String tituloObra = getIntent().getStringExtra(EXTRA_TITULO_OBRA);
        binding.txtAtelie.setText(tituloObra == null ? "" : "Para conhecer de perto \"" + tituloObra + "\"");

        // Legenda: o "12" de exemplo aparece riscado, igual aos dias indisponíveis
        binding.txtLegendaIndisponivel.setPaintFlags(
                binding.txtLegendaIndisponivel.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        adapterDias = new DiaCalendarioAdapter(this::escolherDia);
        binding.rvDias.setLayoutManager(new GridLayoutManager(this, 7));
        binding.rvDias.setAdapter(adapterDias);

        // Começa no mês atual (ou no que estava na tela antes de girar)
        mesNaTela.set(Calendar.DAY_OF_MONTH, 1);
        zerarHora(mesNaTela);
        if (savedInstanceState != null) {
            mesNaTela.set(Calendar.YEAR, savedInstanceState.getInt(ESTADO_ANO, mesNaTela.get(Calendar.YEAR)));
            mesNaTela.set(Calendar.MONTH, savedInstanceState.getInt(ESTADO_MES, mesNaTela.get(Calendar.MONTH)));
            diaEscolhido = savedInstanceState.getString(ESTADO_DIA);
            horaEscolhida = savedInstanceState.getString(ESTADO_HORA);
        }

        binding.btnVoltar.setOnClickListener(v -> finish());
        binding.btnMesAnterior.setOnClickListener(v -> trocarMes(-1));
        binding.btnProximoMes.setOnClickListener(v -> trocarMes(1));
        binding.btnConfirmar.setOnClickListener(v -> confirmar());

        atualizarBotaoConfirmar();
        carregarMes();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ESTADO_ANO, mesNaTela.get(Calendar.YEAR));
        outState.putInt(ESTADO_MES, mesNaTela.get(Calendar.MONTH));
        outState.putString(ESTADO_DIA, diaEscolhido);
        outState.putString(ESTADO_HORA, horaEscolhida);
    }

    // =========================
    // CALENDÁRIO
    // =========================

    private void trocarMes(int delta) {
        Calendar novo = (Calendar) mesNaTela.clone();
        novo.add(Calendar.MONTH, delta);
        if (!mesPermitido(novo)) return;
        mesNaTela.setTime(novo.getTime());
        diaEscolhido = null;
        horaEscolhida = null;
        carregarMes();
    }

    /** Só o mês atual e os próximos MESES_A_FRENTE. */
    private boolean mesPermitido(Calendar mes) {
        Calendar primeiro = Calendar.getInstance();
        primeiro.set(Calendar.DAY_OF_MONTH, 1);
        zerarHora(primeiro);
        Calendar ultimo = (Calendar) primeiro.clone();
        ultimo.add(Calendar.MONTH, MESES_A_FRENTE);
        return !mes.before(primeiro) && !mes.after(ultimo);
    }

    private void carregarMes() {
        String nomeMes = new SimpleDateFormat("MMMM 'de' yyyy", PT_BR).format(mesNaTela.getTime());
        binding.txtMes.setText(nomeMes.substring(0, 1).toUpperCase(PT_BR) + nomeMes.substring(1));

        Calendar anterior = (Calendar) mesNaTela.clone();
        anterior.add(Calendar.MONTH, -1);
        Calendar proximo = (Calendar) mesNaTela.clone();
        proximo.add(Calendar.MONTH, 1);
        binding.btnMesAnterior.setAlpha(mesPermitido(anterior) ? 1f : 0.25f);
        binding.btnProximoMes.setAlpha(mesPermitido(proximo) ? 1f : 0.25f);

        // Mostra o mês todo indisponível enquanto a agenda carrega
        horariosPorDia.clear();
        montarGrade();
        mostrarHorarios();
        binding.txtCarregando.setVisibility(View.VISIBLE);

        int esteePedido = ++pedidoAtual;
        int diasNoMes = mesNaTela.getActualMaximum(Calendar.DAY_OF_MONTH);
        ApiClient.get().buscarAgenda(idArtista, iso(mesNaTela.getTime()), diasNoMes)
                .enqueue(new Callback<AgendaResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AgendaResponse> call,
                                           @NonNull Response<AgendaResponse> resposta) {
                        if (isFinishing() || isDestroyed() || esteePedido != pedidoAtual) return;
                        binding.txtCarregando.setVisibility(View.GONE);

                        if (!resposta.isSuccessful() || resposta.body() == null) {
                            Toast.makeText(AgendarVisitaActivity.this,
                                    ApiClient.mensagemDeErro(resposta, "Não foi possível carregar a agenda"),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        AgendaResponse agenda = resposta.body();
                        binding.txtAtelie.setText(agenda.nomeAtelie
                                + " · visitas das " + agenda.horaAbertura + " às " + agenda.horaFechamento);

                        boolean aberto = agenda.aceitaVisitas;
                        binding.txtFechado.setVisibility(aberto ? View.GONE : View.VISIBLE);
                        binding.secaoCalendario.setVisibility(aberto ? View.VISIBLE : View.GONE);
                        binding.secaoHorarios.setVisibility(aberto ? View.VISIBLE : View.GONE);

                        if (agenda.dias != null) {
                            for (AgendaResponse.DiaLivre dia : agenda.dias) {
                                horariosPorDia.put(dia.data, dia.horarios);
                            }
                        }
                        // Se o dia escolhido antes (ex.: antes de girar a tela) não está mais livre, limpa
                        if (diaEscolhido != null && !horariosPorDia.containsKey(diaEscolhido)) {
                            diaEscolhido = null;
                            horaEscolhida = null;
                        }
                        montarGrade();
                        mostrarHorarios();
                        if (horariosPorDia.isEmpty()) {
                            binding.txtEscolhaDia.setText("Nenhum horário livre neste mês. Veja o próximo mês ›");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AgendaResponse> call, @NonNull Throwable t) {
                        if (isFinishing() || isDestroyed() || esteePedido != pedidoAtual) return;
                        binding.txtCarregando.setVisibility(View.GONE);
                        Toast.makeText(AgendarVisitaActivity.this, "Sem conexão com o servidor",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** Monta as células: espaços vazios até o dia da semana do dia 1, depois os dias do mês. */
    private void montarGrade() {
        List<DiaCalendarioAdapter.Dia> celulas = new ArrayList<>();
        Calendar dia = (Calendar) mesNaTela.clone();
        int vazios = dia.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY; // grade começa no domingo
        for (int i = 0; i < vazios; i++) {
            celulas.add(new DiaCalendarioAdapter.Dia(0, "", false, false));
        }
        String hoje = iso(new Date());
        int total = dia.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int d = 1; d <= total; d++) {
            dia.set(Calendar.DAY_OF_MONTH, d);
            String data = iso(dia.getTime());
            celulas.add(new DiaCalendarioAdapter.Dia(d, data, horariosPorDia.containsKey(data), data.equals(hoje)));
        }
        adapterDias.mostrar(celulas, diaEscolhido);
    }

    private void escolherDia(String dataIso) {
        diaEscolhido = dataIso;
        horaEscolhida = null;
        adapterDias.selecionar(dataIso);
        mostrarHorarios();
    }

    // =========================
    // HORÁRIOS
    // =========================

    private void mostrarHorarios() {
        binding.grupoHorarios.removeAllViews();
        List<String> horarios = diaEscolhido == null ? null : horariosPorDia.get(diaEscolhido);

        if (horarios == null) {
            binding.txtTituloHorarios.setText("HORÁRIOS");
            binding.txtEscolhaDia.setText("Escolha um dia com bolinha coral no calendário.");
            binding.txtEscolhaDia.setVisibility(View.VISIBLE);
            atualizarBotaoConfirmar();
            return;
        }

        binding.txtTituloHorarios.setText("HORÁRIOS EM " + diaCurto(diaEscolhido).toUpperCase(PT_BR));
        binding.txtEscolhaDia.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (String hora : horarios) {
            Chip chip = (Chip) inflater.inflate(R.layout.item_horario, binding.grupoHorarios, false);
            chip.setId(View.generateViewId());
            chip.setText(hora);
            chip.setChecked(hora.equals(horaEscolhida));
            chip.setOnCheckedChangeListener((botao, marcado) -> {
                if (marcado) {
                    horaEscolhida = hora;
                } else if (hora.equals(horaEscolhida)) {
                    horaEscolhida = null;
                }
                atualizarBotaoConfirmar();
            });
            binding.grupoHorarios.addView(chip);
        }
        atualizarBotaoConfirmar();
    }

    private void atualizarBotaoConfirmar() {
        boolean pronto = diaEscolhido != null && horaEscolhida != null;
        binding.btnConfirmar.setEnabled(pronto);
        binding.btnConfirmar.setAlpha(pronto ? 1f : 0.45f);
        binding.btnConfirmar.setText(pronto
                ? "Solicitar visita · " + diaCurto(diaEscolhido) + ", " + horaEscolhida
                : "Solicitar visita");
    }

    // =========================
    // CONFIRMAR
    // =========================

    private void confirmar() {
        if (diaEscolhido == null || horaEscolhida == null) return;
        String dia = diaEscolhido;
        String hora = horaEscolhida;

        binding.btnConfirmar.setEnabled(false);
        binding.btnConfirmar.setText("Enviando...");

        ApiClient.get().agendarVisita(idArtista, new VisitaRequest(idUsuario, dia + "T" + hora))
                .enqueue(new Callback<VisitaResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<VisitaResponse> call,
                                           @NonNull Response<VisitaResponse> resposta) {
                        if (isFinishing() || isDestroyed()) return;
                        if (resposta.isSuccessful()) {
                            String texto = diaPorExtenso(dia) + " às " + hora;
                            Toast.makeText(AgendarVisitaActivity.this,
                                    "Pedido enviado! O artista vai confirmar.", Toast.LENGTH_LONG).show();
                            setResult(RESULT_OK, new Intent().putExtra(EXTRA_RESULTADO, texto));
                            finish();
                        } else {
                            Toast.makeText(AgendarVisitaActivity.this,
                                    ApiClient.mensagemDeErro(resposta, "Não foi possível agendar"),
                                    Toast.LENGTH_LONG).show();
                            // Horário pode ter sido reservado por outra pessoa: recarrega a agenda
                            horaEscolhida = null;
                            carregarMes();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<VisitaResponse> call, @NonNull Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        atualizarBotaoConfirmar();
                        Toast.makeText(AgendarVisitaActivity.this,
                                "Sem conexão com o servidor. A API está rodando?", Toast.LENGTH_LONG).show();
                    }
                });
    }

    // =========================
    // DATAS
    // =========================

    private static String iso(Date data) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(data);
    }

    private static Date deIso(String dataIso) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dataIso);
        } catch (ParseException e) {
            return new Date();
        }
    }

    /** "2026-11-12" -> "qui, 12 nov" */
    private static String diaCurto(String dataIso) {
        return new SimpleDateFormat("EEE, d MMM", PT_BR).format(deIso(dataIso)).replace(".", "");
    }

    /** "2026-11-12" -> "quinta-feira, 12 de novembro" */
    private static String diaPorExtenso(String dataIso) {
        return new SimpleDateFormat("EEEE, d 'de' MMMM", PT_BR).format(deIso(dataIso));
    }

    private static void zerarHora(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }
}
