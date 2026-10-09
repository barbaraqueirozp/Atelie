package com.example.ateli;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ateli.databinding.ItemObraBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter do RecyclerView de obras, usando o padrão ViewHolder
 * com View Binding (sem findViewById).
 */
public class ObraAdapter extends RecyclerView.Adapter<ObraAdapter.ObraViewHolder> {

    /** Callback de clique — implementado com lambda nas Activities. */
    public interface OnObraClickListener {
        void onObraClick(Obra obra);
    }

    private final List<Obra> obras = new ArrayList<>();
    private final OnObraClickListener listener;
    private final boolean larguraFixa;

    /**
     * @param larguraFixa true para listas horizontais (cards com 180dp de largura);
     *                    false para grades, onde o card ocupa a coluna inteira.
     */
    public ObraAdapter(List<Obra> obrasIniciais, boolean larguraFixa,
                       OnObraClickListener listener) {
        this.obras.addAll(obrasIniciais);
        this.larguraFixa = larguraFixa;
        this.listener = listener;
    }

    /** Troca a lista exibida (usado pelos filtros de categoria). */
    public void atualizarLista(List<Obra> novasObras) {
        obras.clear();
        obras.addAll(novasObras);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ObraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemObraBinding binding = ItemObraBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        if (larguraFixa) {
            ViewGroup.LayoutParams params = binding.getRoot().getLayoutParams();
            params.width = parent.getResources()
                    .getDimensionPixelSize(R.dimen.largura_card_obra);
            binding.getRoot().setLayoutParams(params);
        }
        return new ObraViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ObraViewHolder holder, int position) {
        holder.bind(obras.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return obras.size();
    }

    /** ViewHolder: guarda as referências das views de um item para reaproveitá-las. */
    static class ObraViewHolder extends RecyclerView.ViewHolder {

        private final ItemObraBinding binding;

        ObraViewHolder(ItemObraBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Obra obra, OnObraClickListener listener) {
            binding.txtTitulo.setText(obra.getTitulo());
            binding.txtArtista.setText(obra.getArtista());
            binding.txtPreco.setText(obra.getPrecoFormatado());

            binding.imgObra.setBackgroundResource(obra.getFundoRes());
            if (obra.getImagemRes() != 0) {
                binding.imgObra.setImageResource(obra.getImagemRes());
            } else {
                // limpa a imagem reciclada de outro item
                binding.imgObra.setImageDrawable(null);
            }
            binding.imgObra.setContentDescription("Obra " + obra.getTitulo());

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onObraClick(obra);
                }
            });
        }
    }
}
