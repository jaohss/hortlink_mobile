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
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class CatalogoFragment extends Fragment implements ProdutoAdapter.OnProdutoActionListener {

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
        return inflater.inflate(R.layout.fragment_catalogo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerProdutos = view.findViewById(R.id.recyclerProdutos);
        progressBar = view.findViewById(R.id.progressBar);
        layoutVazio = view.findViewById(R.id.layoutVazio);

        // Bind do novo botão do Estado Vazio (Estilo Pedidos)
        MaterialButton btnAdicionarVazio = view.findViewById(R.id.btnAdicionarVazio);
        btnAdicionarVazio.setOnClickListener(v -> onAddProdutoClick());

        configurarRecyclerView();
        carregarProdutos(); // Chama o carregamento
    }

    private void configurarRecyclerView() {
        recyclerProdutos.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProdutoAdapter(listaProdutos, this);
        recyclerProdutos.setAdapter(adapter);
    }

    // --- MÉTODOS DA INTERFACE ---

    @Override
    public void onAddProdutoClick() {
        Intent intent = new Intent(getContext(), AdicionarProdutosActivity.class);
        formProdutoLauncher.launch(intent);
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
        // 1. Estado inicial: Carregando (Esconde tudo, mostra spinner)
        setCarregando(true);

        repository.listarMeusProdutos(new BaseCallback<List<ProdutoListaDTO>>() {
            @Override
            public void onSuccess(List<ProdutoListaDTO> produtos) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    // 2. Parar de carregar
                    setCarregando(false);
                    listaProdutos.clear();

                    if (produtos != null && !produtos.isEmpty()) {
                        // 3a. COM PRODUTOS: Mostra lista, esconde EmptyState
                        listaProdutos.addAll(produtos);
                        layoutVazio.setVisibility(View.GONE);
                        recyclerProdutos.setVisibility(View.VISIBLE);
                    } else {
                        // 3b. SEM PRODUTOS: Esconde lista, mostra EmptyState (Estilo Pedidos + Botão)
                        recyclerProdutos.setVisibility(View.GONE);
                        layoutVazio.setVisibility(View.VISIBLE);
                    }
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(String erro) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    setCarregando(false);
                    Toast.makeText(getContext(), erro, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // Método Helper (Padronizado igual a PedidosProdutorFragment)
    private void setCarregando(boolean carregando) {
        if (carregando) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerProdutos.setVisibility(View.GONE);
            layoutVazio.setVisibility(View.GONE); // Garante que a tela de erro também suma
        } else {
            progressBar.setVisibility(View.GONE);
            // Visibilidade da lista ou layout vazio é decidida no onSuccess
        }
    }
}