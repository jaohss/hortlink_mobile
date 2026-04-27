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

import com.example.hortlink.BancoHelper;
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
        recyclerProdutos.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // ✅ Busca do banco em vez de dados mockados
        BancoHelper db = new BancoHelper(getContext());
        List<Produto> produtos = db.listarProdutosComoObjetos();
        List<Produto> produtosFiltrados = new ArrayList<>(produtos);

        ProdutoAdapter adapter = new ProdutoAdapter(produtosFiltrados, produto -> {
            Intent intent = new Intent(getContext(), DetalheProdutoActivity.class);
            intent.putExtra("nome", produto.nome);
            intent.putExtra("preco", produto.preco);
            intent.putExtra("imagem_uri", produto.imagemUri); // ✅ manda URI
            intent.putExtra("descricao", produto.descricao);
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