package com.example.ateli;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ateli.databinding.ItemDiaCalendarioBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Dias do calendário de visitas (grade de 7 colunas).
 * - Disponível: número escuro + bolinha coral, pode tocar.
 * - Indisponível: número cinza e riscado, não pode tocar.
 * - Selecionado: círculo escuro com número branco.
 */
public class DiaCalendarioAdapter extends RecyclerView.Adapter<DiaCalendarioAdapter.DiaViewHolder> {

    /** Uma célula da grade. dia = 0 é um espaço vazio antes do dia 1. */
    public static class Dia {
        final int dia;
        final String dataIso;     // "2026-11-10"
        final boolean disponivel;
        final boolean hoje;

        public Dia(int dia, String dataIso, boolean disponivel, boolean hoje) {
            this.dia = dia;
            this.dataIso = dataIso;
            this.disponivel = disponivel;
            this.hoje = hoje;
        }
    }

    public interface OnDiaClickListener {
        void onDiaClick(String dataIso);
    }

    private final List<Dia> dias = new ArrayList<>();
    private final OnDiaClickListener listener;
    private String selecionado;

    public DiaCalendarioAdapter(OnDiaClickListener listener) {
        this.listener = listener;
    }

    public void mostrar(List<Dia> novosDias, String diaSelecionado) {
        dias.clear();
        dias.addAll(novosDias);
        selecionado = diaSelecionado;
        notifyDataSetChanged();
    }

    public void selecionar(String dataIso) {
        selecionado = dataIso;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DiaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DiaViewHolder(ItemDiaCalendarioBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DiaViewHolder holder, int position) {
        Dia dia = dias.get(position);
        ItemDiaCalendarioBinding b = holder.binding;

        if (dia.dia == 0) { // espaço vazio
            b.getRoot().setVisibility(View.INVISIBLE);
            b.getRoot().setOnClickListener(null);
            return;
        }
        b.getRoot().setVisibility(View.VISIBLE);
        b.txtDia.setText(String.valueOf(dia.dia));

        boolean escolhido = dia.dataIso.equals(selecionado);
        int corTexto;
        if (escolhido) {
            corTexto = R.color.branco;
            b.txtDia.setBackgroundResource(R.drawable.bg_dia_selecionado);
        } else {
            corTexto = dia.disponivel ? R.color.texto_escuro : R.color.icone_inativo;
            b.txtDia.setBackgroundResource(dia.hoje ? R.drawable.bg_dia_hoje : 0);
        }
        b.txtDia.setTextColor(ContextCompat.getColor(b.getRoot().getContext(), corTexto));
        b.txtDia.setTypeface(null, dia.disponivel ? android.graphics.Typeface.BOLD
                : android.graphics.Typeface.NORMAL);

        // Indisponível: número riscado
        int flags = b.txtDia.getPaintFlags();
        b.txtDia.setPaintFlags(dia.disponivel
                ? flags & ~Paint.STRIKE_THRU_TEXT_FLAG
                : flags | Paint.STRIKE_THRU_TEXT_FLAG);

        b.pontoDisponivel.setVisibility(dia.disponivel && !escolhido ? View.VISIBLE : View.INVISIBLE);

        b.getRoot().setContentDescription(dia.dia + (dia.disponivel ? ", horários livres" : ", indisponível"));
        if (dia.disponivel) {
            b.getRoot().setOnClickListener(v -> listener.onDiaClick(dia.dataIso));
        } else {
            b.getRoot().setOnClickListener(null);
            b.getRoot().setClickable(false);
        }
    }

    @Override
    public int getItemCount() {
        return dias.size();
    }

    static class DiaViewHolder extends RecyclerView.ViewHolder {
        final ItemDiaCalendarioBinding binding;

        DiaViewHolder(ItemDiaCalendarioBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
