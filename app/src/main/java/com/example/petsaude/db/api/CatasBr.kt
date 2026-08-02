package com.example.petsaude.db.api

import java.text.Normalizer

object CatasBr {

    private val racas = linkedMapOf(
        "Persa" to "Persian",
        "Siamês" to "Siamese",
        "Maine Coon" to "Maine Coon",
        "Ragdoll" to "Ragdoll",
        "British Shorthair" to "British Shorthair",
        "Sphynx (Sem Pelo)" to "Sphynx",
        "Bengala" to "Bengal",
        "Abissínio" to "Abyssinian",
        "Azul Russo" to "Russian Blue",
        "Scottish Fold" to "Scottish Fold",
        "Munchkin" to "Munchkin",
        "American Shorthair" to "American Shorthair",
        "Exótico de Pelo Curto" to "Exotic Shorthair",
        "Norueguês da Floresta" to "Norwegian Forest Cat",
        "Devon Rex" to "Devon Rex",
        "Cornish Rex" to "Cornish Rex",
        "Oriental de Pelo Curto" to "Oriental Shorthair",
        "Birmanês (Burmese)" to "Burmese",
        "Himalaio" to "Himalayan",
        "Angorá Turco" to "Turkish Angora",
        "Van Turco" to "Turkish Van",
        "Manx" to "Manx",
        "Bombaim" to "Bombay",
        "Chartreux" to "Chartreux",
        "Korat" to "Korat",
        "Balinês" to "Balinese",
        "Somali" to "Somali",
        "Tonquinês" to "Tonkinese",
        "Egyptian Mau" to "Egyptian Mau",
        "Selkirk Rex" to "Selkirk Rex",
        "American Curl" to "American Curl",
        "American Bobtail" to "American Bobtail",
        "Japanese Bobtail" to "Japanese Bobtail",
        "Pixie-bob" to "Pixie-bob",
        "Ragamuffin" to "Ragamuffin",
        "Savannah" to "Savannah",
        "Siberiano" to "Siberian",
        "Singapura" to "Singapura",
    )

    val nomesEmPortugues: List<String> = racas.keys.toList()

    private fun normalizar(texto: String): String =
        Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
            .lowercase()
            .trim()

    fun sugestoes(prefixo: String, limite: Int = nomesEmPortugues.size): List<String> {
        if (prefixo.isBlank()) return nomesEmPortugues.take(limite)
        val alvo = normalizar(prefixo)
        return racas.keys.filter { normalizar(it).contains(alvo) }.take(limite)
    }

    fun paraIngles(nomePt: String): String {
        val alvo = normalizar(nomePt)
        val encontrada = racas.entries.firstOrNull { normalizar(it.key) == alvo }
        return encontrada?.value ?: nomePt
    }
}
