package com.example.hortlink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hortlink.R;
import com.example.hortlink.bd.SupabaseHelper;
import com.example.hortlink.entidades.CartItem;
import com.example.hortlink.entidades.CartManager;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {
    private List<CartItem> cartItems;
    private String usuarioId;
    private SupabaseHelper supabase;

    private ProgressBar spinner;
    private TextView tvMsg;
    private Button btnPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_checkout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usuarioId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        cartItems = CartManager.getInstance(this).getItems();
        supabase  = new SupabaseHelper(this);

        spinner = findViewById(R.id.spinner);
        tvMsg   = findViewById(R.id.tv_msg);
        btnPay  = findViewById(R.id.btn_pagar);

        btnPay.setOnClickListener(v -> iniciarPagamento());
    }

    // ─── 1. Simula pagamento (2 s) e depois cria o pedido ───────────
    private void iniciarPagamento() {
        btnPay.setEnabled(false);
        spinner.setVisibility(View.VISIBLE);
        tvMsg.setText("Processando pagamento...");

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            tvMsg.setText("Pagamento aprovado ✅");
            spinner.setVisibility(View.GONE);
            criarPedido();
        }, 2000);
    }

    // ─── 2. INSERT em pedidos ────────────────────────────────────────
    private void criarPedido() {
        String producerId = cartItems.get(0).getProducerId();
        double total = 0;
        for (CartItem i : cartItems) total += i.getSubtotal();

        try {
            JSONObject json = new JSONObject();
            json.put("comprador_id", usuarioId);
            json.put("produtor_id",  producerId);
            json.put("valor_total",  total);
            json.put("status",       "pago");

            supabase.inserirPedido(json, new SupabaseHelper.SupabaseCallback() {
                @Override
                public void onSuccess(String resultado) {
                    try {
                        // Supabase retorna array com o objeto inserido
                        String pedidoId = new JSONArray(resultado)
                                .getJSONObject(0)
                                .getString("id");
                        inserirItensPedido(pedidoId);
                    } catch (JSONException e) {
                        logError("Erro ao ler id do pedido: " + e.getMessage());
                    }
                }

                @Override
                public void onError(String erro) {
                    logError(erro);
                }
            });

        } catch (JSONException e) {
            logError("Erro ao montar pedido: " + e.getMessage());
        }
    }

    // ─── 3. INSERT batch em pedido_itens ────────────────────────────
    private void inserirItensPedido(String pedidoId) {
        try {
            JSONArray batch = new JSONArray();
            for (CartItem item : cartItems) {
                JSONObject obj = new JSONObject();
                obj.put("pedido_id",      pedidoId);
                obj.put("produto_id",     item.getProdutoId());
                obj.put("quantidade",     item.getQuantidade());
                obj.put("preco_unitario", item.getPreco());
                batch.put(obj);
            }

            supabase.inserirItensPedido(batch, new SupabaseHelper.SupabaseCallback() {
                @Override
                public void onSuccess(String resultado) {
                    limparCarrinho();
                }

                @Override
                public void onError(String erro) {
                    logError(erro);
                }
            });

        } catch (JSONException e) {
            logError("Erro ao montar itens: " + e.getMessage());
        }
    }

    // ─── 4. Limpa carrinho local e navega para Home ──────────────────
    private void limparCarrinho() {
        String usuarioId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        supabase.delete(
                "/rest/v1/carrinho?usuario_id=eq." + usuarioId,
                new SupabaseHelper.SupabaseCallback() {
                    @Override
                    public void onSuccess(String resultado) {
                        CartManager.getInstance(CheckoutActivity.this).clearCart();

                        runOnUiThread(() -> {
                            Intent intent = new Intent(CheckoutActivity.this, Homec.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            Toast.makeText(CheckoutActivity.this,
                                    "Pedido realizado com sucesso!", Toast.LENGTH_LONG).show();
                            finish();
                        });
                    }

                    @Override
                    public void onError(String erro) {
                        // Mesmo com erro no delete, navega — o pedido já foi criado
                        CartManager.getInstance(CheckoutActivity.this).clearCart();
                        runOnUiThread(() -> {
                            Intent intent = new Intent(CheckoutActivity.this, Homec.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        });
                    }
                }
        );
    }

    // ─── Helper de erro ─────────────────────────────────────────────
    private void logError(String msg) {
        runOnUiThread(() -> {
            btnPay.setEnabled(true);
            spinner.setVisibility(View.GONE);
            tvMsg.setText("Ocorreu um erro. Tente novamente.");
            Toast.makeText(this, "Erro: " + msg, Toast.LENGTH_LONG).show();
        });
    }
}