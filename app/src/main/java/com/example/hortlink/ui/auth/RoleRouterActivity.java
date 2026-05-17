//package com.example.hortlink.ui.auth;
//
//import android.content.Intent;
//import android.os.Bundle;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//import com.example.hortlink.R;
//import com.example.hortlink.data.remote.SupabaseClient;
//import com.example.hortlink.util.SessionManager;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//
//public class RoleRouterActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_role_router);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user == null) {
//            startActivity(new Intent(this, LoginActivity.class));
//            finish();
//            return;
//        }
//
//        // Busca tipo do usuário no Supabase
//        SupabaseClient.getInstance()
//                .from("usuarios")
//                .select("tipo, nome")
//                .eq("id", user.getUid())
//                .execute(response -> {
//                    String tipo = response.getString("tipo");
//                    String nome = response.getString("nome");
//
//                    SessionManager.getInstance().init(user.getUid(), tipo, nome);
//
//                    Class<?> destino = "produtor".equals(tipo)
//                            ? ProdutorActivity.class
//                            : ConsumidorActivity.class;
//
//                    startActivity(new Intent(this, destino));
//                    finish(); // nunca volta para cá com back
//                });
//    }
//    }
//}