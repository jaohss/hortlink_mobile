package com.example.hortlink.ui.produtor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hortlink.R;
import com.example.hortlink.data.dto.ProdutoListaDTO;
import com.example.hortlink.data.model.Produto;
import com.example.hortlink.data.repository.ProdutoRepository;
import com.example.hortlink.service.BaseCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class GerenciarProdutosActivity extends AppCompatActivity {

    private RecyclerView recyclerProdutos;
    // private ProdutoAdapter adapter; // Você precisará criar esse Adapter depois
    private ProgressBar progressBar;
    private View layoutVazio;

    private final ProdutoRepository repository = new ProdutoRepository();
    private final List<ProdutoListaDTO> listaProdutos = new ArrayList<>();

    // ─── O "Ouvinte" que espera a tela de Adicionar fechar ───
    private final ActivityResultLauncher<Intent> formProdutoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Se o usuário salvou um produto com sucesso, recarregamos a lista!
                    carregarProdutos();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerenciar_produtos);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Meu Catálogo");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerProdutos = findViewById(R.id.recyclerProdutos);
        progressBar      = findViewById(R.id.progressBar);
        layoutVazio      = findViewById(R.id.layoutVazio);

        FloatingActionButton fabAdicionar = findViewById(R.id.fabAdicionarProduto);

        // ─── Chama a sua tela de AdicionarProduto ───
        fabAdicionar.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdicionarProdutosActivity.class);
            formProdutoLauncher.launch(intent);
        });

        configurarRecyclerView();
        carregarProdutos();
    }

    private void configurarRecyclerView() {
        recyclerProdutos.setLayoutManager(new LinearLayoutManager(this));
        // adapter = new ProdutoAdapter(listaProdutos);
        // recyclerProdutos.setAdapter(adapter);
    }

    private void carregarProdutos() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerProdutos.setVisibility(View.GONE);

        repository.listarMeusProdutos(new BaseCallback<List<ProdutoListaDTO>>() {
            @Override
            public void onSuccess(List<ProdutoListaDTO> produtos) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    listaProdutos.clear();

                    if (produtos != null && !produtos.isEmpty()) {
                        listaProdutos.addAll(produtos);
                        // adapter.notifyDataSetChanged();
                        recyclerProdutos.setVisibility(View.VISIBLE);
                        layoutVazio.setVisibility(View.GONE);
                    } else {
                        layoutVazio.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onError(String erro) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(GerenciarProdutosActivity.this, "Erro ao carregar catálogo", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}