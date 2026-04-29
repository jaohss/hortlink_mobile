package com.example.hortlink;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.hortlink.entidades.Produto;
import com.example.hortlink.entidades.Produtor;

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
                + "unidade TEXT, descricao TEXT, foto   TEXT, "
                + "produtor_id INTEGER)";

        db.execSQL(CREATE_PRODUTOS);

        db.execSQL("CREATE TABLE produtores ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "nome TEXT, cidade TEXT, contato TEXT, "
                + "avaliacao REAL, foto TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS produtos");
        db.execSQL("DROP TABLE IF EXISTS produtores");
        onCreate(db);
    }

    //===================USUÁRIOS==================================================
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

    //===================PRODUTOS==================================================
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
                Produto p = new Produto();
                p.id       = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                p.nome     = cursor.getString(cursor.getColumnIndexOrThrow("nome"));
                p.categoria = cursor.getString(cursor.getColumnIndexOrThrow("categoria"));
                p.preco    = cursor.getDouble(cursor.getColumnIndexOrThrow("preco"));
                p.descricao = cursor.getString(cursor.getColumnIndexOrThrow("descricao"));
                p.imagemUri = cursor.getString(cursor.getColumnIndexOrThrow("foto"));
                p.produtorId = cursor.getInt(cursor.getColumnIndexOrThrow("produtor_id")); // ✅
                lista.add(p);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return lista;
    }

    public Produto buscarProdutoPorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("produtos", null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null);

        if (cursor.moveToFirst()) {
            Produto p = new Produto();
            p.id         = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            p.nome       = cursor.getString(cursor.getColumnIndexOrThrow("nome"));
            p.preco      = cursor.getDouble(cursor.getColumnIndexOrThrow("preco"));
            p.descricao  = cursor.getString(cursor.getColumnIndexOrThrow("descricao"));
            p.categoria  = cursor.getString(cursor.getColumnIndexOrThrow("categoria"));
            p.imagemUri  = cursor.getString(cursor.getColumnIndexOrThrow("foto")); // ✅ só URI
            p.produtorId = cursor.getInt(cursor.getColumnIndexOrThrow("produtor_id")); // ✅
            cursor.close();
            return p;
        }
        cursor.close();
        return null;
    }

    //========================PRODUTORES=======================================

    public long inserirProdutor(String nome, String cidade, String contato, double avaliacao, String foto) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nome", nome);
        values.put("cidade", cidade);
        values.put("contato", contato);
        values.put("avaliacao", avaliacao);
        values.put("foto", foto);
        return db.insert("produtores", null, values);
    }

    public Produtor buscarProdutorPorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("produtores", null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null);

        if (cursor.moveToFirst()) {
            Produtor p = new Produtor();
            p.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            p.nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"));
            p.cidade = cursor.getString(cursor.getColumnIndexOrThrow("cidade"));
            p.contato  = cursor.getString(cursor.getColumnIndexOrThrow("contato"));
            p.avaliacao = cursor.getDouble(cursor.getColumnIndexOrThrow("avaliacao"));
            p.fotoPerfilUri = cursor.getString(cursor.getColumnIndexOrThrow("foto"));
            cursor.close();
            return p;
        }
        cursor.close();
        return null;
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
