package com.example.hortlink.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hortlink.data.dto.RegistroDTO;
import com.example.hortlink.data.repository.AuthRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class Cadastro extends AppCompatActivity {

    private EditText emailEdt, nomeEdt, passwordEdt, confirmPassEdt, telefoneEdt;
    ImageButton btnVoltar;
    private Button registerButton;
    private ProgressBar progressBar;

    private AuthRepository authRepository;

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

        // Inicializa o Repositório Limpo
        authRepository = new AuthRepository();

        emailEdt       = findViewById(R.id.email);
        nomeEdt        = findViewById(R.id.nomeText);
        passwordEdt    = findViewById(R.id.password);
        confirmPassEdt = findViewById(R.id.confirmPass);
        telefoneEdt    = findViewById(R.id.telefone);
        registerButton = findViewById(R.id.registerButton);
        progressBar    = findViewById(R.id.progressBar);
        btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());

        progressBar.setVisibility(View.GONE);

        registerButton.setOnClickListener(v -> validarECadastrar());
    }

    private void validarECadastrar() {
        String nome     = nomeEdt.getText().toString().trim();
        String email    = emailEdt.getText().toString().trim();
        String senha    = passwordEdt.getText().toString();
        String confirma = confirmPassEdt.getText().toString();
        String telefone = telefoneEdt.getText().toString().trim();

        // Validações
        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confirma.isEmpty() || telefone.isEmpty()) {
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

        String[] opcoesDePerfil = {
                "🛒 Sou Consumidor (Comprar produtos)",
                "🌱 Sou Produtor (Cultivo e vendo no atacado/varejo)",
                "🏪 Sou Comércio (Tenho quitanda/mercado)"
        };

        String[] rolesParaEnviar = {"CONSUMIDOR", "PRODUTOR", "COMERCIO"};

        new MaterialAlertDialogBuilder(this)
                .setTitle("Como você vai usar o HortLink?")
                .setItems(opcoesDePerfil, (dialog, qualItemFoiClicado) -> {
                    String roleSelecionada = rolesParaEnviar[qualItemFoiClicado];
                    criarConta(nome, email, senha, telefone, roleSelecionada);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void criarConta(String nome, String email, String senha, String telefone, String role) {
        setCarregando(true);

        // Monta o DTO com os dados da tela
        RegistroDTO novoUsuario = new RegistroDTO(nome, email, senha, role, telefone);

        // Chama a API Spring Boot
        authRepository.register(novoUsuario, new BaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                setCarregando(false);
                Toast.makeText(Cadastro.this, "Conta criada! Faça login para continuar.", Toast.LENGTH_LONG).show();

                // Manda para a tela de Login.
                // Ao logar, a própria MainActivity vai ler o token e decidir se manda para a Home ou para Completar Perfil!
                startActivity(new Intent(Cadastro.this, com.example.hortlink.activities.MainActivity.class));
                finish();
            }

            @Override
            public void onError(String erro) {
                setCarregando(false);
                Toast.makeText(Cadastro.this, "Erro ao criar conta: " + erro, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setCarregando(boolean carregando) {
        progressBar.setVisibility(carregando ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!carregando);
        nomeEdt.setEnabled(!carregando);
        emailEdt.setEnabled(!carregando);
        passwordEdt.setEnabled(!carregando);
        confirmPassEdt.setEnabled(!carregando);
        telefoneEdt.setEnabled(!carregando);
    }
}