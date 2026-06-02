package com.example.hortlink.ui.produtor;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.hortlink.R;
import com.example.hortlink.data.dto.DetalheOfertaDTO;
import com.example.hortlink.data.dto.ProdutoListaDTO;
// Importe seus DTOs de NovoOfertaDTO e DetalheOfertaDTO (ou similares)
// Importe o OfertaRepository e ProdutoRepository
import com.example.hortlink.data.repository.OfertaRepository;
import com.example.hortlink.data.repository.ProdutoRepository;
import com.example.hortlink.service.BaseCallback;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class FormularioOferta extends AppCompatActivity {

    private TextInputLayout layoutSelecaoProduto;
    private AutoCompleteTextView spinnerProdutos;
    private CardView cardInformacoesProduto;
    private ImageView imgProdutoCard;
    private TextView txtNomeProdutoCard, txtUnidadeMedidaCard;
    private TextInputEditText edtPreco;
    private SwitchMaterial switchAtivo;
    private Button btnSalvar;
    private ProgressBar progressBar;

    private Long idOfertaEditada = -1L;
    private Long idProdutoSelecionado = -1L;

    // Repositórios
    private OfertaRepository ofertaRepository;
    private ProdutoRepository produtoRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_oferta);

        ofertaRepository = new OfertaRepository();
        produtoRepository = new ProdutoRepository();

        bindViews();

        idOfertaEditada = getIntent().getLongExtra("oferta_id", -1L);

        if (idOfertaEditada != -1L) {
            configurarModoEdicao();
        } else {
            configurarModoCricao();
        }

        btnSalvar.setOnClickListener(v -> tentarSalvar());
    }

    private void bindViews() {
        layoutSelecaoProduto = findViewById(R.id.layoutSelecaoProduto);
        spinnerProdutos = findViewById(R.id.spinnerProdutos);
        cardInformacoesProduto = findViewById(R.id.cardInformacoesProduto);
        imgProdutoCard = findViewById(R.id.imgProdutoCard);
        txtNomeProdutoCard = findViewById(R.id.txtNomeProdutoCard);
        txtUnidadeMedidaCard = findViewById(R.id.txtUnidadeMedidaCard);
        edtPreco = findViewById(R.id.edtPreco);
        switchAtivo = findViewById(R.id.switchAtivo);
        btnSalvar = findViewById(R.id.btnSalvarOferta);
        progressBar = findViewById(R.id.progressBarOferta);
    }

    // ─── MODO CRIAÇÃO ────────────────────────────────────────────────────────
    private void configurarModoCricao() {
        layoutSelecaoProduto.setVisibility(View.VISIBLE);
        cardInformacoesProduto.setVisibility(View.GONE);

        setCarregando(true);
        produtoRepository.obterMeusProdutosSemOferta(new BaseCallback<List<ProdutoListaDTO>>() {
            @Override
            public void onSuccess(List<ProdutoListaDTO> produtosLivres) {
                runOnUiThread(() -> {
                    setCarregando(false);
                    if (produtosLivres.isEmpty()) {
                        Toast.makeText(FormularioOferta.this, "Todos os seus produtos já têm ofertas ativas!", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                    ArrayAdapter<ProdutoListaDTO> adapter = new ArrayAdapter<>(
                            FormularioOferta.this,
                            android.R.layout.simple_dropdown_item_1line,
                            produtosLivres
                    );
                    spinnerProdutos.setAdapter(adapter);

                    spinnerProdutos.setOnItemClickListener((parent, view, position, id) -> {
                        ProdutoListaDTO produto = (ProdutoListaDTO) parent.getItemAtPosition(position);
                        preencherCardProduto(produto.getNome(), produto.getUnidadeMedida().toString(), produto.getImagemUrl());
                        idProdutoSelecionado = produto.getId();
                    });
                });
            }

            @Override
            public void onError(String erro) {
                runOnUiThread(() -> {
                    setCarregando(false);
                    Toast.makeText(FormularioOferta.this, "Erro: " + erro, Toast.LENGTH_LONG).show();
                });
            }
        });

    }

    // ─── MODO EDIÇÃO ─────────────────────────────────────────────────────────
    private void configurarModoEdicao() {
        layoutSelecaoProduto.setVisibility(View.GONE);
        cardInformacoesProduto.setVisibility(View.VISIBLE);
        setTitle("Editar Oferta");
        btnSalvar.setText("Atualizar Oferta");


        setCarregando(true);
        ofertaRepository.buscarOfertaDetalhadaPorId(idOfertaEditada, new BaseCallback<DetalheOfertaDTO>() {
            @Override
            public void onSuccess(DetalheOfertaDTO oferta) {
                runOnUiThread(() -> {
                    setCarregando(false);
                    preencherCardProduto(oferta.getNome(), oferta.getUnidade(), oferta.getImagemUrl());

                    edtPreco.setText(String.valueOf(oferta.getValor()));
                    switchAtivo.setChecked(oferta.isDisponivelParaVenda());

                });
            }

            @Override
            public void onError(String erro) {
                runOnUiThread(() -> {
                    setCarregando(false);
                    Toast.makeText(FormularioOferta.this, "Erro ao carregar oferta.", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });

    }

    // ─── FUNÇÕES DE APOIO ────────────────────────────────────────────────────
    private void preencherCardProduto(String nome, String unidade, String imagemUrl) {
        cardInformacoesProduto.setVisibility(View.VISIBLE);
        txtNomeProdutoCard.setText(nome);
        txtUnidadeMedidaCard.setText("Vendido por: " + unidade);

        Glide.with(this)
                .load(imagemUrl != null && !imagemUrl.isEmpty() ? imagemUrl : null)
                .placeholder(R.drawable.hortlink_logo)
                .centerCrop()
                .into(imgProdutoCard);
    }

    private void tentarSalvar() {
        if (idOfertaEditada == -1L && idProdutoSelecionado == -1L) {
            Toast.makeText(this, "Selecione um produto primeiro.", Toast.LENGTH_SHORT).show();
            return;
        }

        String precoStr = edtPreco.getText().toString().trim();
        if (precoStr.isEmpty()) {
            edtPreco.setError("Informe o preço");
            edtPreco.requestFocus();
            return;
        }

        double preco = Double.parseDouble(precoStr.replace(",", "."));
        boolean ativo = switchAtivo.isChecked();

        // setCarregando(true);

        if (idOfertaEditada != -1L) {
            // CHAMAR API DE PUT (Atualizar Oferta)
            Toast.makeText(this, "Simulando Atualização...", Toast.LENGTH_SHORT).show();
        } else {
            // CHAMAR API DE POST (Criar Nova Oferta)
            // Lembre-se de enviar o idProdutoSelecionado no DTO
            Toast.makeText(this, "Simulando Criação...", Toast.LENGTH_SHORT).show();
        }
    }

    private void setCarregando(boolean carregando) {
        progressBar.setVisibility(carregando ? View.VISIBLE : View.GONE);
        btnSalvar.setEnabled(!carregando);
        layoutSelecaoProduto.setEnabled(!carregando);
        edtPreco.setEnabled(!carregando);
        switchAtivo.setEnabled(!carregando);
    }
}