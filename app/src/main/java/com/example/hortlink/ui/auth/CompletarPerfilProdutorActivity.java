package com.example.hortlink.ui.auth;

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
import com.example.hortlink.data.dto.CompletarPerfilComercioDTO;
import com.example.hortlink.data.dto.CoordenadasDTO;
import com.example.hortlink.data.dto.ViaCepResponse;
import com.example.hortlink.data.repository.ComercioRepository;
import com.example.hortlink.data.repository.GeoRepository;
import com.example.hortlink.service.BaseCallback;
import com.example.hortlink.util.SessionManager;

public class CompletarPerfilProdutorActivity extends AppCompatActivity {

    EditText edtNomeComercio, edtCidade, edtTelefone, edtDescricao, edtEstado, edtCep, edtBairro;
    Button btnConcluir;
    ProgressBar progressBar;

    private final GeoRepository geoRepository = new GeoRepository();
    private final ComercioRepository comercioRepository = new ComercioRepository();

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

        bindViews();
        configurarViaCep();
        preencherSeEdicao();
        configurarBotoes();
    }

    private void bindViews(){
        edtNomeComercio = findViewById(R.id.edtNomeComercio); // Novo campo
        edtCep = findViewById(R.id.edtCep);
        edtEstado = findViewById(R.id.edtEstado);
        edtCidade = findViewById(R.id.edtCidade);
        edtTelefone = findViewById(R.id.edtTelefone);
        edtDescricao = findViewById(R.id.edtDescricaoProd);
        edtBairro = findViewById(R.id.edtBairro);
        btnConcluir = findViewById(R.id.btnConcluir);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setVisibility(View.GONE);
    }

    private void configurarViaCep(){
        edtCep.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
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
        geoRepository.buscarEnderecoPorCep(cep, new BaseCallback<ViaCepResponse>() {
            @Override
            public void onSuccess(ViaCepResponse viaCep) {
                runOnUiThread(() -> {
                    setCarregando(false);
                    if (viaCep.getLocalidade() != null) edtCidade.setText(viaCep.getLocalidade());
                    if (viaCep.getUf() != null) edtEstado.setText(viaCep.getUf());
                    if (viaCep.getBairro() != null && !viaCep.getBairro().isEmpty()) {
                        edtBairro.setText(viaCep.getBairro());
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
                    Toast.makeText(CompletarPerfilProdutorActivity.this, motivo, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void configurarBotoes() {
        btnConcluir.setOnClickListener(v -> tentarSalvar());
    }

    private void tentarSalvar() {
        String nome = edtNomeComercio.getText().toString().trim();
        String bairro = edtBairro.getText().toString().trim();
        String cep = edtCep.getText().toString().trim();
        String cidade = edtCidade.getText().toString().trim();
        String estado = edtEstado.getText().toString().trim();
        String telefone = edtTelefone.getText().toString().trim();
        String descricao = edtDescricao.getText().toString().trim();

        if (nome.isEmpty()) {
            edtNomeComercio.setError("Informe o nome da propriedade");
            edtNomeComercio.requestFocus();
            return;
        }
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

        setCarregando(true);

        CompletarPerfilComercioDTO dto = new CompletarPerfilComercioDTO();
        dto.setNomeComercio(nome); // Novo campo
        dto.setTelefone(telefone);
        dto.setCep(cep);
        dto.setBairro(bairro);
        dto.setCidade(cidade);
        dto.setEstado(estado);
        dto.setDescricao(descricao);

        String queryNominatim = String.format("%s, %s, %s, Brasil", bairro, cidade, estado);

        geoRepository.buscarCoordenadas(queryNominatim, new BaseCallback<CoordenadasDTO>() {
            @Override
            public void onSuccess(CoordenadasDTO coordenadas) {
                dto.setLatitude(coordenadas.getLat());
                dto.setLongitude(coordenadas.getLng());
                enviarDTOCompletoParaAPI(dto);
            }
            @Override
            public void onError(String erro) {
                enviarDTOCompletoParaAPI(dto);
            }
        });
    }

    private void enviarDTOCompletoParaAPI(CompletarPerfilComercioDTO dto) {
        comercioRepository.completarPerfil(dto, new BaseCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                runOnUiThread(() -> {
                    setCarregando(false);
                    Toast.makeText(CompletarPerfilProdutorActivity.this, "Perfil atualizado! 🌱", Toast.LENGTH_SHORT).show();
                    SessionManager.getInstance().setCadastroCompleto();
                    irParaHome();
                });
            }
            @Override
            public void onError(String erro) {
                runOnUiThread(() -> {
                    setCarregando(false);
                    Toast.makeText(CompletarPerfilProdutorActivity.this, "Erro: " + erro, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void preencherSeEdicao() {
        boolean modoEdicao = getIntent().getBooleanExtra("modo_edicao", false);
        if (!modoEdicao) return;

        comercioRepository.buscarPerfil(new BaseCallback<CompletarPerfilComercioDTO>() {
            @Override
            public void onSuccess(CompletarPerfilComercioDTO perfil) {
                runOnUiThread(() -> {
                    if (perfil.getNomeComercio() != null) edtNomeComercio.setText(perfil.getNomeComercio());
                    if (perfil.getTelefone() != null) edtTelefone.setText(perfil.getTelefone());
                    if (perfil.getCep() != null) edtCep.setText(perfil.getCep());
                    if (perfil.getEstado() != null) edtEstado.setText(perfil.getEstado());
                    if (perfil.getCidade() != null) edtCidade.setText(perfil.getCidade());
                    if (perfil.getBairro() != null) edtBairro.setText(perfil.getBairro());
                    if (perfil.getDescricao() != null) edtDescricao.setText(perfil.getDescricao());
                });
            }
            @Override
            public void onError(String erro) {}
        });
    }

    private void setCarregando(boolean carregando) {
        progressBar.setVisibility(carregando ? View.VISIBLE : View.GONE);
        btnConcluir.setEnabled(!carregando);
    }

    private void irParaHome() {
        startActivity(new Intent(this, RoleRouterActivity.class));
        finish();
    }
}