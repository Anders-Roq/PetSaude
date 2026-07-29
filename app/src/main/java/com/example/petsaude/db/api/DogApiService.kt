package com.example.petsaude.db.api

import com.example.petsaude.BuildConfig
import org.json.JSONArray
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
    val boaComEstranhos: Int
)

sealed class ResultadoBusca {
    data class Sucesso(val info: RacaCompatibilidade) : ResultadoBusca()
    object RacaNaoEncontrada : ResultadoBusca()
    object ChaveApiAusente : ResultadoBusca()
    data class Erro(val mensagem: String) : ResultadoBusca()
}

class DogApiService {

    private val apiKey = BuildConfig.DOGS_API_KEY

    fun buscarRaca(nomeRaca: String): ResultadoBusca {
        if (apiKey.isBlank()) {
            return ResultadoBusca.ChaveApiAusente
        }

        var conn: HttpURLConnection? = null
        return try {
            val nomeEmIngles = RacasBr.paraIngles(nomeRaca)
            val query = URLEncoder.encode(nomeEmIngles.trim(), "UTF-8")
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
            ResultadoBusca.Sucesso(
                RacaCompatibilidade(
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
            )
        } catch (e: Exception) {
            ResultadoBusca.Erro(e.localizedMessage ?: "Erro de conexão")
        } finally {
            conn?.disconnect()
        }
    }
}
