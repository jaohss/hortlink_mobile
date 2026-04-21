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

import com.example.hortlink.R;

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
        //produto
        String nome = getIntent().getStringExtra("nome");
        double preco = getIntent().getDoubleExtra("preco", 0.0);
        int imagem = getIntent().getIntExtra("imagem",0);
        String descricao = getIntent().getStringExtra("descricao");

        //produtor
        String produtorNome = getIntent().getStringExtra("produtor_nome");
        String produtorCidade = getIntent().getStringExtra("produtor_cidade");
        String produtorContato = getIntent().getStringExtra("produtor_contato");
        int fotoProd = getIntent().getIntExtra("produtor_foto",0);
        int fotoCapa = getIntent().getIntExtra("produtor_fotoC",0);
        double produtorAvaliacao = getIntent().getDoubleExtra("produtor_avaliaco",0.0);

        //detalhes produto
        TextView txtNome = findViewById(R.id.txtNome);
        TextView txtPreco = findViewById(R.id.txtPreco);
        ImageView imageView = findViewById(R.id.imgProduto);
        Button btnCarrinho = findViewById(R.id.btnCarrinho);
        Button btnVoltar = findViewById(R.id.btnVoltar);
        TextView txtDescricao = findViewById(R.id.txtDescricao);

        txtNome.setText(nome);
        txtPreco.setText("R$"+preco);
        imageView.setImageResource(imagem);
        txtDescricao.setText(descricao);

        //detalhes produtor
        TextView txtNomeProd = findViewById(R.id.txtNomeProd);
        TextView txtCidadeProd = findViewById(R.id.txtCidadeProd);
        TextView txtContatoProd = findViewById(R.id.txtContatoProd);
        ImageView imgFotoProd = findViewById(R.id.fotoPerfil);
        TextView txtAvaliaco = findViewById(R.id.txtAvaliacao);

        txtNomeProd.setText(produtorNome);
        txtContatoProd.setText(produtorContato);
        txtCidadeProd.setText(produtorCidade);
        imgFotoProd.setImageResource(fotoProd);
        txtAvaliaco.setText("Avaliação"+produtorAvaliacao);

        btnVoltar.setOnClickListener(v ->{
            finish();
        });

        ConstraintLayout cardProdutor;
        cardProdutor = findViewById(R.id.cardProdutor);

        //Ação ao clicar no card do produtor
        cardProdutor.setOnClickListener(v -> {
            Intent intent = new Intent(DetalheProdutoActivity.this, PerfilProdutorActivity.class);
            intent.putExtra("produtor_nome", produtorNome);
            intent.putExtra("produtor_cidade", produtorCidade);
            intent.putExtra("produtor_contato", produtorContato);
            intent.putExtra("produtor_foto", fotoProd);
            intent.putExtra("produtor_avaliacao", produtorAvaliacao);
            intent.putExtra("foto_capa", fotoCapa);
            startActivity(intent);
        });

    }
}