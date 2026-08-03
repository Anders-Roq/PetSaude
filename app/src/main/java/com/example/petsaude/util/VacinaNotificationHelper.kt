package com.example.petsaude.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.petsaude.view.VacinasActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object VacinaNotificationHelper {

    private const val CHANNEL_ID = "petsaude_vacinas_channel"
    private const val CHANNEL_NAME = "Lembretes de Vacinas"

    fun criarCanalNotificacao(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações para vacinas a vencer ou vencidas do seu Pet"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun enviarNotificacao(context: Context, idNotificacao: Int, titulo: String, mensagem: String) {
        criarCanalNotificacao(context)

        // 1. Cria a Intent que abre a VacinasActivity
        val intent = Intent(context, VacinasActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // 2. Cria a PendingIntent para ser disparada pelo clique do usuário
        val pendingIntent = PendingIntent.getActivity(
            context,
            idNotificacao, // ID único para evitar sobrescrever intents
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Associa a PendingIntent no NotificationCompat.Builder
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Substitua pelo ícone do seu app se preferir
            .setContentTitle(titulo)
            .setContentText(mensagem)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // <-- AQUI FAZ O CLIQUE FUNCIONAR
            .setAutoCancel(true)            // Fechar a notificação ao clicar

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(idNotificacao, builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun parseData(dataStr: String): LocalDate? {
        return try {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            LocalDate.parse(dataStr.trim(), formatter)
        } catch (_: Exception) {
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calcularStatus(proximaDoseStr: String): String {
        val dataProxima = parseData(proximaDoseStr) ?: return "Aplicada"
        val hoje = LocalDate.now()

        return if (hoje.isAfter(dataProxima)) {
            "Vencida"
        } else {
            "Aplicada"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun verificarEAgendarNotificacoes(
        context: Context,
        nomePet: String,
        nomeVacina: String,
        proximaDoseStr: String,
        idVacina: String?
    ) {
        val dataProxima = parseData(proximaDoseStr) ?: return
        val hoje = LocalDate.now()

        val diasRestantes = ChronoUnit.DAYS.between(hoje, dataProxima)
        val notifId = (idVacina ?: nomeVacina).hashCode()

        when {
            diasRestantes <= 0 -> {
                enviarNotificacao(
                    context,
                    notifId,
                    "⚠️ Vacina Vencida!",
                    "A vacina $nomeVacina de $nomePet venceu em $proximaDoseStr. Procure um veterinário!"
                )
            }
            diasRestantes in 1..30 -> {
                enviarNotificacao(
                    context,
                    notifId,
                    "📅 Lembrete de Vacina",
                    "A vacina $nomeVacina de $nomePet vence em $diasRestantes dia(s) ($proximaDoseStr)."
                )
            }
        }
    }
}