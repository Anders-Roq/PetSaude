package com.example.petsaude.util

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.petsaude.db.fb.FBDatabase

class VacinaWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        // Busca vacinas no Firebase em segundo plano
        FBDatabase().getPets { _ ->
            FBDatabase().listenVacinas { lista ->
                lista.forEach { v ->
                    VacinaNotificationHelper.verificarEAgendarNotificacoes(
                        context = applicationContext,
                        nomePet = v.nomePet ?: "",
                        nomeVacina = v.nome ?: "",
                        proximaDoseStr = v.proxima ?: "",
                        idVacina = v.id
                    )
                }
            }
        }
        return Result.success()
    }
}