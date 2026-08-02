package com.example.petsaude.util

import android.content.Context
import androidx.core.content.edit

object FotoPetStore {

    private const val PREFS_NAME = "foto_pet_store"

    fun salvarCaminho(context: Context, petId: String, caminho: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putString(petId, caminho) }
    }

    fun obterCaminho(context: Context, petId: String): String? {
        val caminho = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(petId, null) ?: return null
        return if (java.io.File(caminho).exists()) caminho else null
    }

    fun remover(context: Context, petId: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { remove(petId) }
        FotoStorage.removerFotoPet(context, petId)
    }
}
