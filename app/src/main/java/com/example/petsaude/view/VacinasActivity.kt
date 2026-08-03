package com.example.petsaude.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petsaude.db.fb.FBDatabase
import com.example.petsaude.db.fb.FBVacina
import com.example.petsaude.ui.theme.*
import com.example.petsaude.util.VacinaNotificationHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
class VacinasActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetSaudeTheme {
                VacinasPage(onBackClick = { finish() })
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun VacinasPage(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    var listaVacinas by remember { mutableStateOf<List<FBVacina>>(emptyList()) }

    // Busca as vacinas do Firebase e checa as notificações
    LaunchedEffect(Unit) {
        FBDatabase().listenVacinas { lista ->
            listaVacinas = lista

            // Dispara alertas se necessário
            lista.forEach { item ->
                VacinaNotificationHelper.verificarEAgendarNotificacoes(
                    context = context,
                    nomePet = item.nomePet ?: "",
                    nomeVacina = item.nome ?: "",
                    proximaDoseStr = item.proxima ?: "",
                    idVacina = item.id
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayBg)
            .statusBarsPadding()
    ) {
        // Barra Superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Navy900)
            }
            Text(
                text = "Vacinas",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Navy900,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { context.startActivity(Intent(context, AddVacinaActivity::class.java)) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Nova", fontSize = 13.sp)
            }
        }

        // Lista de Cards
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(listaVacinas) { vacinaItem ->
                VacinaCardItem(
                    v = vacinaItem,
                    onEditar = { item ->
                        val intent = Intent(context, AddVacinaActivity::class.java).apply {
                            putExtra("id", item.id)
                            putExtra("petId", item.petId)
                            putExtra("nomePet", item.nomePet)
                            putExtra("nome", item.nome)
                            putExtra("aplicacao", item.aplicacao)
                            putExtra("proxima", item.proxima)
                            putExtra("lote", item.lote)
                            putExtra("veterinario", item.veterinario)
                            putExtra("status", item.status)
                        }
                        context.startActivity(intent)
                    },
                    onExcluir = { item ->
                        FBDatabase().removeVacina(item)
                    }
                )
            }
        }
    }
}

@Composable
fun VacinaCardItem(
    v: FBVacina,
    onEditar: (FBVacina) -> Unit,
    onExcluir: (FBVacina) -> Unit
) {
    // Trata valores nulos com segurança
    val nomeVacina = v.nome ?: ""
    val nomePet = v.nomePet ?: ""
    val aplicacao = v.aplicacao ?: ""
    val proxima = v.proxima ?: ""
    val lote = v.lote ?: ""
    val veterinario = v.veterinario ?: ""

    // 1. Lógica precisa para verificar se a próxima dose já venceu (comparando com a data de hoje sem horas)
    val estaVencida = remember(proxima) {
        try {
            if (proxima.isNotBlank()) {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dataProxima = sdf.parse(proxima)

                if (dataProxima != null) {
                    val hojeCal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val proximaCal = Calendar.getInstance().apply {
                        time = dataProxima
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    // Se a data de hoje for igual ou maior que a próxima dose, considera Vencida/Pendente
                    !hojeCal.before(proximaCal)
                } else false
            } else false
        } catch (_: Exception) {
            false
        }
    }

    val statusCalculado = if (estaVencida) "Vencida" else "Aplicada"

    // 2. Cores fixas e legíveis para garantir contraste
    val (statusColor, statusBg, statusIcon) = if (estaVencida) {
        Triple(Color(0xFFEF4444), Color(0xFFFEE2E2), Icons.Default.Cancel) // Vermelho
    } else {
        Triple(Color(0xFF10B981), Color(0xFFDCFCE7), Icons.Default.CheckCircle) // Verde
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(nomeVacina, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Navy900)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(statusCalculado, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor)
                }
            }

            if (nomePet.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 28.dp)
                ) {
                    Icon(Icons.Default.Pets, contentDescription = null, tint = Teal500, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(text = nomePet, fontSize = 13.sp, color = Teal500, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column {
                    Text("Aplicação:", fontSize = 11.sp, color = GrayText)
                    Text(aplicacao, fontSize = 13.sp, color = Navy900, fontWeight = FontWeight.Medium)
                }
                Column {
                    Text("Próxima Dose:", fontSize = 11.sp, color = GrayText)
                    Text(
                        text = proxima.ifBlank { "N/A" },
                        fontSize = 13.sp,
                        color = if (estaVencida) Color(0xFFEF4444) else Navy900,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (lote.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text("Lote: $lote", fontSize = 12.sp, color = GrayText)
            }
            if (veterinario.isNotBlank()) {
                Text("Vet: $veterinario", fontSize = 12.sp, color = GrayText)
            }

            Spacer(Modifier.height(12.dp))

            // Botões de Ação
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = { onEditar(v) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Editar", fontSize = 13.sp)
                }
                Button(
                    onClick = { onExcluir(v) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Excluir", fontSize = 13.sp)
                }
            }
        }
    }
}