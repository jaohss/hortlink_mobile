package com.example.hortlink.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hortlink.R;
import com.example.hortlink.data.dto.NovoProdutoDTO;
import com.example.hortlink.data.enums.Categoria;
import com.example.hortlink.data.enums.UnidadeMedida;
import com.example.hortlink.data.model.Produto;
import com.example.hortlink.data.repository.ProdutoRepository;
import com.example.hortlink.entidades.BaseCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class AdicionarProdutosActivity extends AppCompatActivity {

    private TextInputEditText edtNome, edtPreco, edtDescricao;
    private AutoCompleteTextView spinnerCategoria, spinnerUnidade;
    private ImageView imgProduto;
    private LinearLayout layoutPlaceholder;
    private CircularProgressIndicator progressBar; // adicione no seu XML
    private Uri imagemSelecionada;
    private ProdutoRepository produtoRepository = new ProdutoRepository();
    private File arquivoImagemSelecionada = null;

    private Categoria categoriaSelecionada = null;
    private UnidadeMedida unidadeSelecionada = null;


    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    imagemSelecionada = uri;
                    imgProduto.setImageURI(uri);
                    imgProduto.setVisibility(View.VISIBLE);
                    layoutPlaceholder.setVisibility(View.GONE);

                    // ─── O TRATAMENTO DA IMAGEM AQUI ───
                    arquivoImagemSelecionada = uriToFile(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_adicionar_produtos);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Adicionar Produto");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        edtNome          = findViewById(R.id.edtNome);
        edtDescricao     = findViewById(R.id.edtDescricao);
        spinnerCategoria = findViewById(R.id.spnCategoria);
        spinnerUnidade   = findViewById(R.id.spinnerUnidade);
        imgProduto       = findViewById(R.id.imgProduto);
        layoutPlaceholder = findViewById(R.id.layoutPlaceholder);
        // progressBar   = findViewById(R.id.progressBar); // descomente se tiver no XML

        configurarSpinners();

        findViewById(R.id.frameUploadFoto).setOnClickListener(v -> pickImage.launch("image/*"));

        MaterialButton btnSalvar   = findViewById(R.id.btnSalvarProduto);
        MaterialButton btnCancelar = findViewById(R.id.btnCancelar);

        btnSalvar.setOnClickListener(v -> salvarProduto());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void configurarSpinners() {

        // Configura os Adapters (isso você já fez e está correto)
        ArrayAdapter<Categoria> adapterCategoria = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, Categoria.values());
        spinnerCategoria.setAdapter(adapterCategoria);

        ArrayAdapter<UnidadeMedida> adapterUnidade = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, UnidadeMedida.values());
        spinnerUnidade.setAdapter(adapterUnidade);

        // ─── O SEGREDO ESTÁ AQUI: Capturar o clique ───

        spinnerCategoria.setOnItemClickListener((parent, view, position, id) -> {
            // Pega o objeto Categoria real que estava naquela posição da lista
            categoriaSelecionada = (Categoria) parent.getItemAtPosition(position);
        });

        spinnerUnidade.setOnItemClickListener((parent, view, position, id) -> {
            // Pega o objeto UnidadeMedida real
            unidadeSelecionada = (UnidadeMedida) parent.getItemAtPosition(position);
        });
    }

    private void salvarProduto() {
        String nome      = edtNome.getText().toString().trim();
        String descricao = edtDescricao.getText().toString().trim();

        // Validações
        if (nome.isEmpty())      { edtNome.setError("Informe o nome"); return; }
        if (descricao.isEmpty()) { edtDescricao.setError("Informe a descrição"); return; }

        if (categoriaSelecionada == null) { spinnerCategoria.setError("Selecione a categoria"); return; }
        if (unidadeSelecionada == null) { spinnerUnidade.setError("Selecione a unidade"); return; }

        if (arquivoImagemSelecionada == null) {
            Toast.makeText(this, "Selecione uma foto para o produto", Toast.LENGTH_SHORT).show();
            return;
        }

        NovoProdutoDTO dto = new NovoProdutoDTO(
                null,
                nome,
                descricao,
                categoriaSelecionada,
                unidadeSelecionada
        );

        setCarregando(true);

        produtoRepository.cadastrarProduto(dto, arquivoImagemSelecionada, new BaseCallback<Produto>() {
            @Override
            public void onSuccess(Produto resultado) {
                setCarregando(false);
                Toast.makeText(AdicionarProdutosActivity.this, "Produto salvo com sucesso!", Toast.LENGTH_SHORT).show();

                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(String erro) {
                setCarregando(false);
                Toast.makeText(AdicionarProdutosActivity.this, erro, Toast.LENGTH_LONG).show();
            }
        });
        }

    private void setCarregando(boolean carregando) {
        findViewById(R.id.btnSalvarProduto).setEnabled(!carregando);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private File uriToFile(Uri uri) {
        try {
            // Abre um "canal" para ler a imagem da galeria
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            // Cria um arquivo temporário vazio na pasta de cache do seu app
            File tempFile = File.createTempFile("produto_upload_", ".jpg", getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            // Copia os dados da galeria para o arquivo temporário
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            // Fecha os canais
            outputStream.close();
            inputStream.close();

            return tempFile; // Retorna o arquivo físico pronto para envio!
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}