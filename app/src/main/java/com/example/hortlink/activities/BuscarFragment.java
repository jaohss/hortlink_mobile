package com.example.hortlink.activities;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hortlink.bd.SupabaseHelper;

import java.util.List;

public class BuscarFragment extends Fragment {

    // Itens populares
    RecyclerView popularRec;

    SupabaseHelper sp;

     /*
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View root = inflater.inflate(R.layout.fragment_buscar, container, false);
        popularRec = root.findViewById(R.id.popularesR);

        popularRec.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        popularModelList = new ArrayList<>();

        // CORREÇÃO: A ordem dos parâmetros do adapter foi invertida para bater com o construtor (Lista primeiro, Contexto depois)
        popularAdapter = new PopularAdapter(popularModelList, getActivity());
        popularRec.setAdapter(popularAdapter);

        String SUPABASE_URL = "https://dzfbtevidnfarlpnfysd.supabase.co";
        String BUCKET_NAME = "produtos"; // Nome do seu bucket
        String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImR6ZmJ0ZXZpZG5mYXJscG5meXNkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzgyNTcwNzMsImV4cCI6MjA5MzgzMzA3M30.79uc9zT_T-HPoUhJMMyUMKsW4qS2kiCHuuBcpmv3sDQ";

        OkHttpClient client = new OkHttpClient();

        // O Supabase exige um JSON no corpo para listar arquivos de um bucket
        String jsonBody = "{\"prefix\": \"\", \"limit\": 100, \"offset\": 0, \"sortBy\": {\"column\": \"name\", \"order\": \"asc\"}}";
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonBody);

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/storage/v1/object/list/" + BUCKET_NAME)
                .post(body) // Para listar no storage, a requisição é POST
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("SupabaseStorage", "Erro ao listar bucket", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResposta = response.body().string();
                        JSONArray jsonArray = new JSONArray(jsonResposta);

                        popularModelList.clear();

                        // CORREÇÃO: Este é o único loop for necessário, e ele fica dentro do try-catch
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject arquivo = jsonArray.getJSONObject(i);
                            String nomeArquivo = arquivo.optString("name");

                            // Ignora o arquivo oculto .emptyFolderPlaceholder se existir
                            if (nomeArquivo.equals(".emptyFolderPlaceholder")) continue;

                            // Monta a URL pública baseada no nome do arquivo que retornou
                            String imgUrlPublica = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + nomeArquivo;

                            Log.d("SupabaseStorage", "Arquivo encontrado: " + imgUrlPublica);

                            // Cria o modelo e adiciona na lista
                            PopularModel model = new PopularModel(nomeArquivo, "Descrição do produto", imgUrlPublica);
                            popularModelList.add(model);
                        }

                        // Atualiza a tela na thread principal
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> popularAdapter.notifyDataSetChanged());
                        }

                    } catch (Exception e) {
                        Log.e("SupabaseStorage", "Erro no Parse", e);
                    }
                } else {
                    Log.w("SupabaseStorage", "Resposta da API não foi de sucesso: " + response.code());
                }
            }
        });

        return root;


    }
    */
}