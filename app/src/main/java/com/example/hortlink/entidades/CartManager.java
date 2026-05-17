package com.example.hortlink.entidades;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.hortlink.data.model.CartItem;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private static CartManager instance;
    private static final String PREF_NAME = "hortlink_cart";
    private static final String KEY_ITEMS  = "cart_items";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();
    private List<CartItem> items;

    private CartManager(Context ctx) {
        prefs = ctx.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        items = loadFromPrefs();
    }

    public static CartManager getInstance(Context ctx) {
        if (instance == null) instance = new CartManager(ctx);
        return instance;
    }

    // Adiciona ou incrementa quantidade
    public void addItem(CartItem newItem) {
        for (CartItem item : items) {
            if (item.getProdutoId().equals(newItem.getProdutoId())) {
                item.setQuantidade(item.getQuantidade() + 1);
                save();
                return;
            }
        }
        items.add(newItem);
        save();
    }

    public void removeItem(String produtoId) {
        for (int i = 0; i < items.size(); i++) {           // ← sem lambda,
            if (items.get(i).getProdutoId().equals(produtoId)) { // sem ambiguidade
                items.remove(i);
                break;
            }
        }
        save();
    }

    public void clearCart() {
        items.clear();
        save();
    }

    public List<CartItem> getItems() { return items; }

    public double getTotal() {
        double total = 0;
        for (CartItem i : items) total += i.getSubtotal();
        return total;
    }

    public int getItemCount() {
        int count = 0;
        for (CartItem i : items) count += i.getQuantidade();
        return count;
    }

    private void save() {
        prefs.edit().putString(KEY_ITEMS, gson.toJson(items)).apply();
    }

    private List<CartItem> loadFromPrefs() {
        String json = prefs.getString(KEY_ITEMS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<CartItem>>(){}.getType();
        return gson.fromJson(json, type);
    }
}
