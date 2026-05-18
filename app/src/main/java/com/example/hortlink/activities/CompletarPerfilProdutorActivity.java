package com.example.hortlink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hortlink.R;
import com.example.hortlink.data.repository.ProdutoRepository;
import com.example.hortlink.data.repository.ProdutorRepository;
import com.example.hortlink.services.ViacepService;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CompletarPerfilProdutorActivity extends AppCompatActivity {

    EditText edtCidade, edtTelefone, edtDescricao, edtEstado, edtCep, edtBairro;
    Button btnConcluir, btnPular;
    ProgressBar progressBar;
    private String uid;

    private ProdutorRepository produtorRepository = new ProdutorRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_completar_perfil_produtor);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        uid = getIntent().getStringExtra("uid");

        bindViews();
        configurarViaCep();
        configurarBotoes();
    }

    private void bindViews(){
        edtCep = findViewById(R.id.edtCep);
        edtEstado = findViewById(R.id.edtEstado);
        edtCidade = findViewById(R.id.edtCidade);
        edtTelefone = findViewById(R.id.edtTelefone);
        edtDescricao = findViewById(R.id.edtDescricaoProd);
        edtBairro = findViewById(R.id.edtBairro);
        btnConcluir = findViewById(R.id.btnConcluir);
        btnPular = findViewById(R.id.btnPular);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setVisibility(View.GONE);

        // Cidade e estado são preenchidos pelo ViaCEP, mas o usuário
        // pode editar manualmente caso a API falhe ou o dado venha errado.
        edtCidade.setEnabled(true);
        edtEstado.setEnabled(true);
    }

    private void configurarViaCep(){
        edtCep.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // Remove tudo que não for dígito para comparar o tamanho real
                String cepLimpo = s.toString().replaceAll("[^0-9]", "");
                if (cepLimpo.length() == 8) {
                    buscarCep(cepLimpo);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

        });
    }

    private void buscarCep(String cep){
        setCarregando(true);

        ViacepService.buscar(cep, new ViacepService.Callback() {
            @Override
            public void onSuccess(String bairro, String cidade, String estado) {
                runOnUiThread(() -> {
                    setCarregando(false);
                    edtCidade.setText(cidade);
                    edtEstado.setText(estado);

                    if (!bairro.isEmpty()) {
                        edtBairro.setText(bairro);
                        edtTelefone.requestFocus();
                    } else {
                        edtBairro.requestFocus();
                    }
                });
            }

            @Override
            public void onError(String motivo) {
                runOnUiThread(() -> {
                    setCarregando(false);
                    // Avisa o usuário mas não bloqueia — ele pode preencher manualmente
                    Toast.makeText(
                            CompletarPerfilProdutorActivity.this,
                            motivo + "\nPreencha cidade e estado manualmente.",
                            Toast.LENGTH_LONG
                    ).show();
                    edtCidade.requestFocus();
                });
            }
        });
    }

    private void configurarBotoes() {
        btnConcluir.setOnClickListener(v -> tentarSalvar());

        btnPular.setOnClickListener(v -> irParaHome());
    }

    private void tentarSalvar() {
        String bairro = edtBairro.getText().toString().trim();
        String cep = edtCep.getText().toString().trim();
        String cidade = edtCidade.getText().toString().trim();
        String estado = edtEstado.getText().toString().trim();
        String telefone = edtTelefone.getText().toString().trim();
        String descricao = edtDescricao.getText().toString().trim();

        // Campos obrigatórios: telefone, cidade e estado.
        // CEP e descrição são complementares — não bloqueiam o cadastro.
        if (telefone.isEmpty()) {
            edtTelefone.setError("Informe seu telefone");
            edtTelefone.requestFocus();
            return;
        }

        if (cidade.isEmpty()) {
            edtCidade.setError("Informe sua cidade");
            edtCidade.requestFocus();
            return;
        }

        if (estado.isEmpty()) {
            edtEstado.setError("Informe seu estado");
            edtEstado.requestFocus();
            return;
        }

        setCarregando(true);

        produtorRepository.completarPerfil(uid, telefone, cep, bairro, cidade, estado, descricao, new ProdutorRepository.CallbackSimples() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            setCarregando(false);
                            Toast.makeText(
                                    CompletarPerfilProdutorActivity.this,
                                    "Perfil completo! Bem-vindo ao Hortlink 🌱",
                                    Toast.LENGTH_SHORT
                            ).show();
                            irParaHome();
                        });
                    }

                    @Override
                    public void onError(String erro) {
                        runOnUiThread(() -> {
                            setCarregando(false);
                            Toast.makeText(
                                    CompletarPerfilProdutorActivity.this,
                                    "Erro ao salvar perfil: " + erro,
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }


    // ─── Helpers de UI ────────────────────────────────────────────────

    /**
     * Alterna entre estado de carregamento e estado interativo.
     * Centralizado aqui para não repetir em cada callback.
     */
    private void setCarregando(boolean carregando) {
        progressBar.setVisibility(carregando ? View.VISIBLE : View.GONE);
        btnConcluir.setEnabled(!carregando);
        btnPular.setEnabled(!carregando);
        edtCep.setEnabled(!carregando);
    }

    private void irParaHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

}