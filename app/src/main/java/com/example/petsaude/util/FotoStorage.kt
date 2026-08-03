package com.example.petsaude.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Salva a foto de qualquer entidade do app (pet, usuário, etc.) localmente,
 * dentro do armazenamento privado do app (`filesDir`), identificada por uma
 * [chave] arbitrária escolhida pelo chamador (ex.: "pet_123", "usuario_abc").
 * Não sobe nada para o Firestore/nuvem — a foto fica só no aparelho.
 */
object FotoStorage {

    private fun pastaFotos(context: Context): File =
        File(context.filesDir, "fotos").apply { mkdirs() }

    /** Caminho absoluto onde a foto de [chave] ficaria salva (pode não existir ainda). */
    fun caminhoPara(context: Context, chave: String): String =
        File(pastaFotos(context), "$chave.jpg").absolutePath

    /**
     * Copia o conteúdo apontado por [origem] (pode ser uma foto da galeria ou
     * o arquivo temporário gravado pela câmera) para o armazenamento interno
     * do app, associado à [chave], já corrigindo a rotação (EXIF). Retorna
     * o caminho absoluto do arquivo final.
     */
    fun salvarFoto(context: Context, chave: String, origem: Uri): String {
        val destino = File(caminhoPara(context, chave))
        context.contentResolver.openInputStream(origem)?.use { input ->
            destino.outputStream().use { output -> input.copyTo(output) }
        }
        corrigirRotacao(destino)
        return destino.absolutePath
    }

    /** Remove a foto salva de [chave], se existir. */
    fun removerFoto(context: Context, chave: String) {
        File(caminhoPara(context, chave)).let { if (it.exists()) it.delete() }
    }

    /**
     * Cria um arquivo temporário (em cache) para a câmera gravar a foto, e
     * devolve uma Uri de conteúdo (via FileProvider) que pode ser passada
     * para o Intent/contract de captura de imagem.
     */
    fun criarUriTemporariaParaCamera(context: Context): Uri {
        val pastaTemp = File(context.cacheDir, "fotos_temp").apply { mkdirs() }
        val arquivoTemp = File.createTempFile("foto_", ".jpg", pastaTemp)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            arquivoTemp
        )
    }

    /**
     * Lê a tag EXIF de orientação do arquivo e, se necessário, gira a imagem
     * fisicamente (regravando o arquivo já corrigido). Depois disso, qualquer
     * leitura simples com BitmapFactory já mostra a foto na orientação certa,
     * sem precisar lidar com EXIF de novo em nenhum outro lugar do app.
     */
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
            // Se der qualquer problema ao ler/gravar o EXIF, mantemos a foto
            // original (melhor mostrar torta do que travar o cadastro).
        }
    }
}
