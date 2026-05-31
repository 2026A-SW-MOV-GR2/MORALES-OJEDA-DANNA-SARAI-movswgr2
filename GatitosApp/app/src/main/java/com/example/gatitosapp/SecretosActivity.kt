package com.example.gatitosapp

import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.secretosDataStore by preferencesDataStore(name = "secretos_datastore")

class SecretosActivity : AppCompatActivity() {

    private lateinit var etKey: EditText
    private lateinit var etValue: EditText
    private lateinit var spinnerTipo: Spinner
    private lateinit var btnGuardar: Button
    private lateinit var btnRecuperar: Button
    private lateinit var txtResultado: TextView
    private lateinit var btnVolver: Button

    private val opciones = listOf(
        "SharedPreferences",
        "DataStore",
        "EncryptedSharedPreferences"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secretos)

        etKey = findViewById(R.id.etKey)
        etValue = findViewById(R.id.etValue)
        spinnerTipo = findViewById(R.id.spinnerTipo)
        btnGuardar = findViewById(R.id.btnGuardarSecreto)
        btnRecuperar = findViewById(R.id.btnRecuperarSecreto)
        txtResultado = findViewById(R.id.txtResultadoSecreto)
        btnVolver = findViewById(R.id.btnVolverSecretos)

        spinnerTipo.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            opciones
        )

        btnGuardar.setOnClickListener {
            guardarSecreto()
        }

        btnRecuperar.setOnClickListener {
            recuperarSecreto()
        }

        btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun guardarSecreto() {
        val key = etKey.text.toString().trim()
        val value = etValue.text.toString().trim()
        val tipo = spinnerTipo.selectedItem.toString()

        if (key.isEmpty() || value.isEmpty()) {
            Toast.makeText(this, "Ingrese llave y valor", Toast.LENGTH_SHORT).show()
            return
        }

        when (tipo) {
            "SharedPreferences" -> {
                getSharedPreferences("secretos_simples", MODE_PRIVATE)
                    .edit()
                    .putString(key, value)
                    .apply()

                txtResultado.text = "Guardado en SharedPreferences"
            }

            "DataStore" -> {
                lifecycleScope.launch {
                    val dataKey = stringPreferencesKey(key)

                    secretosDataStore.edit { preferences ->
                        preferences[dataKey] = value
                    }

                    txtResultado.text = "Guardado en DataStore"
                }
            }

            "EncryptedSharedPreferences" -> {
                val encryptedPrefs = obtenerEncryptedPrefs()

                encryptedPrefs.edit()
                    .putString(key, value)
                    .apply()

                txtResultado.text = "Guardado en EncryptedSharedPreferences"
            }
        }
    }


    private fun recuperarSecreto() {
        val key = etKey.text.toString().trim()
        val tipo = spinnerTipo.selectedItem.toString()

        if (key.isEmpty()) {
            Toast.makeText(this, "Ingrese una llave", Toast.LENGTH_SHORT).show()
            return
        }

        when (tipo) {
            "SharedPreferences" -> {
                val value = getSharedPreferences("secretos_simples", MODE_PRIVATE)
                    .getString(key, null)

                txtResultado.text = value ?: "Secreto no encontrado"
            }

            "DataStore" -> {
                lifecycleScope.launch {
                    val dataKey = stringPreferencesKey(key)
                    val preferences = secretosDataStore.data.first()
                    val value = preferences[dataKey]

                    txtResultado.text = value ?: "Secreto no encontrado"
                }
            }

            "EncryptedSharedPreferences" -> {
                val value = obtenerEncryptedPrefs().getString(key, null)
                txtResultado.text = value ?: "Secreto no encontrado"
            }
        }
    }

    private fun obtenerEncryptedPrefs() =
        EncryptedSharedPreferences.create(
            this,
            "secretos_encriptados",
            MasterKey.Builder(this)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
}