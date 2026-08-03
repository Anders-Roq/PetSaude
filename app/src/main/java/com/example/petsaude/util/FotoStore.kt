package com.example.petsaude.util

import android.content.Context

/**
 * Guarda, localmente no aparelho, qual caminho de arquivo corresponde à foto
 * de qualquer entidade do app (petId, uid de usuário, etc. -> caminho
 * absoluto). Como a foto não é sincronizada pelo Firestore, esse mapeamento
 * também precisa ser local.
 */
object FotoStore {

    private const val PREFS_NAME = "foto_store"

    fun salvarCaminho(context: Context, chave: String, caminho: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(chave, caminho).apply()
    }

    fun obterCaminho(context: Context, chave: String): String? {
        val caminho = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(chave, null) ?: return null
        return if (java.io.File(caminho).exists()) caminho else null
    }

    fun remover(context: Context, chave: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().remove(chave).apply()
        FotoStorage.removerFoto(context, chave)
    }
}
