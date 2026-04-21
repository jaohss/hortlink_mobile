package com.example.hortlink.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hortlink.R;
import com.example.hortlink.adapters.CategoriaAdapter;
import com.example.hortlink.adapters.ProdutoAdapter;
import com.example.hortlink.entidades.Produto;
import com.example.hortlink.entidades.Produtor;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerProdutos = view.findViewById(R.id.recyclerProdutos);

        //layout em grid(2 colunas)
        recyclerProdutos.setLayoutManager(new GridLayoutManager(getContext(), 2));


        //Criando dados mockados
        Produtor p1 = new Produtor("Fazenda Ipanema", "Sorocaba", "(15)99999-0000",R.drawable.fazenda, 4.8,R.drawable.fotocapa);

        List<Produto> produtos = new ArrayList<>();
        produtos.add(new Produto("Tomate", 5.99, "Legumes", R.drawable.hortlink_logo, "Fruta fresca e deliciosa",p1));
        produtos.add(new Produto("Alface", 3.58, "Verduras", R.drawable.hortlink_logo, "Fruta fresca e deliciosa",p1));
        produtos.add(new Produto("Banana", 4.26, "Frutas", R.drawable.hortlink_logo, "Fruta fresca e deliciosa",p1));
        produtos.add(new Produto("Melancia", 5.99, "Frutas", R.drawable.hortlink_logo, "Fruta fresca e deliciosa",p1));
        produtos.add(new Produto("Couve", 7.89, "Verduras", R.drawable.hortlink_logo, "Fruta fresca e deliciosa",p1));
        produtos.add(new Produto("Batata", 1.87, "Legumes", R.drawable.hortlink_logo, "Fruta fresca e deliciosa",p1));


        //adapter
        List<Produto> produtosFiltrados = new ArrayList<>(produtos);
        ProdutoAdapter adapter = new ProdutoAdapter(produtosFiltrados, produto ->{
            Intent intent = new Intent(getContext(), DetalheProdutoActivity.class);
            intent.putExtra("nome", produto.nome);
            intent.putExtra("preco", produto.preco);
            intent.putExtra("imagem",produto.imagem);
            intent.putExtra("descricao",produto.descricao);
            intent.putExtra("produtor_nome", produto.produtor.nome);
            intent.putExtra("produtor_contato", produto.produtor.contato);
            intent.putExtra("produtor_cidade", produto.produtor.cidade);
            intent.putExtra("produtor_foto", produto.produtor.fotoPerfil);
            intent.putExtra("produtor_avaliacao", produto.produtor.avaliacao);
            intent.putExtra("produtor_fotoC", produto.produtor.fotoCapa);



            startActivity(intent);
        });
        recyclerProdutos.setAdapter(adapter);


        //Lista de categorias
        List<String> categorias = new ArrayList<>();
        categorias.add("Frutas");
        categorias.add("Verduras");
        categorias.add("Legumes");
        categorias.add(0, "Todos");

        RecyclerView recyclerCategorias = view.findViewById(R.id.recyclerCategorias);

        recyclerCategorias.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
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