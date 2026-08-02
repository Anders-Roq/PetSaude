package com.example.petsaude.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream

object FotoStorage {

    private fun pastaFotos(context: Context): File =
        File(context.filesDir, "fotos_pets").apply { mkdirs() }

    fun caminhoParaPet(context: Context, petId: String): String =
        File(pastaFotos(context), "$petId.jpg").absolutePath

    fun salvarFotoPet(context: Context, petId: String, origem: Uri): String {
        val destino = File(caminhoParaPet(context, petId))
        context.contentResolver.openInputStream(origem)?.use { input ->
            destino.outputStream().use { output -> input.copyTo(output) }
        }
        corrigirRotacao(destino)
        return destino.absolutePath
    }

    fun removerFotoPet(context: Context, petId: String) {
        File(caminhoParaPet(context, petId)).let { if (it.exists()) it.delete() }
    }

    fun criarUriTemporariaParaCamera(context: Context): Uri {
        val pastaTemp = File(context.cacheDir, "fotos_temp").apply { mkdirs() }
        val arquivoTemp = File.createTempFile("foto_", ".jpg", pastaTemp)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            arquivoTemp
        )
    }

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
        } catch (e: Exception) {

        }
    }
}
