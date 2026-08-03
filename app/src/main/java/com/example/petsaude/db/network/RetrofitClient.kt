package com.example.petsaude.db.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Sua URL real gerada pelo Render!
    private const val BASE_URL = "https://petsaude-vacinas-api.onrender.com/"

    val apiService: PetSaudeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PetSaudeApiService::class.java)
    }
}