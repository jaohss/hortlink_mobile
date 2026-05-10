package com.example.hortlink.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hortlink.R;
import com.example.hortlink.adapters.ProdutoAdapter;
import com.example.hortlink.bd.SupabaseHelper;
import com.example.hortlink.entidades.Produto;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PerfilProdutorActivity extends AppCompatActivity {

    private TextView txtNome, txtAvaliacao, txtCidade, txtContato;
    private ImageView imgFazenda;
    private RecyclerView recyclerProdutosPerfil;
    private SupabaseHelper supabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil_produtor);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        supabase = new SupabaseHelper(this);

        txtNome      = findViewById(R.id.txtNomeProd);
        txtAvaliacao = findViewById(R.id.txtAvaliacao);
        txtCidade    = findViewById(R.id.txtCidadeProd);
        txtContato   = findViewById(R.id.txtContatoProd);
        imgFazenda   = findViewById(R.id.imgFazenda);
        recyclerProdutosPerfil = findViewById(R.id.recyclerProdutosPerfil);

        recyclerProdutosPerfil.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        String produtorId = getIntent().getStringExtra("produtor_id");
        if (produtorId == null) { finish(); return; }

        carregarPerfilProdutor(produtorId);
        carregarProdutosDoProdutor(produtorId);
    }

    // ─── Dados do produtor ───────────────────────────────────────
    private void carregarPerfilProdutor(String uid) {
        supabase.buscarProdutorPorId(uid, new SupabaseHelper.SupabaseCallback() {
            @Override
            public void onSuccess(String json) {
                try {
                    JSONArray array = new JSONArray(json);
                    if (array.length() == 0) return;

                    JSONObject obj   = array.getJSONObject(0);
                    String nome      = obj.optString("nome", "");
                    String cidade    = obj.optString("cidade", "Cidade não informada");
                    String contato   = obj.optString("contato", "Sem contato");
                    double avaliacao = obj.optDouble("avaliacao", 0.0);
                    String fotoUrl   = obj.optString("foto_url", "");

                    runOnUiThread(() -> {
                        txtNome.setText(nome);
                        txtCidade.setText(cidade);
                        txtContato.setText(contato);
                        txtAvaliacao.setText(String.valueOf(avaliacao));

                        if (!fotoUrl.isEmpty()) {
                            Glide.with(PerfilProdutorActivity.this)
                                    .load(fotoUrl)
                                    .placeholder(R.drawable.hortlink_logo)
                                    .circleCrop()
                                    .into(imgFazenda);
                        }
                    });

                } catch (Exception e) { e.printStackTrace(); }
            }

            @Override
            public void onError(String erro) {
                runOnUiThread(() -> Toast.makeText(PerfilProdutorActivity.this,
                        "Erro ao carregar perfil", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // ─── Produtos do produtor na lista horizontal ────────────────
    private void carregarProdutosDoProdutor(String uid) {
        supabase.listarProdutosPorProdutor(uid, new SupabaseHelper.SupabaseCallback() {
            @Override
            public void onSuccess(String json) {
                List<Produto> lista = parseProdutos(json);
                runOnUiThread(() -> {
                    ProdutoAdapter adapter = new ProdutoAdapter(lista, produto -> {
                        android.content.Intent intent = new android.content.Intent(
                                PerfilProdutorActivity.this, DetalheProdutoActivity.class);
                        intent.putExtra("produto_id", produto.id);
                        startActivity(intent);
                    });
                    recyclerProdutosPerfil.setAdapter(adapter);
                });
            }

            @Override
            public void onError(String erro) { /* falha silenciosa */ }
        });
    }

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
                lista.add(p);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }
}