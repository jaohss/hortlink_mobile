package com.example.hortlink.activities;

import android.database.Cursor;
import android.os.Bundle;
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

import java.util.ArrayList;

public class Cadastro extends AppCompatActivity {

    EditText emailEdt, nomeEdt, passwordEdt, confirmPassEdt, telefoneEdt;
    Button registerButton;
    ArrayAdapter<String> adapter;
    ArrayList<String> listaUsuarios;
    ArrayList<Integer> listaIds;
    ListView listViewUsuarios;

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
                    //VALIDAÇÃO DAS SENHAS
                    if (!senha.equals(confirmaSenha)) {
                        Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show();
                        confirmPassEdt.setError("As senhas não coincidem");
                        confirmPassEdt.requestFocus();
                        return;

                    }

                    long resultado = databaseHelper.inserirUsuario(nome, email, senha, telefone);
                    if (resultado != -1) {
                        Toast.makeText(this, "Usuário salvo!", Toast.LENGTH_SHORT).show();
                        nomeEdt.setText("");
                        emailEdt.setText("");
                        telefoneEdt.setText("");
                        carregarUsuarios();
                    } else {
                        Toast.makeText(this, "Erro ao salvar!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                }
            });

        }
        catch(Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

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