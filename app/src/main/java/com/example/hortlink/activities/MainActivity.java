package com.example.hortlink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hortlink.R;
import com.example.hortlink.data.model.Usuario;
import com.example.hortlink.data.repository.AuthRepository;
import com.example.hortlink.entidades.BaseCallback;
import com.example.hortlink.util.SessionManager;

public class MainActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private TextView cadastroText, resetPass;
    private Button loginButton;
    private ProgressBar progressBar;

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ─── AUTO-LOGIN (Verifica se já tem token salvo) ───
        if (SessionManager.getInstance().getToken() != null) {
            finish();
            return; // Impede que a tela de login carregue atoa
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Binds
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        cadastroText = findViewById(R.id.cadastroText);
        resetPass = findViewById(R.id.resetPass);
        progressBar = findViewById(R.id.progressBar2);

        progressBar.setVisibility(View.GONE);
        authRepository = new AuthRepository(); // Super limpo, sem Context!

        // ─── AÇÃO DO BOTÃO DE LOGIN ───
        loginButton.setOnClickListener(v -> {
            String email = username.getText().toString().trim();
            String senha = password.getText().toString().trim();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show();
                return;
            }

            setCarregando(true); // Trava a tela e mostra a bolinha rodando

            authRepository.login(email, senha, new BaseCallback<Usuario>() {
                @Override
                public void onSuccess(Usuario usuarioLogado) {
                    setCarregando(false);

                    finish(); // Fecha a tela de login
                }

                @Override
                public void onError(String erro) {
                    setCarregando(false);
                    Toast.makeText(MainActivity.this, erro, Toast.LENGTH_LONG).show();
                }
            });
        });

        // ─── RECUPERAR SENHA ───
        resetPass.setOnClickListener(v -> {
            String email = username.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Digite seu e-mail no campo acima para redefinir a senha.", Toast.LENGTH_LONG).show();
            } else {
                // Em breve você criará esse endpoint na sua API Spring!
                Toast.makeText(this, "Recuperação de senha pela nova API em construção...", Toast.LENGTH_SHORT).show();
            }
        });

        // ─── IR PARA CADASTRO ───
        cadastroText.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Cadastro.class);
            startActivity(intent);
        });
    }

    // ─── MÉTODO AUXILIAR PARA CONTROLAR O VISUAL DA TELA ───
    private void setCarregando(boolean carregando) {
        progressBar.setVisibility(carregando ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!carregando);
        username.setEnabled(!carregando);
        password.setEnabled(!carregando);
        cadastroText.setEnabled(!carregando);
    }
}