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

data class RacaCompatibilidade(
    val nomeRaca: String,
    val quedaDePelo: Int,
    val latido: Int,
    val energia: Int,
    val protecao: Int,
    val facilidadeDeTreino: Int,
    val boaComCriancas: Int,
    val boaComOutrosCaes: Int,
    val boaComEstranhos: Int,
    val viaCache: Boolean = false
)

sealed class ResultadoBusca {
    data class Sucesso(val info: RacaCompatibilidade) : ResultadoBusca()
    object RacaNaoEncontrada : ResultadoBusca()
    object ChaveApiAusente : ResultadoBusca()
    data class Erro(val mensagem: String) : ResultadoBusca()
}

class DogApiService(context: Context) {

    private val apiKey = BuildConfig.DOGS_API_KEY
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "dog_compat_cache"
    }

    fun buscarRaca(nomeRaca: String): ResultadoBusca {
        if (apiKey.isBlank()) {
            return ResultadoBusca.ChaveApiAusente
        }

        val nomeEmIngles = RacasBr.paraIngles(nomeRaca).trim()
        val chaveCache = normalizarChaveCache(nomeEmIngles)

        obterDoCache(chaveCache)?.let { info ->
            return ResultadoBusca.Sucesso(info.copy(viaCache = true))
        }

        var conn: HttpURLConnection? = null
        return try {
            val query = URLEncoder.encode(nomeEmIngles, "UTF-8")
            val url = URL("https://api.api-ninjas.com/v1/dogs?name=$query")

            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("X-Api-Key", apiKey)
                connectTimeout = 10_000
                readTimeout = 10_000
            }

            if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                return ResultadoBusca.Erro("Falha na consulta (código ${conn.responseCode})")
            }

            val corpo = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            val array = JSONArray(corpo)

            if (array.length() == 0) {
                return ResultadoBusca.RacaNaoEncontrada
            }

            val obj = array.getJSONObject(0)
            val info = RacaCompatibilidade(
                nomeRaca = obj.optString("name", nomeRaca),
                quedaDePelo = obj.optInt("shedding", 0),
                latido = obj.optInt("barking", 0),
                energia = obj.optInt("energy", 0),
                protecao = obj.optInt("protectiveness", 0),
                facilidadeDeTreino = obj.optInt("trainability", 0),
                boaComCriancas = obj.optInt("good_with_children", 0),
                boaComOutrosCaes = obj.optInt("good_with_other_dogs", 0),
                boaComEstranhos = obj.optInt("good_with_strangers", 0)
            )

            salvarNoCache(chaveCache, info)
            ResultadoBusca.Sucesso(info)
        } catch (e: Exception) {
            ResultadoBusca.Erro(e.localizedMessage ?: "Erro de conexão")
        } finally {
            conn?.disconnect()
        }
    }

    fun limparCache() {
        prefs.edit().clear().apply()
    }

    private fun normalizarChaveCache(nomeEmIngles: String): String =
        nomeEmIngles.lowercase()

    private fun obterDoCache(chave: String): RacaCompatibilidade? {
        val json = prefs.getString(chave, null) ?: return null
        return try {
            val obj = JSONObject(json)
            RacaCompatibilidade(
                nomeRaca = obj.getString("nomeRaca"),
                quedaDePelo = obj.getInt("quedaDePelo"),
                latido = obj.getInt("latido"),
                energia = obj.getInt("energia"),
                protecao = obj.getInt("protecao"),
                facilidadeDeTreino = obj.getInt("facilidadeDeTreino"),
                boaComCriancas = obj.getInt("boaComCriancas"),
                boaComOutrosCaes = obj.getInt("boaComOutrosCaes"),
                boaComEstranhos = obj.getInt("boaComEstranhos")
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun salvarNoCache(chave: String, info: RacaCompatibilidade) {
        val obj = JSONObject().apply {
            put("nomeRaca", info.nomeRaca)
            put("quedaDePelo", info.quedaDePelo)
            put("latido", info.latido)
            put("energia", info.energia)
            put("protecao", info.protecao)
            put("facilidadeDeTreino", info.facilidadeDeTreino)
            put("boaComCriancas", info.boaComCriancas)
            put("boaComOutrosCaes", info.boaComOutrosCaes)
            put("boaComEstranhos", info.boaComEstranhos)
        }
        prefs.edit().putString(chave, obj.toString()).apply()
    }
}
