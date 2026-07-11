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
import androidx.compose.runtime.Composable
import android.content.Intent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petsaude.db.fb.FBConsulta
import com.example.petsaude.db.fb.FBDatabase
import com.example.petsaude.ui.theme.*
import com.example.petsaude.view.MapActivity




class ConsultasActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetSaudeTheme {
                ConsultasPage(onBackClick = { finish() })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConsultasPage(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    var consultas by remember {
        mutableStateOf<List<FBConsulta>>(emptyList())
    }
    LaunchedEffect(Unit) {

        FBDatabase().listenConsultas(
            onChange = {
                consultas = it
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayBg)
            .statusBarsPadding()
    ) {
        // Top bar
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
                text = "Consultas",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Navy900,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { context.startActivity(Intent(context, AddConsultaActivity::class.java)) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal500),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Nova", fontSize = 13.sp)
            }
            Spacer(Modifier.width(8.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(consultas) { consulta ->
                ConsultaCard(
                    c = consulta,
                    onEditar = {consulta ->
                        val intent = Intent(context, AddConsultaActivity::class.java)
                        intent.putExtra("id", consulta.id)
                        intent.putExtra("petId", consulta.petId)
                        intent.putExtra("nomePet", consulta.nomePet)
                        intent.putExtra("veterinario", consulta.veterinario)
                        intent.putExtra("motivo", consulta.motivo)
                        intent.putExtra("data", consulta.data)
                        intent.putExtra("horario", consulta.horario)
                        intent.putExtra("endereco", consulta.endereco)
                        context.startActivity(intent)
                    },
                    onExcluir = {
                        FBDatabase().removeConsulta(it)
                    }
                )
            }
        }
    }
}

@Composable
fun ConsultaCard(c: FBConsulta, onEditar: (FBConsulta) -> Unit, onExcluir: (FBConsulta) -> Unit) {
    val statusColor = Teal500
    val statusBg = Teal100
    val context = LocalContext.current

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
                Text(c.motivo, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Navy900)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("Agendada", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = statusColor)
                }
            }
            Text(c.veterinario, fontSize = 13.sp, color = GrayText)
            Spacer(Modifier.height(10.dp))
            InfoRow(Icons.Default.CalendarMonth, c.data)
            Spacer(Modifier.height(4.dp))
            InfoRow(Icons.Default.Schedule, c.horario)
            Spacer(Modifier.height(4.dp))
            InfoRow(Icons.Default.LocationOn, c.endereco)
            Spacer(Modifier.height(10.dp))
            TextButton(
                onClick = {
                    val intent = Intent(context, MapActivity::class.java).apply {
                        putExtra(MapActivity.EXTRA_ENDERECO, c.endereco)
                        putExtra(MapActivity.EXTRA_TITULO,   c.veterinario)
                    }
                    context.startActivity(intent)
                },

                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Map, contentDescription = null, tint = Teal500, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Ver no mapa", color = Teal500, fontSize = 13.sp)
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onEditar(c)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Editar")
                }
                Button(
                    onClick = {
                        onExcluir(c)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Excluir")
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Teal500, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 13.sp, color = GrayText)
    }
}
