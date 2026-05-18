package com.example.hortlink.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hortlink.ui.consumidor.Homec;
import com.example.hortlink.ui.produtor.HomeProdutorActivity;
import com.example.hortlink.util.SessionManager;

public class RoleRouterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sem layout — só redireciona

        if (!SessionManager.getInstance().estaLogado()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        Class<?> destino = SessionManager.getInstance().isProdutor()
                ? HomeProdutorActivity.class
                : Homec.class;

        startActivity(new Intent(this, destino));
        finish();
    }
}