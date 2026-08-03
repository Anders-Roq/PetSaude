package com.example.petsaude.db.api

import java.text.Normalizer

object RacasBr {

    private val racas = linkedMapOf(
        "Labrador Retriever" to "Labrador Retriever",
        "Golden Retriever" to "Golden Retriever",
        "Pastor Alemão" to "German Shepherd",
        "Buldogue Francês" to "French Bulldog",
        "Buldogue Inglês" to "Bulldog",
        "Buldogue Americano" to "American Bulldog",
        "Poodle Standard" to "Poodle (Standard)",
        "Poodle Miniatura" to "Poodle (Miniature)",
        "Poodle Toy" to "Poodle (Toy)",
        "Rottweiler" to "Rottweiler",
        "Yorkshire Terrier" to "Yorkshire Terrier",
        "Shih Tzu" to "Shih Tzu",
        "Boxer" to "Boxer",
        "Dachshund (Salsicha)" to "Dachshund",
        "Chihuahua" to "Chihuahua",
        "Pug" to "Pug",
        "Beagle" to "Beagle",
        "Border Collie" to "Border Collie",
        "Husky Siberiano" to "Siberian Husky",
        "Akita" to "Akita",
        "Bull Terrier" to "Bull Terrier",
        "Pit Bull Americano" to "American Pit Bull Terrier",
        "Cocker Spaniel" to "Cocker Spaniel",
        "Doberman" to "Doberman Pinscher",
        "Dogue Alemão" to "Great Dane",
        "Fila Brasileiro" to "Fila Brasileiro",
        "Basset Hound" to "Basset Hound",
        "Lhasa Apso" to "Lhasa Apso",
        "Maltês" to "Maltese",
        "Lulu da Pomerânia" to "Pomeranian",
        "Pastor Australiano" to "Australian Shepherd",
        "Pastor de Shetland" to "Shetland Sheepdog",
        "Cane Corso" to "Cane Corso",
        "Mastim Napolitano" to "Neapolitan Mastiff",
        "São Bernardo" to "Saint Bernard",
        "Braco Alemão de Pelo Curto" to "German Shorthaired Pointer",
        "Chow Chow" to "Chow Chow",
        "Dálmata" to "Dalmatian",
        "Corgi Galês (Pembroke)" to "Pembroke Welsh Corgi",
        "Corgi Galês (Cardigan)" to "Cardigan Welsh Corgi",
        "Shar Pei" to "Shar-Pei",
        "Jack Russell Terrier" to "Jack Russell Terrier",
        "Boston Terrier" to "Boston Terrier",
        "Boiadeiro Bernês" to "Bernese Mountain Dog",
        "Terra-Nova" to "Newfoundland",
        "Samoieda" to "Samoyed",
        "Malamute do Alasca" to "Alaskan Malamute",
        "Bull Mastiff" to "Bullmastiff",
        "Terrier Escocês" to "Scottish Terrier",
        "Fox Terrier" to "Wire Fox Terrier",
        "Pastor Belga Malinois" to "Belgian Malinois",
        "Cavalier King Charles Spaniel" to "Cavalier King Charles Spaniel",
        "Cão de Água Português" to "Portuguese Water Dog",
        "Terrier Tibetano" to "Tibetan Terrier",
        "American Staffordshire Terrier" to "American Staffordshire Terrier",
        "Staffordshire Bull Terrier" to "Staffordshire Bull Terrier",
        "Boiadeiro Australiano" to "Australian Cattle Dog",
    )

    val nomesEmPortugues: List<String> = racas.keys.toList()

    private fun normalizar(texto: String): String =
        Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "") // remove acentos
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
