package com.example.hortlink.ui.produtor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.hortlink.R;
import com.example.hortlink.data.dto.NovoProdutoDTO;
import com.example.hortlink.data.enums.Categoria;
import com.example.hortlink.data.enums.UnidadeMedida;
import com.example.hortlink.data.model.Produto;
import com.example.hortlink.data.repository.ProdutoRepository;
import com.example.hortlink.service.BaseCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class AdicionarProdutosActivity extends AppCompatActivity {

    private TextInputEditText edtNome, edtDescricao;
    private AutoCompleteTextView spinnerCategoria, spinnerUnidade;
    private ImageView imgProduto;
    private LinearLayout layoutPlaceholder;
    private ProgressBar progressBar;
    private MaterialButton btnSalvar;

    private Uri imagemSelecionada;
    private File arquivoImagemSelecionada = null;
    private final ProdutoRepository produtoRepository = new ProdutoRepository();

    private Categoria categoriaSelecionada = null;
    private UnidadeMedida unidadeSelecionada = null;
    private Long idProdutoEditado = -1L;

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    imagemSelecionada = uri;
                    imgProduto.setImageURI(uri);
                    imgProduto.setVisibility(View.VISIBLE);
                    layoutPlaceholder.setVisibility(View.GONE);
                    arquivoImagemSelecionada = uriToFile(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_adicionar_produtos);

        bindViews();
        configurarSpinners();

        // Verifica se é edição ou criação
        idProdutoEditado = getIntent().getLongExtra("produto_id", -1L);

        if (idProdutoEditado != -1L) {
            configurarModoEdicao();
        } else {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Adicionar Produto");
        }

        findViewById(R.id.frameUploadFoto).setOnClickListener(v -> pickImage.launch("image/*"));
        btnSalvar.setOnClickListener(v -> salvarProduto());
        findViewById(R.id.btnCancelar).setOnClickListener(v -> finish());
    }

    private void bindViews() {
        edtNome = findViewById(R.id.edtNome);
        edtDescricao = findViewById(R.id.edtDescricao);
        spinnerCategoria = findViewById(R.id.spnCategoria);
        spinnerUnidade = findViewById(R.id.spinnerUnidade);
        imgProduto = findViewById(R.id.imgProduto);
        layoutPlaceholder = findViewById(R.id.layoutPlaceholder);
        progressBar = findViewById(R.id.progressBar);
        btnSalvar = findViewById(R.id.btnSalvarProduto);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void configurarModoEdicao() {
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Editar Produto");
        btnSalvar.setText("Atualizar Produto");
        setCarregando(true);

        produtoRepository.buscarPorId(idProdutoEditado, new BaseCallback<Produto>() {
            @Override
            public void onSuccess(Produto produto) {
                runOnUiThread(() -> {
                    setCarregando(false);
                    preencherCampos(produto);
                });
            }

            @Override
            public void onError(String erro) {
                runOnUiThread(() -> {
                    setCarregando(false);
                    Toast.makeText(AdicionarProdutosActivity.this, "Erro ao carregar produto", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void preencherCampos(Produto produto) {
        edtNome.setText(produto.getNome());
        edtDescricao.setText(produto.getDescricao());

        // Configura Categoria
        categoriaSelecionada = produto.getCategoria();
        spinnerCategoria.setText(categoriaSelecionada.toString(), false);

        // Configura Unidade
        unidadeSelecionada = produto.getUnidadeMedida();
        spinnerUnidade.setText(unidadeSelecionada.toString(), false);

        // Imagem (Note: O model Produto precisa ter o campo imagemUrl ou similar)
        // Se você salvou a URL no Supabase, use o Glide:
        if (produto.getImagemUrl() != null && !produto.getImagemUrl().isEmpty()) {
            imgProduto.setVisibility(View.VISIBLE);
            layoutPlaceholder.setVisibility(View.GONE);
            Glide.with(this).load(produto.getImagemUrl()).into(imgProduto);
        }
    }

    private void configurarSpinners() {
        spinnerCategoria.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, Categoria.values()));
        spinnerUnidade.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, UnidadeMedida.values()));

        spinnerCategoria.setOnItemClickListener((parent, view, position, id) -> categoriaSelecionada = (Categoria) parent.getItemAtPosition(position));
        spinnerUnidade.setOnItemClickListener((parent, view, position, id) -> unidadeSelecionada = (UnidadeMedida) parent.getItemAtPosition(position));
    }

    private void salvarProduto() {
        String nome = edtNome.getText().toString().trim();
        String descricao = edtDescricao.getText().toString().trim();

        if (nome.isEmpty()) { edtNome.setError("Informe o nome"); return; }
        if (categoriaSelecionada == null) { Toast.makeText(this, "Selecione uma categoria", Toast.LENGTH_SHORT).show(); return; }
        if (unidadeSelecionada == null) { Toast.makeText(this, "Selecione uma unidade", Toast.LENGTH_SHORT).show(); return; }

        Long idParaEnvio = (idProdutoEditado != -1L) ? idProdutoEditado : null;
        NovoProdutoDTO dto = new NovoProdutoDTO(idParaEnvio, nome, descricao, categoriaSelecionada, unidadeSelecionada);
        setCarregando(true);

        BaseCallback<Produto> callback = new BaseCallback<Produto>() {
            @Override
            public void onSuccess(Produto resultado) {
                runOnUiThread(() -> {
                    setCarregando(false);
                    Toast.makeText(AdicionarProdutosActivity.this, "Produto salvo com sucesso!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onError(String erro) {
                runOnUiThread(() -> {
                    setCarregando(false);
                    Toast.makeText(AdicionarProdutosActivity.this, erro, Toast.LENGTH_LONG).show();
                });
            }
        };

        if (idProdutoEditado != -1L) {
            produtoRepository.atualizarProduto(idProdutoEditado, dto, arquivoImagemSelecionada, callback);
        } else {
            if (arquivoImagemSelecionada == null) {
                setCarregando(false);
                Toast.makeText(this, "Selecione uma foto para o produto", Toast.LENGTH_SHORT).show();
                return;
            }
            produtoRepository.cadastrarProduto(dto, arquivoImagemSelecionada, callback);
        }
    }

    private void setCarregando(boolean carregando) {
        progressBar.setVisibility(carregando ? View.VISIBLE : View.GONE);
        btnSalvar.setEnabled(!carregando);
        edtNome.setEnabled(!carregando);
        edtDescricao.setEnabled(!carregando);
        spinnerCategoria.setEnabled(!carregando);
        spinnerUnidade.setEnabled(!carregando);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private File uriToFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            File tempFile = File.createTempFile("produto_upload_", ".jpg", getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}