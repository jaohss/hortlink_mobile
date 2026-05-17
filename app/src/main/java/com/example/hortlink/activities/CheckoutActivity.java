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
import com.example.hortlink.data.model.CartItem;
import com.example.hortlink.entidades.CartManager;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

        btnPay.setOnClickListener(v -> validarItensAntesDeComprar());
    }

    // ─── 0. Valida cada produto individualmente antes de cobrar ─────
    //
    // Uma requisição por produto — sem encoding de URL, sem query
    // malformada. Array vazio na resposta = produto inativo.
    //
    // AtomicInteger coordena os N callbacks paralelos: só avança
    // quando todos chegarem (pendentes chega a 0).
    private void validarItensAntesDeComprar() {
        if (cartItems == null || cartItems.isEmpty()) {
            Toast.makeText(this, "Carrinho vazio", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPay.setEnabled(false);
        spinner.setVisibility(View.VISIBLE);
        tvMsg.setText("Verificando disponibilidade...");

        List<CartItem> inativos = new ArrayList<>();
        AtomicInteger pendentes = new AtomicInteger(cartItems.size());

        for (CartItem item : cartItems) {
            // UUID vai direto na URL sem vírgulas nem parênteses — sem problema de encoding
            String url = "/rest/v1/produtos"
                    + "?select=id"
                    + "&id=eq." + item.getProdutoId()
                    + "&ativo=eq.true";

            supabase.get(url, new SupabaseHelper.SupabaseCallback() {
                @Override
                public void onSuccess(String resultado) {
                    try {
                        if (new JSONArray(resultado).length() == 0) {
                            // Array vazio: produto inativo ou removido
                            synchronized (inativos) { inativos.add(item); }
                        }
                    } catch (Exception e) {
                        // JSON malformado — bloqueia por segurança
                        synchronized (inativos) { inativos.add(item); }
                    }
                    onRespostaChegou(pendentes, inativos);
                }

                @Override
                public void onError(String erro) {
                    // Erro de rede — bloqueia por segurança
                    synchronized (inativos) { inativos.add(item); }
                    onRespostaChegou(pendentes, inativos);
                }
            });
        }
    }

    // Chamado após cada resposta — age só quando a última chegar
    private void onRespostaChegou(AtomicInteger pendentes, List<CartItem> inativos) {
        if (pendentes.decrementAndGet() != 0) return;

        runOnUiThread(() -> {
            if (!inativos.isEmpty()) {
                removerItensInativosEAvisar(inativos);
            } else {
                iniciarPagamento();
            }
        });
    }

    // ─── Remove inativos do carrinho e avisa o usuário ────────────────
    private void removerItensInativosEAvisar(List<CartItem> itensInativos) {
        StringBuilder nomes = new StringBuilder();

        for (CartItem item : itensInativos) {
            nomes.append("\n• ").append(item.getNomeProduto());
            cartItems.remove(item);
            CartManager.getInstance(this).removeItem(item.getProdutoId());

            supabase.delete(
                    "/rest/v1/carrinho?id=eq." + item.getCarrinhoId(),
                    new SupabaseHelper.SupabaseCallback() {
                        @Override public void onSuccess(String r) {}
                        @Override public void onError(String e) {}
                    });
        }

        spinner.setVisibility(View.GONE);
        btnPay.setEnabled(true);
        tvMsg.setText("");

        Toast.makeText(
                this,
                "Produto(s) indisponível(is):" + nomes
                        + "\n\nRevise o carrinho e tente novamente.",
                Toast.LENGTH_LONG
        ).show();

        finish();
    }

    // ─── 1. Simula pagamento (2 s) e depois cria o pedido ───────────
    private void iniciarPagamento() {
        // Aqui o spinner já está visível desde validarItensAntesDeComprar()
        runOnUiThread(() -> tvMsg.setText("Processando pagamento..."));

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