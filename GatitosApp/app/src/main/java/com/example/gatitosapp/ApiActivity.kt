package com.example.gatitosapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ApiActivity : AppCompatActivity() {

    private lateinit var etId: EditText
    private lateinit var etTitle: EditText
    private lateinit var etBody: EditText
    private lateinit var btnObtener: Button
    private lateinit var btnActualizar: Button
    private lateinit var txtEstado: TextView
    private lateinit var btnVolver: Button

    private var postActual: Post? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_api)

        etId = findViewById(R.id.etId)
        etTitle = findViewById(R.id.etTitle)
        etBody = findViewById(R.id.etBody)
        btnObtener = findViewById(R.id.btnObtener)
        btnActualizar = findViewById(R.id.btnActualizar)
        txtEstado = findViewById(R.id.txtEstado)

        btnObtener.setOnClickListener {
            obtenerPost()
        }

        btnActualizar.setOnClickListener {
            actualizarPost()
        }

        btnVolver = findViewById(R.id.btnVolver)

        btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun obtenerPost() {
        val id = etId.text.toString().toIntOrNull()

        if (id == null) {
            Toast.makeText(this, "Ingrese un ID válido", Toast.LENGTH_SHORT).show()
            return
        }

        cambiarLoading(true, "Consultando post...")

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getPost(id)

                if (response.isSuccessful && response.body() != null) {
                    postActual = response.body()

                    etTitle.setText(postActual!!.title)
                    etBody.setText(postActual!!.body)

                    txtEstado.text = "Post obtenido correctamente"
                } else {
                    txtEstado.text = "No se encontró el post"
                }
            } catch (e: Exception) {
                txtEstado.text = "Error de conexión: ${e.message}"
            } finally {
                cambiarLoading(false)
            }
        }
    }

    private fun actualizarPost() {
        val post = postActual

        if (post == null) {
            Toast.makeText(this, "Primero obtenga un post", Toast.LENGTH_SHORT).show()
            return
        }

        val postEditado = Post(
            userId = post.userId,
            id = post.id,
            title = etTitle.text.toString(),
            body = etBody.text.toString()
        )

        cambiarLoading(true, "Actualizando post...")

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.updatePost(post.id, postEditado)

                if (response.isSuccessful && response.body() != null) {
                    postActual = response.body()

                    etTitle.setText(postActual!!.title)
                    etBody.setText(postActual!!.body)

                    txtEstado.text = "Actualización exitosa. Código: ${response.code()}"
                } else {
                    txtEstado.text = "No se pudo actualizar"
                }
            } catch (e: Exception) {
                txtEstado.text = "Error de conexión: ${e.message}"
            } finally {
                cambiarLoading(false)
            }
        }
    }

    private fun cambiarLoading(cargando: Boolean, mensaje: String = "") {
        etId.isEnabled = !cargando
        etTitle.isEnabled = !cargando
        etBody.isEnabled = !cargando
        btnObtener.isEnabled = !cargando
        btnActualizar.isEnabled = !cargando

        if (mensaje.isNotEmpty()) {
            txtEstado.text = mensaje
        }
    }
}