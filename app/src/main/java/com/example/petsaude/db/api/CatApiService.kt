package com.example.petsaude.db.api

import android.content.Context
import com.example.petsaude.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import androidx.core.content.edit

data class RacaCompatibilidadeGato(
    val nomeRaca: String,
    val porte: String,
    val origem: String,
    val quedaDePelo: Int,
    val miado: Int,
    val brincalhice: Int,
    val amigavelComFamilia: Int,
    val inteligencia: Int,
    val boaComCriancas: Int,
    val boaComOutrosPets: Int,
    val boaComEstranhos: Int,
    val cuidadosComPelagem: Int,
    val saudeGeral: Int,
    val viaCache: Boolean = false
)

sealed class ResultadoBuscaGato {
    data class Sucesso(val info: RacaCompatibilidadeGato) : ResultadoBuscaGato()
    object RacaNaoEncontrada : ResultadoBuscaGato()
    object ChaveApiAusente : ResultadoBuscaGato()
    data class Erro(val mensagem: String) : ResultadoBuscaGato()
}

class CatApiService(context: Context) {

    private val apiKey = BuildConfig.DOGS_API_KEY
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "cat_compat_cache"
    }

    fun buscarRaca(nomeRaca: String): ResultadoBuscaGato {
        if (apiKey.isBlank()) {
            return ResultadoBuscaGato.ChaveApiAusente
        }

        val nomeEmIngles = CatasBr.paraIngles(nomeRaca).trim()
        val chaveCache = nomeEmIngles.lowercase()

        obterDoCache(chaveCache)?.let { info ->
            return ResultadoBuscaGato.Sucesso(info.copy(viaCache = true))
        }

        var conn: HttpURLConnection? = null
        return try {
            val query = URLEncoder.encode(nomeEmIngles, "UTF-8")
            val url = URL("https://api.api-ninjas.com/v1/cats?name=$query")

            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("X-Api-Key", apiKey)
                connectTimeout = 10_000
                readTimeout = 10_000
            }

            if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                return ResultadoBuscaGato.Erro("Falha na consulta (código ${conn.responseCode})")
            }

            val corpo = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            val array = JSONArray(corpo)

            if (array.length() == 0) {
                return ResultadoBuscaGato.RacaNaoEncontrada
            }

            val obj = array.getJSONObject(0)
            val info = RacaCompatibilidadeGato(
                nomeRaca = obj.optString("name", nomeRaca),
                porte = traduzirPorte(obj.optString("length", "")),
                origem = obj.optString("origin", "—"),
                quedaDePelo = obj.optInt("shedding", 0),
                miado = obj.optInt("meowing", 0),
                brincalhice = obj.optInt("playfulness", 0),
                amigavelComFamilia = obj.optInt("family_friendly", 0),
                inteligencia = obj.optInt("intelligence", 0),
                boaComCriancas = obj.optInt("children_friendly", 0),
                boaComOutrosPets = obj.optInt("other_pets_friendly", 0),
                boaComEstranhos = obj.optInt("stranger_friendly", 0),
                cuidadosComPelagem = obj.optInt("grooming", 0),
                saudeGeral = obj.optInt("general_health", 0)
            )

            salvarNoCache(chaveCache, info)
            ResultadoBuscaGato.Sucesso(info)
        } catch (e: Exception) {
            ResultadoBuscaGato.Erro(e.localizedMessage ?: "Erro de conexão")
        } finally {
            conn?.disconnect()
        }
    }
    fun limparCache() {
        prefs.edit { clear() }
    }

    private fun traduzirPorte(valorApi: String): String = when (valorApi.trim().lowercase()) {
        "short" -> "Curto"
        "medium" -> "Médio"
        "long" -> "Longo"
        "" -> "—"
        else -> valorApi
    }

    private fun obterDoCache(chave: String): RacaCompatibilidadeGato? {
        val json = prefs.getString(chave, null) ?: return null
        return try {
            val obj = JSONObject(json)
            RacaCompatibilidadeGato(
                nomeRaca = obj.getString("nomeRaca"),
                porte = obj.getString("porte"),
                origem = obj.getString("origem"),
                quedaDePelo = obj.getInt("quedaDePelo"),
                miado = obj.getInt("miado"),
                brincalhice = obj.getInt("brincalhice"),
                amigavelComFamilia = obj.getInt("amigavelComFamilia"),
                inteligencia = obj.getInt("inteligencia"),
                boaComCriancas = obj.getInt("boaComCriancas"),
                boaComOutrosPets = obj.getInt("boaComOutrosPets"),
                boaComEstranhos = obj.getInt("boaComEstranhos"),
                cuidadosComPelagem = obj.getInt("cuidadosComPelagem"),
                saudeGeral = obj.getInt("saudeGeral")
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun salvarNoCache(chave: String, info: RacaCompatibilidadeGato) {
        val obj = JSONObject().apply {
            put("nomeRaca", info.nomeRaca)
            put("porte", info.porte)
            put("origem", info.origem)
            put("quedaDePelo", info.quedaDePelo)
            put("miado", info.miado)
            put("brincalhice", info.brincalhice)
            put("amigavelComFamilia", info.amigavelComFamilia)
            put("inteligencia", info.inteligencia)
            put("boaComCriancas", info.boaComCriancas)
            put("boaComOutrosPets", info.boaComOutrosPets)
            put("boaComEstranhos", info.boaComEstranhos)
            put("cuidadosComPelagem", info.cuidadosComPelagem)
            put("saudeGeral", info.saudeGeral)
        }
        prefs.edit { putString(chave, obj.toString()) }
    }
}
