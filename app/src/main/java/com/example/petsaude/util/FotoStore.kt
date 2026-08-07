package com.example.petsaude.util

import android.content.Context
import androidx.core.content.edit


object FotoStore {

    private const val PREFS_NAME = "foto_store"

    //Salva localmente o caminho da foto tirada ou escolhida na galeria, associada a uma chave
    fun salvarCaminho(context: Context, chave: String, caminho: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putString(chave, caminho) }
    }

    //Procura e retorna o caminho da foto armazenada, se existir
    fun obterCaminho(context: Context, chave: String): String? {
        val caminho = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(chave, null) ?: return null
        return if (java.io.File(caminho).exists()) caminho else null
    }

    //Remove a foto: apaga a entrada do índice e o arquivo em disco
    fun remover(context: Context, chave: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { remove(chave) }
        FotoStorage.removerFoto(context, chave)
    }
}
