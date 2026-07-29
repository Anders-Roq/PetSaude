package com.example.petsaude.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.petsaude.db.api.DogApiService
import com.example.petsaude.db.api.RacaCompatibilidade
import com.example.petsaude.db.api.RacasBr
import com.example.petsaude.db.api.ResultadoBusca
import com.example.petsaude.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DogCompatibilityActivity : ComponentActivity() {

    private val service = DogApiService()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetSaudeTheme {
                var estado by remember { mutableStateOf<EstadoBusca>(EstadoBusca.Ocioso) }

                DogCompatibilityPage(
                    estado = estado,
                    onBackClick = { finish() },
                    onBuscarClick = { raca ->
                        estado = EstadoBusca.Carregando
                        lifecycleScope.launch {
                            val resultado = withContext(Dispatchers.IO) {
                                service.buscarRaca(raca)
                            }
                            estado = when (resultado) {
                                is ResultadoBusca.Sucesso -> EstadoBusca.Sucesso(resultado.info)
                                is ResultadoBusca.RacaNaoEncontrada -> EstadoBusca.Mensagem(
                                    "Não encontramos essa raça. Selecione uma opção da lista de sugestões enquanto digita."
                                )
                                is ResultadoBusca.ChaveApiAusente -> EstadoBusca.Mensagem(
                                    "Chave da API não configurada. Adicione DOGS_API_KEY no gradle.properties."
                                )
                                is ResultadoBusca.Erro -> EstadoBusca.Mensagem(
                                    "Não foi possível consultar agora: ${resultado.mensagem}"
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

/** Estado da tela de busca. */
sealed class EstadoBusca {
    object Ocioso : EstadoBusca()
    object Carregando : EstadoBusca()
    data class Sucesso(val info: RacaCompatibilidade) : EstadoBusca()
    data class Mensagem(val texto: String) : EstadoBusca()
}

private data class ItemCompatibilidade(val rotulo: String, val nivel: Int)

@ExperimentalMaterial3Api
@Preview(showBackground = true)
@Composable
fun DogCompatibilityPage(
    estado: EstadoBusca = EstadoBusca.Ocioso,
    onBackClick: () -> Unit = {},
    onBuscarClick: (String) -> Unit = {}
) {
    var raca by rememberSaveable { mutableStateOf("") }
    var tentouBuscar by rememberSaveable { mutableStateOf(false) }
    var mostrarSugestoes by rememberSaveable { mutableStateOf(false) }
    val racaInvalida = tentouBuscar && raca.isBlank()
    val sugestoes = remember(raca) { RacasBr.sugestoes(raca) }

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
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Navy900)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Este pet é bom para mim?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )
            Text(
                text = "Toque no campo para ver todas as raças ou digite para filtrar",
                fontSize = 13.sp,
                color = GrayText
            )

            Spacer(modifier = Modifier.height(24.dp))

            ExposedDropdownMenuBox(
                expanded = mostrarSugestoes,
                onExpandedChange = { mostrarSugestoes = it }
            ) {
                OutlinedTextField(
                    value = raca,
                    onValueChange = {
                        raca = it
                        mostrarSugestoes = true
                    },
                    label = { Text("Raça do cachorro") },
                    placeholder = { Text("Ex: Labrador Retriever") },
                    leadingIcon = { Icon(Icons.Default.Pets, null, tint = Teal500) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = mostrarSugestoes)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true)
                        .onFocusChanged { if (it.isFocused) mostrarSugestoes = true },
                    singleLine = true,
                    isError = racaInvalida,
                    supportingText = {
                        if (racaInvalida) {
                            Text("Selecione o nome da raça", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("Digite em português, nós traduzimos para a busca")
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = petFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = mostrarSugestoes && sugestoes.isNotEmpty(),
                    onDismissRequest = { mostrarSugestoes = false }
                ) {
                    sugestoes.forEach { nomePt ->
                        DropdownMenuItem(
                            text = { Text(nomePt) },
                            onClick = {
                                raca = nomePt
                                mostrarSugestoes = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    tentouBuscar = true
                    mostrarSugestoes = false
                    if (raca.isNotBlank()) onBuscarClick(raca)
                },
                enabled = estado != EstadoBusca.Carregando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal500)
            ) {
                if (estado == EstadoBusca.Carregando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            when (estado) {
                is EstadoBusca.Sucesso -> ResultadoCompatibilidade(estado.info)
                is EstadoBusca.Mensagem -> Text(
                    text = estado.texto,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
                else -> {}
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ResultadoCompatibilidade(info: RacaCompatibilidade) {
    val itens = listOf(
        ItemCompatibilidade("Queda de pelo", info.quedaDePelo),
        ItemCompatibilidade("Latido", info.latido),
        ItemCompatibilidade("Energia", info.energia),
        ItemCompatibilidade("Proteção / territorialidade", info.protecao),
        ItemCompatibilidade("Facilidade de treino", info.facilidadeDeTreino),
        ItemCompatibilidade("Boa com crianças", info.boaComCriancas),
        ItemCompatibilidade("Boa com outros cães", info.boaComOutrosCaes),
        ItemCompatibilidade("Boa com estranhos", info.boaComEstranhos)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = info.nomeRaca,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )

            Spacer(modifier = Modifier.height(16.dp))

            itens.forEach { item ->
                NivelCompatibilidade(item.rotulo, item.nivel)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun NivelCompatibilidade(rotulo: String, nivel: Int) {
    val nivelSeguro = nivel.coerceIn(0, 5)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(rotulo, fontSize = 13.sp, color = Navy900)
            Text("$nivelSeguro/5", fontSize = 12.sp, color = GrayText)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(5) { indice ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .background(
                            color = if (indice < nivelSeguro) Teal500 else GrayBorder,
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }
        }
    }
}
