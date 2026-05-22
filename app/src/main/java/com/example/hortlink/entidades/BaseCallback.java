package com.example.hortlink.entidades;

public interface BaseCallback<T> {
    void onSuccess(T resultado);
    void onError(String erro);
}
