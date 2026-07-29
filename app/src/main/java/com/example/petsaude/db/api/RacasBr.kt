package com.example.petsaude.db.api

import java.text.Normalizer

object RacasBr {

    /** Nome em português (exibido ao usuário) -> nome em inglês (usado na consulta à API). */
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
        "Schnauzer Standard" to "Schnauzer (Standard)",
        "Schnauzer Miniatura" to "Schnauzer (Miniature)",
        "Schnauzer Gigante" to "Schnauzer (Giant)",
        "Lulu da Pomerânia" to "Pomeranian",
        "Weimaraner" to "Weimaraner",
        "Whippet" to "Whippet",
        "Pastor Australiano" to "Australian Shepherd",
        "Pastor de Shetland" to "Shetland Sheepdog",
        "Cane Corso" to "Cane Corso",
        "Mastim Napolitano" to "Neapolitan Mastiff",
        "São Bernardo" to "Saint Bernard",
        "Setter Irlandês" to "Irish Setter",
        "Pointer Inglês" to "Pointer",
        "Braco Alemão de Pelo Curto" to "German Shorthaired Pointer",
        "Chow Chow" to "Chow Chow",
        "Dálmata" to "Dalmatian",
        "Corgi Galês (Pembroke)" to "Pembroke Welsh Corgi",
        "Corgi Galês (Cardigan)" to "Cardigan Welsh Corgi",
        "Basenji" to "Basenji",
        "Papillon" to "Papillon",
        "Bichon Frisé" to "Bichon Frise",
        "Shar Pei" to "Shar-Pei",
        "West Highland White Terrier" to "West Highland White Terrier",
        "Jack Russell Terrier" to "Jack Russell Terrier",
        "Boston Terrier" to "Boston Terrier",
        "Galgo Inglês (Greyhound)" to "Greyhound",
        "Rhodesian Ridgeback" to "Rhodesian Ridgeback",
        "Boiadeiro Bernês" to "Bernese Mountain Dog",
        "Terra-Nova" to "Newfoundland",
        "Airedale Terrier" to "Airedale Terrier",
        "Vizsla" to "Vizsla",
        "Samoieda" to "Samoyed",
        "Malamute do Alasca" to "Alaskan Malamute",
        "Setter Inglês" to "English Setter",
        "Springer Spaniel Inglês" to "English Springer Spaniel",
        "Collie" to "Collie",
        "Bobtail (Old English Sheepdog)" to "Old English Sheepdog",
        "Havanês" to "Havanese",
        "Affenpinscher" to "Affenpinscher",
        "Pequinês" to "Pekingese",
        "Bull Mastiff" to "Bullmastiff",
        "Terrier Escocês" to "Scottish Terrier",
        "Fox Terrier" to "Wire Fox Terrier",
        "Bearded Collie" to "Bearded Collie",
        "Setter Gordon" to "Gordon Setter",
        "Spaniel Bretão" to "Brittany",
        "Pastor Belga Malinois" to "Belgian Malinois",
        "Cavalier King Charles Spaniel" to "Cavalier King Charles Spaniel",
        "Cão de Água Português" to "Portuguese Water Dog",
        "Terrier Tibetano" to "Tibetan Terrier",
        "Keeshond" to "Keeshond",
        "Griffon de Bruxelas" to "Brussels Griffon",
        "Cairn Terrier" to "Cairn Terrier",
        "Silky Terrier" to "Silky Terrier",
        "American Staffordshire Terrier" to "American Staffordshire Terrier",
        "Staffordshire Bull Terrier" to "Staffordshire Bull Terrier",
        "Boiadeiro Australiano" to "Australian Cattle Dog",
        "Chinese Crested" to "Chinese Crested",
        "Xoloitzcuintle" to "Xoloitzcuintli"
    )

    /** Lista de nomes em português, na ordem cadastrada, para exibir sugestões. */
    val nomesEmPortugues: List<String> = racas.keys.toList()

    private fun normalizar(texto: String): String =
        Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "") // remove acentos
            .lowercase()
            .trim()

    /**
     * Raças em português cujo nome contém o [prefixo] digitado (ignorando
     * acentuação e caixa).
     */
    fun sugestoes(prefixo: String, limite: Int = nomesEmPortugues.size): List<String> {
        if (prefixo.isBlank()) return nomesEmPortugues.take(limite)
        val alvo = normalizar(prefixo)
        return racas.keys.filter { normalizar(it).contains(alvo) }.take(limite)
    }

    /**
     * Traduz um nome em português para o nome em inglês esperado pela API.
     * Se o texto digitado não bater com nenhuma raça do dicionário
     * (ex.: já digitado em inglês, ou raça fora da lista), devolve o
     * próprio texto e deixa a API tentar resolver.
     */
    fun paraIngles(nomePt: String): String {
        val alvo = normalizar(nomePt)
        val encontrada = racas.entries.firstOrNull { normalizar(it.key) == alvo }
        return encontrada?.value ?: nomePt
    }
}
