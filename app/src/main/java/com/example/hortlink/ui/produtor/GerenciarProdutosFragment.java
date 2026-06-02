package com.example.hortlink.ui.produtor;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hortlink.R;
import com.example.hortlink.adapters.ProdutoAdapter;
import com.example.hortlink.data.dto.ProdutoListaDTO;
import com.example.hortlink.data.repository.ProdutoRepository;
import com.example.hortlink.service.BaseCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class GerenciarProdutosFragment extends Fragment implements ProdutoAdapter.OnProdutoActionListener {

    private RecyclerView recyclerProdutos;
    private ProdutoAdapter adapter;
    private ProgressBar progressBar;
    private View layoutVazio;

    private final ProdutoRepository repository = new ProdutoRepository();
    private final List<ProdutoListaDTO> listaProdutos = new ArrayList<>();

    private final ActivityResultLauncher<Intent> formProdutoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    carregarProdutos();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_gerenciar_produtos, container, false);

        // Ajuste o ID do botão se necessário ou remova o FAB se ele já existir na Home
        FloatingActionButton fab = view.findViewById(R.id.fabAdicionarProduto);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AdicionarProdutosActivity.class);
            formProdutoLauncher.launch(intent);
        });

        recyclerProdutos = view.findViewById(R.id.recyclerProdutos);
        progressBar = view.findViewById(R.id.progressBar);
        layoutVazio = view.findViewById(R.id.layoutVazio);

        configurarRecyclerView();
        carregarProdutos();

        return view;
    }

    private void configurarRecyclerView() {
        recyclerProdutos.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProdutoAdapter(listaProdutos, this);
        recyclerProdutos.setAdapter(adapter);
    }

    @Override
    public void onEditarClick(ProdutoListaDTO produto) {
        Intent intent = new Intent(getContext(), AdicionarProdutosActivity.class);
        intent.putExtra("produto_id", produto.getId());
        formProdutoLauncher.launch(intent);
    }

    @Override
    public void onStatusClick(ProdutoListaDTO produto, int position) {
        Toast.makeText(getContext(), "Funcionalidade em breve!", Toast.LENGTH_SHORT).show();
    }

    private void carregarProdutos() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        repository.listarMeusProdutos(new BaseCallback<List<ProdutoListaDTO>>() {
            @Override
            public void onSuccess(List<ProdutoListaDTO> produtos) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    listaProdutos.clear();
                    if (produtos != null && !produtos.isEmpty()) {
                        listaProdutos.addAll(produtos);
                        adapter.notifyDataSetChanged();
                        layoutVazio.setVisibility(View.GONE);
                    } else {
                        layoutVazio.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onError(String erro) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), erro, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}