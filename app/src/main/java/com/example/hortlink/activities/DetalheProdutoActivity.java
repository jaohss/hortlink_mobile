package com.example.hortlink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.hortlink.R;
import com.example.hortlink.bd.SupabaseHelper;
import com.example.hortlink.entidades.Produto;
import com.example.hortlink.entidades.Produtor;

import org.json.JSONArray;
import org.json.JSONObject;

public class DetalheProdutoActivity extends AppCompatActivity {

    private SupabaseHelper supabase;

    // Views do produto
    private TextView  txtNome, txtPreco, txtDescricao;
    private ImageView imgProduto;

    // Views do produtor
    private TextView  txtNomeProd, txtCidadeProd, txtContatoProd, txtAvaliacao;
    private ImageView fotoPerfil;
    private ConstraintLayout cardProdutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalhe_produto);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        supabase = new SupabaseHelper(this);

        // Views
        txtNome      = findViewById(R.id.txtNome);
        txtPreco     = findViewById(R.id.txtPreco);
        txtDescricao = findViewById(R.id.txtDescricao);
        imgProduto   = findViewById(R.id.imgProduto);
        txtNomeProd  = findViewById(R.id.txtNomeProd);
        txtCidadeProd   = findViewById(R.id.txtCidadeProd);
        txtContatoProd  = findViewById(R.id.txtContatoProd);
        txtAvaliacao    = findViewById(R.id.txtAvaliacao);
        fotoPerfil      = findViewById(R.id.fotoPerfil);
        cardProdutor    = findViewById(R.id.cardProdutor);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        // ID vindo do HomeFragment (agora String UUID)
        String produtoId = getIntent().getStringExtra("produto_id");
        if (produtoId == null) { finish(); return; }

        carregarProduto(produtoId);
    }

    // ─── 1. Busca o produto ──────────────────────────────────────
    private void carregarProduto(String produtoId) {
        supabase.buscarProdutoPorId(produtoId, new SupabaseHelper.SupabaseCallback() {
            @Override
            public void onSuccess(String json) {
                try {
                    JSONArray array  = new JSONArray(json);
                    if (array.length() == 0) {
                        runOnUiThread(() -> { Toast.makeText(DetalheProdutoActivity.this,
                                "Produto não encontrado", Toast.LENGTH_SHORT).show(); finish(); });
                        return;
                    }
                    JSONObject obj = array.getJSONObject(0);

                    String nome       = obj.optString("nome");
                    double preco      = obj.optDouble("preco", 0.0);
                    String descricao  = obj.optString("descricao");
                    String fotoUrl    = obj.optString("foto_url");
                    String produtorId = obj.optString("produtor_id"); // UID do Firebase

                    runOnUiThread(() -> {
                        txtNome.setText(nome);
                        txtPreco.setText(String.format("R$ %.2f", preco));
                        txtDescricao.setText(descricao);

                        Glide.with(DetalheProdutoActivity.this)
                                .load(fotoUrl.isEmpty() ? null : fotoUrl)
                                .placeholder(R.drawable.hortlink_logo)
                                .error(R.drawable.hortlink_logo)
                                .centerCrop()
                                .into(imgProduto);
                    });

                    // 2. Com o produtorId em mãos, busca o produtor
                    if (!produtorId.isEmpty()) {
                        carregarProdutor(produtorId);
                    }

                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(DetalheProdutoActivity.this,
                            "Erro ao processar produto", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onError(String erro) {
                runOnUiThread(() -> Toast.makeText(DetalheProdutoActivity.this,
                        "Erro: " + erro, Toast.LENGTH_LONG).show());
            }
        });
    }

    // ─── 2. Busca o produtor depois que o produto carregou ───────
    private void carregarProdutor(String produtorId) {
        supabase.buscarProdutorPorId(produtorId, new SupabaseHelper.SupabaseCallback() {
            @Override
            public void onSuccess(String json) {
                try {
                    JSONArray array = new JSONArray(json);
                    if (array.length() == 0) return;

                    JSONObject obj   = array.getJSONObject(0);
                    String nome      = obj.optString("nome");
                    String cidade    = obj.optString("cidade");
                    String contato   = obj.optString("contato");
                    double avaliacao = obj.optDouble("avaliacao", 0.0);
                    String fotoUrl   = obj.optString("foto_url");
                    String uid       = obj.optString("id");

                    runOnUiThread(() -> {
                        txtNomeProd.setText(nome);
                        txtCidadeProd.setText(cidade);
                        txtContatoProd.setText(contato);
                        txtAvaliacao.setText("Avaliação: " + avaliacao);

                        Glide.with(DetalheProdutoActivity.this)
                                .load(fotoUrl.isEmpty() ? null : fotoUrl)
                                .placeholder(R.drawable.hortlink_logo)
                                .error(R.drawable.hortlink_logo)
                                .circleCrop() // foto de perfil fica redonda
                                .into(fotoPerfil);

                        cardProdutor.setOnClickListener(v -> {
                            Intent intent = new Intent(DetalheProdutoActivity.this,
                                    PerfilProdutorActivity.class);
                            intent.putExtra("produtor_id", uid);
                            startActivity(intent);
                        });
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String erro) {
                // Falhou silenciosamente — produto já está visível
            }
        });
    }
}