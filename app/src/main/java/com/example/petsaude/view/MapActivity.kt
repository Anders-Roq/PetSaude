package com.example.petsaude.view

import android.location.Geocoder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petsaude.ui.theme.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MapActivity : ComponentActivity() {

    companion object {
        const val EXTRA_ENDERECO = "endereco"
        const val EXTRA_TITULO   = "titulo"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val endereco = intent.getStringExtra(EXTRA_ENDERECO) ?: ""
        val titulo   = intent.getStringExtra(EXTRA_TITULO)   ?: "Localização"

        setContent {
            PetSaudeTheme {
                MapScreen(
                    endereco    = endereco,
                    titulo      = titulo,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@Composable
fun MapScreen(
    endereco: String,
    titulo: String = "Localização",
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var latLng     by remember { mutableStateOf<LatLng?>(null) }
    var carregando by remember { mutableStateOf(true) }
    var erro       by remember { mutableStateOf(false) }

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-14.235, -51.925), 4f)
    }

    // Geocodifica o endereço assim que a tela abre
    LaunchedEffect(endereco) {
        if (endereco.isBlank()) {
            erro       = true
            carregando = false
            return@LaunchedEffect
        }
        scope.launch {
            val resultado = withContext(Dispatchers.IO) {
                try {
                    val geocoder   = Geocoder(context, Locale("pt", "BR"))
                    @Suppress("DEPRECATION")
                    val resultados = geocoder.getFromLocationName(endereco, 1)
                    if (!resultados.isNullOrEmpty()) {
                        LatLng(resultados[0].latitude, resultados[0].longitude)
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            if (resultado != null) {
                latLng = resultado
                cameraState.animate(
                    CameraUpdateFactory.newLatLngZoom(resultado, 16f),
                    durationMs = 1000
                )
            } else {
                erro = true
            }
            carregando = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Mapa ─────────────────────────────────────────────────
        GoogleMap(
            modifier            = Modifier.fillMaxSize(),
            cameraPositionState = cameraState,
            uiSettings          = MapUiSettings(
                zoomControlsEnabled     = true,
                myLocationButtonEnabled = false,
                compassEnabled          = true
            )
        ) {
            latLng?.let { pos ->
                Marker(
                    state   = MarkerState(position = pos),
                    title   = titulo,
                    snippet = endereco
                )
            }
        }

        // ── Barra superior flutuante com botão voltar ─────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(12.dp)
                .background(White.copy(alpha = 0.95f), RoundedCornerShape(14.dp))
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint               = Navy900
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = titulo,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Navy900,
                    maxLines   = 1
                )
                if (endereco.isNotBlank()) {
                    Text(
                        text     = endereco,
                        fontSize = 12.sp,
                        color    = GrayText,
                        maxLines = 2
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
        }

        // ── Loading ───────────────────────────────────────────────
        if (carregando) {
            Box(
                modifier         = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier            = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        CircularProgressIndicator(color = Teal500)
                        Text(
                            text     = "Localizando endereço...",
                            color    = GrayText,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // ── Erro de geocodificação ────────────────────────────────
        if (erro && !carregando) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Card(
                    shape  = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = RedExpired.copy(alpha = 0.93f)
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier              = Modifier.padding(16.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("⚠️", fontSize = 20.sp)
                        Text(
                            text      = "Não foi possível localizar:\n\"$endereco\"",
                            color     = White,
                            fontSize  = 13.sp,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
    }
}