package com.example.hortlink.ui.produtor;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.hortlink.R;
import com.example.hortlink.ui.consumidor.HomeFragment;
import com.example.hortlink.util.BotpressActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class HomeProdutorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_produtor);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // Fragment inicial (Agora é a Vitrine de Ofertas)
        carregarFragment(new MinhaVitrineFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_prod_vitrine) {
                carregarFragment(new MinhaVitrineFragment());

            } else if (id == R.id.nav_prod_catalogo) {
                carregarFragment(new CatalogoFragment());

            } else if (id == R.id.nav_prod_pedidos) {
                carregarFragment(new PedidosProdutorFragment());

            } else if (id == R.id.nav_prod_mercado) {
                // A mágica acontece aqui: carrega a visão do consumidor!
                carregarFragment(new HomeFragment());

            } else if (id == R.id.nav_prod_perfil) {
                carregarFragment(new PerfilFragment());
            }

            return true;
        });

        // Suporte via Botpress mantido
        findViewById(R.id.fabSuporte).setOnClickListener(v ->
                startActivity(new Intent(this, BotpressActivity.class)));
    }

    private void carregarFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}