package com.example.hortlink.ui.consumidor;

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
// Substituído ProdutoAdapter por OfertaAdapter
import com.example.hortlink.adapters.OfertaAdapter;
import com.example.hortlink.data.dto.ComercioDTO;
import com.example.hortlink.data.model.OfertaDTO;
import com.example.hortlink.data.repository.ComercioRepository;
import com.example.hortlink.data.repository.OfertaRepository;
import com.example.hortlink.service.BaseCallback;

import java.util.List;

public class PerfilProdutorActivity extends AppCompatActivity {

    private TextView txtNome, txtCidade, txtContato, txtDescricao, txtAvaliacao;
    private ImageView imgFazenda;
    private RecyclerView recyclerProdutosPerfil;

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
        txtCidade    = findViewById(R.id.txtCidadeProd);
        txtContato   = findViewById(R.id.txtContatoProd);
        txtDescricao = findViewById(R.id.txtDescricao);
        txtAvaliacao = findViewById(R.id.txtAvaliacao); // Agora existe no XML!
        imgFazenda   = findViewById(R.id.imgFazenda);

        recyclerProdutosPerfil = findViewById(R.id.recyclerProdutosPerfil);
        recyclerProdutosPerfil.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        long comercioId = getIntent().getLongExtra("comercio_id", -1);
        if (comercioId == -1) {
            finish();
            return;
        }

        carregarPerfilProdutor(comercioId);
        carregarProdutosDoProdutor(comercioId);
    }

    private void carregarPerfilProdutor(Long comercioId) {
        comercioRepository.obterDadosComercio(comercioId, new BaseCallback<ComercioDTO>() {
            @Override
            public void onSuccess(ComercioDTO comercio) {
                txtNome.setText(comercio.getNome());
                txtCidade.setText(comercio.getCidade());
                txtContato.setText(comercio.getTelefone());

                // Prevenção contra nulos na avaliação
                txtAvaliacao.setText(comercio.getAvaliacao() != null ? comercio.getAvaliacao() : "Novo");

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

    private void carregarProdutosDoProdutor(Long comercioId) {
        ofertaRepository.buscarOfertasPorComercioId(comercioId, new BaseCallback<List<OfertaDTO>>() {
            @Override
            public void onSuccess(List<OfertaDTO> lista) {

                // Utilizando OfertaAdapter e tratando o objeto corretamente como 'oferta'
                OfertaAdapter adapter = new OfertaAdapter(lista, oferta -> {
                    Intent intent = new Intent(PerfilProdutorActivity.this, DetalheProdutoActivity.class);

                    // A tela de detalhes precisa do ID da oferta, não do produto genérico
                    intent.putExtra("oferta_id", oferta.getId());

                    // Puxando a URL da imagem da oferta
                    intent.putExtra("imagem_url", oferta.getImagemUrl());

                    startActivity(intent);
                });

                recyclerProdutosPerfil.setAdapter(adapter);
            }

            @Override
            public void onError(String erro) {
                Toast.makeText(PerfilProdutorActivity.this, "Erro ao buscar produtos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}