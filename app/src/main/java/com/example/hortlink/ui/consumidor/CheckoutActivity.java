package com.example.hortlink.ui.consumidor;

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
import com.example.hortlink.data.dto.CheckoutRequestDTO;
import com.example.hortlink.data.repository.PedidoRepository;
import com.example.hortlink.service.BaseCallback;

public class CheckoutActivity extends AppCompatActivity {

    private final PedidoRepository pedidoRepository = new PedidoRepository();

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

        spinner = findViewById(R.id.spinner);
        tvMsg   = findViewById(R.id.tv_msg);
        btnPay  = findViewById(R.id.btn_pagar);

        // O único gatilho da tela agora é este botão
        btnPay.setOnClickListener(v -> iniciarPagamento());
    }

    // ─── 1. Simula o tempo de um Gateway de Pagamento ─────────
    private void iniciarPagamento() {
        btnPay.setEnabled(false);
        spinner.setVisibility(View.VISIBLE);
        tvMsg.setText("Processando pagamento...");

        // Aguarda 2 segundos para dar feedback visual ao usuário
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            tvMsg.setText("Pagamento aprovado ✅\nGerando pedido...");
            enviarCheckoutParaAPI();
        }, 2000);
    }

    // ─── 2. Envia apenas as decisões para o Spring Boot ───────
    private void enviarCheckoutParaAPI() {
        // Dados fixos para o teste atual.
        // No futuro, pegar de componentes UI (RadioGroup, Spinners, etc)
        String formaPagtoEscolhida = "PIX";
        Long enderecoId = null;
        String observacoes = "Nenhuma observação";

        CheckoutRequestDTO dto = new CheckoutRequestDTO(formaPagtoEscolhida, enderecoId, observacoes);

        pedidoRepository.finalizarCheckout(dto, new BaseCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                runOnUiThread(() -> {
                    spinner.setVisibility(View.GONE);
                    Toast.makeText(CheckoutActivity.this, "Pedido realizado com sucesso!", Toast.LENGTH_LONG).show();

                    // Como não temos mais o CartManager, é só navegar para a Home!
                    // O Spring Boot já limpou o carrinho no banco.
                    navegarParaHome();
                });
            }

            @Override
            public void onError(String erro) {
                runOnUiThread(() -> {
                    btnPay.setEnabled(true);
                    spinner.setVisibility(View.GONE);
                    tvMsg.setText("Ocorreu um problema.");

                    // Mostra o que a API devolveu (ex: "Sem estoque")
                    Toast.makeText(CheckoutActivity.this, erro, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    // ─── 3. Finaliza o fluxo e volta para a tela inicial ──────
    private void navegarParaHome() {
        Intent intent = new Intent(CheckoutActivity.this, Homec.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}