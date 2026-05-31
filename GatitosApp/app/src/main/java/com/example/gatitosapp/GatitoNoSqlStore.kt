package com.example.gatitosapp

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first

private val Context.gatitosDataStore by preferencesDataStore(name = "gatitos_nosql")

class GatitoNoSqlStore(private val context: Context) {

    private val gson = Gson()
    private val keyGatitos = stringPreferencesKey("gatitos_json")

    suspend fun obtenerTodos(): List<Gatito> {
        val preferences = context.gatitosDataStore.data.first()
        val json = preferences[keyGatitos] ?: return emptyList()

        val tipo = object : TypeToken<List<Gatito>>() {}.type
        return gson.fromJson(json, tipo)
    }

    suspend fun insertar(gatito: Gatito) {
        val listaActual = obtenerTodos().toMutableList()

        val nuevoGatito = gatito.copy(
            id = if (gatito.id == 0) System.currentTimeMillis().toInt() else gatito.id
        )

        listaActual.add(0, nuevoGatito)
        guardarLista(listaActual)
    }

    suspend fun actualizar(gatito: Gatito) {
        val listaActual = obtenerTodos().toMutableList()
        val index = listaActual.indexOfFirst { it.id == gatito.id }

        if (index != -1) {
            listaActual[index] = gatito
            guardarLista(listaActual)
        }
    }

    suspend fun eliminar(id: Int) {
        val listaActual = obtenerTodos().toMutableList()
        listaActual.removeAll { it.id == id }
        guardarLista(listaActual)
    }

    private suspend fun guardarLista(lista: List<Gatito>) {
        val json = gson.toJson(lista)

        context.gatitosDataStore.edit { preferences ->
            preferences[keyGatitos] = json
        }
    }
}