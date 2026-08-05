package com.example.petsaude.view

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.petsaude.util.FotoStorage
import com.example.petsaude.util.FotoStore

/**
 * Avatar genérico para qualquer entidade do app (pet, usuário, etc.): mostra
 * a foto salva localmente para [chave] se existir, ou o [conteudoPadrao]
 * (ex.: um emoji ou um Icon) como placeholder.
 */
@Composable
fun AvatarImage(
    chave: String,
    tamanho: Dp,
    shape: CornerBasedShape,
    caminhoFoto: String? = null,
    conteudoPadrao: @Composable () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    var caminhoAtual by remember(chave, caminhoFoto) {
        mutableStateOf(caminhoFoto ?: FotoStore.obterCaminho(context, chave))
    }

    if (caminhoFoto == null) {
        DisposableEffect(lifecycleOwner, chave) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    caminhoAtual = FotoStore.obterCaminho(context, chave)
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
    }

    val bitmap = remember(caminhoAtual) {
        caminhoAtual?.let { BitmapFactory.decodeFile(it)?.asImageBitmap() }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = "Foto",
            modifier = Modifier.size(tamanho).clip(shape),
            contentScale = ContentScale.Crop
        )
    } else {
        conteudoPadrao()
    }
}

/** Conjunto de ações prontas para abrir a câmera ou a galeria. */
data class FotoPickerLaunchers(
    val aoTirarFoto: () -> Unit,
    val aoEscolherGaleria: () -> Unit
)

/**
 * Centraliza toda a lógica de captura de foto (permissão de câmera, launcher
 * da câmera, launcher da galeria/Photo Picker) para uma [chave] qualquer.
 * Sempre que uma foto é obtida com sucesso, ela já é salva via [FotoStorage]
 * + [FotoStore], e [onFotoAtualizada] é chamado com o novo caminho.
 */
@Composable
fun rememberFotoPickerLaunchers(
    context: Context,
    chave: String,
    onFotoAtualizada: (String) -> Unit
): FotoPickerLaunchers {
    var uriCameraTemp by remember(chave) { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { sucesso ->
        val uriTemp = uriCameraTemp
        if (sucesso && uriTemp != null) {
            val caminhoFinal = FotoStorage.salvarFoto(context, chave, uriTemp)
            FotoStore.salvarCaminho(context, chave, caminhoFinal)
            onFotoAtualizada(caminhoFinal)
        }
    }

    val permissaoCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedida ->
        if (concedida) {
            val uriTemp = FotoStorage.criarUriTemporariaParaCamera(context)
            uriCameraTemp = uriTemp
            cameraLauncher.launch(uriTemp)
        }
    }

    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val caminhoFinal = FotoStorage.salvarFoto(context, chave, uri)
            FotoStore.salvarCaminho(context, chave, caminhoFinal)
            onFotoAtualizada(caminhoFinal)
        }
    }

    return FotoPickerLaunchers(
        aoTirarFoto = { permissaoCameraLauncher.launch(Manifest.permission.CAMERA) },
        aoEscolherGaleria = {
            galeriaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    )
}

/**
 * Diálogo genérico "Tirar foto agora / Escolher da galeria / Remover foto",
 * usado tanto no cadastro de pet quanto no perfil do usuário.
 */
@Composable
fun EscolherFotoDialog(
    titulo: String = "Foto",
    temFotoAtual: Boolean,
    onTirarFoto: () -> Unit,
    onEscolherGaleria: () -> Unit,
    onRemoverFoto: () -> Unit,
    onFechar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text(titulo) },
        text = {
            androidx.compose.foundation.layout.Column {
                OpcaoFotoDialog(Icons.Default.PhotoCamera, "Tirar foto agora", MaterialTheme.colorScheme.primary) {
                    onFechar(); onTirarFoto()
                }
                OpcaoFotoDialog(Icons.Default.Image, "Escolher da galeria", MaterialTheme.colorScheme.primary) {
                    onFechar(); onEscolherGaleria()
                }
                if (temFotoAtual) {
                    OpcaoFotoDialog(Icons.Default.Delete, "Remover foto", MaterialTheme.colorScheme.error) {
                        onFechar(); onRemoverFoto()
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onFechar) { Text("Cancelar") }
        }
    )
}

@Composable
private fun OpcaoFotoDialog(icone: ImageVector, texto: String, cor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icone, contentDescription = null, tint = cor)
        Spacer(modifier = Modifier.width(12.dp))
        Text(texto, color = cor)
    }
}
