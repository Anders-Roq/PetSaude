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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
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
import com.example.petsaude.db.fb.toFBConsulta
import com.example.petsaude.model.Consulta
import com.example.petsaude.ui.theme.*
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Pets
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.example.petsaude.db.fb.FBConsulta

class AddConsultaActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val consultaId = intent.getStringExtra("id")
        val petId = intent.getStringExtra("petId")
        val nomePet = intent.getStringExtra("nomePet")
        val veterinario = intent.getStringExtra("veterinario")
        val motivo = intent.getStringExtra("motivo")
        val data = intent.getStringExtra("data")
        val horario = intent.getStringExtra("horario")
        val endereco = intent.getStringExtra("endereco")

        enableEdgeToEdge()
        setContent {
            PetSaudeTheme {
                AddConsultaPage(
                    consultaId = consultaId,
                    petIdInicial = petId,
                    nomePetInicial = nomePet,
                    veterinarioInicial = veterinario,
                    motivoInicial = motivo,
                    dataInicial = data,
                    horarioInicial = horario,
                    enderecoInicial = endereco,
                    onSalvarClick = { id, pet, vet, dt, hr, mot, end ->
                        salvarConsulta(id, pet, vet, dt, hr, mot, end)
                    },
                    onBackClick = { finish() }
                )
            }
        }
    }

    private fun salvarConsulta(
        id: String?,
        pet: FBPet,
        veterinario: String,
        data: String,
        horario: String,
        motivo: String,
        endereco: String
    ) {
        val consulta = FBConsulta(
            id = id,
            petId = pet.id ?: "",
            nomePet = pet.nomePet ?: "",
            veterinario = veterinario,
            motivo = motivo,
            data = data,
            horario = horario,
            endereco = endereco
        )

        if (id.isNullOrBlank()) {
            FBDatabase().addConsulta(consulta)
            Toast.makeText(this, "Consulta cadastrada com sucesso!", Toast.LENGTH_LONG).show()
        } else {
            FBDatabase().updateConsulta(consulta)
            Toast.makeText(this, "Consulta atualizada com sucesso!", Toast.LENGTH_LONG).show()
        }
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddConsultaPage(
    consultaId: String?,
    petIdInicial: String?,
    nomePetInicial: String?,
    veterinarioInicial: String?,
    motivoInicial: String?,
    dataInicial: String?,
    horarioInicial: String?,
    enderecoInicial: String?,
    onSalvarClick: (String?, FBPet, String, String, String, String, String) -> Unit,
    onBackClick: () -> Unit
) {
    var veterinario by rememberSaveable { mutableStateOf(veterinarioInicial ?: "") }
    var data by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = dataInicial ?: "", selection = TextRange((dataInicial ?: "").length)))
    }
    var horario by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = horarioInicial ?: "", selection = TextRange((horarioInicial ?: "").length)))
    }
    var motivo by rememberSaveable { mutableStateOf(motivoInicial ?: "") }
    var endereco by rememberSaveable { mutableStateOf(enderecoInicial ?: "") }
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
        LaunchedEffect(Unit) {
            FBDatabase().getPets {
                listaPets = it
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Teal50,
                            White
                        )
                    )
                )
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Navy900
                    )
                }
                Text(
                    text = "Agendar Consulta",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                SectionLabel("DETALHES DO AGENDAMENTO")
                Spacer(Modifier.height(12.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = {
                        expanded = !expanded
                    }
                ) {
                    OutlinedTextField(
                        value = petSelecionado?.nomePet ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text("Pet")
                        },
                        placeholder = {
                            Text("Selecione o pet da consulta")
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Pets,
                                null,
                                tint = Teal500
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                null
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = petFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Selecione o pet da consulta"
                                )
                            },
                            enabled = false,
                            onClick = {}
                        )
                        listaPets.forEach { pet ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        pet.nomePet ?: ""
                                    )
                                },
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
                    value = veterinario,
                    onValueChange = {
                        veterinario = it
                    },
                    label = {
                        Text("Nome do Veterinário")
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            null,
                            tint = Teal500
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = petFieldColors()
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        value = data,
                        onValueChange = {value ->
                            val numeros = value.text.filter { it.isDigit() }.take(8)
                            val texto = buildString {
                                numeros.forEachIndexed { index, c ->
                                    append(c)
                                    if (index == 1 && numeros.length > 2) append("/")
                                    if (index == 3 && numeros.length > 4) append("/")
                                }
                            }
                            data = TextFieldValue(
                                text = texto,
                                selection = TextRange(texto.length) // cursor no final
                            )
                        },
                        label = {
                            Text("Data")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = petFieldColors()
                    )
                    OutlinedTextField(
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        value = horario,
                        onValueChange = { value ->
                            val digits = value.text.filter { it.isDigit() }.take(4)
                            val textoFormatado = buildString {
                                digits.forEachIndexed { index, c ->
                                    append(c)
                                    if (index == 1 && digits.length > 2) {
                                        append(":")
                                    }
                                }
                            }
                            horario = TextFieldValue(
                                text = textoFormatado,
                                selection = TextRange(textoFormatado.length)
                            )
                        },
                        label = {
                            Text("Horário")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = petFieldColors()
                    )
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = motivo,
                    onValueChange = {
                        motivo = it
                    },
                    label = {
                        Text("Motivo / Especialidade")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = petFieldColors()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = endereco,
                    onValueChange = {
                        endereco = it
                    },
                    label = {
                        Text("Endereço")
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Business,
                            null,
                            tint = Teal500
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = petFieldColors()
                )
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = {
                        if (petSelecionado == null) {
                            return@Button
                        }
                        // 💡 ALTERADO: Agora chama a função genérica que decide se vai criar ou editar
                        onSalvarClick(
                            consultaId,
                            petSelecionado!!,
                            veterinario,
                            data.text,
                            horario.text,
                            motivo,
                            endereco
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Teal500
                    )
                ) {
                    Icon(
                        Icons.Default.Event,
                        null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        // 💡 ALTERADO: O texto muda dinamicamente se for uma edição ou um novo cadastro
                        text = if (consultaId.isNullOrBlank()) "Confirmar Agendamento" else "Salvar Alterações",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
        }
    }
}