package com.example.hortlink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.example.hortlink.data.repository.UsuarioRepository;
import com.example.hortlink.util.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    //Tela de LOGIN
    EditText username,password;
    TextView cadastroText, resetPass;
    Button loginButton;
    ProgressBar progressBar;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    //Dependências
    private UsuarioRepository usuarioRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usuarioRepository = new UsuarioRepository();

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        cadastroText = findViewById(R.id.cadastroText);
        resetPass = findViewById(R.id.resetPass);
        progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.GONE);


        loginButton.setOnClickListener(v -> realizarLogin());

        resetPass.setOnClickListener(v -> enviarResetSenha());

        cadastroText.setOnClickListener(v -> startActivity(new Intent(this, Cadastro.class)));

    }

    private void realizarLogin(){
        String email = username.getText().toString().trim();
        String senha = password.getText().toString().trim();

        Log.d("LOGIN", "email: '" + email + "' | senha length: " + senha.length());
        // ...

        if(email.isEmpty() || senha.isEmpty()){
            Toast.makeText(this, "Preencha email e senha", Toast.LENGTH_SHORT).show();
            return;
        }

        setCarregando(true);

        usuarioRepository.login(email, senha, new UsuarioRepository.Callback() {
            @Override
            public void onSuccess(Usuario usuario) {
                SessionManager.getInstance().setUsuario(usuario);

                runOnUiThread(() -> {
                    setCarregando(false);
                    Toast.makeText(MainActivity.this, "Bem-vindo, " + usuario.nome + " !", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, Homec.class));
                    finish();
                });
            }

            @Override
            public void onError(String erro) {
                runOnUiThread(() -> {
                    setCarregando(false);
                    Toast.makeText(MainActivity.this, erro, Toast.LENGTH_SHORT).show();
                });

            }
        });
    }

    private void enviarResetSenha(){
        String email = username.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Digite seu email primeiro", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Verifique seu email", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Erro ao enviar email", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setCarregando(boolean carregando) {
        progressBar.setVisibility(carregando ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!carregando);
    }


}