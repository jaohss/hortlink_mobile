package com.example.hortlink.activities;

import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hortlink.BancoHelper;
import com.example.hortlink.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AdicionarProdutosActivity extends AppCompatActivity {

    private TextInputEditText edtNome, edtPreco, edtDescricao;
    private AutoCompleteTextView spinnerCategoria, spinnerUnidade;
    private android.widget.ImageView imgProduto;
    private android.widget.LinearLayout layoutPlaceholder;
    private Uri imagemSelecionada;
    BancoHelper database;
    // Launcher para abrir a galeria
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    // Salva a permissão permanentemente
                    getContentResolver().takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                    imagemSelecionada = uri;
                    imgProduto.setImageURI(uri);
                    imgProduto.setVisibility(android.view.View.VISIBLE);
                    layoutPlaceholder.setVisibility(android.view.View.GONE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_adicionar_produtos);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = new BancoHelper(this);

        // Toolbar com botão voltar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Adicionar Produto");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Views
        edtNome        = findViewById(R.id.edtNome);
        edtPreco       = findViewById(R.id.edtPreco);
        edtDescricao   = findViewById(R.id.edtDescricao);
        spinnerCategoria    = findViewById(R.id.spnCategoria);
        spinnerUnidade = findViewById(R.id.spinnerUnidade);
        imgProduto     = findViewById(R.id.imgProduto);
        layoutPlaceholder = findViewById(R.id.layoutPlaceholder);

        configurarSpinners();

        // Upload de foto
        findViewById(R.id.frameUploadFoto).setOnClickListener(v ->
                pickImage.launch("image/*"));

        // Salvar
        MaterialButton btnSalvar = findViewById(R.id.btnSalvarProduto);
        btnSalvar.setOnClickListener(v -> salvarProduto());

        // Cancelar
        MaterialButton btnCancelar = findViewById(R.id.btnCancelar);
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void configurarSpinners() {
        // Tipos
        String[] categorias = {"Fruta", "Legume", "Verdura"};
        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, categorias);
        spinnerCategoria.setAdapter(adapterCategoria);

        // Unidades de medida
        String[] unidades = {"kg", "g", "un", "dúzia", "maço", "caixa"};
        ArrayAdapter<String> adapterUnidade = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, unidades);
        spinnerUnidade.setAdapter(adapterUnidade);
    }

    private void salvarProduto() {
        String nome      = edtNome.getText().toString().trim();
        String categoria = spinnerCategoria.getText().toString().trim();
        String preco     = edtPreco.getText().toString().trim();
        String unidade   = spinnerUnidade.getText().toString().trim();
        String descricao = edtDescricao.getText().toString().trim();
        String foto      = imagemSelecionada != null ? imagemSelecionada.toString() : "";

        if (nome.isEmpty()) { edtNome.setError("Informe o nome"); return; }
        if (categoria.isEmpty()) { spinnerCategoria.setError("Selecione a categoria"); return; }
        if (preco.isEmpty()) { edtPreco.setError("Informe o preço"); return; }
        if (unidade.isEmpty()) { spinnerUnidade.setError("Selecione a unidade"); return; }

        // ✅ Agora sim — passando Strings para o banco
        double precoDouble = Double.parseDouble(preco.replace(",", "."));
        long resultado = database.inserirProduto(nome, categoria, precoDouble, unidade, descricao, foto);

        if (resultado != -1) {
            Toast.makeText(this, "Produto salvo com sucesso!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Erro ao salvar produto!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}