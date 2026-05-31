package com.example.gatitosapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GatitoAdapter(
    private var lista: List<Gatito>,
    private val onEditar: (Gatito) -> Unit,
    private val onEliminar: (Gatito) -> Unit
) : RecyclerView.Adapter<GatitoAdapter.GatitoViewHolder>() {

    class GatitoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtEmoji: TextView = view.findViewById(R.id.txtEmoji)
        val txtNombre: TextView = view.findViewById(R.id.txtNombre)
        val txtDescripcion: TextView = view.findViewById(R.id.txtDescripcion)
        val txtMeta: TextView = view.findViewById(R.id.txtMeta)
        val btnEliminar: TextView = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GatitoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gatito, parent, false)

        return GatitoViewHolder(view)
    }

    override fun onBindViewHolder(holder: GatitoViewHolder, position: Int) {
        val gatito = lista[position]

        holder.txtEmoji.text = gatito.emoji
        holder.txtNombre.text = gatito.nombre
        holder.txtDescripcion.text = gatito.descripcion
        holder.txtMeta.text = "${if (gatito.vacunado) "Vacunado" else "Sin vacuna"} · ${gatito.fecha}"

        holder.itemView.setOnClickListener {
            onEditar(gatito)
        }

        holder.btnEliminar.setOnClickListener {
            onEliminar(gatito)
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<Gatito>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}