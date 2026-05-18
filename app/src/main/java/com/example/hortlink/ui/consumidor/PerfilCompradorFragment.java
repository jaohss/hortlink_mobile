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
import com.example.hortlink.data.model.Usuario;
import com.example.hortlink.data.repository.UsuarioRepository;
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

        // Exibe dados do SessionManager imediatamente (sem esperar rede)
        preencherComSessao();

        // Refresca do banco em background
        String uid = SessionManager.getInstance().getUid();
        if (uid != null) carregarPerfil(uid);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recarrega ao voltar da tela de edição
        String uid = SessionManager.getInstance().getUid();
        if (uid != null) carregarPerfil(uid);
    }

    // ─── Bind ─────────────────────────────────────────────────────────

    private void bindViews(View view) {
        txtNome   = view.findViewById(R.id.txtNomeComprador);
        txtCidade = view.findViewById(R.id.txtCidadeComprador);
        txtEmail  = view.findViewById(R.id.txtEmailComprador);
        imgPerfil = view.findViewById(R.id.imgPerfilComprador);
    }

    // ─── Sessão local ─────────────────────────────────────────────────

    private void preencherComSessao() {
        Usuario u = SessionManager.getInstance().getUsuario();
        if (u == null) return;
        txtNome.setText(u.nome != null ? u.nome : "");
        txtEmail.setText(u.email != null ? u.email : "");
        txtCidade.setText(montarLocalidade(u));
    }

    // ─── Rede ─────────────────────────────────────────────────────────

    private void carregarPerfil(String uid) {
        usuarioRepository.buscarPorId(uid, new UsuarioRepository.Callback() {

            @Override
            public void onSuccess(Usuario usuario) {
                if (!isAdded()) return;

                // Atualiza sessão com dados frescos
                SessionManager.getInstance().setUsuario(usuario);

                requireActivity().runOnUiThread(() -> {
                    txtNome.setText(usuario.nome);
                    txtEmail.setText(usuario.email);
                    txtCidade.setText(montarLocalidade(usuario));

                    if (usuario.fotoUrl != null
                            && !usuario.fotoUrl.isEmpty()
                            && !usuario.fotoUrl.equals("null")) {
                        Glide.with(requireContext())
                                .load(usuario.fotoUrl)
                                .placeholder(R.drawable.hortlink_logo)
                                .circleCrop()
                                .into(imgPerfil);
                    }
                });
            }

            @Override
            public void onError(String erro) {
                // Falha silenciosa — sessão já exibida
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

        // Editar perfil — reaproveita CompletarPerfilCompradorActivity em modo edição
        LinearLayout btnEditarPerfil = view.findViewById(R.id.btnEditarPerfil);
        btnEditarPerfil.setOnClickListener(v -> {
            String uid = SessionManager.getInstance().getUid();
            if (uid == null) return;
            Intent intent = new Intent(getActivity(), CompletarPerfilCompradorActivity.class);
            intent.putExtra("uid", uid);
            intent.putExtra("modo_edicao", true);
            startActivity(intent);
        });

        // Configurações — em breve
        view.findViewById(R.id.itemConfiguracoes).setOnClickListener(v -> {
            // Em breve
        });

        // Sair
        view.findViewById(R.id.itemSair).setOnClickListener(v -> {
            usuarioRepository.logout();
            SessionManager.getInstance().limpar();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    // ─── Helper ───────────────────────────────────────────────────────

    private String montarLocalidade(Usuario u) {
        String cidade = u.cidade != null ? u.cidade : "";
        String estado = u.estado != null ? u.estado : "";
        if (!cidade.isEmpty() && !estado.isEmpty()) return cidade + ", " + estado;
        if (!cidade.isEmpty()) return cidade;
        return "Localização não informada";
    }
}