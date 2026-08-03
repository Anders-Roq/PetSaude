package com.example.petsaude.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.petsaude.ui.theme.*
import com.example.petsaude.R
import com.example.petsaude.util.VacinaWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Solicita permissão de Notificação para Android 13+ (API 33+)
        solicitarPermissaoNotificacao()

        // 2. Agenda a checagem diária automática de vacinas em segundo plano
        agendarVerificacaoVacinas()

        enableEdgeToEdge()
        setContent {
            PetSaudeTheme {
                MainPage(
                    onLoginClick = {
                        startActivity(Intent(this, LoginActivity::class.java))
                    },
                    onRegisterClick = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    }
                )
            }
        }
    }

    private fun solicitarPermissaoNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }
    }

    private fun agendarVerificacaoVacinas() {
        // Executa uma vez a cada 24 horas
        val workRequest = PeriodicWorkRequestBuilder<VacinaWorker>(1, TimeUnit.DAYS).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "VerificacaoVacinasDiaria",
            ExistingPeriodicWorkPolicy.KEEP, // Mantém o agendamento se já existir
            workRequest
        )
    }

}

@Preview(showBackground = true)
@Composable
fun MainPage(
    onLoginClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Teal50, White)
                )
            )
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo area
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {

                Image(
                    painter = painterResource(R.drawable.petsaude),
                    contentDescription = "Logo PetSaúde",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PetSaúde",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Teal500
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Cuide do seu pet com carinho",
                fontSize = 15.sp,
                color = GrayText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(64.dp))

            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal500)
            ) {
                Text("Entrar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, Teal500),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Teal500)
            ) {
                Text("Criar conta", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}