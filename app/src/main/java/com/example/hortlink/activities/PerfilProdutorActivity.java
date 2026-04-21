package com.example.hortlink.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hortlink.R;

public class PerfilProdutorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil_produtor);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String nome = getIntent().getStringExtra("produtor_nome");
        String cidade = getIntent().getStringExtra("produtor_cidade");
        String contato = getIntent().getStringExtra("produtor_contato");
        int foto = getIntent().getIntExtra("produtor_foto",0);
        int fotoC = getIntent().getIntExtra("foto_capa",0);
        double avaliacao = getIntent().getDoubleExtra("produtor_avaliacao",0.0);

        TextView txtNomeProd = findViewById(R.id.txtNomeProd);
        TextView txtAvaliacao = findViewById(R.id.txtAvaliacao);
        TextView txtCidade = findViewById(R.id.txtCidadeProd);
        //TextView txtDesc = findViewById(R.id.txtDescricao);
        TextView txtContato = findViewById(R.id.txtContatoProd);
        Button btnMais = findViewById(R.id.maisProd);
        ImageView fotoPerfil = findViewById(R.id.imgFotoPerfil);
        ImageView fotoCapa = findViewById(R.id.imgCapa);

        txtNomeProd.setText(nome);
        txtCidade.setText(cidade);
        txtAvaliacao.setText("Avaliação"+avaliacao);
        txtContato.setText(contato);



    }
}