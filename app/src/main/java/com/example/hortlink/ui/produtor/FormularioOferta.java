package com.example.hortlink.ui.produtor;

import android.app.DatePickerDialog;
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
import com.example.hortlink.data.dto.NovaOfertaDTO;
import com.example.hortlink.data.dto.OfertaEdicaoDTO;
import com.example.hortlink.data.dto.ProdutoListaDTO;
import com.example.hortlink.data.model.OfertaDTO;
import com.example.hortlink.data.repository.OfertaRepository;
import com.example.hortlink.data.repository.ProdutoRepository;
import com.example.hortlink.service.BaseCallback;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

public class FormularioOferta extends AppCompatActivity {

    private TextInputLayout layoutSelecaoProduto;
    private AutoCompleteTextView spinnerProdutos;
    private CardView cardInformacoesProduto;
    private ImageView imgProdutoCard;
    private TextView txtNomeProdutoCard, txtUnidadeMedidaCard;

    // Adicionados os novos campos editáveis
    private TextInputEditText edtPreco, edtEstoque, edtDataColheita;
    private SwitchMaterial switchAtivo;
    private Button btnSalvar, btnCancelar; // Adicione o btnCancelar aqui
    private ProgressBar progressBar;

    private Long idOfertaEditada = -1L;
    private Long idProdutoSelecionado = -1L;

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
            configurarModoCriacao();
        }

        btnSalvar.setOnClickListener(v -> tentarSalvar());
        btnCancelar.setOnClickListener(v -> finish());

        edtDataColheita.setOnClickListener(v -> abrirCalendario());
    }

    private void bindViews() {
        btnCancelar = findViewById(R.id.btnCancelar);
        layoutSelecaoProduto = findViewById(R.id.layoutSelecaoProduto);
        spinnerProdutos = findViewById(R.id.spinnerProdutos);
        cardInformacoesProduto = findViewById(R.id.cardInformacoesProduto);
        imgProdutoCard = findViewById(R.id.imgProdutoCard);
        txtNomeProdutoCard = findViewById(R.id.txtNomeProdutoCard);
        txtUnidadeMedidaCard = findViewById(R.id.txtUnidadeMedidaCard);

        edtPreco = findViewById(R.id.edtPreco);
        edtEstoque = findViewById(R.id.edtEstoque);
        edtDataColheita = findViewById(R.id.edtDataColheita);

        switchAtivo = findViewById(R.id.switchAtivo);
        btnSalvar = findViewById(R.id.btnSalvarOferta);
        progressBar = findViewById(R.id.progressBarOferta);
    }

    // ─── MODO CRIAÇÃO ────────────────────────────────────────────────────────
    private void configurarModoCriacao() {
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

        // Utilizando o método novo e esperando o OfertaEdicaoDTO
        ofertaRepository.buscarOfertaEdicaoPorId(idOfertaEditada, new BaseCallback<OfertaEdicaoDTO>() {
            @Override
            public void onSuccess(OfertaEdicaoDTO oferta) {
                runOnUiThread(() -> {
                    setCarregando(false);

                    // Preenche o cabeçalho bloqueado
                    preencherCardProduto(oferta.getNomeProduto(), oferta.getUnidadeSimbolo(), oferta.getImagemUrl());

                    // Preenche os campos editáveis
                    edtPreco.setText(oferta.getPreco() != null ? oferta.getPreco().toString() : "");
                    edtEstoque.setText(oferta.getEstoqueAtual() != null ? oferta.getEstoqueAtual().toString() : "");
                    edtDataColheita.setText(oferta.getDataColheita() != null ? oferta.getDataColheita() : "");

                    if (oferta.getDisponivelParaVenda() != null) {
                        switchAtivo.setChecked(oferta.getDisponivelParaVenda());
                    }
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
        String estoqueStr = edtEstoque.getText().toString().trim();
        String dataColheitaStr = edtDataColheita.getText().toString().trim();

        if (precoStr.isEmpty()) {
            edtPreco.setError("Informe o preço");
            edtPreco.requestFocus();
            return;
        }

        if (estoqueStr.isEmpty()) {
            edtEstoque.setError("Informe o estoque");
            edtEstoque.requestFocus();
            return;
        }

        // Conversão exata utilizando BigDecimal
        BigDecimal preco = new BigDecimal(precoStr.replace(",", "."));
        BigDecimal estoque = new BigDecimal(estoqueStr.replace(",", "."));
        boolean ativo = switchAtivo.isChecked();

        // Na edição, o idProduto é irrelevante para a API de PUT, então enviamos nulo.
        // Na criação, enviamos o ID do produto selecionado no dropdown.
        Long idProdutoParaEnvio = (idOfertaEditada != -1L) ? null : idProdutoSelecionado;

        // Monta o payload único de envio (Request)
        NovaOfertaDTO payload = new NovaOfertaDTO(
                idProdutoParaEnvio,
                preco,
                estoque,
                dataColheitaStr,
                ativo
        );

        setCarregando(true);

        if (idOfertaEditada != -1L) {
            ofertaRepository.atualizarOferta(idOfertaEditada, payload, new BaseCallback<OfertaDTO>() {
                @Override
                public void onSuccess(OfertaDTO response) {
                    runOnUiThread(() -> {
                        setCarregando(false);
                        Toast.makeText(FormularioOferta.this, "Oferta atualizada com sucesso!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }

                @Override
                public void onError(String erro) {
                    runOnUiThread(() -> {
                        setCarregando(false);
                        Toast.makeText(FormularioOferta.this, "Erro ao atualizar: " + erro, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            ofertaRepository.criarOferta(payload, new BaseCallback<OfertaDTO>() {
                @Override
                public void onSuccess(OfertaDTO response) {
                    runOnUiThread(() -> {
                        setCarregando(false);
                        Toast.makeText(FormularioOferta.this, "Oferta criada com sucesso!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }

                @Override
                public void onError(String erro) {
                    runOnUiThread(() -> {
                        setCarregando(false);
                        Toast.makeText(FormularioOferta.this, "Erro ao criar: " + erro, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }

    private void setCarregando(boolean carregando) {
        progressBar.setVisibility(carregando ? View.VISIBLE : View.GONE);
        btnSalvar.setEnabled(!carregando);
        layoutSelecaoProduto.setEnabled(!carregando);
        edtPreco.setEnabled(!carregando);
        edtEstoque.setEnabled(!carregando);
        edtDataColheita.setEnabled(!carregando);
        switchAtivo.setEnabled(!carregando);
    }

    private void abrirCalendario() {
        // Pega a data atual para o calendário abrir no dia de hoje
        Calendar calendar = Calendar.getInstance();
        int ano = calendar.get(Calendar.YEAR);
        int mes = calendar.get(Calendar.MONTH);
        int dia = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    // Formata a data para o padrão ISO que o Spring Boot exige: YYYY-MM-DD
                    String dataFormatada = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    edtDataColheita.setText(dataFormatada);
                }, ano, mes, dia);

        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }
}