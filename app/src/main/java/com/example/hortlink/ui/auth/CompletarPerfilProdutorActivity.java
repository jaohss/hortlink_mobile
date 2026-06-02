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

public class CompletarPerfilProdutorActivity extends AppCompatActivity {

    EditText edtCidade, edtTelefone, edtDescricao, edtEstado, edtCep, edtBairro;
    Button btnConcluir, btnPular;
    ProgressBar progressBar;

    // Inicializando os repositórios para evitar NullPointerException!
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

        edtCidade.setEnabled(true);
        edtEstado.setEnabled(true);
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

                    String cidade = viaCep.getLocalidade();
                    String estado = viaCep.getUf();
                    String bairro = viaCep.getBairro();

                    if (cidade != null) edtCidade.setText(cidade);
                    if (estado != null) edtEstado.setText(estado);

                    if (bairro != null && !bairro.trim().isEmpty()) {
                        edtBairro.setText(bairro);
                        edtTelefone.requestFocus();
                    } else {
                        edtBairro.setText("");
                        edtBairro.requestFocus();
                    }
                });
            }

            @Override
            public void onError(String motivo) {
                runOnUiThread(() -> {
                    setCarregando(false);
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

    // ─── A NOVA LÓGICA DO MAESTRO ──────────────────────────────────────
    private void tentarSalvar() {
        String bairro = edtBairro.getText().toString().trim();
        String cep = edtCep.getText().toString().trim();
        String cidade = edtCidade.getText().toString().trim();
        String estado = edtEstado.getText().toString().trim();
        String telefone = edtTelefone.getText().toString().trim();
        String descricao = edtDescricao.getText().toString().trim();

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

        // Agora o CEP passa a ser obrigatório, pois não podemos calcular GPS sem ele!
        if (cep.isEmpty()) {
            edtCep.setError("Informe seu CEP para localizarmos sua fazenda/comércio");
            edtCep.requestFocus();
            return;
        }

        setCarregando(true);

        // 1. Monta o DTO Base
        CompletarPerfilComercioDTO dto = new CompletarPerfilComercioDTO();
        dto.setTelefone(telefone);
        dto.setCep(cep);
        dto.setBairro(bairro);
        dto.setCidade(cidade);
        dto.setEstado(estado);
        dto.setDescricao(descricao);

        // 2. Transforma o CEP em Lat/Lng antes de enviar
        geoRepository.buscarCoordenadas(cep, new BaseCallback<CoordenadasDTO>() {
            @Override
            public void onSuccess(CoordenadasDTO coordenadas) {
                // 3. Sucesso! Injeta as coordenadas no DTO
                dto.setLatitude(coordenadas.getLat());
                dto.setLongitude(coordenadas.getLng());

                // 4. Manda o DTO recheado para o Spring Boot
                enviarDTOCompletoParaAPI(dto);
            }

            @Override
            public void onError(String erro) {
                runOnUiThread(() -> {
                    setCarregando(false);
                    // Opcional: Se o Nominatim falhar, você quer deixar ele salvar sem GPS ou bloquear o cadastro?
                    // No HortiLink, o foco é na proximidade, então bloqueamos e avisamos:
                    Toast.makeText(CompletarPerfilProdutorActivity.this, "Erro de geolocalização: " + erro, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void enviarDTOCompletoParaAPI(CompletarPerfilComercioDTO dto) {
        comercioRepository.completarPerfil(dto, new BaseCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
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
        });
    }

    // ─── Helpers Finais ───────────────────────────────────────────────

    private void preencherSeEdicao() {
        boolean modoEdicao = getIntent().getBooleanExtra("modo_edicao", false);
        if (!modoEdicao) return;

        setCarregando(true);

        comercioRepository.buscarPerfil(new BaseCallback<CompletarPerfilComercioDTO>() {
            @Override
            public void onSuccess(CompletarPerfilComercioDTO perfil) {
                runOnUiThread(() -> {
                    setCarregando(false);
                    if (perfil.getTelefone() != null) edtTelefone.setText(perfil.getTelefone());
                    if (perfil.getCep() != null) edtCep.setText(perfil.getCep());
                    if (perfil.getEstado() != null) edtEstado.setText(perfil.getEstado());
                    if (perfil.getCidade() != null) edtCidade.setText(perfil.getCidade());
                    if (perfil.getBairro() != null) edtBairro.setText(perfil.getBairro());
                    if (perfil.getDescricao() != null) edtDescricao.setText(perfil.getDescricao());
                });
            }

            @Override
            public void onError(String erro) {
                runOnUiThread(() -> {
                    setCarregando(false);
                    Toast.makeText(CompletarPerfilProdutorActivity.this,
                            "Erro ao carregar dados antigos", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setCarregando(boolean carregando) {
        progressBar.setVisibility(carregando ? View.VISIBLE : View.GONE);
        btnConcluir.setEnabled(!carregando);
        btnPular.setEnabled(!carregando);
        edtCep.setEnabled(!carregando);
    }

    private void irParaHome() {
        startActivity(new Intent(this, RoleRouterActivity.class));
        finish();
    }
}