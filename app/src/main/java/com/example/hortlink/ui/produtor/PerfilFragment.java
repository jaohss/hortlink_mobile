package com.example.hortlink.ui.produtor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.hortlink.R;
import com.example.hortlink.data.dto.ComercioDTO;
import com.example.hortlink.data.repository.ComercioRepository;
import com.example.hortlink.service.BaseCallback;
import com.example.hortlink.ui.auth.CompletarPerfilProdutorActivity;
import com.example.hortlink.ui.auth.MainActivity;
import com.example.hortlink.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PerfilFragment extends Fragment {

    private TextView txtNome, txtAvaliacao, txtCidade;
    private ImageView imgFazenda;

    private final ComercioRepository comercioRepository  = new ComercioRepository();

    public PerfilFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtNome      = view.findViewById(R.id.txtNomeProd);
        txtCidade    = view.findViewById(R.id.txtCidadeProd);
        txtAvaliacao = view.findViewById(R.id.txtAvaliacao);
        imgFazenda   = view.findViewById(R.id.imgFazenda);

        // ── Seção: Conta (Mantida) ──────────────────────────────
        view.findViewById(R.id.btnEditarPerfil).setOnClickListener(v -> {
            Long usuarioId = SessionManager.getInstance().getUsuarioId();
            Intent intent = new Intent(getActivity(), CompletarPerfilProdutorActivity.class);
            intent.putExtra("usuario_id", usuarioId);
            intent.putExtra("modo_edicao", true);
            startActivity(intent);
        });

        // ── Seção: Utilitários (Mantida) ────────────────────────
        view.findViewById(R.id.btnDashboard).setOnClickListener(v ->
                Toast.makeText(getContext(), "Em breve: dashboard", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.itemConfiguracoes).setOnClickListener(v ->
                Toast.makeText(getContext(), "Em breve: configurações", Toast.LENGTH_SHORT).show());

        // ── BOTÃO DE LOGOUT (Mantido) ──────────────────────────
        view.findViewById(R.id.itemSair).setOnClickListener(v -> {
            SessionManager.getInstance().clear();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // O código do btnCatalogo foi removido daqui!

        carregarPerfilLocal();
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarPerfilLocal();
    }

    private void carregarPerfilLocal() {
        comercioRepository.obterPerfilBase(new BaseCallback<ComercioDTO>() {
            @Override
            public void onSuccess(ComercioDTO perfil) {
                if (!isAdded()) return; // Proteção contra fragmento destruído

                requireActivity().runOnUiThread(() -> {
                    // Atualiza os campos com os dados da API
                    txtNome.setText(perfil.getNome());
                    txtCidade.setText(perfil.getCidade() != null ? perfil.getCidade() : "Localização não informada");

                    // Formata a avaliação (se precisar de prefixo)
                    String avaliacao = (perfil.getAvaliacao() != null) ? perfil.getAvaliacao() : "5.0";
                    txtAvaliacao.setText("⭐ " + avaliacao);

                    // Carrega a imagem com Glide
                    if (perfil.getImg_url() != null && !perfil.getImg_url().isEmpty()) {
                        Glide.with(requireContext())
                                .load(perfil.getImg_url())
                                .placeholder(R.drawable.hortlink_logo)
                                .circleCrop()
                                .into(imgFazenda);
                    } else {
                        imgFazenda.setImageResource(R.drawable.hortlink_logo);
                    }
                });
            }

            @Override
            public void onError(String erro) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Não foi possível carregar o perfil.", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}