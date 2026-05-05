package com.example.hortlink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hortlink.BancoHelper;
import com.example.hortlink.R;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    //Tela de LOGIN
    EditText username;
    EditText password;
    TextView cadastroText, resetPass;
    Button loginButton;
    BancoHelper databaseHelper;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();


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

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        cadastroText = findViewById(R.id.cadastroText);
        databaseHelper = new BancoHelper(this);
        resetPass = findViewById(R.id.resetPass);


        loginButton.setOnClickListener(v -> {
            String email = username.getText().toString().trim();
            String senha = password.getText().toString().trim();

            mAuth.signInWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            Intent intent = new Intent(MainActivity.this, Homec.class);
                            Toast.makeText(this, "Login realizado com Sucesso", Toast.LENGTH_SHORT).show();
                            startActivity(intent);
                            finish();

                        } else {
                            Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        resetPass.setOnClickListener(v -> {
            String email = username.getText().toString().trim();

            if (!email.isEmpty()) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Verifique seu email", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Erro ao enviar email", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        cadastroText.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Cadastro.class);
            startActivity(intent);
        });

    }
}