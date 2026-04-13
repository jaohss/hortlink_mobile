package com.example.hortlink;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

//Home do comprador
public class Homec extends AppCompatActivity {

    //Home do comprador
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homec);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerProdutos = findViewById(R.id.recyclerProdutos);

        //layout em grid(2 colunas)
        recyclerProdutos.setLayoutManager(new GridLayoutManager(this, 2));


        //Criando dados mockados
        List<Produto> produtos = new ArrayList<>();
        produtos.add(new Produto("Tomate", 5.99, "Legumes", R.drawable.hortlink_logo, "Fruta fresca e deliciosa"));
        produtos.add(new Produto("Alface", 3.58, "Verduras", R.drawable.hortlink_logo, "Fruta fresca e deliciosa"));
        produtos.add(new Produto("Banana", 4.26, "Frutas", R.drawable.hortlink_logo, "Fruta fresca e deliciosa"));
        produtos.add(new Produto("Melancia", 5.99, "Frutas", R.drawable.hortlink_logo, "Fruta fresca e deliciosa"));
        produtos.add(new Produto("Couve", 7.89, "Verduras", R.drawable.hortlink_logo, "Fruta fresca e deliciosa"));
        produtos.add(new Produto("Batata", 1.87, "Legumes", R.drawable.hortlink_logo, "Fruta fresca e deliciosa"));

        //adapter
        List<Produto> produtosFiltrados = new ArrayList<>(produtos);
        ProdutoAdapter adapter = new ProdutoAdapter(produtosFiltrados,produto ->{
            Intent intent = new Intent(Homec.this, DetalheProdutoActivity.class);
            intent.putExtra("nome", produto.nome);
            intent.putExtra("preco", produto.preco);
            intent.putExtra("imagem",produto.imagem);
            intent.putExtra("descricao",produto.descricao);

            startActivity(intent);
        });
        recyclerProdutos.setAdapter(adapter);


        //Lista de categorias
        List<String> categorias = new ArrayList<>();
        categorias.add("Frutas");
        categorias.add("Verduras");
        categorias.add("Legumes");
        categorias.add(0, "Todos");

        RecyclerView recyclerCategorias = findViewById(R.id.recyclerCategorias);

        recyclerCategorias.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        CategoriaAdapter adapterCat = new CategoriaAdapter(categorias, categoriaSelecionada -> {

            produtosFiltrados.clear();

            if (categoriaSelecionada.equals("Todos")) {
                produtosFiltrados.clear();
                produtosFiltrados.addAll(produtos);
            } else {
                produtosFiltrados.clear();
                for (Produto p : produtos) {
                    if (p.categoria.equals(categoriaSelecionada)) {
                        produtosFiltrados.add(p);
                    }
                }
            }

            adapter.notifyDataSetChanged();

        });
        recyclerCategorias.setAdapter(adapterCat);


    }
}