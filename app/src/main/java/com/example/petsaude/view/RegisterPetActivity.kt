package com.example.petsaude.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petsaude.db.fb.FBDatabase
import com.example.petsaude.db.fb.toFBPet
import com.example.petsaude.model.Pet
import com.example.petsaude.ui.theme.*

class RegisterPetActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetSaudeTheme {
                RegisterPetPage(
                    onSaveClick = { pet ->
                        FBDatabase().add(pet.toFBPet())
                        finish() // volta para a HomeActivity, que já escuta os pets via listener
                    },
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterPetPage(
    onSaveClick: (Pet) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var nome by rememberSaveable { mutableStateOf("") }
    var especie by rememberSaveable { mutableStateOf("Cachorro") }
    var raca by rememberSaveable { mutableStateOf("") }
    var idade by rememberSaveable { mutableStateOf("") }
    var peso by rememberSaveable { mutableStateOf("") }
    var sexo by rememberSaveable { mutableStateOf("Macho") }
    var cor by rememberSaveable { mutableStateOf("") }
    var microchip by rememberSaveable { mutableStateOf("") }

    var tentouSalvar by rememberSaveable { mutableStateOf(false) }

    val nomeInvalido = tentouSalvar && nome.isBlank()
    val racaInvalida = tentouSalvar && raca.isBlank()
    val especieInvalida = tentouSalvar && especie.isBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Teal50, White)))
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 20.dp)
        ) {
            // Top bar
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Navy900)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Cadastrar Pet",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )
            Text(
                text = "Adicione as informações do seu pet",
                fontSize = 13.sp,
                color = GrayText
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(Teal100, RoundedCornerShape(50.dp))
                    .border(2.dp, Teal300, RoundedCornerShape(50.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🐶", fontSize = 38.sp)
                    Text("Foto", fontSize = 10.sp, color = Teal500, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionLabel("IDENTIFICAÇÃO")

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome do pet *") },
                leadingIcon = { Icon(Icons.Default.Pets, null, tint = Teal500) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nomeInvalido,
                supportingText = {
                    if (nomeInvalido) Text("Informe o nome do pet", color = MaterialTheme.colorScheme.error)
                },
                shape = RoundedCornerShape(12.dp),
                colors = petFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Espécie row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Cachorro" to "🐕", "Gato" to "🐈").forEach { (label, emoji) ->
                    val selected = especie == label
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .background(
                                if (selected) Teal500 else White,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.5.dp,
                                if (selected) Teal500 else if (especieInvalida) MaterialTheme.colorScheme.error else GrayBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { especie = label },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(emoji, fontSize = 18.sp)
                            Text(
                                label,
                                fontSize = 11.sp,
                                color = if (selected) White else GrayText,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
            if (especieInvalida) {
                Text(
                    "Selecione a espécie",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = raca,
                onValueChange = { raca = it },
                label = { Text("Raça *") },
                leadingIcon = { Icon(Icons.Default.Info, null, tint = Teal500) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = racaInvalida,
                supportingText = {
                    if (racaInvalida) Text("Informe a raça", color = MaterialTheme.colorScheme.error)
                },
                shape = RoundedCornerShape(12.dp),
                colors = petFieldColors()
            )

            Spacer(modifier = Modifier.height(20.dp))

            SectionLabel("DETALHES")

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = idade,
                    onValueChange = { idade = it },
                    label = { Text("Idade") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = petFieldColors()
                )
                OutlinedTextField(
                    value = peso,
                    onValueChange = { peso = it },
                    label = { Text("Peso") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = petFieldColors()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sexo selector
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("Macho", "Fêmea").forEach { s ->
                    val selected = sexo == s
                    FilterChip(
                        selected = selected,
                        onClick = { sexo = s },
                        label = { Text(s, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal) },
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Teal100,
                            selectedLabelColor = Teal500
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = GrayBorder,
                            selectedBorderColor = Teal500,
                            enabled = true,
                            selected = selected
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = cor,
                onValueChange = { cor = it },
                label = { Text("Cor / Pelagem") },
                leadingIcon = { Icon(Icons.Default.ColorLens, null, tint = Teal500) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = petFieldColors()
            )

            Spacer(modifier = Modifier.height(20.dp))

            SectionLabel("OPCIONAL")

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = microchip,
                onValueChange = { microchip = it },
                label = { Text("Número do Microchip") },
                leadingIcon = { Icon(Icons.Default.Memory, null, tint = Teal500) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = petFieldColors()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    tentouSalvar = true
                    if (nome.isNotBlank() && raca.isNotBlank() && especie.isNotBlank()) {
                        val pet = Pet(
                            nomePet = nome,
                            especie = especie,
                            raca = raca,
                            idade = idade.filter { it.isDigit() }.toIntOrNull() ?: 0,
                            peso = peso.replace(",", ".").filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 0f,
                            sexo = sexo,
                            pelagem = cor,
                            microchip = microchip.ifBlank { null }
                        )
                        onSaveClick(pet)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal500)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Salvar e Continuar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
