package com.example.hortlink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
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
import com.example.hortlink.data.model.Produto;
import com.example.hortlink.data.repository.ProdutoRepository;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONObject;

public class DetalheProdutoActivity extends AppCompatActivity {


    private SupabaseHelper supabase;
    private Produto produto; // populado uma vez, reutilizado onde precisar

    private TextView      txtNome, txtPreco, txtDescricao;
    private ImageView     imgProduto;
    private TextView      txtNomeProd, txtCidadeProd, txtContatoProd;
    private ImageView     fotoPerfil;
    private Button        btnCarrinho;
    private ConstraintLayout cardProdutor;
    private ProdutoRepository produtoRepository = new ProdutoRepository();

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

        txtNome        = findViewById(R.id.txtNome);
        txtPreco       = findViewById(R.id.txtPreco);
        txtDescricao   = findViewById(R.id.txtDescricao);
        imgProduto     = findViewById(R.id.imgProduto);
        txtNomeProd    = findViewById(R.id.txtNomeProd);
        txtCidadeProd  = findViewById(R.id.txtCidadeProd);
        txtContatoProd = findViewById(R.id.txtContatoProd);
        fotoPerfil     = findViewById(R.id.fotoPerfil);
        cardProdutor   = findViewById(R.id.cardProdutor);
        btnCarrinho    = findViewById(R.id.btnCarrinho);

        btnCarrinho.setEnabled(false); // só libera depois que produto carregar
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        String produtoId = getIntent().getStringExtra("produto_id");
        if (produtoId == null) { finish(); return; }

        carregarProduto(produtoId);
    }

    // ─── 1. Busca produto ────────────────────────────────────────────
    private void carregarProduto(String produtoId) {
        produtoRepository.buscarProdutoPorId(produtoId, new ProdutoRepository.Callback() {
            @Override
            public void onSuccess(String json) {
                try {
                    JSONArray array = new JSONArray(json);
                    if (array.length() == 0) {
                        runOnUiThread(() -> { finish(); });
                        return;
                    }

                    JSONObject obj = array.getJSONObject(0);

                    // Popula o objeto de domínio uma única vez
                    produto = new Produto();
                    produto.setId(obj.optString("id"));
                    produto.setNome(obj.optString("nome"));
                    produto.setPreco(obj.optDouble("preco", 0.0));
                    produto.setDescricao(obj.optString("descricao"));
                    produto.setImagemUri(obj.optString("foto_url"));
                    produto.setUnidade(obj.optString("unidade", "un"));
                    produto.setProdutorId(obj.optString("produtor_id"));

                    runOnUiThread(() -> {
                        txtNome.setText(produto.getNome());
                        txtPreco.setText(String.format("R$ %.2f", produto.getPreco()));
                        txtDescricao.setText(produto.getDescricao());

                        Glide.with(DetalheProdutoActivity.this)
                                .load(produto.getImagemUri().isEmpty() ? null : produto.getImagemUri())
                                .placeholder(R.drawable.hortlink_logo)
                                .error(R.drawable.hortlink_logo)
                                .centerCrop()
                                .into(imgProduto);

                        // Listener definido aqui — produto já está pronto
                        btnCarrinho.setEnabled(true);
                        btnCarrinho.setOnClickListener(v -> adicionarAoCarrinho());
                    });

                    if (!produto.getProdutorId().isEmpty()) {
                        carregarProdutor(produto.getProdutorId());
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

    // ─── 2. Busca produtor ───────────────────────────────────────────
    private void carregarProdutor(String produtorId) {
        supabase.buscarProdutorPorId(produtorId, new SupabaseHelper.SupabaseCallback() {
            @Override
            public void onSuccess(String json) {
                try {
                    JSONArray array = new JSONArray(json);
                    if (array.length() == 0) return;

                    JSONObject obj = array.getJSONObject(0);
                    String nome    = obj.optString("nome");
                    String cidade  = obj.optString("cidade");
                    String contato = obj.optString("contato");
                    String fotoUrl = obj.optString("foto_url");
                    String uid     = obj.optString("id");

                    runOnUiThread(() -> {
                        txtNomeProd.setText(nome);
                        txtCidadeProd.setText(cidade);
                        txtContatoProd.setText(contato);

                        Glide.with(DetalheProdutoActivity.this)
                                .load(fotoUrl.isEmpty() ? null : fotoUrl)
                                .placeholder(R.drawable.hortlink_logo)
                                .error(R.drawable.hortlink_logo)
                                .circleCrop()
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
            public void onError(String erro) { /* falha silenciosa */ }
        });
    }

    // ─── 3. Adiciona ao carrinho ─────────────────────────────────────
    private void adicionarAoCarrinho() {
        String usuarioId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        btnCarrinho.setEnabled(false);

        String urlVerifica = "/rest/v1/carrinho"
                + "?usuario_id=eq." + usuarioId
                + "&produto_id=eq." + produto.getId()
                + "&select=id,quantidade";

        supabase.get(urlVerifica, new SupabaseHelper.SupabaseCallback() {
            @Override
            public void onSuccess(String resultado) {
                try {
                    JSONArray rows = new JSONArray(resultado);

                    if (rows.length() > 0) {
                        // Já existe → incrementa quantidade
                        JSONObject existing = rows.getJSONObject(0);
                        String carrinhoId   = existing.getString("id");
                        int novaQtd         = existing.getInt("quantidade") + 1;

                        JSONObject body = new JSONObject();
                        body.put("quantidade", novaQtd);

                        supabase.patch(
                                "/rest/v1/carrinho?id=eq." + carrinhoId,
                                body,
                                new SupabaseHelper.SupabaseCallback() {
                                    @Override
                                    public void onSuccess(String r) {
                                        runOnUiThread(() -> {
                                            btnCarrinho.setEnabled(true);
                                            Toast.makeText(DetalheProdutoActivity.this,
                                                    "Quantidade atualizada ✓",
                                                    Toast.LENGTH_SHORT).show();
                                        });
                                    }
                                    @Override
                                    public void onError(String erro) { erroCarrinho(erro); }
                                });

                    } else {
                        // Novo item → insere
                        JSONObject body = new JSONObject();
                        body.put("usuario_id", usuarioId);
                        body.put("produto_id", produto.getId());
                        body.put("quantidade", 1);

                        supabase.post(
                                "/rest/v1/carrinho",
                                body,
                                new SupabaseHelper.SupabaseCallback() {
                                    @Override
                                    public void onSuccess(String r) {
                                        runOnUiThread(() -> {
                                            btnCarrinho.setEnabled(true);
                                            Toast.makeText(DetalheProdutoActivity.this,
                                                    "Adicionado ao carrinho ✓",
                                                    Toast.LENGTH_SHORT).show();
                                        });
                                    }
                                    @Override
                                    public void onError(String erro) { erroCarrinho(erro); }
                                });
                    }

                } catch (Exception e) {
                    erroCarrinho(e.getMessage());
                }
            }

            @Override
            public void onError(String erro) { erroCarrinho(erro); }
        });
    }

    private void erroCarrinho(String erro) {
        runOnUiThread(() -> {
            btnCarrinho.setEnabled(true);
            Toast.makeText(this, "Erro ao adicionar: " + erro, Toast.LENGTH_LONG).show();
        });
    }
}