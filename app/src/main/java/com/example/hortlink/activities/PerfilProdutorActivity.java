package com.example.hortlink.activities;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.hortlink.data.model.Produto;
import com.example.hortlink.data.model.Produtor;
import com.example.hortlink.data.repository.ProdutoRepository;
import com.example.hortlink.data.repository.ProdutorRepository;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Tela pública do produtor — aberta quando o comprador clica num produtor.
 *
 * Sem SupabaseHelper aqui. Dados chegam via:
 *  - ProdutorRepository.buscarPorId()  → cabeçalho do perfil
 *  - ProdutoRepository.listarProdutosPorProdutor() → lista horizontal de produtos
 */

public class PerfilProdutorActivity extends AppCompatActivity {

    private TextView txtNome, txtCidade, txtContato, txtDescricao;
    private ImageView imgFazenda;
    private RecyclerView recyclerProdutosPerfil;
    private SupabaseHelper supabase;

    //Dependências
    private final ProdutoRepository produtoRepository = new ProdutoRepository();
    private final ProdutorRepository produtorRepository = new ProdutorRepository();

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

        bindViews();

        String produtorId = getIntent().getStringExtra("produtor_id");
        if (produtorId == null) {
            finish();
            return;
        }

        carregarPerfil(produtorId);
        carregarProdutos(produtorId);
    }

    // ─── Dados do produtor ───────────────────────────────────────
    // ─── Bind de views ────────────────────────────────────────────────

    private void bindViews() {
        txtNome      = findViewById(R.id.txtNomeProd);
        txtCidade    = findViewById(R.id.txtCidadeProd);
        txtContato   = findViewById(R.id.txtContatoProd);
        txtDescricao = findViewById(R.id.txtDescricao);
        imgFazenda   = findViewById(R.id.imgFazenda);

        recyclerProdutosPerfil = findViewById(R.id.recyclerProdutosPerfil);
        recyclerProdutosPerfil.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
    }

    // ─── Dados do produtor ────────────────────────────────────────────

    private void carregarPerfil(String produtorId) {
        produtorRepository.buscarPorId(produtorId, new ProdutorRepository.CallbackUnico() {

            @Override
            public void onSuccess(Produtor produtor) {
                runOnUiThread(() -> {
                    txtNome.setText(produtor.getNome());

                    // Cidade + estado juntos, com fallback
                    String cidade  = produtor.getCidade();
                    String estado  = produtor.getUsuario().estado;
                    String local   = (!cidade.isEmpty() && !estado.isEmpty())
                            ? cidade + ", " + estado
                            : !cidade.isEmpty() ? cidade : "Localização não informada";
                    txtCidade.setText(local);

                    // Telefone com fallback
                    String tel = produtor.getTelefone();
                    txtContato.setText(!tel.isEmpty() ? tel : "Sem contato");

                    // Descrição com fallback
                    String desc = produtor.getDescricao();
                    txtDescricao.setText(!desc.isEmpty() ? desc : "");


                    // Foto de perfil
                    String fotoUrl = produtor.getFotoUrl();
                    if (fotoUrl != null && !fotoUrl.isEmpty()) {
                        Glide.with(PerfilProdutorActivity.this)
                                .load(fotoUrl)
                                .placeholder(R.drawable.hortlink_logo)
                                .circleCrop()
                                .into(imgFazenda);
                    }
                });
            }

            @Override
            public void onError(String erro) {
                runOnUiThread(() ->
                        Toast.makeText(PerfilProdutorActivity.this,
                                "Erro ao carregar perfil", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // ─── Produtos do produtor ─────────────────────────────────────────

    private void carregarProdutos(String produtorId) {
        produtoRepository.listarProdutosPorProdutor(produtorId, new ProdutoRepository.Callback() {

            @Override
            public void onSuccess(String json) {
                // ProdutoRepository já retorna JSON string — parse feito aqui
                // para não criar dependência desnecessária
                List<Produto> lista = parseProdutos(json);
                runOnUiThread(() -> {
                    ProdutoAdapter adapter = new ProdutoAdapter(lista, produto -> {
                        Intent intent = new Intent(
                                PerfilProdutorActivity.this, DetalheProdutoActivity.class);
                        intent.putExtra("produto_id", produto.id);
                        startActivity(intent);
                    });
                    recyclerProdutosPerfil.setAdapter(adapter);
                });
            }

            @Override
            public void onError(String erro) {
                // Falha silenciosa — perfil ainda é útil sem a lista de produtos
            }
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
}