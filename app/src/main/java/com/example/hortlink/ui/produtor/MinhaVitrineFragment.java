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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hortlink.R;
import com.example.hortlink.adapters.OfertaVitrineAdapter;
import com.example.hortlink.data.dto.ProdutoListaDTO;
import com.example.hortlink.data.model.OfertaDTO;
import com.example.hortlink.data.repository.OfertaRepository;
import com.example.hortlink.data.repository.ProdutoRepository;
import com.example.hortlink.service.BaseCallback;
import com.example.hortlink.util.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class MinhaVitrineFragment extends Fragment {

    private RecyclerView recyclerVitrine;
    private ProgressBar progressBar;
    private View layoutVazio;
    private OfertaVitrineAdapter adapter;

    private final List<OfertaDTO> listaOfertas = new ArrayList<>();
    private final OfertaRepository ofertaRepository = new OfertaRepository();
    // Adicionamos o repositório de produtos para fazer a verificação
    private final ProdutoRepository produtoRepository = new ProdutoRepository();

    private final ActivityResultLauncher<Intent> formOfertaLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    carregarVitrine();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_minha_vitrine, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerVitrine = view.findViewById(R.id.recyclerVitrine);
        progressBar = view.findViewById(R.id.progressBarVitrine);
        layoutVazio = view.findViewById(R.id.layoutVazio);

        MaterialButton btnAdicionarVazio = view.findViewById(R.id.btnAdicionarVazio);
        btnAdicionarVazio.setOnClickListener(v -> abrirFormularioOferta());

        configurarRecyclerView();
        carregarVitrine();
    }

    private void configurarRecyclerView() {
        recyclerVitrine.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        adapter = new OfertaVitrineAdapter(listaOfertas, new OfertaVitrineAdapter.OnOfertaActionListener() {
            @Override
            public void onAddOfertaClick() {
                abrirFormularioOferta();
            }
        });

        recyclerVitrine.setAdapter(adapter);
    }

    // --- NOVA LÓGICA DE VALIDAÇÃO AQUI ---
    private void abrirFormularioOferta() {
        // Mostra o spinner na tela para o usuário saber que o app está pensando
        setCarregando(true);

        produtoRepository.listarMeusProdutos(new BaseCallback<List<ProdutoListaDTO>>() {
            @Override
            public void onSuccess(List<ProdutoListaDTO> produtos) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    // Esconde o spinner e restaura a tela ao que era antes
                    setCarregando(false);

                    if (produtos != null && !produtos.isEmpty()) {
                        // SINAL VERDE: O produtor tem produtos! Pode abrir a tela.
                        Intent intent = new Intent(getContext(), FormularioOferta.class);
                        formOfertaLauncher.launch(intent);
                    } else {
                        // BLOQUEADO: Produtor sem produtos.
                        Toast.makeText(getContext(), "Para criar uma oferta, você precisa cadastrar pelo menos um produto no Catálogo primeiro!", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(String erro) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    setCarregando(false);
                    Toast.makeText(getContext(), "Erro ao verificar catálogo. Tente novamente.", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void carregarVitrine() {
        Long comercioId = SessionManager.getInstance().getComercioProfileId();
        if (comercioId == -1L) return;

        setCarregando(true);

        ofertaRepository.buscarOfertasPorComercioId(comercioId, new BaseCallback<List<OfertaDTO>>() {
            @Override
            public void onSuccess(List<OfertaDTO> ofertas) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    listaOfertas.clear();
                    if (ofertas != null) {
                        listaOfertas.addAll(ofertas);
                    }

                    // Atualizamos o adapter antes do setCarregando(false) para a tela restaurar corretamente
                    adapter.notifyDataSetChanged();
                    setCarregando(false);
                });
            }

            @Override
            public void onError(String erro) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    setCarregando(false);
                    Toast.makeText(getContext(), "Erro ao carregar vitrine", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // --- MELHORIA NO MÉTODO HELPER ---
    // Agora o setCarregando(false) sabe exatamente o que deve voltar a ficar visível
    private void setCarregando(boolean carregando) {
        if (carregando) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerVitrine.setVisibility(View.GONE);
            layoutVazio.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            // Restaura a tela com base em ter ou não ofertas!
            if (listaOfertas.isEmpty()) {
                layoutVazio.setVisibility(View.VISIBLE);
            } else {
                recyclerVitrine.setVisibility(View.VISIBLE);
            }
        }
    }
}