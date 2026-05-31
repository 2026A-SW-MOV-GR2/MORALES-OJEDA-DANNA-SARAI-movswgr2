package com.example.gatitosapp

data class Gatito(
    val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val fecha: String,
    val vacunado: Boolean,
    val emoji: String
)