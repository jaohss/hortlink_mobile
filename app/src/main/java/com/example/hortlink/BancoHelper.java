package com.example.hortlink;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.hortlink.entidades.Produto;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class BancoHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "hortlink.db";
    private static final int DATABASE_VERSION = 3;

    // Nome da tabela e colunas – Para o caso de tabela única
    private static final String TABLE_NAME = "usuarios";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NOME = "nome";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_SENHA = "senha";
    private static final String COLUMN_TELEFONE = "telefone";

    // ✅ Nova tabela produtos
    private static final String TABLE_PRODUTOS = "produtos";
    private static final String COLUMN_PROD_ID = "id";
    private static final String COLUMN_PROD_NOME = "nome";
    private static final String COLUMN_PROD_CATEGORIA = "categoria";
    private static final String COLUMN_PROD_PRECO = "preco";
    private static final String COLUMN_PROD_UNIDADE = "unidade";
    private static final String COLUMN_PROD_DESCRICAO = "descricao";
    private static final String COLUMN_PROD_FOTO = "foto"; // caminho da imagem

    public BancoHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+ COLUMN_NOME + " TEXT, "
                + COLUMN_EMAIL + " TEXT," + COLUMN_SENHA + " TEXT," + COLUMN_TELEFONE + " TEXT)";
        db.execSQL(CREATE_TABLE);

        String CREATE_PRODUTOS = "CREATE TABLE produtos ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "nome TEXT, categoria TEXT, preco REAL, "
                + "unidade TEXT, descricao TEXT, foto TEXT)";
        db.execSQL(CREATE_PRODUTOS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS produtos");
        onCreate(db);
    }

    public long inserirUsuario(String nome, String email, String senha, String telefone)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOME, nome);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_SENHA, hashSenha(senha));
        values.put(COLUMN_TELEFONE, telefone);
        return db.insert(TABLE_NAME, null, values);
    }

    public Cursor listarUsuarios()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    public int atualizarUsuario(int id, String nome, String email)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOME, nome);
        values.put(COLUMN_EMAIL, email);
        return db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public int excluirUsuario(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    //Produtos
    public long inserirProduto(String nome, String categoria, double preco, String unidade, String descricao, String foto) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nome", nome);
        values.put("categoria", categoria);
        values.put("preco", preco);
        values.put("unidade", unidade);
        values.put("descricao", descricao);
        values.put("foto", foto);
        return db.insert("produtos", null, values);
    }

    public Cursor listarProdutos() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM produtos", null);
    }

    public int atualizarProduto(int id, String nome, String descricao, double preco, String foto) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nome", nome);
        values.put("descricao", descricao);
        values.put("preco", preco);
        if (!foto.isEmpty()) {
            values.put("foto", foto); // só atualiza foto se trocou
        }
        return db.update("produtos", values, "id=?", new String[]{String.valueOf(id)});
    }

    public int excluirProduto(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("produtos", "id=?", new String[]{String.valueOf(id)});
    }

    public List<Produto> listarProdutosComoObjetos() {
        List<Produto> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM produtos", null);

        if (cursor.moveToFirst()) {
            do {
                String nome     = cursor.getString(cursor.getColumnIndexOrThrow("nome"));
                String categoria     = cursor.getString(cursor.getColumnIndexOrThrow("categoria"));
                double preco    = cursor.getDouble(cursor.getColumnIndexOrThrow("preco"));
                String descricao = cursor.getString(cursor.getColumnIndexOrThrow("descricao"));
                String foto     = cursor.getString(cursor.getColumnIndexOrThrow("foto"));

                Produto p = new Produto(nome, preco, categoria, foto, descricao);
                lista.add(p);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return lista;
    }

    //SENHAS e autenticação
    public String hashSenha(String senha) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(senha.getBytes());
            StringBuilder hex = new StringBuilder();

            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean autenticar(String email, String senha) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM usuarios WHERE email = ? AND senha = ?",
                new String[]{email, hashSenha(senha)} // 🔥 mesma lógica
        );

        boolean autenticado = cursor.getCount() > 0;
        cursor.close();

        return autenticado;
    }
}
