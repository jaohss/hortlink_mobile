package com.example.hortlink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hortlink.R;
import com.example.hortlink.adapters.CategoriaAdapter;
import com.example.hortlink.adapters.ProdutoAdapter;
import com.example.hortlink.data.model.OfertaDTO;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ProdutoAdapter adapter;
    private List<OfertaDTO> todosProdutos    = new ArrayList<>();
    private List<OfertaDTO> produtosFiltrados = new ArrayList<>();

    private RecyclerView recyclerProdutos;
    private View progressBar; // seu loading view no XML

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerProdutos = view.findViewById(R.id.recyclerProdutos);
        recyclerProdutos.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Adapter começa vazio
        adapter = new ProdutoAdapter(produtosFiltrados, produto -> {
            Intent intent = new Intent(getContext(), DetalheProdutoActivity.class);
            intent.putExtra("imagem_url", produto.imagemUri);
            intent.putExtra("produto_id", produto.id);
            startActivity(intent);
        });
        recyclerProdutos.setAdapter(adapter);

        configurarCategorias(view);
        //carregarProdutos();
    }

    // ─── Busca produtos na API Spring Boot via Retrofit ──────────────────────────────
//    private void carregarProdutos() {
//        setCarregando(true);
//
//        OfertaRepository ofertaRepository = new OfertaRepository();
//
//        // Chamando o novo método que usa o Retrofit
//        ofertaRepository.listarOfertas(new OfertaRepository.OnlineCallback() {
//
//            @Override
//            public void onSuccess(List<OfertaDTO> produtos) {
//                // Proteção caso o usuário feche a tela antes da internet responder
//                if (!isAdded() || getActivity() == null) return;
//
//                setCarregando(false);
//
//                // Limpa as listas antigas e adiciona os produtos que vieram da API
//                todosProdutos.clear();
//                todosProdutos.addAll(produtos);
//
//                produtosFiltrados.clear();
//                produtosFiltrados.addAll(produtos);
//
//                // Avisa o visual que a lista mudou
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onError(String erro) {
//                if (!isAdded() || getActivity() == null) return;
//
//                setCarregando(false);
//                Toast.makeText(getContext(),
//                        "Erro ao carregar ofertas: " + erro, Toast.LENGTH_LONG).show();
//
//                android.util.Log.e("HortiLink_API", "Motivo da falha: " + erro);
//            }
//        });
//    }

    // ─── Filtro por categoria ────────────────────────────────────
    private void configurarCategorias(View view) {
        List<String> categorias = new ArrayList<>();
        categorias.add("Todos");
        categorias.add("Frutas");
        categorias.add("Verduras");
        categorias.add("Legumes");

        RecyclerView recyclerCategorias = view.findViewById(R.id.recyclerCategorias);
        recyclerCategorias.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        CategoriaAdapter adapterCat = new CategoriaAdapter(categorias, categoriaSelecionada -> {
            produtosFiltrados.clear();

            if (categoriaSelecionada.equals("Todos")) {
                produtosFiltrados.addAll(todosProdutos);
            } else {
                String filtro = categoriaSelecionada.toLowerCase().replaceAll("s$", "");
                for (OfertaDTO p : todosProdutos) {
                    if (p.categoria != null &&
                            p.categoria.toLowerCase().startsWith(filtro)) {
                        produtosFiltrados.add(p);
                    }
                }
            }

            adapter.notifyDataSetChanged();
        });

        recyclerCategorias.setAdapter(adapterCat);
    }

    private void setCarregando(boolean carregando) {
        // if (progressBar != null)
        //     progressBar.setVisibility(carregando ? View.VISIBLE : View.GONE);
        recyclerProdutos.setVisibility(carregando ? View.GONE : View.VISIBLE);
    }
}