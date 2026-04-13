package com.example.hortlink;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

        String nome = getIntent().getStringExtra("nome");
        Double preco = getIntent().getDoubleExtra("preco", 0.0);
        int imagem = getIntent().getIntExtra("imagem",0);
        String descricao = getIntent().getStringExtra("descricao");

        TextView txtNome = findViewById(R.id.txtNome);
        TextView txtPreco = findViewById(R.id.txtPreco);
        ImageView imageView = findViewById(R.id.imgProduto);
        Button btnCarrinho = findViewById(R.id.btnCarrinho);
        Button btnVoltar = findViewById(R.id.btnVoltar);
        TextView txtDescricao = findViewById(R.id.txtDescricao);

        txtNome.setText(nome);
        txtPreco.setText("R$ "+preco);
        imageView.setImageResource(imagem);
        txtDescricao.setText(descricao);

        btnVoltar.setOnClickListener(v ->{
            finish();
        });
    }
}