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
import com.example.hortlink.data.dto.ComercioDTO;
import com.example.hortlink.data.model.OfertaDTO;
import com.example.hortlink.data.repository.ComercioRepository;
import com.example.hortlink.data.repository.OfertaRepository;
import com.example.hortlink.entidades.BaseCallback;

import java.util.List;

public class PerfilProdutorActivity extends AppCompatActivity {

    private TextView txtNome, txtAvaliacao, txtCidade, txtContato;
    private ImageView imgFazenda;
    private RecyclerView recyclerProdutosPerfil;

    // Instanciando os novos Repositórios
    private final OfertaRepository ofertaRepository = new OfertaRepository();
    private final ComercioRepository comercioRepository = new ComercioRepository();

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

        // Binds
        txtNome      = findViewById(R.id.txtNomeProd);
        txtAvaliacao = findViewById(R.id.txtAvaliacao);
        txtCidade    = findViewById(R.id.txtCidadeProd);
        txtContato   = findViewById(R.id.txtContatoProd);
        imgFazenda   = findViewById(R.id.imgFazenda);
        recyclerProdutosPerfil = findViewById(R.id.recyclerProdutosPerfil);

        recyclerProdutosPerfil.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        // Recebe o ID do comércio passado pela tela de Detalhes
        long comercioId = getIntent().getLongExtra("comercio_id", -1);
        if (comercioId == -1) {
            finish();
            return;
        }

        // Chama as duas requisições na API
        carregarPerfilProdutor(comercioId);
        carregarProdutosDoProdutor(comercioId);
    }

    // ─── Dados do produtor ───────────────────────────────────────
    private void carregarPerfilProdutor(Long comercioId) {
        comercioRepository.buscarPorId(comercioId, new BaseCallback<ComercioDTO>() {
            @Override
            public void onSuccess(ComercioDTO comercio) {
                txtNome.setText(comercio.getNome());
                txtCidade.setText(comercio.getCidade());
                txtContato.setText(comercio.getTelefone());

                // Formata a avaliação para ter apenas 1 casa decimal (ex: "4.5")
                txtAvaliacao.setText(String.valueOf(comercio.getAvaliacao()));

                // Carrega a foto do comércio (se houver)
                String fotoUrl = comercio.getImg_url();
                Glide.with(PerfilProdutorActivity.this)
                        .load(fotoUrl != null && !fotoUrl.isEmpty() ? fotoUrl : null)
                        .placeholder(R.drawable.hortlink_logo)
                        .error(R.drawable.hortlink_logo)
                        .circleCrop()
                        .into(imgFazenda);
            }

            @Override
            public void onError(String erro) {
                Toast.makeText(PerfilProdutorActivity.this, "Erro: " + erro, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─── Produtos do produtor na lista horizontal ────────────────
    private void carregarProdutosDoProdutor(Long comercioId) {
        ofertaRepository.buscarOfertasPorComercioId(comercioId, new BaseCallback<List<OfertaDTO>>() {
            @Override
            public void onSuccess(List<OfertaDTO> lista) {

                // Cria o adapter passando a lista pronta
                ProdutoAdapter adapter = new ProdutoAdapter(lista, produto -> {
                    Intent intent = new Intent(PerfilProdutorActivity.this, DetalheProdutoActivity.class);

                    // Passa o ID da oferta clicada
                    intent.putExtra("produto_id", produto.getId());

                    // Aplica a nossa estratégia Ninja: manda a foto para carregar instantaneamente
                    intent.putExtra("imagem_url", produto.getImagemUri());

                    startActivity(intent);
                });

                recyclerProdutosPerfil.setAdapter(adapter);
            }

            @Override
            public void onError(String erro) {
                // Falha silenciosa ou avisa o usuário
                Toast.makeText(PerfilProdutorActivity.this, "Erro ao buscar produtos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}