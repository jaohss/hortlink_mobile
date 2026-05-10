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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

public class Cadastro extends AppCompatActivity {

    EditText emailEdt, nomeEdt, passwordEdt, confirmPassEdt, telefoneEdt;
    Button registerButton;
    ProgressBar progressBar;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    SupabaseHelper supabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        supabase = new SupabaseHelper(this);

        emailEdt       = findViewById(R.id.email);
        nomeEdt        = findViewById(R.id.nomeText);
        passwordEdt    = findViewById(R.id.password);
        confirmPassEdt = findViewById(R.id.confirmPass);
        telefoneEdt    = findViewById(R.id.telefone);
        registerButton = findViewById(R.id.registerButton);
        progressBar    = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        registerButton.setOnClickListener(v -> validarECadastrar());
    }

    private void validarECadastrar() {
        String nome    = nomeEdt.getText().toString().trim();
        String email   = emailEdt.getText().toString().trim();
        String senha   = passwordEdt.getText().toString();
        String confirma = confirmPassEdt.getText().toString();
        String telefone = telefoneEdt.getText().toString().trim();

        // Validações
        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()
                || confirma.isEmpty() || telefone.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!senha.equals(confirma)) {
            Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (senha.length() < 6) {
            Toast.makeText(this, "Senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pergunta o tipo de conta antes de criar
        new MaterialAlertDialogBuilder(this)
                .setTitle("Tipo de conta")
                .setMessage("Como você vai usar o HortLink?")
                .setPositiveButton("🛒 Sou comprador", (d, w) ->
                        criarConta(nome, email, senha, telefone, "comprador"))
                .setNegativeButton("🌱 Sou produtor", (d, w) ->
                        criarConta(nome, email, senha, telefone, "produtor"))
                .setCancelable(false)
                .show();
    }

    private void criarConta(String nome, String email, String senha,
                            String telefone, String tipo) {
        progressBar.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);

        // 1º — cria no Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);
                        registerButton.setEnabled(true);
                        Toast.makeText(this,
                                task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    String uid = task.getResult().getUser().getUid();

                    // 2º — salva perfil no Supabase
                    supabase.salvarUsuario(uid, nome, email, telefone, tipo,
                            new SupabaseHelper.SupabaseCallback() {

                                @Override
                                public void onSuccess(String r) {
                                    runOnUiThread(() -> {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(Cadastro.this,
                                                "Conta criada com sucesso!", Toast.LENGTH_SHORT).show();

                                        if (tipo.equals("produtor")) {
                                            // Produtor completa o perfil na próxima tela
                                            Intent intent = new Intent(Cadastro.this,
                                                    CompletarPerfilProdutorActivity.class);
                                            intent.putExtra("uid", uid);
                                            intent.putExtra("nome", nome);
                                            startActivity(intent);
                                        } else {
                                            // Comprador vai direto para a home
                                            startActivity(new Intent(Cadastro.this, MainActivity.class));
                                        }
                                        finish();
                                    });
                                }

                                @Override
                                public void onError(String erro) {
                                    runOnUiThread(() -> {
                                        progressBar.setVisibility(View.GONE);
                                        registerButton.setEnabled(true);
                                        // Firebase criou mas Supabase falhou — avisa mas deixa continuar
                                        Toast.makeText(Cadastro.this,
                                                "Conta criada, mas erro ao salvar perfil: " + erro,
                                                Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(Cadastro.this, MainActivity.class));
                                        finish();
                                    });
                                }
                            });
                });
    }
}