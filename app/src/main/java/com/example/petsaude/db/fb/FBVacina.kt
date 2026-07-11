package com.example.petsaude.db.fb

data class FBVacina(
    var id: String? = null,
    val petId: String = "",
    val nomePet: String = "",
    var nome: String = "",
    var aplicacao: String = "",
    var proxima: String = "",
    var lote: String = "",
    var veterinario: String = "",
    var status: String = "Aplicada" // Pode ser "Aplicada", "Pendente" ou "Vencida"
)