package com.example.hortlink.ui.consumidor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hortlink.R;
import com.example.hortlink.adapters.CategoriaAdapter;
import com.example.hortlink.adapters.OfertaHomeAdapter;
import com.example.hortlink.adapters.ProdutoAdapter;
import com.example.hortlink.data.model.OfertaDTO;
import com.example.hortlink.data.repository.OfertaRepository;
import com.example.hortlink.service.BaseCallback;
import com.example.hortlink.ui.consumidor.DetalheProdutoActivity;
import com.example.hortlink.util.DistanciaUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HortLink.Home";

    private OfertaHomeAdapter adapter;
    private final List<OfertaDTO> todasOfertas = new ArrayList<>();
    private final List<OfertaDTO> ofertasFiltradas = new ArrayList<>();

    private RecyclerView recyclerProdutos;
    private View progressBar; // Lembre-se de instanciar isso no onViewCreated se usar no XML

    // Localização do consumidor — Double.MAX_VALUE = ainda não obtida / negada
    private double latConsumidor = Double.MAX_VALUE;
    private double lngConsumidor = Double.MAX_VALUE;

    private FusedLocationProviderClient fusedLocationClient;
    private final OfertaRepository ofertaRepository = new OfertaRepository();

    // ─── Launcher de permissão de localização ────────────────────────────────
    private final ActivityResultLauncher<String[]> permissaoLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    resultado -> {
                        boolean concedida = Boolean.TRUE.equals(resultado.get(Manifest.permission.ACCESS_FINE_LOCATION))
                                || Boolean.TRUE.equals(resultado.get(Manifest.permission.ACCESS_COARSE_LOCATION));

                        if (concedida) {
                            obterLocalizacaoEBuscarOfertas();
                        } else {
                            Log.w(TAG, "Permissão negada. Carregando sem ordenar por distância.");
                            carregarOfertasDaApi();
                        }
                    });

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerProdutos = view.findViewById(R.id.recyclerProdutos);
        // progressBar = view.findViewById(R.id.progressBar); // Descomente se tiver progressBar

        recyclerProdutos.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Inicializa o Adapter vazio. Preste atenção aos getters do OfertaDTO!
        adapter = new OfertaHomeAdapter(ofertasFiltradas, oferta -> {
            Intent intent = new Intent(getContext(), DetalheProdutoActivity.class);
            intent.putExtra("imagem_url", oferta.getImagemUrl());
            intent.putExtra("oferta_id", oferta.getId());
            startActivity(intent);
        });
        recyclerProdutos.setAdapter(adapter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        configurarCategorias(view);

        // Inicia o fluxo principal
        pedirPermissaoLocalizacao();
    }

    // ─── 1. Fluxo de GPS ─────────────────────────────────────────────────────

    private void pedirPermissaoLocalizacao() {
        boolean fineOk = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseOk = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (fineOk || coarseOk) {
            obterLocalizacaoEBuscarOfertas();
        } else {
            permissaoLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    @SuppressLint("MissingPermission") // Já checamos a permissão antes de chamar isso
    private void obterLocalizacaoEBuscarOfertas() {
        setCarregando(true);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        latConsumidor = location.getLatitude();
                        lngConsumidor = location.getLongitude();
                        Log.d(TAG, "GPS obtido: " + latConsumidor + ", " + lngConsumidor);
                    } else {
                        Log.w(TAG, "GPS retornou null. Pode estar desativado.");
                    }
                    // Independente de ter pego o GPS ou não, buscamos as ofertas
                    carregarOfertasDaApi();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao acessar GPS: " + e.getMessage());
                    carregarOfertasDaApi(); // Segue a vida sem GPS
                });
    }

    // ─── 2. Busca na API Spring Boot ─────────────────────────────────────────

    private void carregarOfertasDaApi() {
        setCarregando(true);

        ofertaRepository.listarOfertas(new BaseCallback<List<OfertaDTO>>() {
            @Override
            public void onSuccess(List<OfertaDTO> ofertas) {
                if (!isAdded() || getActivity() == null) return;

                // Com as ofertas na mão, calculamos a distância
                calcularDistanciasEOrdenar(ofertas);
            }

            @Override
            public void onError(String erro) {
                if (!isAdded() || getActivity() == null) return;
                setCarregando(false);
                Toast.makeText(getContext(), "Erro ao carregar catálogo: " + erro, Toast.LENGTH_LONG).show();
            }
        });
    }

    // ─── 3. Cálculo de Distância e Ordenação ─────────────────────────────────

    private void calcularDistanciasEOrdenar(List<OfertaDTO> listaApi) {
        // Se não temos a localização do usuário, apenas exibe como a API mandou
        if (latConsumidor == Double.MAX_VALUE) {
            exibirOfertas(listaApi);
            return;
        }

        // Calcula a distância para cada item
        for (OfertaDTO oferta : listaApi) {
            if (oferta.getLatitude() != null && oferta.getLongitude() != null) {
                double km = DistanciaUtil.calcularKm(
                        latConsumidor, lngConsumidor,
                        oferta.getLatitude(), oferta.getLongitude()
                );
                oferta.setDistanciaKm(km);
            } else {
                // Oferta sem GPS vai para o final da lista
                oferta.setDistanciaKm(Double.MAX_VALUE);
            }
        }

        // Ordena a lista do menor KM para o maior
        Collections.sort(listaApi, (o1, o2) -> {
            Double d1 = o1.getDistanciaKm() != null ? o1.getDistanciaKm() : Double.MAX_VALUE;
            Double d2 = o2.getDistanciaKm() != null ? o2.getDistanciaKm() : Double.MAX_VALUE;
            return Double.compare(d1, d2);
        });

        exibirOfertas(listaApi);
    }

    private void exibirOfertas(List<OfertaDTO> listaPronta) {
        requireActivity().runOnUiThread(() -> {
            setCarregando(false);

            todasOfertas.clear();
            todasOfertas.addAll(listaPronta);

            ofertasFiltradas.clear();
            ofertasFiltradas.addAll(listaPronta);

            adapter.notifyDataSetChanged();
        });
    }

    // ─── 4. Filtros de Categoria e UI ────────────────────────────────────────

    private void configurarCategorias(View view) {
        List<String> categorias = new ArrayList<>();
        categorias.add("Todos");
        categorias.add("Frutas");
        categorias.add("Verduras");
        categorias.add("Legumes");
        categorias.add("Temperos");

        RecyclerView recyclerCategorias = view.findViewById(R.id.recyclerCategorias);
        recyclerCategorias.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        CategoriaAdapter adapterCat = new CategoriaAdapter(categorias, categoriaSelecionada -> {
            // 1. Limpa o que está na tela atualmente
            ofertasFiltradas.clear();

            if (categoriaSelecionada.equals("Todos")) {
                // 2. Se for "Todos", puxa de volta tudo do backup
                ofertasFiltradas.addAll(todasOfertas);
            } else {
                // 3. Transforma "Frutas" em "FRUTA", "Verduras" em "VERDURA", etc.
                String filtroEnum = categoriaSelecionada.toUpperCase().replaceAll("S$", "");

                // 4. Procura na lista de backup (que nunca é apagada)
                for (OfertaDTO oferta : todasOfertas) {
                    // Proteção contra nulos e comparação exata com o nome do Enum
                    if (oferta.getCategoria() != null && oferta.getCategoria().name().equals(filtroEnum)) {
                        ofertasFiltradas.add(oferta);
                    }
                }
            }

            // 5. Manda a tela redesenhar apenas com os itens filtrados
            adapter.notifyDataSetChanged();
        });

        recyclerCategorias.setAdapter(adapterCat);
    }

    private void setCarregando(boolean carregando) {
        // Se você tiver um ProgressBar no XML, descomente a linha abaixo:
        // if (progressBar != null) progressBar.setVisibility(carregando ? View.VISIBLE : View.GONE);
        if (recyclerProdutos != null) {
            recyclerProdutos.setVisibility(carregando ? View.GONE : View.VISIBLE);
        }
    }
}