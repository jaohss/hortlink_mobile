package com.example.hortlink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hortlink.BancoHelper;
import com.example.hortlink.R;
import com.example.hortlink.entidades.Produto;
import com.example.hortlink.entidades.Produtor;

public class DetalheProdutoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalhe_produto);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Busca produto no banco pelo ID
        int produtoId = getIntent().getIntExtra("produto_id", -1);
        BancoHelper db = new BancoHelper(this);
        Produto produto = db.buscarProdutoPorId(produtoId);

        if (produto == null) {
            finish();
            return;
        }

        // Views do produto
        TextView txtNome = findViewById(R.id.txtNome);
        TextView txtPreco = findViewById(R.id.txtPreco);
        TextView txtDescricao = findViewById(R.id.txtDescricao);
        ImageView imgProduto = findViewById(R.id.imgProduto);
        Button btnVoltar = findViewById(R.id.btnVoltar);

        txtNome.setText(produto.nome);
        txtPreco.setText("R$ " + produto.preco);
        txtDescricao.setText(produto.descricao);

        if (produto.imagemUri != null && !produto.imagemUri.isEmpty()) {
            imgProduto.setImageURI(android.net.Uri.parse(produto.imagemUri));
            if (imgProduto.getDrawable() == null) {
                imgProduto.setImageResource(R.drawable.hortlink_logo);
            }
        } else {
            imgProduto.setImageResource(R.drawable.hortlink_logo);
        }


        // Busca produtor no banco pelo ID
        Produtor produtor = db.buscarProdutorPorId(produto.produtorId);

        // Views do produtor
        TextView txtNomeProd = findViewById(R.id.txtNomeProd);
        TextView txtCidadeProd = findViewById(R.id.txtCidadeProd);
        TextView txtContatoProd = findViewById(R.id.txtContatoProd);
        TextView txtAvaliacao = findViewById(R.id.txtAvaliacao);
        ImageView fotoPerfil = findViewById(R.id.fotoPerfil);



        if (produtor != null) {
            txtNomeProd.setText(produtor.nome);
            txtCidadeProd.setText(produtor.cidade);
            txtContatoProd.setText(produtor.contato);
            txtAvaliacao.setText("Avaliação: " + produtor.avaliacao);

            if (produtor.fotoPerfilUri != null && !produtor.fotoPerfilUri.isEmpty()) {
                fotoPerfil.setImageURI(android.net.Uri.parse(produtor.fotoPerfilUri));
                if (fotoPerfil.getDrawable() == null) {
                    fotoPerfil.setImageResource(R.drawable.hortlink_logo);
                }
            } else {
                fotoPerfil.setImageResource(R.drawable.hortlink_logo);
            }
        }

        btnVoltar.setOnClickListener(v -> finish());

        // Navega para perfil do produtor
        ConstraintLayout cardProdutor = findViewById(R.id.cardProdutor);
        cardProdutor.setOnClickListener(v -> {
            if (produtor == null) return;
            Intent intent = new Intent(this, PerfilProdutorActivity.class);
            intent.putExtra("produtor_id", produtor.id);


            startActivity(intent);
        });

    }
}