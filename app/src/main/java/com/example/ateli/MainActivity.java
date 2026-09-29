package com.example.ateli;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        TextView btnLocalizacao = findViewById(R.id.btnLocalizacao);
        TextView btnFiltros = findViewById(R.id.btnFiltros);
        EditText campoBusca = findViewById(R.id.campoBusca);

        btnLocalizacao.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(this, btnLocalizacao);
            menu.getMenu().add("Usar localização atual");
            menu.getMenu().add("Escolher outra localização");

            menu.setOnMenuItemClickListener(item -> {
                btnLocalizacao.setText(item.getTitle() + "  ▾");
                return true;
            });

            menu.show();
        });

        btnFiltros.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(this, btnFiltros);
            menu.getMenu().add("Todas as categorias");
            menu.getMenu().add("Pintura");
            menu.getMenu().add("Escultura");
            menu.getMenu().add("Fotografia");

            menu.setOnMenuItemClickListener(item -> {
                campoBusca.setHint("Pesquisar em: " + item.getTitle());
                return true;
            });

            menu.show();
        });
        findViewById(R.id.btnExplorar).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ExplorarActivity.class));
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById(R.id.btnVerTudo).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ExplorarActivity.class));
        });
    }
}