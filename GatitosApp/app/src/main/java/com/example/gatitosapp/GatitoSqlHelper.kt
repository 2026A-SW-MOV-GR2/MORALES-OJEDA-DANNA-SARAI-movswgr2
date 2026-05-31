package com.example.gatitosapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class GatitoSqlHelper(context: Context) :
    SQLiteOpenHelper(context, "gatitos_sql.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE gatitos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                descripcion TEXT NOT NULL,
                fecha TEXT NOT NULL,
                vacunado INTEGER NOT NULL,
                emoji TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS gatitos")
        onCreate(db)
    }

    fun obtenerTodos(): List<Gatito> {
        val lista = mutableListOf<Gatito>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM gatitos ORDER BY id DESC", null)

        cursor.use {
            while (it.moveToNext()) {
                lista.add(
                    Gatito(
                        id = it.getInt(it.getColumnIndexOrThrow("id")),
                        nombre = it.getString(it.getColumnIndexOrThrow("nombre")),
                        descripcion = it.getString(it.getColumnIndexOrThrow("descripcion")),
                        fecha = it.getString(it.getColumnIndexOrThrow("fecha")),
                        vacunado = it.getInt(it.getColumnIndexOrThrow("vacunado")) == 1,
                        emoji = it.getString(it.getColumnIndexOrThrow("emoji"))
                    )
                )
            }
        }

        return lista
    }

    fun insertar(gatito: Gatito) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nombre", gatito.nombre)
            put("descripcion", gatito.descripcion)
            put("fecha", gatito.fecha)
            put("vacunado", if (gatito.vacunado) 1 else 0)
            put("emoji", gatito.emoji)
        }

        db.insert("gatitos", null, values)
    }

    fun actualizar(gatito: Gatito) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nombre", gatito.nombre)
            put("descripcion", gatito.descripcion)
            put("fecha", gatito.fecha)
            put("vacunado", if (gatito.vacunado) 1 else 0)
            put("emoji", gatito.emoji)
        }

        db.update(
            "gatitos",
            values,
            "id = ?",
            arrayOf(gatito.id.toString())
        )
    }

    fun eliminar(id: Int) {
        val db = writableDatabase
        db.delete("gatitos", "id = ?", arrayOf(id.toString()))
    }
}