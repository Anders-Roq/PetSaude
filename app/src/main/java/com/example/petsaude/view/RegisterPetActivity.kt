package com.example.petsaude.view

import android.Manifest
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petsaude.db.fb.FBDatabase
import com.example.petsaude.db.fb.toFBPet
import com.example.petsaude.model.Pet
import com.example.petsaude.ui.theme.*
import com.example.petsaude.util.FotoPetStore
import com.example.petsaude.util.FotoStorage

class RegisterPetActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PET_ID = "extra_pet_id"
        const val EXTRA_NOME = "extra_nome"
        const val EXTRA_ESPECIE = "extra_especie"
        const val EXTRA_RACA = "extra_raca"
        const val EXTRA_IDADE = "extra_idade"
        const val EXTRA_PESO = "extra_peso"
        const val EXTRA_SEXO = "extra_sexo"
        const val EXTRA_PELAGEM = "extra_pelagem"
        const val EXTRA_MICROCHIP = "extra_microchip"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Se veio um petId, estamos editando um pet já existente
        val petId = intent.getStringExtra(EXTRA_PET_ID)
        val petExistente = petId?.let {
            Pet(
                id = it,
                nomePet = intent.getStringExtra(EXTRA_NOME) ?: "",
                especie = intent.getStringExtra(EXTRA_ESPECIE) ?: "Cachorro",
                raca = intent.getStringExtra(EXTRA_RACA) ?: "",
                idade = intent.getIntExtra(EXTRA_IDADE, 0),
                peso = intent.getFloatExtra(EXTRA_PESO, 0f),
                sexo = intent.getStringExtra(EXTRA_SEXO) ?: "Macho",
                pelagem = intent.getStringExtra(EXTRA_PELAGEM) ?: "",
                microchip = intent.getStringExtra(EXTRA_MICROCHIP)
            )
        }

        setContent {
            PetSaudeTheme {
                RegisterPetPage(
                    petParaEditar = petExistente,
                    onSaveClick = { pet ->
                        FBDatabase().add(pet.toFBPet())
                        finish() // volta para a HomeActivity, que já escuta os pets via listener
                    },
                    onDeleteClick = { pet ->
                        FBDatabase().remove(pet.toFBPet())
                        finish()
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
    petParaEditar: Pet? = null,
    onSaveClick: (Pet) -> Unit = {},
    onDeleteClick: (Pet) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val emEdicao = petParaEditar != null
    val context = LocalContext.current

    // Id estável do pet: se for edição, usa o id existente; se for novo pet,
    // gera um id agora mesmo (não só ao salvar), pois a foto precisa de um
    // id para ser salva localmente antes do botão "Salvar" ser tocado.
    val petId = remember { petParaEditar?.id ?: java.util.UUID.randomUUID().toString() }

    var caminhoFoto by rememberSaveable {
        mutableStateOf(FotoPetStore.obterCaminho(context, petId))
    }
    var mostrarEscolhaFoto by rememberSaveable { mutableStateOf(false) }
    var uriCameraTemp by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { sucesso ->
        val uriTemp = uriCameraTemp
        if (sucesso && uriTemp != null) {
            val caminhoFinal = FotoStorage.salvarFotoPet(context, petId, uriTemp)
            FotoPetStore.salvarCaminho(context, petId, caminhoFinal)
            caminhoFoto = caminhoFinal
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
            val caminhoFinal = FotoStorage.salvarFotoPet(context, petId, uri)
            FotoPetStore.salvarCaminho(context, petId, caminhoFinal)
            caminhoFoto = caminhoFinal
        }
    }

    var nome by rememberSaveable { mutableStateOf(petParaEditar?.nomePet ?: "") }
    var especie by rememberSaveable { mutableStateOf(petParaEditar?.especie ?: "Cachorro") }
    var raca by rememberSaveable { mutableStateOf(petParaEditar?.raca ?: "") }
    var idade by rememberSaveable { mutableStateOf(petParaEditar?.idade?.takeIf { it > 0 }?.toString() ?: "") }
    var peso by rememberSaveable { mutableStateOf(petParaEditar?.peso?.takeIf { it > 0f }?.toString() ?: "") }
    var sexo by rememberSaveable { mutableStateOf(petParaEditar?.sexo ?: "Macho") }
    var cor by rememberSaveable { mutableStateOf(petParaEditar?.pelagem ?: "") }
    var microchip by rememberSaveable { mutableStateOf(petParaEditar?.microchip ?: "") }

    var tentouSalvar by rememberSaveable { mutableStateOf(false) }
    var mostrarDialogoExcluir by rememberSaveable { mutableStateOf(false) }

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
                text = if (emEdicao) "Editar Pet" else "Cadastrar Pet",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )
            Text(
                text = if (emEdicao) "Atualize as informações do seu pet" else "Adicione as informações do seu pet",
                fontSize = 13.sp,
                color = GrayText
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Avatar (toque para tirar foto ou escolher da galeria)
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(Teal100, RoundedCornerShape(50.dp))
                    .border(2.dp, Teal300, RoundedCornerShape(50.dp))
                    .clickable { mostrarEscolhaFoto = true },
                contentAlignment = Alignment.Center
            ) {
                if (caminhoFoto != null) {
                    PetAvatarImage(
                        petId = petId,
                        especie = especie,
                        tamanho = 100.dp,
                        shape = RoundedCornerShape(50.dp),
                        caminhoFoto = caminhoFoto
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (especie == "Gato") "🐈" else "🐕", fontSize = 38.sp)
                        Text("Foto", fontSize = 10.sp, color = Teal500, fontWeight = FontWeight.Medium)
                    }
                }

                // Selo indicando que dá para tocar para trocar a foto
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                        .background(Teal500, RoundedCornerShape(50.dp))
                        .border(2.dp, White, RoundedCornerShape(50.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = "Alterar foto",
                        tint = White,
                        modifier = Modifier.size(14.dp)
                    )
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
                            id = petId,
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
                Text(
                    if (emEdicao) "Salvar Alterações" else "Salvar e Continuar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (emEdicao) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { mostrarDialogoExcluir = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Remover Pet", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (mostrarDialogoExcluir && petParaEditar != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoExcluir = false },
            title = { Text("Remover ${petParaEditar.nomePet}?") },
            text = { Text("Essa ação não pode ser desfeita. As consultas e vacinas já registradas para este pet não serão apagadas automaticamente.") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoExcluir = false
                    FotoPetStore.remover(context, petId)
                    onDeleteClick(petParaEditar)
                }) {
                    Text("Remover", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoExcluir = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarEscolhaFoto) {
        AlertDialog(
            onDismissRequest = { mostrarEscolhaFoto = false },
            title = { Text("Foto do pet") },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                mostrarEscolhaFoto = false
                                permissaoCameraLauncher.launch(Manifest.permission.CAMERA)
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Teal500)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Tirar foto agora")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                mostrarEscolhaFoto = false
                                galeriaLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = Teal500)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Escolher da galeria")
                    }
                    if (caminhoFoto != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    mostrarEscolhaFoto = false
                                    FotoPetStore.remover(context, petId)
                                    caminhoFoto = null
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Remover foto", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { mostrarEscolhaFoto = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
