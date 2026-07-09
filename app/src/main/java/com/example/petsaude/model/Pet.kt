package com.example.petsaude.model

import java.util.UUID

data class Pet(
    val id: String = UUID.randomUUID().toString(),
    val nomePet: String,
    val especie: String,
    val raca: String,
    val idade: Int,
    val peso: Float,
    val sexo: String,
    val pelagem: String,
    val microchip: String? = null
)
