package com.example.hortlink.ui.consumidor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.hortlink.R;
import com.example.hortlink.data.dto.PerfilCompradorDTO;
import com.example.hortlink.data.repository.UsuarioRepository;
import com.example.hortlink.service.BaseCallback;
import com.example.hortlink.ui.auth.CompletarPerfilCompradorActivity;
import com.example.hortlink.ui.auth.MainActivity;
import com.example.hortlink.util.SessionManager;

public class PerfilCompradorFragment extends Fragment {

    private TextView txtNome, txtCidade, txtEmail;
    private ImageView imgPerfil;

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();

    public PerfilCompradorFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil_comprador, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        configurarBotoes(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Chama a API toda vez que a tela volta a ficar visível (ex: após fechar a edição)
        carregarPerfil();
    }

    // ─── Bind ─────────────────────────────────────────────────────────

    private void bindViews(View view) {
        txtNome   = view.findViewById(R.id.txtNomeComprador);
        txtCidade = view.findViewById(R.id.txtCidadeComprador);
        txtEmail  = view.findViewById(R.id.txtEmailComprador);
        imgPerfil = view.findViewById(R.id.imgPerfilComprador);
    }

    // ─── Rede ─────────────────────────────────────────────────────────

    private void carregarPerfil() {
        usuarioRepository.obterPerfil(new BaseCallback<PerfilCompradorDTO>() {

            @Override
            public void onSuccess(PerfilCompradorDTO dto) {
                if (!isAdded()) return;

                requireActivity().runOnUiThread(() -> {
                    txtNome.setText(dto.getNome() != null ? dto.getNome() : "Sem nome");
                    txtEmail.setText(dto.getEmail() != null ? dto.getEmail() : "Sem e-mail");
                    txtCidade.setText(montarLocalidade(dto));

                    // Carrega apenas o placeholder estático por enquanto
                    Glide.with(requireContext())
                            .load((String) null) // Passar null força o Glide a exibir o placeholder
                            .placeholder(R.drawable.hortlink_logo)
                            .circleCrop()
                            .into(imgPerfil);
                });
            }

            @Override
            public void onError(String erro) {
                // Falha silenciosa — sessão já exibida ou manter os dados do cache
            }
        });
    }

    // ─── Botões ───────────────────────────────────────────────────────

    private void configurarBotoes(View view) {

        // Meus pedidos
        LinearLayout btnMeusPedidos = view.findViewById(R.id.btnMeusPedidos);
        btnMeusPedidos.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, new PedidosFragment())
                        .addToBackStack(null)
                        .commit());

        // Editar perfil
        LinearLayout btnEditarPerfil = view.findViewById(R.id.btnEditarPerfil);
        btnEditarPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CompletarPerfilCompradorActivity.class);
            intent.putExtra("modo_edicao", true);
            startActivity(intent);
        });

        // Configurações — em breve
        view.findViewById(R.id.itemConfiguracoes).setOnClickListener(v -> {
            // Em breve
        });

        // Sair
        view.findViewById(R.id.itemSair).setOnClickListener(v -> {
            // Limpa o token e todos os dados do SharedPreferences
            SessionManager.getInstance().clear();

            // Redireciona para a MainActivity limpando a pilha de navegação
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    // ─── Helper ───────────────────────────────────────────────────────

    private String montarLocalidade(PerfilCompradorDTO dto) {
        String cidade = dto.getCidade() != null ? dto.getCidade() : "";
        String estado = dto.getEstado() != null ? dto.getEstado() : "";

        if (!cidade.isEmpty() && !estado.isEmpty()) return cidade + ", " + estado;
        if (!cidade.isEmpty()) return cidade;
        return "Localização não informada";
    }
}