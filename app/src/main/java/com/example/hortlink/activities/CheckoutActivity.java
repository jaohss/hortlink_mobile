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
import com.example.hortlink.data.model.CartItem;
import com.example.hortlink.data.repository.CarrinhoRepository;
import com.example.hortlink.data.repository.PedidoRepository;
import com.example.hortlink.data.repository.ProdutoRepository;
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

    private final ProdutoRepository  produtoRepository  = new ProdutoRepository();
    private final CarrinhoRepository carrinhoRepository = new CarrinhoRepository();
    private final PedidoRepository   pedidoRepository   = new PedidoRepository();

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

        spinner = findViewById(R.id.spinner);
        tvMsg   = findViewById(R.id.tv_msg);
        btnPay  = findViewById(R.id.btn_pagar);

        btnPay.setOnClickListener(v -> validarItensAntesDeComprar());
    }

    // ─── 0. Valida cada produto antes de cobrar ──────────────────────
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
            produtoRepository.verificarStatus(item.getProdutoId(), new ProdutoRepository.Callback() {
                @Override
                public void onSuccess(String resultado) {
                    try {
                        if (new JSONArray(resultado).length() == 0) {
                            synchronized (inativos) { inativos.add(item); }
                        }
                    } catch (Exception e) {
                        synchronized (inativos) { inativos.add(item); }
                    }
                    onRespostaChegou(pendentes, inativos);
                }

                @Override
                public void onError(String erro) {
                    synchronized (inativos) { inativos.add(item); }
                    onRespostaChegou(pendentes, inativos);
                }
            });
        }
    }

    private void onRespostaChegou(AtomicInteger pendentes, List<CartItem> inativos) {
        if (pendentes.decrementAndGet() != 0) return;
        runOnUiThread(() -> {
            if (!inativos.isEmpty()) removerItensInativosEAvisar(inativos);
            else iniciarPagamento();
        });
    }

    // ─── Remove inativos do carrinho e avisa ─────────────────────────
    private void removerItensInativosEAvisar(List<CartItem> itensInativos) {
        StringBuilder nomes = new StringBuilder();

        for (CartItem item : itensInativos) {
            nomes.append("\n• ").append(item.getNomeProduto());
            cartItems.remove(item);
            CartManager.getInstance(this).removeItem(item.getProdutoId());

            carrinhoRepository.removerItem(item.getCarrinhoId(), new CarrinhoRepository.Callback() {
                @Override public void onSuccess(String r) {}
                @Override public void onError(String e) {}
            });
        }

        spinner.setVisibility(View.GONE);
        btnPay.setEnabled(true);
        tvMsg.setText("");

        Toast.makeText(this,
                "Produto(s) indisponível(is):" + nomes + "\n\nRevise o carrinho e tente novamente.",
                Toast.LENGTH_LONG).show();

        finish();
    }

    // ─── 1. Simula pagamento e cria o pedido ─────────────────────────
    private void iniciarPagamento() {
        runOnUiThread(() -> tvMsg.setText("Processando pagamento..."));

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            tvMsg.setText("Pagamento aprovado ✅");
            spinner.setVisibility(View.GONE);
            criarPedido();
        }, 2000);
    }

    // ─── 2. INSERT em pedidos ────────────────────────────────────────
    private void criarPedido() {
        String produtorId = cartItems.get(0).getProducerId();
        double total = 0;
        for (CartItem i : cartItems) total += i.getSubtotal();

        pedidoRepository.inserirPedido(usuarioId, produtorId, total, new PedidoRepository.Callback() {
            @Override
            public void onSuccess(String resultado) {
                try {
                    String pedidoId = new JSONArray(resultado)
                            .getJSONObject(0)
                            .getString("id");
                    inserirItensPedido(pedidoId);
                } catch (JSONException e) {
                    logError("Erro ao ler id do pedido: " + e.getMessage());
                }
            }

            @Override
            public void onError(String erro) { logError(erro); }
        });
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

            pedidoRepository.inserirItensPedido(batch, new PedidoRepository.Callback() {
                @Override
                public void onSuccess(String resultado) { limparCarrinho(); }

                @Override
                public void onError(String erro) { logError(erro); }
            });

        } catch (JSONException e) {
            logError("Erro ao montar itens: " + e.getMessage());
        }
    }

    // ─── 4. Limpa carrinho e navega para Home ───────────────────────
    private void limparCarrinho() {
        carrinhoRepository.limparCarrinho(usuarioId, new CarrinhoRepository.Callback() {
            @Override
            public void onSuccess(String resultado) {
                CartManager.getInstance(CheckoutActivity.this).clearCart();
                navegarParaHome();
                runOnUiThread(() -> Toast.makeText(CheckoutActivity.this,
                        "Pedido realizado com sucesso!", Toast.LENGTH_LONG).show());
            }

            @Override
            public void onError(String erro) {
                // Pedido já foi criado — navega mesmo assim
                CartManager.getInstance(CheckoutActivity.this).clearCart();
                navegarParaHome();
            }
        });
    }

    private void navegarParaHome() {
        runOnUiThread(() -> {
            Intent intent = new Intent(CheckoutActivity.this, Homec.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
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