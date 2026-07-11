package com.example.petsaude.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import android.content.Intent
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petsaude.db.fb.FBVacina
import com.example.petsaude.db.fb.FBDatabase
import com.example.petsaude.ui.theme.*

class VacinasActivity : ComponentActivity() {
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

@Composable
fun VacinasPage(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    var vacinas by remember { mutableStateOf<List<FBVacina>>(emptyList()) }

    LaunchedEffect(Unit) {
        FBDatabase().listenVacinas { vacinas = it }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayBg)
            .statusBarsPadding()
    ) {
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
            Text("Vacinas", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Navy900, modifier = Modifier.weight(1f))
            Button(
                onClick = { context.startActivity(Intent(context, AddVacinaActivity::class.java)) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenApplied),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Nova", fontSize = 13.sp)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(vacinas) { v ->
                VacinaCard(
                    v = v,
                    onEditar = { vacina ->
                        val intent = Intent(context, AddVacinaActivity::class.java).apply {
                            putExtra("id", vacina.id)
                            putExtra("petId", vacina.petId)         // 👈 ENVIANDO ID DO PET PARA EDIÇÃO
                            putExtra("nomePet", vacina.nomePet)     // 👈 ENVIANDO NOME DO PET PARA EDIÇÃO
                            putExtra("nome", vacina.nome)
                            putExtra("aplicacao", vacina.aplicacao)
                            putExtra("proxima", vacina.proxima)
                            putExtra("lote", vacina.lote)
                            putExtra("veterinario", vacina.veterinario)
                            putExtra("status", vacina.status)
                        }
                        context.startActivity(intent)
                    },
                    onExcluir = { vacina ->
                        FBDatabase().removeVacina(vacina)
                    }
                )
            }
        }
    }
}

@Composable
fun VacinaCard(v: FBVacina, onEditar: (FBVacina) -> Unit, onExcluir: (FBVacina) -> Unit) {
    val (statusColor, statusBg, statusIcon) = when (v.status) {
        "Aplicada" -> Triple(GreenApplied, Color(0xFFDCFCE7), Icons.Default.CheckCircle)
        "Pendente" -> Triple(OrangeWarning, Color(0xFFFEF3C7), Icons.Default.Warning)
        "Vencida"  -> Triple(RedExpired,   Color(0xFFFEE2E2),  Icons.Default.Cancel)
        else       -> Triple(GrayText,     GrayBorder,          Icons.Default.Info)
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
                    Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(v.nome, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Navy900)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(v.status, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = statusColor)
                }
            }

            // 👈 LINHA ADICIONADA: Exibe o nome do Pet com o ícone de patinha abaixo do título
            if (v.nomePet.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 28.dp) // Alinha bonitinho embaixo do texto do título
                ) {
                    Icon(Icons.Default.Pets, null, tint = Teal500, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(text = v.nomePet, fontSize = 13.sp, color = Teal500, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column {
                    Text("Aplicação:", fontSize = 11.sp, color = GrayText)
                    Text(v.aplicacao, fontSize = 13.sp, color = Navy900, fontWeight = FontWeight.Medium)
                }
                Column {
                    Text("Próxima:", fontSize = 11.sp, color = GrayText)
                    Text(v.proxima, fontSize = 13.sp, color = Navy900, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(8.dp))

            if (v.lote.isNotBlank()) {
                Text("Lote: ${v.lote}", fontSize = 12.sp, color = GrayText)
            }
            if (v.veterinario.isNotBlank()) {
                Text("Vet: ${v.veterinario}", fontSize = 12.sp, color = GrayText)
            }

            Spacer(Modifier.height(12.dp))

            // Botões de Ação
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = { onEditar(v) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Editar", fontSize = 13.sp)
                }
                Button(
                    onClick = { onExcluir(v) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Excluir", fontSize = 13.sp)
                }
            }
        }
    }
}