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
import com.example.hortlink.data.dto.DetalheOfertaDTO;
import com.example.hortlink.data.repository.OfertaRepository;
import com.example.hortlink.service.BaseCallback;
import com.example.hortlink.ui.consumidor.PerfilProdutorActivity;

public class DetalheProdutoActivity extends AppCompatActivity {

    private TextView txtNome, txtPreco, txtDescricao;
    private ImageView imgProduto;
    private TextView txtNomeProd, txtCidadeProd, txtContatoProd;
    private ImageView fotoPerfil;
    private Button btnCarrinho;
    private ConstraintLayout cardProdutor;

    private OfertaRepository ofertaRepository;
    // private CarrinhoRepository carrinhoRepository; // Para substituir o Supabase no carrinho

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

        ofertaRepository = new OfertaRepository();
        // carrinhoRepository = new CarrinhoRepository();

        // Fazendo os binds (findViewById)
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

        btnCarrinho.setEnabled(false);
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        // 1. Recebe os dados que vieram do HomeFragment
        long ofertaId = getIntent().getLongExtra("produto_id", -1);
        String imagemUrl = getIntent().getStringExtra("imagem_url");

        if (ofertaId == -1) {
            finish();
            return;
        }

        // 2. ESTRATÉGIA NINJA: Carrega a imagem imediatamente, sem esperar a API!
        Glide.with(this)
                .load(imagemUrl != null && !imagemUrl.isEmpty() ? imagemUrl : null)
                .placeholder(R.drawable.hortlink_logo)
                .error(R.drawable.hortlink_logo)
                .centerCrop()
                .into(imgProduto);

        // 3. Busca os textos pesados na API
        carregarOferta(ofertaId);
    }

    // ─── 1. Busca oferta detalhada na API ────────────────────────────
    private void carregarOferta(Long ofertaId) {
        ofertaRepository.buscarOfertaDetalhadaPorId(ofertaId, new BaseCallback<DetalheOfertaDTO>() {

            @Override
            public void onSuccess(DetalheOfertaDTO detalhe) {
                if (detalhe == null) {
                    finish();
                    return;
                }

                // 1. Preenche os dados da Oferta
                txtNome.setText(detalhe.getNome());
                txtPreco.setText(String.format("R$ %.2f", detalhe.getValor()));
                txtDescricao.setText(detalhe.getDescricao());

                // 2. Preenche os dados do Produtor (Tudo na mesma requisição!)
                txtNomeProd.setText(detalhe.getNomeProdutor());
                txtCidadeProd.setText(detalhe.getCidadeUf());
                txtContatoProd.setText(detalhe.getTelefone());

                // Libera o botão de carrinho
                btnCarrinho.setEnabled(true);
                btnCarrinho.setOnClickListener(v -> adicionarAoCarrinho(detalhe.getId()));


                cardProdutor.setOnClickListener(v -> {
                    Intent intent = new Intent(DetalheProdutoActivity.this, PerfilProdutorActivity.class);
                    intent.putExtra("comercio_id", detalhe.getComercioId());
                    startActivity(intent);
                });

            }

            @Override
            public void onError(String erro) {
                Toast.makeText(DetalheProdutoActivity.this, "Erro: " + erro, Toast.LENGTH_LONG).show();
            }
        });
    }

    // ─── 2. Adiciona ao carrinho (Via Spring Boot) ─────────────────────
    private void adicionarAoCarrinho(Long idOferta) {
        btnCarrinho.setEnabled(false);

        // ATENÇÃO: Você não precisa mais pegar o ID do usuário (Firebase).
        // O seu Spring Boot vai saber quem está adicionando ao carrinho através do token JWT!

        /* // Exemplo de como ficará quando você criar o CarrinhoRepository:
        carrinhoRepository.adicionarItem(idOferta, 1, new BaseCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                btnCarrinho.setEnabled(true);
                Toast.makeText(DetalheProdutoActivity.this, "Adicionado ao carrinho ✓", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String erro) {
                btnCarrinho.setEnabled(true);
                Toast.makeText(DetalheProdutoActivity.this, "Erro ao adicionar: " + erro, Toast.LENGTH_LONG).show();
            }
        });
        */

        // Temporário até você implementar o endpoint do carrinho no Spring:
        Toast.makeText(this, "Funcionalidade de carrinho em construção para a nova API", Toast.LENGTH_SHORT).show();
        btnCarrinho.setEnabled(true);
    }
}