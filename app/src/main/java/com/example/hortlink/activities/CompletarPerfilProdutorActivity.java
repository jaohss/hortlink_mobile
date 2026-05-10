package com.example.hortlink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hortlink.R;
import com.example.hortlink.bd.SupabaseHelper;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CompletarPerfilProdutorActivity extends AppCompatActivity {

    EditText edtCidade, edtContato, edtDescricao;
    Button btnConcluir, btnPular;
    ProgressBar progressBar;
    SupabaseHelper supabase;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_completar_perfil_produtor);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        uid      = getIntent().getStringExtra("uid");
        supabase = new SupabaseHelper(this);

        edtCidade   = findViewById(R.id.edtCidade);
        edtContato  = findViewById(R.id.edtContato);
        edtDescricao = findViewById(R.id.edtDescricaoProd);
        btnConcluir = findViewById(R.id.btnConcluir);
        btnPular = findViewById(R.id.btnPular);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        btnConcluir.setOnClickListener(v -> salvarPerfilProdutor());

        btnPular.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void salvarPerfilProdutor() {
        String cidade   = edtCidade.getText().toString().trim();
        String contato  = edtContato.getText().toString().trim();
        String descricao = edtDescricao.getText().toString().trim();

        if (cidade.isEmpty() || contato.isEmpty()) {
            Toast.makeText(this, "Preencha cidade e contato", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnConcluir.setEnabled(false);

        // PATCH — atualiza apenas os campos do produtor
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("cidade",    cidade);
                json.put("contato",   contato);
                json.put("descricao", descricao);

                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(
                        json.toString(), MediaType.parse("application/json"));

                // Pega a URL e key do seu SupabaseHelper
                String url = "https://dzfbtevidnfarlpnfysd.supabase.co/rest/v1/usuarios?id=eq." + uid;
                String key  = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImR6ZmJ0ZXZpZG5mYXJscG5meXNkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzgyNTcwNzMsImV4cCI6MjA5MzgzMzA3M30.79uc9zT_T-HPoUhJMMyUMKsW4qS2kiCHuuBcpmv3sDQ";

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + key)
                        .addHeader("apikey", key)
                        .addHeader("Content-Type", "application/json")
                        .patch(body)
                        .build();

                Response response = client.newCall(request).execute();

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        Toast.makeText(this, "Perfil completo!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        btnConcluir.setEnabled(true);
                        Toast.makeText(this, "Erro ao salvar perfil", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnConcluir.setEnabled(true);
                    Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}