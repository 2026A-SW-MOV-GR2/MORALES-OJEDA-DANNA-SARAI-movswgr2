package com.example.gatitosapp

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import android.content.Intent

class MainActivity : AppCompatActivity() {

    private lateinit var sqlHelper: GatitoSqlHelper
    private lateinit var noSqlStore: GatitoNoSqlStore
    private lateinit var adapter: GatitoAdapter

    private var usandoNoSql = false
    private var gatitoEditando: Gatito? = null

    private lateinit var txtModo: TextView
    private lateinit var switchDb: Switch
    private lateinit var recyclerGatitos: RecyclerView
    private lateinit var btnAgregar: com.google.android.material.floatingactionbutton.FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sqlHelper = GatitoSqlHelper(this)
        noSqlStore = GatitoNoSqlStore(this)

        txtModo = findViewById(R.id.txtModo)
        switchDb = findViewById(R.id.switchDb)
        recyclerGatitos = findViewById(R.id.recyclerGatitos)
        btnAgregar = findViewById(R.id.btnAgregar)

        val btnApi = findViewById<Button>(R.id.btnApi)

        btnApi.setOnClickListener {
            startActivity(Intent(this, ApiActivity::class.java))
        }

        val btnSecretos = findViewById<Button>(R.id.btnSecretos)

        btnSecretos.setOnClickListener {
            startActivity(Intent(this, SecretosActivity::class.java))
        }

        adapter = GatitoAdapter(
            emptyList(),
            onEditar = { gatito -> mostrarFormulario(gatito) },
            onEliminar = { gatito -> confirmarEliminar(gatito) }
        )

        recyclerGatitos.layoutManager = LinearLayoutManager(this)
        recyclerGatitos.adapter = adapter

        switchDb.setOnCheckedChangeListener { _, isChecked ->
            usandoNoSql = isChecked
            txtModo.text = if (usandoNoSql) "Modo NoSQL" else "Modo SQL"
            cargarGatitos()
        }

        btnAgregar.setOnClickListener {
            mostrarFormulario(null)
        }

        insertarDatosIniciales()
    }

    private fun insertarDatosIniciales() {
        if (sqlHelper.obtenerTodos().isEmpty()) {
            sqlHelper.insertar(
                Gatito(0, "Michi", "Gatito tranquilo y cariñoso", "2026-05-01", true, "🐱")
            )
            sqlHelper.insertar(
                Gatito(0, "Luna", "Le gusta dormir en cajas", "2026-05-03", false, "😺")
            )
        }

        lifecycleScope.launch {
            if (noSqlStore.obtenerTodos().isEmpty()) {
                noSqlStore.insertar(
                    Gatito(0, "Nube", "Gatito curioso desde NoSQL", "2026-05-10", true, "😸")
                )
                noSqlStore.insertar(
                    Gatito(0, "Sol", "Gatito guardado como documento", "2026-05-11", false, "😻")
                )
            }

            cargarGatitos()
        }
    }

    private fun cargarGatitos() {
        if (usandoNoSql) {
            lifecycleScope.launch {
                adapter.actualizarLista(noSqlStore.obtenerTodos())
            }
        } else {
            adapter.actualizarLista(sqlHelper.obtenerTodos())
        }
    }

    private fun mostrarFormulario(gatito: Gatito?) {
        gatitoEditando = gatito

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 25, 40, 10)
        }

        val inputNombre = EditText(this).apply {
            hint = "Nombre del gatito"
            setText(gatito?.nombre ?: "")
        }

        val inputDescripcion = EditText(this).apply {
            hint = "Descripción"
            setText(gatito?.descripcion ?: "")
        }

        val inputFecha = EditText(this).apply {
            hint = "Fecha (YYYY-MM-DD)"
            setText(gatito?.fecha ?: "")
        }

        val inputEmoji = EditText(this).apply {
            hint = "Emoji"
            setText(gatito?.emoji ?: "🐱")
        }

        val checkVacunado = CheckBox(this).apply {
            text = "Vacunado"
            isChecked = gatito?.vacunado ?: false
        }

        layout.addView(inputNombre)
        layout.addView(inputDescripcion)
        layout.addView(inputFecha)
        layout.addView(inputEmoji)
        layout.addView(checkVacunado)

        AlertDialog.Builder(this)
            .setTitle(if (gatito == null) "Registrar gatito" else "Editar gatito")
            .setView(layout)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoGatito = Gatito(
                    id = gatito?.id ?: 0,
                    nombre = inputNombre.text.toString(),
                    descripcion = inputDescripcion.text.toString(),
                    fecha = inputFecha.text.toString(),
                    vacunado = checkVacunado.isChecked,
                    emoji = inputEmoji.text.toString()
                )

                guardarGatito(nuevoGatito)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun guardarGatito(gatito: Gatito) {
        if (usandoNoSql) {
            lifecycleScope.launch {
                if (gatitoEditando == null) {
                    noSqlStore.insertar(gatito)
                    Toast.makeText(this@MainActivity, "Guardado en NoSQL", Toast.LENGTH_SHORT).show()
                } else {
                    noSqlStore.actualizar(gatito)
                    Toast.makeText(this@MainActivity, "Actualizado en NoSQL", Toast.LENGTH_SHORT).show()
                }

                cargarGatitos()
            }
        } else {
            if (gatitoEditando == null) {
                sqlHelper.insertar(gatito)
                Toast.makeText(this, "Guardado en SQL", Toast.LENGTH_SHORT).show()
            } else {
                sqlHelper.actualizar(gatito)
                Toast.makeText(this, "Actualizado en SQL", Toast.LENGTH_SHORT).show()
            }

            cargarGatitos()
        }
    }

    private fun confirmarEliminar(gatito: Gatito) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar gatito")
            .setMessage("¿Deseas eliminar a ${gatito.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarGatito(gatito)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarGatito(gatito: Gatito) {
        if (usandoNoSql) {
            lifecycleScope.launch {
                noSqlStore.eliminar(gatito.id)
                Toast.makeText(this@MainActivity, "Eliminado de NoSQL", Toast.LENGTH_SHORT).show()
                cargarGatitos()
            }
        } else {
            sqlHelper.eliminar(gatito.id)
            Toast.makeText(this, "Eliminado de SQL", Toast.LENGTH_SHORT).show()
            cargarGatitos()
        }
    }
}