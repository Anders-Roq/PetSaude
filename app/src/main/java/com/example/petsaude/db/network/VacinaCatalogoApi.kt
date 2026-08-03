package com.example.petsaude.db.network

import com.google.gson.annotations.SerializedName

// Modelo para ler o catálogo do FastAPI
data class VacinaCatalogoApi(
    val id: Int,
    val nome: String,
    val especie: String,
    val descricao: String,
    val obrigatoria: Boolean,
    @SerializedName("intervalo_doses_dias") val intervaloDosesDias: Int?,
    @SerializedName("fabricante_comum") val fabricanteComum: String
)

// Modelo para enviar/receber vacinas aplicadas na API
data class VacinaAplicadaApi(
    val id: Int? = null,
    @SerializedName("pet_id") val petId: Int,
    @SerializedName("vacina_nome") val vacinaNome: String,
    val lote: String,
    val fabricante: String,
    @SerializedName("data_aplicacao") val dataAplicacao: String, // YYYY-MM-DD
    @SerializedName("data_proxima_dose") val dataProximaDose: String, // YYYY-MM-DD
    @SerializedName("veterinario_crmv") val veterinarioCrmv: String? = null,
    val observacoes: String? = null
)
