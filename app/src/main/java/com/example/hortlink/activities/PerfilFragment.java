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
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONObject;

public class PerfilFragment extends Fragment {

    private TextView txtNome, txtAvaliacao, txtCidade;
    private ImageView imgFazenda;
    private RecyclerView recyclerProdutosPerfil;
    private SupabaseHelper supabase;

    public PerfilFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        supabase = new SupabaseHelper(requireContext());

        // ── Views do perfil ─────────────────────────────────────
        txtNome      = view.findViewById(R.id.txtNomeProd);
        txtCidade    = view.findViewById(R.id.txtCidadeProd);
        txtAvaliacao = view.findViewById(R.id.txtAvaliacao);
        imgFazenda   = view.findViewById(R.id.imgFazenda);

        // ── RecyclerView horizontal de produtos ─────────────────
        // O novo XML não tem recyclerProdutosPerfil no fragment_perfil,
        // pois os produtos ficam no GerenciarProdutosFragment.
        // Se quiser reativar, adicione o RecyclerView no XML.

        // ── Seção: Gerenciar loja ───────────────────────────────
        LinearLayout btnMeusProdutos = view.findViewById(R.id.btnMeusProdutos);
        btnMeusProdutos.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, new GerenciarProdutosFragment())
                        .addToBackStack(null)
                        .commit());

        LinearLayout btnAddProduto = view.findViewById(R.id.btnAddProduto);
        btnAddProduto.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AdicionarProdutosActivity.class)));

        LinearLayout btnPedidos = view.findViewById(R.id.btnPedidos);
        btnPedidos.setOnClickListener(v ->
                Toast.makeText(getContext(), "Em breve: pedidos", Toast.LENGTH_SHORT).show());

        // ── Seção: Conta ────────────────────────────────────────
        LinearLayout btnEditarPerfil = view.findViewById(R.id.btnEditarPerfil);
        btnEditarPerfil.setOnClickListener(v -> {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Intent intent = new Intent(getActivity(), CompletarPerfilProdutorActivity.class);
            intent.putExtra("uid", uid);
            intent.putExtra("modo_edicao", true);
            startActivity(intent);
        });

        LinearLayout btnDashboard = view.findViewById(R.id.btnDashboard);
        btnDashboard.setOnClickListener(v ->
                Toast.makeText(getContext(), "Em breve: dashboard", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.itemConfiguracoes).setOnClickListener(v ->
                Toast.makeText(getContext(), "Em breve: configurações", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.itemSair).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // ── Carrega dados do produtor logado ────────────────────
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (uid != null) {
            carregarPerfil(uid);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid != null) carregarPerfil(uid);
    }

    // ─── Busca e preenche dados do produtor ──────────────────────
    private void carregarPerfil(String uid) {
        supabase.buscarProdutorPorId(uid, new SupabaseHelper.SupabaseCallback() {
            @Override
            public void onSuccess(String json) {
                try {
                    JSONArray array = new JSONArray(json);
                    if (array.length() == 0) return;

                    JSONObject obj   = array.getJSONObject(0);
                    String nome      = obj.optString("nome", "");
                    String cidade    = obj.optString("cidade", "Cidade não informada");
                    double avaliacao = obj.optDouble("avaliacao", 0.0);
                    String fotoUrl   = obj.optString("foto_url", "");

                    requireActivity().runOnUiThread(() -> {
                        txtNome.setText(nome);
                        txtCidade.setText(cidade);
                        txtAvaliacao.setText(String.format("⭐ %.1f", avaliacao));

                        if (!fotoUrl.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(fotoUrl)
                                    .placeholder(R.drawable.hortlink_logo)
                                    .circleCrop()
                                    .into(imgFazenda);
                        }
                    });

                } catch (Exception e) { e.printStackTrace(); }
            }

            @Override
            public void onError(String erro) { /* silencioso */ }
        });
    }
}