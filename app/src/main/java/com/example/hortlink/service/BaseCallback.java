package com.example.hortlink.service;

public interface BaseCallback<T> {
    void onSuccess(T resultado);
    void onError(String erro);
}
