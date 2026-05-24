package com.example.hortlink.ui.consumidor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeFragment extends Fragment {

    private ProdutoAdapter adapter;
    private List<Produto> todosProdutos    = new ArrayList<>();
    private List<Produto> produtosFiltrados = new ArrayList<>();

    private RecyclerView recyclerProdutos;

    // Localização do consumidor — null se negou permissão
    private double latConsumidor = Double.MAX_VALUE;
    private double lngConsumidor = Double.MAX_VALUE;

    private FusedLocationProviderClient fusedLocation;
    private final GeoRepository geoRepository = new GeoRepository();

    // Launcher de permissão de localização
    private final ActivityResultLauncher<String[]> permissaoLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            resultado -> {
                        boolean concedida = Boolean.TRUE.equals(
                                resultado.get(Manifest.permission.ACCESS_FINE_LOCATION))
                                || Boolean.TRUE.equals(
                                resultado.get(Manifest.permission.ACCESS_COARSE_LOCATION));

                        if (concedida) {
                            obterLocalizacao();
                        } else {
                            // Sem localização — carrega produtos sem ordenar
                            carregarProdutos();
                        }
                    }
     );


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
        // progressBar = view.findViewById(R.id.progressBar); // descomente se tiver

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
        //carregarProdutos();
    }

    private void pedirPermissaoLocalizacao() {
        boolean fineOk = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
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

    // ─── 2. Localização do consumidor ─────────────────────────────────

    private void obterLocalizacao() {
        try {
            fusedLocation.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            latConsumidor = location.getLatitude();
                            lngConsumidor = location.getLongitude();
                        }
                        carregarProdutos(); // segue com ou sem localização
                    })
                    .addOnFailureListener(e -> carregarProdutos());
        } catch (SecurityException e) {
            carregarProdutos();
        }
    }


    // ─── Busca produtos no ProdutoRepository/Supabase ──────────────────────────────
    private void carregarProdutos() {
        setCarregando(true);

        new ProdutoRepository().listarProdutos(new ProdutoRepository.Callback() {
            @Override
            public void onSuccess(String json) {
                if (!isAdded()) return;

                List<Produto> lista = parseProdutos(json);

                // Se não tem localização do consumidor, exibe sem ordenar
                if (latConsumidor == Double.MAX_VALUE) {
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

    // ─── 4. Calcula distância de cada produto via CEP do produtor ─────
    private void calcularDistanciasEOrdenar(List<Produto> lista) {
        if (lista.isEmpty()) {
            exibirProdutos(lista);
            return;
        }

        // Conta quantas distâncias ainda faltam calcular
        AtomicInteger pendentes = new AtomicInteger(lista.size());

        ProdutorRepository produtorRepo = new ProdutorRepository();

        for (Produto produto : lista) {
            String produtorId = produto.produtorId;

            if (produtorId == null || produtorId.isEmpty()) {
                // Produto sem produtor vinculado — vai pro final
                if (pendentes.decrementAndGet() == 0) ordenarEExibir(lista);
                continue;
            }

            // Busca o CEP do produtor
            produtorRepo.buscarPorId(produtorId, new ProdutorRepository.CallbackUnico() {

                @Override
                public void onSuccess(com.example.hortlink.data.model.Produtor produtor) {
                    String cep = produtor.getUsuario().cep; // ← pega o CEP

                    if (cep == null || cep.isEmpty()) {
                        if (pendentes.decrementAndGet() == 0) ordenarEExibir(lista);
                        return;
                    }

                    // Converte CEP em coordenadas
                    geoRepository.buscarCoordenadas(cep, new GeoRepository.CallbackCoordenadas() {

                        @Override
                        public void onSuccess(double latProdutor, double lngProdutor) {
                            double km = DistanciaUtil.calcularKm(
                                    latConsumidor, lngConsumidor,
                                    latProdutor,   lngProdutor);
                            produto.setDistanciaKm(km);

                            if (pendentes.decrementAndGet() == 0) ordenarEExibir(lista);
                        }

                        @Override
                        public void onError(String erro) {
                            // CEP não resolvido — produto vai pro final
                            if (pendentes.decrementAndGet() == 0) ordenarEExibir(lista);
                        }
                    });
                }

                @Override
                public void onError(String erro) {
                    if (pendentes.decrementAndGet() == 0) ordenarEExibir(lista);
                }
            });
        }
    }

    // ─── 5. Ordena por distância crescente e exibe ────────────────────

    private void ordenarEExibir(List<Produto> lista) {
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

    // ─── Converte JSON do Supabase → List<Produto> ───────────────
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
                        obj.optString("foto_url"),   // ← vira imagemUri
                        obj.optString("descricao"),
                        obj.optString("unidade")
                );

                lista.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    // ─── Filtro por categoria ────────────────────────────────────
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
                // Normaliza para comparação (ex: "Frutas" bate com "Fruta" do banco)
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
        // if (progressBar != null)
        //     progressBar.setVisibility(carregando ? View.VISIBLE : View.GONE);
        recyclerProdutos.setVisibility(carregando ? View.GONE : View.VISIBLE);
    }
}