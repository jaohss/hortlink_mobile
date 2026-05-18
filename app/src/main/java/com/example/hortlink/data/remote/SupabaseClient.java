package com.example.hortlink.data.remote;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class SupabaseClient {

    private static final String URL = "https://dzfbtevidnfarlpnfysd.supabase.co";
    private static final String KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImR6ZmJ0ZXZpZG5mYXJscG5meXNkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzgyNTcwNzMsImV4cCI6MjA5MzgzMzA3M30.79uc9zT_T-HPoUhJMMyUMKsW4qS2kiCHuuBcpmv3sDQ";
    //private static final String BUCKET_NAME = "produtos";
    private static SupabaseClient instance;
    private final OkHttpClient http = new OkHttpClient();

    public static SupabaseClient getInstance() {
        if (instance == null) instance = new SupabaseClient();
        return instance;
    }

    // Monta uma Request.Builder já com os headers obrigatórios
    public Request.Builder baseRequest(String path) {
        return new Request.Builder()
                .url(URL + path)
                .addHeader("Authorization", "Bearer " + KEY)
                .addHeader("apikey", KEY)
                .addHeader("Content-Type", "application/json");
    }

    public OkHttpClient getHttp() { return http; }
    public String getStorageUrl() { return URL + "/storage/v1"; }
    public String getKey() { return KEY; }
}

