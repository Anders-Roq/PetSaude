package com.example.petsaude.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petsaude.db.fb.FBDatabase
import com.example.petsaude.db.fb.FBPet
import com.example.petsaude.db.fb.FBVacina
import com.example.petsaude.ui.theme.*

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
        val vacina = FBVacina(
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

        if (id.isNullOrBlank()) {
            FBDatabase().addVacina(vacina)
            Toast.makeText(this, "Vacina cadastrada com sucesso!", Toast.LENGTH_LONG).show()
        } else {
            FBDatabase().updateVacina(vacina)
            Toast.makeText(this, "Vacina atualizada com sucesso!", Toast.LENGTH_LONG).show()
        }
        finish()
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

    // 🛠️ ESTADOS DO DROPDOWN DO PET
    var expanded by remember { mutableStateOf(false) }
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

    // 🛠️ BUSCA OS PETS DO FIREBASE
    LaunchedEffect(Unit) {
        FBDatabase().getPets {
            listaPets = it
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Teal50, White)))
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
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

            // 🛠️ DROPDOWN DE SELEÇÃO DO PET
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = petSelecionado?.nomePet ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Pet") },
                    placeholder = { Text("Selecione o pet que tomou a vacina") },
                    leadingIcon = { Icon(Icons.Default.Pets, null, tint = Teal500) },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = petFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
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
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome da Vacina") },
                leadingIcon = { Icon(Icons.Default.Vaccines, null, tint = Teal500) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = petFieldColors()
            )

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = data,
                    onValueChange = { data = it },
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
                    // Garante que o pet, o nome da vacina e a data não estão vazios antes de enviar
                    if (petSelecionado != null && nome.isNotBlank() && data.isNotBlank()) {
                        onSalvarClick(vacinaId, petSelecionado!!, nome, data, proxima, lote, veterinario)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
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