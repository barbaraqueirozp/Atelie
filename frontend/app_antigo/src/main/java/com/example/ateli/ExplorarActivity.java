package com.example.ateli;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ExplorarActivity extends AppCompatActivity {

    private TextView filtroEmAlta;
    private TextView filtroPintura;
    private TextView filtroEscultura;
    private TextView filtroFotografia;
    private TextView mensagemCategoria;
    private View listaPinturas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_explorar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top,
                    systemBars.right, systemBars.bottom);
            return insets;
        });

        filtroEmAlta = findViewById(R.id.filtroEmAlta);
        filtroPintura = findViewById(R.id.filtroPintura);
        filtroEscultura = findViewById(R.id.filtroEscultura);
        filtroFotografia = findViewById(R.id.filtroFotografia);
        listaPinturas = findViewById(R.id.listaPinturas);
        mensagemCategoria = findViewById(R.id.mensagemCategoria);

        filtroEmAlta.setOnClickListener(v -> selecionarFiltro(filtroEmAlta, true));
        filtroPintura.setOnClickListener(v -> selecionarFiltro(filtroPintura, true));
        filtroEscultura.setOnClickListener(v -> selecionarFiltro(filtroEscultura, false));
        filtroFotografia.setOnClickListener(v -> selecionarFiltro(filtroFotografia, false));
    }

    private void selecionarFiltro(TextView selecionado, boolean mostrarPinturas) {
        TextView[] filtros = {
                filtroEmAlta, filtroPintura, filtroEscultura, filtroFotografia
        };

        for (TextView filtro : filtros) {
            filtro.setBackgroundResource(R.drawable.bg_filtro);
            filtro.setTextColor(Color.parseColor("#261B16"));
        }

        selecionado.setBackgroundResource(R.drawable.bg_filtro_ativo);
        selecionado.setTextColor(Color.WHITE);

        listaPinturas.setVisibility(mostrarPinturas ? View.VISIBLE : View.GONE);
        mensagemCategoria.setVisibility(mostrarPinturas ? View.GONE : View.VISIBLE);
    }
}