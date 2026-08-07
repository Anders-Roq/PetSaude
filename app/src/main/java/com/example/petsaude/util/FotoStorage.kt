package com.example.petsaude.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object FotoStorage {

    private fun pastaFotos(context: Context): File =
        File(context.filesDir, "fotos").apply { mkdirs() }

    // Caminho onde a foto de [chave] fica (ou ficaria) salva
    fun caminhoPara(context: Context, chave: String): String =
        File(pastaFotos(context), "$chave.jpg").absolutePath


    // Copia a foto (câmera ou galeria) para o armazenamento do app, corrige a rotação e devolve o caminho final
    fun salvarFoto(context: Context, chave: String, origem: Uri): String {
        val destino = File(caminhoPara(context, chave))
        context.contentResolver.openInputStream(origem)?.use { input ->
            destino.outputStream().use { output -> input.copyTo(output) }
        }
        corrigirRotacao(destino)
        return destino.absolutePath
    }

    // Apaga a foto de [chave], se existir
    fun removerFoto(context: Context, chave: String) {
        File(caminhoPara(context, chave)).let { if (it.exists()) it.delete() }
    }

    // Cria um arquivo temporário para a câmera gravar a foto
    fun criarUriTemporariaParaCamera(context: Context): Uri {
        val pastaTemp = File(context.cacheDir, "fotos_temp").apply { mkdirs() }
        val arquivoTemp = File.createTempFile("foto_", ".jpg", pastaTemp)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            arquivoTemp
        )
    }

    // Corrige a rotação da foto usando o EXIF
    private fun corrigirRotacao(arquivo: File) {
        try {
            val exif = ExifInterface(arquivo.absolutePath)
            val orientacao = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            val graus = when (orientacao) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            if (graus == 0f) return

            val original = BitmapFactory.decodeFile(arquivo.absolutePath) ?: return
            val matriz = Matrix().apply { postRotate(graus) }
            val corrigido = Bitmap.createBitmap(
                original, 0, 0, original.width, original.height, matriz, true
            )

            FileOutputStream(arquivo).use { saida ->
                corrigido.compress(Bitmap.CompressFormat.JPEG, 90, saida)
            }

            original.recycle()
            corrigido.recycle()
        } catch (_: Exception) {
        }
    }
}
