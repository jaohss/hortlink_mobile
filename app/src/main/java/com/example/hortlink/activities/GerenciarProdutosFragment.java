package com.example.hortlink.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hortlink.R;
import com.example.hortlink.adapters.GerenciarAdapter;
import com.example.hortlink.bd.SupabaseHelper;
import com.example.hortlink.data.model.Produto;
import com.example.hortlink.data.repository.ProdutoRepository;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GerenciarProdutosFragment extends Fragment {

    private RecyclerView recyclerGerenciar;
    private TextView txtListaVazia;
    private List<Produto> listaProdutos = new ArrayList<>();
    private GerenciarAdapter adapter;
    //private SupabaseHelper supabase;
    private ProdutoRepository produtoRepository = new ProdutoRepository();

    public GerenciarProdutosFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gerenciar_produtos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //supabase = new SupabaseHelper(requireContext());
        recyclerGerenciar = view.findViewById(R.id.recyclerGerenciar);
        txtListaVazia     = view.findViewById(R.id.txtListaVazia);

        recyclerGerenciar.setLayoutManager(new LinearLayoutManager(getContext()));
        configurarAdapter();
        carregarProdutos();
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarProdutos(); // recarrega ao voltar do EditarProdutosFragment
    }

    // ─── Configura adapter com as ações de editar e deletar ──────
    private void configurarAdapter() {
        adapter = new GerenciarAdapter(
                listaProdutos,
                produto -> {
                    EditarProdutosFragment fragment =
                            EditarProdutosFragment.newInstance(produto.id);
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.container, fragment)
                            .addToBackStack(null)
                            .commit();
                }
        );
        recyclerGerenciar.setAdapter(adapter);
    }

    // ─── Busca só os produtos do produtor logado ─────────────────
    private void carregarProdutos() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (uid == null) {
            Toast.makeText(getContext(), "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        produtoRepository.listarProdutosPorProdutor(uid, new ProdutoRepository.Callback()  {
            @Override
            public void onSuccess(String json) {
                List<Produto> lista = parseProdutos(json);

                requireActivity().runOnUiThread(() -> {
                    listaProdutos.clear();
                    listaProdutos.addAll(lista);
                    adapter.notifyDataSetChanged();

                    boolean vazia = listaProdutos.isEmpty();
                    txtListaVazia.setVisibility(vazia ? View.VISIBLE : View.GONE);
                    recyclerGerenciar.setVisibility(vazia ? View.GONE : View.VISIBLE);
                });
            }

            @Override
            public void onError(String erro) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(),
                                "Erro ao carregar: " + erro, Toast.LENGTH_LONG).show());
            }
        });
    }


    // ─── Parse JSON → List<Produto> ──────────────────────────────
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
                        obj.optString("foto_url"),
                        obj.optString("descricao"),
                        obj.optString("unidade")
                );
                p.status = obj.optBoolean("status", true);
                lista.add(p);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }
}