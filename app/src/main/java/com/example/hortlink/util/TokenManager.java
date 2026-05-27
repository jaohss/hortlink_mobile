package com.example.hortlink.util;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {

    private static TokenManager instance;
    private SharedPreferences prefs;

    private static final String PREF_NAME = "HortiLink_Auth";
    private static final String KEY_TOKEN = "jwt_token";

    private TokenManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context);
        }
        return instance;
    }

    public void salvarToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void limparToken() {
        prefs.edit().remove(KEY_TOKEN).apply();
    }
}
