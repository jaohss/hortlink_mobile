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

        SessionManager session = SessionManager.getInstance();

        // 1. Barreira de Segurança: Não está logado? Volta pro Login.
        if (!session.estaLogado()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // 2. Regra de Cadastro Incompleto: Trava o usuário até ele preencher tudo
        if (session.isCadastroIncompleto()) {
            if (session.isProdutor()) {
                // Vai para a tela de completar perfil do vendedor/produtor
                startActivity(new Intent(this, CompletarPerfilProdutorActivity.class));
            } else {
                // Vai para a tela de completar perfil do consumidor
                startActivity(new Intent(this, CompletarPerfilCompradorActivity.class));
            }
            finish();
            return; // Encerra o fluxo aqui
        }

        // 3. Fluxo Normal: Cadastro 100% completo, libera o acesso às Homes
        Class<?> destino = session.isProdutor()
                ? HomeProdutorActivity.class
                : Homec.class;

        startActivity(new Intent(this, destino));
        finish();
    }
}