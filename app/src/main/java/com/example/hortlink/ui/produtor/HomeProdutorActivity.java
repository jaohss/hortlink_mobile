package com.example.hortlink.ui.produtor;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.hortlink.R;
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

        // Fragment inicial
        carregarFragment(new GerenciarProdutosFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_prod_produtos) {
                carregarFragment(new GerenciarProdutosFragment());

            } else if (id == R.id.nav_prod_pedidos) {
                carregarFragment(new PedidosProdutorFragment());

            } else if (id == R.id.nav_prod_adicionar) {
                startActivity(new Intent(this, AdicionarProdutosActivity.class));
                // Mantém o item selecionado atual visualmente
                bottomNav.setSelectedItemId(R.id.nav_prod_produtos);
                return true;

            } else if (id == R.id.nav_prod_perfil) {
                carregarFragment(new PerfilFragment());
            }

            return true;
        });

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