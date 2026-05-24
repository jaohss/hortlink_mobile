package com.example.hortlink.ui.consumidor;

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
import com.example.hortlink.data.model.Produto;
import com.example.hortlink.data.model.Produtor;
import com.example.hortlink.data.repository.CarrinhoRepository;
import com.example.hortlink.data.repository.ProdutoRepository;
import com.example.hortlink.data.repository.ProdutorRepository;
import com.example.hortlink.util.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

public class DetalheProdutoActivity extends AppCompatActivity {

    private Produto produto;

    private TextView txtNome, txtPreco, txtDescricao;
    private ImageView imgProduto;
    private TextView txtNomeProd, txtCidadeProd, txtContatoProd;
    private ImageView fotoPerfil;
    private Button btnCarrinho;
    private ConstraintLayout cardProdutor;

    //controle de quantidade
    private TextView txtQuantidade;
    private ImageView btnAdd, btnRemove;
    private int quantidadeSelecionada = 1;
    private static final int QUANTIDADE_MINIMA = 1;
    private static final int QUANTIDADE_MAXIMA = 99;

    private final ProdutoRepository produtoRepository     = new ProdutoRepository();
    private final ProdutorRepository produtorRepository   = new ProdutorRepository();
    private final CarrinhoRepository carrinhoRepository   = new CarrinhoRepository();

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

        // ← NOVO: bind dos botões de quantidade
        txtQuantidade  = findViewById(R.id.quantidade);
        btnAdd = findViewById(R.id.additem);
        btnRemove = findViewById(R.id.removeitem);
        configurarBotoesQuantidade();

        btnCarrinho.setEnabled(false);
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        String produtoId = getIntent().getStringExtra("produto_id");
        if (produtoId == null) { finish(); return; }

        carregarProduto(produtoId);
    }

    // ─── 1. Busca produto ─────────────────────────────────────────────

    private void carregarProduto(String produtoId) {
        produtoRepository.buscarProdutoPorId(produtoId, new ProdutoRepository.Callback() {

            @Override
            public void onSuccess(String json) {
                try {
                    JSONArray array = new JSONArray(json);
                    if (array.length() == 0) { runOnUiThread(() -> finish()); return; }

                    JSONObject obj = array.getJSONObject(0);

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

    // ─── 2. Busca produtor ────────────────────────────────────────────

    private void carregarProdutor(String produtorId) {
        produtorRepository.buscarPorId(produtorId, new ProdutorRepository.CallbackUnico() {

            @Override
            public void onSuccess(Produtor produtor) {
                runOnUiThread(() -> {
                    txtNomeProd.setText(produtor.getNome());
                    txtCidadeProd.setText(produtor.getCidade());
                    txtContatoProd.setText(produtor.getTelefone());

                    Glide.with(DetalheProdutoActivity.this)
                            .load(produtor.getFotoUrl().isEmpty() ? null : produtor.getFotoUrl())
                            .placeholder(R.drawable.hortlink_logo)
                            .error(R.drawable.hortlink_logo)
                            .circleCrop()
                            .into(fotoPerfil);

                    cardProdutor.setOnClickListener(v -> {
                        Intent intent = new Intent(DetalheProdutoActivity.this,
                                PerfilProdutorActivity.class);
                        intent.putExtra("produtor_id", produtor.getId());
                        startActivity(intent);
                    });
                });
            }

            @Override
            public void onError(String erro) { /* falha silenciosa */ }
        });
    }

    // ─── 3. Adiciona ao carrinho ──────────────────────────────────────
    private void adicionarAoCarrinho() {
        String usuarioId = SessionManager.getInstance().getUid();
        if (usuarioId == null) {
            Toast.makeText(this, "Faça login para adicionar ao carrinho", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCarrinho.setEnabled(false);

        carrinhoRepository.buscarItemExistente(usuarioId, produto.getId(),
                new CarrinhoRepository.Callback() {

                    @Override
                    public void onSuccess(String resultado) {
                        try {
                            JSONArray rows = new JSONArray(resultado);

                            if (rows.length() > 0) {
                                // Já existe → soma a quantidade atual com a selecionada
                                JSONObject existing = rows.getJSONObject(0);
                                String carrinhoId   = existing.getString("id");
                                int qtdAtual        = existing.getInt("quantidade");
                                int novaQtd         = Math.min(
                                        qtdAtual + quantidadeSelecionada, // ← NOVO
                                        QUANTIDADE_MAXIMA
                                );

                                carrinhoRepository.atualizarQuantidade(carrinhoId, novaQtd,
                                        new CarrinhoRepository.Callback() {
                                            @Override
                                            public void onSuccess(String r) {
                                                runOnUiThread(() -> {
                                                    btnCarrinho.setEnabled(true);
                                                    Toast.makeText(DetalheProdutoActivity.this,
                                                            "Carrinho atualizado ✓", Toast.LENGTH_SHORT).show();
                                                    resetarQuantidade(); // ← NOVO
                                                });
                                            }
                                            @Override
                                            public void onError(String erro) { erroCarrinho(erro); }
                                        });

                            } else {
                                // Novo item → insere com a quantidade selecionada
                                carrinhoRepository.inserirItemComQuantidade( // ← NOVO
                                        usuarioId, produto.getId(), quantidadeSelecionada,
                                        new CarrinhoRepository.Callback() {
                                            @Override
                                            public void onSuccess(String r) {
                                                runOnUiThread(() -> {
                                                    btnCarrinho.setEnabled(true);
                                                    Toast.makeText(DetalheProdutoActivity.this,
                                                            "Adicionado ao carrinho ✓", Toast.LENGTH_SHORT).show();
                                                    resetarQuantidade(); // ← NOVO
                                                });
                                            }
                                            @Override
                                            public void onError(String erro) { erroCarrinho(erro); }
                                        });
                            }

                        } catch (Exception e) { erroCarrinho(e.getMessage()); }
                    }

                    @Override
                    public void onError(String erro) { erroCarrinho(erro); }
                });
    }

    // ← NOVO: reseta o seletor para 1 após adicionar
    private void resetarQuantidade() {
        quantidadeSelecionada = QUANTIDADE_MINIMA;
        atualizarDisplayQuantidade();
    }

    // ─── NOVO: lógica dos botões +/− ─────────────────────────────────

    private void configurarBotoesQuantidade() {
        atualizarDisplayQuantidade();

        btnAdd.setOnClickListener(v -> {
            if (quantidadeSelecionada < QUANTIDADE_MAXIMA) {
                quantidadeSelecionada++;
                atualizarDisplayQuantidade();
            }
        });

        btnRemove.setOnClickListener(v -> {
            if (quantidadeSelecionada > QUANTIDADE_MINIMA) {
                quantidadeSelecionada--;
                atualizarDisplayQuantidade();
            }
        });
    }

    private void atualizarDisplayQuantidade() {
        txtQuantidade.setText(String.valueOf(quantidadeSelecionada));

        // Feedback visual: botão − fica opaco quando está no mínimo
        btnRemove.setAlpha(quantidadeSelecionada <= QUANTIDADE_MINIMA ? 0.4f : 1.0f);
        btnAdd.setAlpha(quantidadeSelecionada >= QUANTIDADE_MAXIMA ? 0.4f : 1.0f);
    }

    private void erroCarrinho(String erro) {
        runOnUiThread(() -> {
            btnCarrinho.setEnabled(true);
            Toast.makeText(this, "Erro ao adicionar: " + erro, Toast.LENGTH_LONG).show();
        });
    }
}