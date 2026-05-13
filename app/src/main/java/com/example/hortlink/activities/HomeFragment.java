package com.example.hortlink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hortlink.R;
import com.example.hortlink.adapters.CategoriaAdapter;
import com.example.hortlink.adapters.ProdutoAdapter;
import com.example.hortlink.bd.SupabaseHelper;
import com.example.hortlink.entidades.Produto;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ProdutoAdapter adapter;
    private List<Produto> todosProdutos    = new ArrayList<>();
    private List<Produto> produtosFiltrados = new ArrayList<>();

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
        // progressBar = view.findViewById(R.id.progressBar); // descomente se tiver

        // Adapter começa vazio — dados chegam do Supabase
        adapter = new ProdutoAdapter(produtosFiltrados, produto -> {
            Intent intent = new Intent(getContext(), DetalheProdutoActivity.class);
            intent.putExtra("produto_id", produto.id);
            startActivity(intent);
        });
        recyclerProdutos.setAdapter(adapter);

        configurarCategorias(view);
        carregarProdutos();
    }

    // ─── Busca produtos no Supabase ──────────────────────────────
    private void carregarProdutos() {
        setCarregando(true);

        SupabaseHelper supabase = new SupabaseHelper(requireContext());
        supabase.listarProdutos(new SupabaseHelper.SupabaseCallback() {

            @Override
            public void onSuccess(String json) {
                if (!isAdded() || getActivity() == null) return; // ← proteção

                List<Produto> lista = parseProdutos(json);

                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) return; // ← proteção extra dentro do runOnUiThread
                    setCarregando(false);
                    todosProdutos.clear();
                    todosProdutos.addAll(lista);
                    produtosFiltrados.clear();
                    produtosFiltrados.addAll(lista);
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(String erro) {
                if (!isAdded() || getActivity() == null) return; // ← proteção

                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) return; // ← proteção extra
                    setCarregando(false);
                    Toast.makeText(getContext(),
                            "Erro ao carregar produtos: " + erro, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    // ─── Converte JSON do Supabase → List<Produto> ───────────────
    private List<Produto> parseProdutos(String json) {
        List<Produto> lista = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                Produto p = new Produto(
                        obj.optString("id"),
                        obj.optString("nome"),
                        obj.optDouble("preco", 0.0),
                        obj.optString("categoria"),
                        obj.optString("foto_url"),   // ← vira imagemUri
                        obj.optString("descricao"),
                        obj.optString("unidade")
                );

                lista.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

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
                // Normaliza para comparação (ex: "Frutas" bate com "Fruta" do banco)
                String filtro = categoriaSelecionada.toLowerCase().replaceAll("s$", "");
                for (Produto p : todosProdutos) {
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