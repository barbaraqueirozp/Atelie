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
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;

public class MainActivity extends AppCompatActivity {
    private final ActivityResultLauncher<String> pedirLocalizacao =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    permitida -> {
                        if (permitida) {
                            obterLocalizacaoAtual();
                        } else {
                            Toast.makeText(this,
                                    "Permissão de localização não concedida",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

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
                if (item.getTitle().toString().equals("Usar localização atual")) {
                    if (ContextCompat.checkSelfPermission(
                            this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        obterLocalizacaoAtual();
                    } else {
                        pedirLocalizacao.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
                    }
                } else {
                    Toast.makeText(this,
                            "A escolha manual da cidade será adicionada em seguida",
                            Toast.LENGTH_SHORT).show();
                }
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
    private void obterLocalizacaoAtual() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationManager gerenciador =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (gerenciador == null ||
                !gerenciador.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(this,
                    "Ative a localização do celular e tente novamente",
                    Toast.LENGTH_LONG).show();
            return;
        }

        TextView btnLocalizacao = findViewById(R.id.btnLocalizacao);
        btnLocalizacao.setText("Obtendo localização...");

        LocationManagerCompat.getCurrentLocation(
                gerenciador,
                LocationManager.NETWORK_PROVIDER,
                new android.os.CancellationSignal(),
                ContextCompat.getMainExecutor(this),
                local -> {
                    if (local == null) {
                        btnLocalizacao.setText("⌖  Artistas próximos  ▾");
                        Toast.makeText(this,
                                "Não foi possível obter a localização",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    btnLocalizacao.setText("⌖  Minha localização  ▾");

                    // Coordenadas disponíveis para a futura busca de artistas.
                    double latitude = local.getLatitude();
                    double longitude = local.getLongitude();

                    Toast.makeText(this,
                            "Localização obtida: " + latitude + ", " + longitude,
                            Toast.LENGTH_LONG).show();
                });
    }
}