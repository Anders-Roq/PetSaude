package com.example.petsaude.db.network

import retrofit2.Response
import retrofit2.http.*

interface PetSaudeApiService {

    // Busca o catálogo geral de vacinas (pode filtrar por 'cao' ou 'gato')
    @GET("catalogo/vacinas")
    suspend fun getCatalogoVacinas(
        @Query("especie") especie: String? = null
    ): Response<List<VacinaCatalogoApi>>

    // Busca as vacinas salvas no histórico do pet na API
    @GET("pets/{pet_id}/vacinas")
    suspend fun getVacinasDoPet(
        @Path("pet_id") petId: Int
    ): Response<List<VacinaAplicadaApi>>

    // Registra uma nova vacina na API
    @POST("pets/vacinas/aplicar")
    suspend fun registrarVacina(
        @Body vacina: VacinaAplicadaApi
    ): Response<VacinaAplicadaApi>
}