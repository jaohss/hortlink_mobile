package com.example.hortlink;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {

    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this; // Guarda a instância global assim que o app abre
    }

    // Método para qualquer classe pegar o Context sozinho
    public static Context getAppContext() {
        return instance.getApplicationContext();
    }
}
