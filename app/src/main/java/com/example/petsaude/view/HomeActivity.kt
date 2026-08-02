package com.example.petsaude.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petsaude.db.fb.FBDatabase
import com.example.petsaude.model.Pet
import com.example.petsaude.ui.theme.*
import com.example.petsaude.viewmodel.HomeViewModel
import com.example.petsaude.viewmodel.HomeViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import androidx.compose.foundation.lazy.items

class HomeActivity : ComponentActivity() {

    //val fbDB = remember { FBDatabase() }
    val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(FBDatabase())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {

            PetSaudeTheme {

                  HomePage(
                    pets = viewModel.pets,
                    nomeUsuario = viewModel.usuario?.nome,
                    onConsultasClick = { startActivity(Intent(this, ConsultasActivity::class.java)) },
                    onVacinasClick   = { startActivity(Intent(this, VacinasActivity::class.java)) },
                    onAddPetClick    = { startActivity(Intent(this, RegisterPetActivity::class.java)) },
                    onPetClick       = { pet -> abrirEdicaoPet(pet) },
                    onProfileClick   = { startActivity(Intent(this, ProfileActivity::class.java)) },
                    onCompatibilidadeClick = { startActivity(Intent(this, PetCompatibilityActivity::class.java)) },
                    onLogoutClick    = { Firebase.auth.signOut() }
                )
            }
        }
    }

    private fun abrirEdicaoPet(pet: Pet) {
        val intent = Intent(this, RegisterPetActivity::class.java).apply {
            putExtra(RegisterPetActivity.EXTRA_PET_ID, pet.id)
            putExtra(RegisterPetActivity.EXTRA_NOME, pet.nomePet)
            putExtra(RegisterPetActivity.EXTRA_ESPECIE, pet.especie)
            putExtra(RegisterPetActivity.EXTRA_RACA, pet.raca)
            putExtra(RegisterPetActivity.EXTRA_IDADE, pet.idade)
            putExtra(RegisterPetActivity.EXTRA_PESO, pet.peso)
            putExtra(RegisterPetActivity.EXTRA_SEXO, pet.sexo)
            putExtra(RegisterPetActivity.EXTRA_PELAGEM, pet.pelagem)
            putExtra(RegisterPetActivity.EXTRA_MICROCHIP, pet.microchip)
        }
        startActivity(intent)
    }
}

@Preview(showBackground = true)
@Composable
fun HomePage(
    pets: List<Pet> = emptyList(),
    nomeUsuario: String? = null,
    onConsultasClick: () -> Unit = {},
    onVacinasClick:   () -> Unit = {},
    onAddPetClick:    () -> Unit = {},
    onPetClick:       (Pet) -> Unit = {},
    onProfileClick:   () -> Unit = {},
    onCompatibilidadeClick: () -> Unit = {},
    onLogoutClick:    () -> Unit = {}
) {
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = nomeUsuario?.let { "Olá, $it" } ?: "PetSaúde",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Teal500
            )
            Row {
                IconButton(onClick = onProfileClick) {
                    Icon(Icons.Default.AccountCircle, contentDescription = "Perfil", tint = Navy900)
                }
                IconButton(onClick = onLogoutClick) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Sair", tint = Navy900)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Meus Pets",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Navy900
                )
                TextButton(onClick = {}) {
                    Text("Ver todos", color = Teal500, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(pets) { pet ->
                    Card(
                        modifier = Modifier
                            .size(width = 140.dp, height = 160.dp)
                            .clickable(onClick = { onPetClick(pet) }),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(Teal100, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                PetAvatarImage(
                                    petId = pet.id,
                                    especie = pet.especie,
                                    tamanho = 60.dp,
                                    shape = RoundedCornerShape(50)
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            Text(
                                pet.nomePet,
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )

                            Text(
                                pet.raca,
                                fontSize = 11.sp,
                                color = GrayText
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .size(width = 140.dp, height = 160.dp)
                            .clickable(onClick = onAddPetClick),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = GrayBorder.copy(alpha = 0.3f)
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = GrayText,
                                modifier = Modifier.size(32.dp)
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                "Adicionar",
                                color = GrayText,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Categorias",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Navy900
            )

            Spacer(modifier = Modifier.height(12.dp))

            MenuCard(
                icon = Icons.Default.CalendarMonth,
                title = "Consultas",
                description = "Agende e gerencie consultas veterinárias",
                iconBg = Teal100,
                iconColor = Teal500,
                onClick = onConsultasClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            MenuCard(
                icon = Icons.Default.Vaccines,
                title = "Vacinas",
                description = "Controle o histórico de vacinação",
                iconBg = Color(0xFFDCFCE7),
                iconColor = GreenApplied,
                onClick = onVacinasClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            MenuCard(
                icon = Icons.Default.Pets,
                title = "Este pet é bom para mim?",
                description = "Veja o perfil de comportamento de uma raça",
                iconBg = Color(0xFFFFE4CC),
                iconColor = Color(0xFFFF8A3D),
                onClick = onCompatibilidadeClick
            )

        }
    }
}

@Composable
fun StatItem(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(number, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = White)
        Text(label, fontSize = 10.sp, color = White.copy(alpha = 0.8f))
    }
}

@Composable
fun MenuCard(
    icon: ImageVector,
    title: String,
    description: String,
    iconBg: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Navy900)
                Text(description, fontSize = 12.sp, color = GrayText)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GrayText)
        }
    }
}
