package com.example.hortlink.ui.consumidor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hortlink.R;
import com.example.hortlink.adapters.CategoriaAdapter;
import com.example.hortlink.adapters.ProdutoAdapter;
import com.example.hortlink.data.model.Produto;
import com.example.hortlink.data.repository.GeoRepository;
import com.example.hortlink.data.repository.ProdutoRepository;
import com.example.hortlink.data.repository.ProdutorRepository;
import com.example.hortlink.util.DistanciaUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeFragment extends Fragment {

    private static final String TAG = "HortLink.HomeFragment";

    private ProdutoAdapter adapter;
    private List<Produto> todosProdutos    = new ArrayList<>();
    private List<Produto> produtosFiltrados = new ArrayList<>();

    private RecyclerView recyclerProdutos;

    // Localização do consumidor — Double.MAX_VALUE = ainda não obtida / negada
    private double latConsumidor = Double.MAX_VALUE;
    private double lngConsumidor = Double.MAX_VALUE;

    private FusedLocationProviderClient fusedLocation;
    private final GeoRepository geoRepository = new GeoRepository();

    // Referência ao LocationCallback para poder remover depois de uma leitura
    private LocationCallback locationCallback;

    // Launcher de permissão de localização
    private final ActivityResultLauncher<String[]> permissaoLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    resultado -> {
                        boolean concedida =
                                Boolean.TRUE.equals(resultado.get(Manifest.permission.ACCESS_FINE_LOCATION))
                                        || Boolean.TRUE.equals(resultado.get(Manifest.permission.ACCESS_COARSE_LOCATION));

                        if (concedida) {
                            obterLocalizacao();
                        } else {
                            Log.w(TAG, "Permissão de localização negada pelo usuário.");
                            // Sem localização — carrega produtos sem ordenar
                            carregarProdutos();
                        }
                    });


    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerProdutos = view.findViewById(R.id.recyclerProdutos);
        recyclerProdutos.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Adapter começa vazio — dados chegam do Supabase
        adapter = new ProdutoAdapter(produtosFiltrados, produto -> {
            Intent intent = new Intent(getContext(), DetalheProdutoActivity.class);
            intent.putExtra("produto_id", produto.id);
            startActivity(intent);
        });
        recyclerProdutos.setAdapter(adapter);

        fusedLocation = LocationServices.getFusedLocationProviderClient(requireActivity());

        configurarCategorias(view);
        pedirPermissaoLocalizacao();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Garante que não ficam callbacks pendurados se o fragment for destruído
        if (locationCallback != null && fusedLocation != null) {
            fusedLocation.removeLocationUpdates(locationCallback);
            locationCallback = null;
        }
    }

    // ─── 1. Verifica / solicita permissão ────────────────────────────

    private void pedirPermissaoLocalizacao() {
        boolean fineOk  = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)   == PackageManager.PERMISSION_GRANTED;
        boolean coarseOk = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (fineOk || coarseOk) {
            obterLocalizacao();
        } else {
            permissaoLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    // ─── 2. Obtém localização (cache → ativo como fallback) ──────────

    private void obterLocalizacao() {
        try {
            // Tenta o cache primeiro (getLastLocation) — instantâneo quando disponível
            fusedLocation.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            // CORREÇÃO 2a: getLastLocation() retornou algo válido
                            Log.d(TAG, "Localização obtida do cache: "
                                    + location.getLatitude() + ", " + location.getLongitude());
                            latConsumidor = location.getLatitude();
                            lngConsumidor = location.getLongitude();
                            carregarProdutos();
                        } else {
                            // CORREÇÃO 2b: cache vazio → pede leitura ativa (uma vez só)
                            Log.d(TAG, "Cache de localização vazio — solicitando leitura ativa.");
                            solicitarLocalizacaoAtiva();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Falha ao obter lastLocation: " + e.getMessage());
                        carregarProdutos(); // segue sem localização
                    });

        } catch (SecurityException e) {
            // CORREÇÃO 3: loga a exceção em vez de engolir silenciosamente
            Log.e(TAG, "SecurityException — permissão revogada após concessão: " + e.getMessage());
            carregarProdutos();
        }
    }

    /**
     * Solicita UMA atualização de localização ativa via FusedLocationProvider.
     * Usado como fallback quando getLastLocation() retorna null
     * (emulador, GPS recém-ligado, primeiro uso do app).
     */
    @SuppressLint("MissingPermission") // permissão já verificada em pedirPermissaoLocalizacao()
    private void solicitarLocalizacaoAtiva() {
        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY, 5000L)
                .setMaxUpdates(1) // apenas uma leitura — não fica em loop
                .setWaitForAccurateLocation(false)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult resultado) {
                // Remove o callback imediatamente — já temos o que precisamos
                fusedLocation.removeLocationUpdates(this);
                locationCallback = null;

                Location loc = resultado.getLastLocation();
                if (loc != null) {
                    Log.d(TAG, "Localização ativa obtida: "
                            + loc.getLatitude() + ", " + loc.getLongitude());
                    latConsumidor = loc.getLatitude();
                    lngConsumidor = loc.getLongitude();
                } else {
                    Log.w(TAG, "onLocationResult retornou location nula.");
                }

                // Segue com ou sem localização
                carregarProdutos();
            }
        };

        try {
            fusedLocation.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());

            // Timeout de segurança: se em 8s não chegou nada, carrega sem localização
            recyclerProdutos.postDelayed(() -> {
                if (locationCallback != null) {
                    Log.w(TAG, "Timeout de localização ativa — carregando sem ordenação.");
                    fusedLocation.removeLocationUpdates(locationCallback);
                    locationCallback = null;
                    carregarProdutos();
                }
            }, 8000L);

        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException em solicitarLocalizacaoAtiva: " + e.getMessage());
            carregarProdutos();
        }
    }

    // ─── 3. Busca produtos no Supabase ───────────────────────────────

    private void carregarProdutos() {
        // Evita chamadas duplicadas (ex.: timeout disparou depois do callback normal)
        if (!isAdded()) return;

        setCarregando(true);

        new ProdutoRepository().listarProdutos(new ProdutoRepository.Callback() {
            @Override
            public void onSuccess(String json) {
                if (!isAdded()) return;

                List<Produto> lista = parseProdutos(json);

                // Se não tem localização do consumidor, exibe sem ordenar
                if (latConsumidor == Double.MAX_VALUE) {
                    Log.d(TAG, "Sem localização — exibindo produtos sem ordenação.");
                    exibirProdutos(lista);
                    return;
                }

                calcularDistanciasEOrdenar(lista);
            }

            @Override
            public void onError(String erro) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    setCarregando(false);
                    Toast.makeText(getContext(),
                            "Erro ao carregar produtos: " + erro, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    // ─── 4. Calcula distância de cada produto via CEP do produtor ────

    private void calcularDistanciasEOrdenar(List<Produto> lista) {
        if (lista.isEmpty()) {
            exibirProdutos(lista);
            return;
        }

        AtomicInteger pendentes = new AtomicInteger(lista.size());
        ProdutorRepository produtorRepo = new ProdutorRepository();

        for (Produto produto : lista) {
            String produtorId = produto.produtorId;

            if (produtorId == null || produtorId.isEmpty()) {
                // Produto sem produtor vinculado — vai pro final da lista
                Log.d(TAG, "Produto '" + produto.nome + "' sem produtorId — pulando distância.");
                if (pendentes.decrementAndGet() == 0) ordenarEExibir(lista);
                continue;
            }

            produtorRepo.buscarPorId(produtorId, new ProdutorRepository.CallbackUnico() {

                @Override
                public void onSuccess(com.example.hortlink.data.model.Produtor produtor) {
                    String cep = produtor.getUsuario().cep;

                    if (cep == null || cep.isEmpty()) {
                        Log.d(TAG, "Produtor sem CEP — produto vai pro final.");
                        if (pendentes.decrementAndGet() == 0) ordenarEExibir(lista);
                        return;
                    }

                    geoRepository.buscarCoordenadas(cep, new GeoRepository.CallbackCoordenadas() {

                        @Override
                        public void onSuccess(double latProdutor, double lngProdutor) {
                            double km = DistanciaUtil.calcularKm(
                                    latConsumidor, lngConsumidor,
                                    latProdutor,   lngProdutor);
                            produto.setDistanciaKm(km);
                            Log.d(TAG, "Distância para '" + produto.nome + "': " + km + " km");

                            if (pendentes.decrementAndGet() == 0) ordenarEExibir(lista);
                        }

                        @Override
                        public void onError(String erro) {
                            Log.w(TAG, "Erro ao obter coordenadas (CEP=" + cep + "): " + erro);
                            if (pendentes.decrementAndGet() == 0) ordenarEExibir(lista);
                        }
                    });
                }

                @Override
                public void onError(String erro) {
                    Log.w(TAG, "Erro ao buscar produtor '" + produtorId + "': " + erro);
                    if (pendentes.decrementAndGet() == 0) ordenarEExibir(lista);
                }
            });
        }
    }

    // ─── 5. Ordena por distância crescente e exibe ────────────────────

    private void ordenarEExibir(List<Produto> lista) {
        // Produtos sem distância (Double.MAX_VALUE) ficam no final naturalmente
        Collections.sort(lista, (a, b) ->
                Double.compare(a.getDistanciaKm(), b.getDistanciaKm()));
        exibirProdutos(lista);
    }

    private void exibirProdutos(List<Produto> lista) {
        if (!isAdded() || getActivity() == null) return;
        requireActivity().runOnUiThread(() -> {
            if (!isAdded()) return;
            setCarregando(false);
            todosProdutos.clear();
            todosProdutos.addAll(lista);
            produtosFiltrados.clear();
            produtosFiltrados.addAll(lista);
            adapter.notifyDataSetChanged();
        });
    }

    // ─── 6. Converte JSON do Supabase → List<Produto> ────────────────

    private List<Produto> parseProdutos(String json) {
        List<Produto> lista = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                Produto p = new Produto(
                        obj.optString("id"),
                        obj.optString("nome"),
                        obj.optDouble("preco", 0.0),
                        obj.optString("categoria"),
                        obj.optString("foto_url"),
                        obj.optString("descricao"),
                        obj.optString("unidade")
                );

                // CORREÇÃO 1: popula produtorId a partir do JSON
                // Verifique no Supabase se a coluna se chama exatamente "produtor_id"
                p.produtorId = obj.optString("produtor_id", null);

                if (p.produtorId == null || p.produtorId.isEmpty()) {
                    Log.w(TAG, "Produto '" + p.nome + "' veio sem produtor_id no JSON.");
                }

                lista.add(p);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao parsear produtos: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    // ─── Filtro por categoria ─────────────────────────────────────────

    private void configurarCategorias(View view) {
        List<String> categorias = new ArrayList<>();
        categorias.add("Todos");
        categorias.add("Frutas");
        categorias.add("Verduras");
        categorias.add("Legumes");

        RecyclerView recyclerCategorias = view.findViewById(R.id.recyclerCategorias);
        recyclerCategorias.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        CategoriaAdapter adapterCat = new CategoriaAdapter(categorias, categoriaSelecionada -> {
            produtosFiltrados.clear();

            if (categoriaSelecionada.equals("Todos")) {
                produtosFiltrados.addAll(todosProdutos);
            } else {
                String filtro = categoriaSelecionada.toLowerCase().replaceAll("s$", "");
                for (Produto p : todosProdutos) {
                    if (p.categoria != null &&
                            p.categoria.toLowerCase().startsWith(filtro)) {
                        produtosFiltrados.add(p);
                    }
                }
            }

            adapter.notifyDataSetChanged();
        });

        recyclerCategorias.setAdapter(adapterCat);
    }

    private void setCarregando(boolean carregando) {
        recyclerProdutos.setVisibility(carregando ? View.GONE : View.VISIBLE);
    }
}