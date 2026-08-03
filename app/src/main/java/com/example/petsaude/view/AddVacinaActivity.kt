package com.example.petsaude.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.petsaude.db.fb.FBDatabase
import com.example.petsaude.db.fb.FBPet
import com.example.petsaude.db.fb.FBVacina
import com.example.petsaude.db.network.RetrofitClient
import com.example.petsaude.db.network.VacinaAplicadaApi
import com.example.petsaude.db.network.VacinaCatalogoApi
import com.example.petsaude.ui.theme.GrayText
import com.example.petsaude.ui.theme.GreenApplied
import com.example.petsaude.ui.theme.Navy900
import com.example.petsaude.ui.theme.PetSaudeTheme
import com.example.petsaude.ui.theme.SectionLabel
import com.example.petsaude.ui.theme.Teal50
import com.example.petsaude.ui.theme.Teal500
import com.example.petsaude.ui.theme.White
import com.example.petsaude.ui.theme.petFieldColors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddVacinaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vacinaId = intent.getStringExtra("id")
        val petId = intent.getStringExtra("petId")
        val nomePet = intent.getStringExtra("nomePet")
        val nome = intent.getStringExtra("nome")
        val aplicacao = intent.getStringExtra("aplicacao")
        val proxima = intent.getStringExtra("proxima")
        val lote = intent.getStringExtra("lote")
        val veterinario = intent.getStringExtra("veterinario")
        val status = intent.getStringExtra("status")

        enableEdgeToEdge()
        setContent {
            PetSaudeTheme {
                AddVacinaPage(
                    vacinaId = vacinaId,
                    petIdInicial = petId,
                    nomePetInicial = nomePet,
                    nomeInicial = nome,
                    aplicacaoInicial = aplicacao,
                    proximaInicial = proxima,
                    loteInicial = lote,
                    veterinarioInicial = veterinario,
                    statusInicial = status,
                    onSalvarClick = { id, pet, nm, ap, pr, lt, vt ->
                        salvarVacina(id, pet, nm, ap, pr, lt, vt, status)
                    },
                    onBackClick = { finish() }
                )
            }
        }
    }

    private fun salvarVacina(
        id: String?,
        pet: FBPet,
        nome: String,
        aplicacao: String,
        proxima: String,
        lote: String,
        veterinario: String,
        statusAntigo: String?
    ) {
        val vacinaFB = FBVacina(
            id = id,
            petId = pet.id ?: "",
            nomePet = pet.nomePet ?: "",
            nome = nome,
            aplicacao = aplicacao,
            proxima = proxima,
            lote = lote,
            veterinario = veterinario,
            status = if (id.isNullOrBlank()) "Aplicada" else statusAntigo ?: "Aplicada"
        )

        // 1. Salva/Atualiza no Firebase Database
        if (id.isNullOrBlank()) {
            FBDatabase().addVacina(vacinaFB)
            Toast.makeText(this, "Vacina cadastrada com sucesso!", Toast.LENGTH_LONG).show()
        } else {
            FBDatabase().updateVacina(vacinaFB)
            Toast.makeText(this, "Vacina atualizada com sucesso!", Toast.LENGTH_LONG).show()
        }

        // 2. Envia uma cópia para a sua API FastAPI no Render (em segundo plano)
        lifecycleScope.launch {
            try {
                val petIdInt = pet.id?.toIntOrNull() ?: 101

                val vacinaApi = VacinaAplicadaApi(
                    petId = petIdInt,
                    vacinaNome = nome,
                    lote = lote.ifBlank { "Sem Lote" },
                    fabricante = "Não informado",
                    dataAplicacao = formatarDataParaApi(aplicacao),
                    dataProximaDose = formatarDataParaApi(proxima),
                    veterinarioCrmv = veterinario.ifBlank { "Não informado" }
                )

                RetrofitClient.apiService.registrarVacina(vacinaApi)
            } catch (_: Exception) {
                // Silencioso se der erro de rede para não travar a usabilidade do app
            }
        }

        finish()
    }

    // Helper para formatar de DD/MM/AAAA para YYYY-MM-DD exigido pelo FastAPI
    private fun formatarDataParaApi(dataStr: String): String {
        val partes = dataStr.split("/")
        return if (partes.size == 3) {
            "${partes[2]}-${partes[1]}-${partes[0]}"
        } else {
            "2026-08-02"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVacinaPage(
    vacinaId: String?,
    petIdInicial: String?,
    nomePetInicial: String?,
    nomeInicial: String?,
    aplicacaoInicial: String?,
    proximaInicial: String?,
    loteInicial: String?,
    veterinarioInicial: String?,
    statusInicial: String?,
    onSalvarClick: (String?, FBPet, String, String, String, String, String) -> Unit,
    onBackClick: () -> Unit
) {
    var nome by rememberSaveable { mutableStateOf(nomeInicial ?: "") }
    var data by rememberSaveable { mutableStateOf(aplicacaoInicial ?: "") }
    var proxima by rememberSaveable { mutableStateOf(proximaInicial ?: "") }
    var lote by rememberSaveable { mutableStateOf(loteInicial ?: "") }
    var veterinario by rememberSaveable { mutableStateOf(veterinarioInicial ?: "") }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Estado do Catálogo da API FastAPI no Render
    var catalogoApi by remember { mutableStateOf<List<VacinaCatalogoApi>>(emptyList()) }
    var showCatalogoMenu by remember { mutableStateOf(false) }

    // Busca o catálogo da sua API assim que a tela abre
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.apiService.getCatalogoVacinas()
            if (response.isSuccessful) {
                catalogoApi = response.body() ?: emptyList()
            }
        } catch (_: Exception) {
            // Continua com lista vazia caso esteja sem conexão
        }
    }

    // ESTADOS DO DROPDOWN DO PET
    var expandedPetDropdown by remember { mutableStateOf(false) }
    var petSelecionado by remember {
        mutableStateOf<FBPet?>(
            if (petIdInicial != null) {
                FBPet().apply {
                    id = petIdInicial
                    nomePet = nomePetInicial ?: ""
                }
            } else null
        )
    }
    var listaPets by remember { mutableStateOf(listOf<FBPet>()) }

    // BUSCA OS PETS DO FIREBASE
    LaunchedEffect(Unit) {
        FBDatabase().getPets {
            listaPets = it
        }
    }

    // Filtro de sugestões baseadas no que o usuário digitou
    val sugestoesFiltradas = remember(nome, catalogoApi) {
        if (nome.isBlank()) catalogoApi else catalogoApi.filter {
            it.nome.contains(nome, ignoreCase = true)
        }
    }

    // Helper interno para somar meses usando Calendar (compatível com todas versões do Android)
    fun somarMeses(dataBase: Date, meses: Int): String {
        val cal = Calendar.getInstance()
        cal.time = dataBase
        cal.add(Calendar.MONTH, meses)
        return dateFormat.format(cal.time)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Teal50, White)))
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Navy900)
            }
            Text(
                text = if (vacinaId.isNullOrBlank()) "Registrar Vacina" else "Editar Vacina",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            SectionLabel("INFORMAÇÕES DA VACINA")
            Spacer(Modifier.height(12.dp))

            // DROPDOWN DE SELEÇÃO DO PET
            ExposedDropdownMenuBox(
                expanded = expandedPetDropdown,
                onExpandedChange = { expandedPetDropdown = !expandedPetDropdown }
            ) {
                OutlinedTextField(
                    value = petSelecionado?.nomePet ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Pet") },
                    placeholder = { Text("Selecione o pet que tomou a vacina") },
                    leadingIcon = { Icon(Icons.Default.Pets, null, tint = Teal500) },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = petFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expandedPetDropdown,
                    onDismissRequest = { expandedPetDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Selecione o pet que tomou a vacina") },
                        enabled = false,
                        onClick = {}
                    )
                    listaPets.forEach { pet ->
                        DropdownMenuItem(
                            text = { Text(pet.nomePet ?: "") },
                            onClick = {
                                petSelecionado = pet
                                expandedPetDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // DROPDOWN DO NOME DA VACINA COM AUTOCOMPLETE E PREENCHIMENTO AUTOMÁTICO
            ExposedDropdownMenuBox(
                expanded = showCatalogoMenu && sugestoesFiltradas.isNotEmpty(),
                onExpandedChange = { showCatalogoMenu = !showCatalogoMenu }
            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = {
                        nome = it
                        showCatalogoMenu = true
                    },
                    label = { Text("Nome da Vacina") },
                    placeholder = { Text("Ex: V8/V10, Antirrábica...") },
                    leadingIcon = { Icon(Icons.Default.Vaccines, null, tint = Teal500) },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = petFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = showCatalogoMenu && sugestoesFiltradas.isNotEmpty(),
                    onDismissRequest = { showCatalogoMenu = false }
                ) {
                    sugestoesFiltradas.forEach { vacinaItem ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(vacinaItem.nome, fontWeight = FontWeight.Bold, color = Navy900)
                                    Text(vacinaItem.descricao ?: "", fontSize = 11.sp, color = GrayText, maxLines = 1)
                                }
                            },
                            onClick = {
                                nome = vacinaItem.nome
                                showCatalogoMenu = false

                                // --- PREENCHIMENTO AUTOMÁTICO DAS DATAS (COMPATÍVEL) ---
                                val hojeDate = Date()
                                if (data.isBlank()) {
                                    data = dateFormat.format(hojeDate)
                                    proxima = somarMeses(hojeDate, 12)
                                } else if (proxima.isBlank()) {
                                    try {
                                        val parsedData = dateFormat.parse(data.trim())
                                        if (parsedData != null) {
                                            proxima = somarMeses(parsedData, 12)
                                        }
                                    } catch (_: Exception) {}
                                }
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = data,
                    onValueChange = { novaData ->
                        data = novaData
                        if (novaData.length == 10 && proxima.isBlank()) {
                            try {
                                val parsed = dateFormat.parse(novaData)
                                if (parsed != null) {
                                    proxima = somarMeses(parsed, 12)
                                }
                            } catch (_: Exception) {}
                        }
                    },
                    label = { Text("Data Aplicação") },
                    placeholder = { Text("DD/MM/AAAA") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = petFieldColors()
                )
                OutlinedTextField(
                    value = proxima,
                    onValueChange = { proxima = it },
                    label = { Text("Próxima Dose") },
                    placeholder = { Text("DD/MM/AAAA") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = petFieldColors()
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = lote,
                onValueChange = { lote = it },
                label = { Text("Lote (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = petFieldColors()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = veterinario,
                onValueChange = { veterinario = it },
                label = { Text("Veterinário Responsável") },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = Teal500) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = petFieldColors()
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    if (petSelecionado != null && nome.isNotBlank() && data.isNotBlank()) {
                        onSalvarClick(vacinaId, petSelecionado!!, nome, data, proxima, lote, veterinario)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenApplied)
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (vacinaId.isNullOrBlank()) "Salvar Vacina" else "Salvar Alterações",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}