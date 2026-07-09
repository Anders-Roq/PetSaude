package com.example.petsaude.db.fb

import com.example.petsaude.model.Pet

class FBPet {
    var id: String? = null
    var nomePet: String? = null
    var especie: String? = null
    var raca: String? = null
    var idade: Int? = null
    var peso: Float? = null
    var sexo: String? = null
    var pelagem: String? = null
    var microchip: String? = null

    fun toPet() = Pet(
        id = id ?: "",
        nomePet = nomePet ?: "",
        especie = especie ?: "",
        raca = raca ?: "",
        idade = idade ?: 0,
        peso = peso ?: 0f,
        sexo = sexo ?: "",
        pelagem = pelagem ?: "",
        microchip = microchip
    )
}

fun Pet.toFBPet(): FBPet {
    val fbPet = FBPet()
    fbPet.id = this.id
    fbPet.nomePet = this.nomePet
    fbPet.especie = this.especie
    fbPet.raca = this.raca
    fbPet.idade = this.idade
    fbPet.peso = this.peso
    fbPet.sexo = this.sexo
    fbPet.pelagem = this.pelagem
    fbPet.microchip = this.microchip
    return fbPet
}
