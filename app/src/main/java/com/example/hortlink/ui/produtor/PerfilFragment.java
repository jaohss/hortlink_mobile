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
import com.example.hortlink.ui.auth.CompletarPerfilProdutorActivity;
import com.example.hortlink.ui.auth.MainActivity;
import com.example.hortlink.util.SessionManager;

public class PerfilFragment extends Fragment {

    private TextView txtNome, txtAvaliacao, txtCidade;
    private ImageView imgFazenda;

    public PerfilFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ── Views do perfil ─────────────────────────────────────
        txtNome      = view.findViewById(R.id.txtNomeProd);
        txtCidade    = view.findViewById(R.id.txtCidadeProd);
        txtAvaliacao = view.findViewById(R.id.txtAvaliacao);
        imgFazenda   = view.findViewById(R.id.imgFazenda);

        // ── Seção: Conta ────────────────────────────────────────
        LinearLayout btnEditarPerfil = view.findViewById(R.id.btnEditarPerfil);
        btnEditarPerfil.setOnClickListener(v -> {
            // Pegando o ID do nosso SessionManager ao invés do Firebase
            Long usuarioId = SessionManager.getInstance().getUsuarioId();

            Intent intent = new Intent(getActivity(), CompletarPerfilProdutorActivity.class);
            intent.putExtra("usuario_id", usuarioId);
            intent.putExtra("modo_edicao", true);
            startActivity(intent);
        });

        LinearLayout btnDashboard = view.findViewById(R.id.btnDashboard);
        btnDashboard.setOnClickListener(v ->
                Toast.makeText(getContext(), "Em breve: dashboard", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.itemConfiguracoes).setOnClickListener(v ->
                Toast.makeText(getContext(), "Em breve: configurações", Toast.LENGTH_SHORT).show());

        // ── BOTÃO DE LOGOUT ─────────────────────────────────────
        view.findViewById(R.id.itemSair).setOnClickListener(v -> {
            // 1. Apaga o Token JWT e os dados do usuário do SharedPreferences
            SessionManager.getInstance().clear();

            // 2. Volta para a tela de Login limpando o histórico (para não voltar com o botão "Voltar" do Android)
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // ── Seção: Catálogo ────────────────────────────────────────
        LinearLayout btnCatalogo = view.findViewById(R.id.btnMeuCatalogo);
        btnCatalogo.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), GerenciarProdutosActivity.class);
            startActivity(intent);
        });

        // Carrega as informações na tela
        carregarPerfilLocal();
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarPerfilLocal();
    }

    // ─── Carrega dados de forma instantânea da Sessão ────────────
    private void carregarPerfilLocal() {
        // Puxa os dados que o AuthRepository salvou na hora do Login
        String nome = SessionManager.getInstance().getNomeUsuario();
        String fotoUrl = SessionManager.getInstance().getUrlFoto();
        String role = SessionManager.getInstance().getRole();

        // Atualiza a UI
        txtNome.setText(nome != null ? nome : "Usuário");

        // Como ainda não puxamos o endereço da nova API, mostramos a Role do usuário
        txtCidade.setText(role != null ? role : "Perfil do HortiLink");

        txtAvaliacao.setText("⭐ 5.0"); // Fictício até criarmos a rota de avaliações

        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            Glide.with(requireContext())
                    .load(fotoUrl)
                    .placeholder(R.drawable.hortlink_logo)
                    .circleCrop()
                    .into(imgFazenda);
        } else {
            imgFazenda.setImageResource(R.drawable.hortlink_logo);
        }
    }
}