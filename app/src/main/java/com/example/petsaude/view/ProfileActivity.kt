package com.example.petsaude.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.petsaude.model.Usuario
import com.example.petsaude.ui.theme.*
import com.example.petsaude.util.FotoStore
import com.example.petsaude.viewmodel.ProfileViewModel
import com.example.petsaude.viewmodel.ProfileViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class ProfileActivity : ComponentActivity() {

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(FBDatabase())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PetSaudeTheme {
                ProfilePage(
                    usuario = viewModel.usuario,
                    onSalvarClick = { nome, email, telefone ->
                        viewModel.salvar(nome, email, telefone)
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
fun ProfilePage(
    usuario: Usuario? = null,
    onSalvarClick: (String, String, String) -> Unit = { _, _, _ -> },
    onBackClick: () -> Unit = {}
) {
    var nome by rememberSaveable(usuario) { mutableStateOf(usuario?.nome ?: "") }
    var email by rememberSaveable(usuario) { mutableStateOf(usuario?.email ?: "") }
    var telefone by rememberSaveable(usuario) { mutableStateOf(usuario?.telefone ?: "") }

    val context = LocalContext.current
    // A foto do usuário é guardada com a própria uid do Firebase Auth como chave
    val chaveFoto = remember { "usuario_${Firebase.auth.currentUser?.uid ?: "local"}" }

    var caminhoFoto by rememberSaveable { mutableStateOf(FotoStore.obterCaminho(context, chaveFoto)) }
    var mostrarEscolhaFoto by rememberSaveable { mutableStateOf(false) }

    val fotoPicker = rememberFotoPickerLaunchers(context, chaveFoto) { novoCaminho ->
        caminhoFoto = novoCaminho
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Teal50, White)))
            .statusBarsPadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Navy900)
            }
            Text(
                text = "Meu Perfil",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Navy900,
                modifier = Modifier.weight(1f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Foto de perfil (toque para tirar foto ou escolher da galeria)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Teal100, CircleShape)
                    .align(Alignment.CenterHorizontally)
                    .clickable { mostrarEscolhaFoto = true },
                contentAlignment = Alignment.Center
            ) {
                AvatarImage(
                    chave = chaveFoto,
                    tamanho = 120.dp,
                    shape = RoundedCornerShape(50),
                    caminhoFoto = caminhoFoto
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(64.dp), tint = Teal500)
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .background(Teal500, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            SectionLabel("DADOS DA CONTA")

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome Completo") },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = Teal500) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = petFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                leadingIcon = { Icon(Icons.Default.Email, null, tint = Teal500) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = petFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = telefone,
                onValueChange = { telefone = it },
                label = { Text("Telefone") },
                leadingIcon = { Icon(Icons.Default.Phone, null, tint = Teal500) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = petFieldColors()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onSalvarClick(nome, email, telefone) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal500)
            ) {
                Text("Salvar Alterações", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (mostrarEscolhaFoto) {
        EscolherFotoDialog(
            titulo = "Foto de perfil",
            temFotoAtual = caminhoFoto != null,
            onTirarFoto = fotoPicker.aoTirarFoto,
            onEscolherGaleria = fotoPicker.aoEscolherGaleria,
            onRemoverFoto = {
                FotoStore.remover(context, chaveFoto)
                caminhoFoto = null
            },
            onFechar = { mostrarEscolhaFoto = false }
        )
    }
}
