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
import com.example.hortlink.data.model.Usuario;
import com.example.hortlink.data.repository.UsuarioRepository;
import com.example.hortlink.util.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

public class Cadastro extends AppCompatActivity {

    EditText emailEdt, nomeEdt, passwordEdt, confirmPassEdt;
    Button registerButton;
    ProgressBar progressBar;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private UsuarioRepository usuarioRepository = new UsuarioRepository();
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


        emailEdt       = findViewById(R.id.email);
        nomeEdt        = findViewById(R.id.nomeText);
        passwordEdt    = findViewById(R.id.password);
        confirmPassEdt = findViewById(R.id.confirmPass);
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

        // Validações
        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()
                || confirma.isEmpty()) {
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
                        criarConta(nome, email, senha, Usuario.TIPO_COMPRADOR))
                .setNegativeButton("🌱 Sou produtor", (d, w) ->
                        criarConta(nome, email, senha, Usuario.TIPO_PRODUTOR))
                .setCancelable(false)
                .show();
    }

    private void criarConta(String nome, String email, String senha, String tipo) {
        setCarregando(true);

        usuarioRepository.cadastrar(nome, email, senha, tipo, new UsuarioRepository.Callback() {
            @Override
            public void onSuccess(Usuario usuario) {
                // Salva na sessão imediatamente após cadastro
                SessionManager.getInstance().setUsuario(usuario);

                runOnUiThread(() -> {
                    setCarregando(false);
                    Toast.makeText(Cadastro.this,
                                    "Conta criada com sucesso!", Toast.LENGTH_SHORT).show();

                    Intent intent;
                    if (usuario.isProdutor()) {
                                // Produtor completa o perfil antes de entrar na home
                         intent = new Intent(Cadastro.this, CompletarPerfilProdutorActivity.class);
                    } else {
                         intent = new Intent(Cadastro.this, CompletarPerfilCompradorActivity.class);
                    }

                    // uid passado explicitamente — a Activity não deve depender
                    // do SessionManager para uma informação que já temos aqui
                    intent.putExtra("uid", usuario.id);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(String erro) {
                runOnUiThread(() -> {
                    setCarregando(false);
                    Toast.makeText(Cadastro.this, "Erro ao criar conta: " + erro, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void setCarregando(boolean carregando) {
        progressBar.setVisibility(carregando ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!carregando);
    }

}