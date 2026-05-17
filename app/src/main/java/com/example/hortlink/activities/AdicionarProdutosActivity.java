package com.example.hortlink.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hortlink.R;
import com.example.hortlink.bd.SupabaseHelper;
import com.example.hortlink.data.remote.StorageHelper;
import com.example.hortlink.data.repository.ProdutoRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class AdicionarProdutosActivity extends AppCompatActivity {

    private TextInputEditText edtNome, edtPreco, edtDescricao;
    private AutoCompleteTextView spinnerCategoria, spinnerUnidade;
    private ImageView imgProduto;
    private LinearLayout layoutPlaceholder;
    private CircularProgressIndicator progressBar; // adicione no seu XML
    private Uri imagemSelecionada;

    private SupabaseHelper supabase;
    private final ProdutoRepository produtoRepository = new ProdutoRepository();
    //private final StorageHelper storageHelper = new StorageHelper(this);


    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    imagemSelecionada = uri;
                    imgProduto.setImageURI(uri);
                    imgProduto.setVisibility(View.VISIBLE);
                    layoutPlaceholder.setVisibility(View.GONE);
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

        supabase = new SupabaseHelper(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Adicionar Produto");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        edtNome          = findViewById(R.id.edtNome);
        edtPreco         = findViewById(R.id.edtPreco);
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
        String[] categorias = {"Fruta", "Legume", "Verdura"};
        spinnerCategoria.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, categorias));

        String[] unidades = {"kg", "g", "un", "dúzia", "maço", "caixa"};
        spinnerUnidade.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, unidades));
    }

    private void salvarProduto() {
        String nome      = edtNome.getText().toString().trim();
        String categoria = spinnerCategoria.getText().toString().trim();
        String preco     = edtPreco.getText().toString().trim();
        String unidade   = spinnerUnidade.getText().toString().trim();
        String descricao = edtDescricao.getText().toString().trim();

        // Validações
        if (nome.isEmpty())      { edtNome.setError("Informe o nome"); return; }
        if (categoria.isEmpty()) { spinnerCategoria.setError("Selecione a categoria"); return; }
        if (preco.isEmpty())     { edtPreco.setError("Informe o preço"); return; }
        if (unidade.isEmpty())   { spinnerUnidade.setError("Selecione a unidade"); return; }

        double precoDouble = Double.parseDouble(preco.replace(",", "."));

        // UID do vendedor logado via Firebase
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "anonimo";

        setCarregando(true);

        if (imagemSelecionada != null) {
            // 1º: faz upload da imagem, depois salva o produto com a URL
            String nomeArquivo = uid + "_" + System.currentTimeMillis() + ".jpg";

            supabase.uploadImagem(imagemSelecionada, nomeArquivo, new SupabaseHelper.SupabaseCallback() {
                @Override
                public void onSuccess(String fotoUrl) {
                    // Imagem enviada → agora salva o produto com a URL
                    produtoRepository.inserirProduto(nome, categoria, precoDouble, unidade,
                            descricao, fotoUrl, uid, new ProdutoRepository.Callback() {
                                @Override
                                public void onSuccess(String r) {
                                    runOnUiThread(() -> {
                                        setCarregando(false);
                                        Toast.makeText(AdicionarProdutosActivity.this,
                                                "Produto salvo!", Toast.LENGTH_SHORT).show();
                                        setResult(RESULT_OK);
                                        finish();
                                    });
                                }
                                @Override
                                public void onError(String erro) {
                                    runOnUiThread(() -> {
                                        setCarregando(false);
                                        Toast.makeText(AdicionarProdutosActivity.this,
                                                "Erro ao salvar: " + erro, Toast.LENGTH_LONG).show();
                                    });
                                }
                            });
                }
                @Override
                public void onError(String erro) {
                    runOnUiThread(() -> {
                        setCarregando(false);
                        Toast.makeText(AdicionarProdutosActivity.this,
                                "Erro no upload da imagem: " + erro, Toast.LENGTH_LONG).show();
                    });
                }
            });

        } else {
            // Sem imagem — salva direto
            produtoRepository.inserirProduto(nome, categoria, precoDouble, unidade,
                    descricao, "", uid, new ProdutoRepository.Callback() {
                        @Override
                        public void onSuccess(String r) {
                            runOnUiThread(() -> {
                                setCarregando(false);
                                Toast.makeText(AdicionarProdutosActivity.this,
                                        "Produto salvo!", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            });
                        }
                        @Override
                        public void onError(String erro) {
                            runOnUiThread(() -> {
                                setCarregando(false);
                                Toast.makeText(AdicionarProdutosActivity.this,
                                        "Erro: " + erro, Toast.LENGTH_LONG).show();
                            });
                        }
                    });
            }
        }

    private void setCarregando(boolean carregando) {
        findViewById(R.id.btnSalvarProduto).setEnabled(!carregando);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}