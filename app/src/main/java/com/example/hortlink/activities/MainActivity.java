package com.example.hortlink.activities;

import android.content.Intent;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {
    //Tela de LOGIN
    EditText username;
    EditText password;
    TextView cadastroText;
    Button loginButton;
    BancoHelper databaseHelper;


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


        loginButton.setOnClickListener(v -> {
            String email = username.getText().toString();
            String senha = password.getText().toString();
            boolean autenticado = databaseHelper.autenticar(email, senha);

            if (autenticado) {

                // ir para próxima tela
                Intent intent = new Intent(MainActivity.this, Homec.class);
                startActivity(intent);
                Toast.makeText(this, "Login realizado!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Email ou senha inválidos!", Toast.LENGTH_SHORT).show();
            }

        });

        cadastroText.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Cadastro.class);
            startActivity(intent);
        });




    }
}