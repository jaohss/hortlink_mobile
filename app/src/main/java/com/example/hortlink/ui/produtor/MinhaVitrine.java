package com.example.hortlink.ui.produtor;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hortlink.R;
import com.example.hortlink.adapters.OfertaVitrineAdapter;
import com.example.hortlink.data.model.OfertaDTO;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MinhaVitrine extends AppCompatActivity {

    private RecyclerView recyclerVitrine;
    private ProgressBar progressBar;
    private OfertaVitrineAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minha_vitrine);

        // Remove a Action Bar padrão porque já fizemos o cabeçalho verde "Minha Banca"
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        recyclerVitrine = findViewById(R.id.recyclerVitrine);
        progressBar = findViewById(R.id.progressBarVitrine);
        ExtendedFloatingActionButton fabNovaOferta = findViewById(R.id.fabNovaOferta);

        configurarRecyclerView();

        fabNovaOferta.setOnClickListener(v -> {
            Toast.makeText(this, "Em breve: Selecionar produto do catálogo para ofertar", Toast.LENGTH_SHORT).show();
            // Aqui você vai abrir a tela de criar oferta
        });
    }

    private void configurarRecyclerView() {
        // A mágica acontece aqui: Cria uma grade com 2 colunas!
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerVitrine.setLayoutManager(gridLayoutManager);

        // adapter = new OfertaVitrineAdapter(listaDeOfertas);
        // recyclerVitrine.setAdapter(adapter);
    }

}