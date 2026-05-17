package com.example.hortlink.activities;

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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hortlink.R;
import com.example.hortlink.bd.SupabaseHelper;
import com.example.hortlink.data.model.Usuario;
import com.example.hortlink.data.repository.UsuarioRepository;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Fragment de perfil do produtor logado.
 * é o próprio usuário consultando o próprio perfil, não um produtor
 * público, por isso usamos UsuarioRepository e não ProdutorRepository.
 */
public class PerfilFragment extends Fragment {

    private TextView txtNome, txtCidade;
    private ImageView imgFazenda;
    private RecyclerView recyclerProdutosPerfil;
    private SupabaseHelper supabase;


    // ─── Dependências ─────────────────────────────────────────────────
    private final UsuarioRepository usuarioRepository = new UsuarioRepository();

    public PerfilFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        configurarBotoes(view);

        String uid = getUidAtual();
        if (uid != null) carregarPerfil(uid);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recarrega ao voltar da tela de edição de perfil
        String uid = getUidAtual();
        if (uid != null) carregarPerfil(uid);
    }

    // ─── Bind de views ────────────────────────────────────────────────

    private void bindViews(View view) {
        txtNome      = view.findViewById(R.id.txtNomeProd);
        txtCidade    = view.findViewById(R.id.txtCidadeProd);
        imgFazenda   = view.findViewById(R.id.imgFazenda);

    }

    // ─── Configuração de botões ───────────────────────────────────────

    private void configurarBotoes(View view) {

        // Meus produtos
        LinearLayout btnMeusProdutos = view.findViewById(R.id.btnMeusProdutos);
        btnMeusProdutos.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, new GerenciarProdutosFragment())
                        .addToBackStack(null)
                        .commit());

        // Adicionar produto
        LinearLayout btnAddProduto = view.findViewById(R.id.btnAddProduto);
        btnAddProduto.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AdicionarProdutosActivity.class)));

        // Pedidos — em breve
        LinearLayout btnPedidos = view.findViewById(R.id.btnPedidos);
        btnPedidos.setOnClickListener(v ->
                Toast.makeText(getContext(), "Em breve: pedidos", Toast.LENGTH_SHORT).show());

        // Editar perfil — reaproveita CompletarPerfilProdutorActivity em modo edição
        LinearLayout btnEditarPerfil = view.findViewById(R.id.btnEditarPerfil);
        btnEditarPerfil.setOnClickListener(v -> {
            String uid = getUidAtual();
            if (uid == null) return;
            Intent intent = new Intent(getActivity(), CompletarPerfilProdutorActivity.class);
            intent.putExtra("uid", uid);
            intent.putExtra("modo_edicao", true);
            startActivity(intent);
        });

        // Dashboard — em breve
        LinearLayout btnDashboard = view.findViewById(R.id.btnDashboard);
        btnDashboard.setOnClickListener(v ->
                Toast.makeText(getContext(), "Em breve: dashboard", Toast.LENGTH_SHORT).show());

        // Configurações — em breve
        view.findViewById(R.id.itemConfiguracoes).setOnClickListener(v ->
                Toast.makeText(getContext(), "Em breve: configurações", Toast.LENGTH_SHORT).show());

        // Sair
        view.findViewById(R.id.itemSair).setOnClickListener(v -> {
            usuarioRepository.logout();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }



    // ─── Carregamento do perfil ───────────────────────────────────────

    private void carregarPerfil(String uid) {
        usuarioRepository.buscarPorId(uid, new UsuarioRepository.Callback() {

            @Override
            public void onSuccess(Usuario usuario) {
                if (!isAdded()) return; // fragment pode ter sido destacado

                requireActivity().runOnUiThread(() -> {
                    txtNome.setText(usuario.nome);

                    // Cidade + estado juntos, com fallback
                    String cidade = usuario.cidade  != null ? usuario.cidade  : "";
                    String estado = usuario.estado  != null ? usuario.estado  : "";
                    String local  = (!cidade.isEmpty() && !estado.isEmpty())
                            ? cidade + ", " + estado
                            : !cidade.isEmpty() ? cidade : "Localização não informada";
                    txtCidade.setText(local);

                    // Foto de perfil
                    if (usuario.fotoUrl != null && !usuario.fotoUrl.isEmpty() && !usuario.fotoUrl.equals("null")) {
                        Glide.with(requireContext())
                                .load(usuario.fotoUrl)
                                .placeholder(R.drawable.hortlink_logo)
                                .circleCrop()
                                .into(imgFazenda);
                    }
                });
            }

            @Override
            public void onError(String erro) {
                // Falha silenciosa — não bloqueia a tela de perfil
            }
        });
    }

    private String getUidAtual() {
        var user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : null;
    }
}