package com.example.petsaude.view

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.petsaude.util.FotoPetStore

@Composable
fun PetAvatarImage(
    petId: String,
    especie: String,
    tamanho: Dp,
    shape: CornerBasedShape,
    caminhoFoto: String? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var caminhoAtual by remember(petId, caminhoFoto) {
        mutableStateOf(caminhoFoto ?: FotoPetStore.obterCaminho(context, petId))
    }

    if (caminhoFoto == null) {
        DisposableEffect(lifecycleOwner, petId) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    caminhoAtual = FotoPetStore.obterCaminho(context, petId)
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
            contentDescription = "Foto do pet",
            modifier = Modifier.size(tamanho).clip(shape),
            contentScale = ContentScale.Crop
        )
    } else {
        Text(
            text = if (especie == "Gato") "🐈" else "🐕",
            fontSize = (tamanho.value * 0.42f).sp
        )
    }
}
