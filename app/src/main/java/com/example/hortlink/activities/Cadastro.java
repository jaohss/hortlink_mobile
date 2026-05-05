package com.example.hortlink.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hortlink.BancoHelper;
import com.example.hortlink.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class Cadastro extends AppCompatActivity {

    EditText emailEdt, nomeEdt, passwordEdt, confirmPassEdt, telefoneEdt;
    Button registerButton;
    ArrayAdapter<String> adapter;
    ArrayList<String> listaUsuarios;
    ArrayList<Integer> listaIds;
    ListView listViewUsuarios;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    BancoHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        try{
            emailEdt = findViewById(R.id.email);
            nomeEdt = findViewById(R.id.nomeText);
            passwordEdt = findViewById(R.id.password);
            confirmPassEdt = findViewById(R.id.confirmPass);
            telefoneEdt = findViewById(R.id.telefone);
            registerButton = findViewById(R.id.registerButton);
            databaseHelper = new BancoHelper(this);

            registerButton.setOnClickListener(v -> {
                String nome = nomeEdt.getText().toString();
                String email =emailEdt.getText().toString();
                String senha = passwordEdt.getText().toString();
                String confirmaSenha = confirmPassEdt.getText().toString();
                String telefone = telefoneEdt.getText().toString();

                if (!nome.isEmpty() && !email.isEmpty() && !senha.isEmpty() && !confirmaSenha.isEmpty() && !telefone.isEmpty()) {

                    if (!senha.equals(confirmaSenha)) {
                        Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (senha.length() < 6) {
                        Toast.makeText(this, "Senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //FIREBASE
                    mAuth.createUserWithEmailAndPassword(email, senha)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {

                                    // 🟢 SQLite só depois
                                    long resultado = databaseHelper.inserirUsuario(nome, email, senha, telefone);

                                    if (resultado != -1) {
                                        Toast.makeText(this, "Usuário criado!", Toast.LENGTH_SHORT).show();
                                        carregarUsuarios();
                                    } else {
                                        Toast.makeText(this, "Salvo no Firebase, mas erro local", Toast.LENGTH_SHORT).show();
                                    }

                                } else {
                                    Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });

                } else {
                    Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                }


            });

        }
        catch(Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    //Carrega os usuarios no banco helper
    private void carregarUsuarios() {
        Cursor cursor = databaseHelper.listarUsuarios();
        listaUsuarios = new ArrayList<>();
        listaIds = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String nome = cursor.getString(1);
                String email = cursor.getString(2);
                listaUsuarios.add(id + " - " + nome + " - " + email);
                listaIds.add(id);
            } while (cursor.moveToNext());
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaUsuarios);
        listViewUsuarios.setAdapter(adapter);
    }
}