package com.example.petsaude.db.fb

import com.example.petsaude.model.Usuario

class FBUsuario {
    var nome: String? = null
    var email: String? = null
    var telefone: String? = null

    fun toUsuario() = Usuario(
        nome = nome ?: "",
        email = email ?: "",
        telefone = telefone ?: "",
        senha = ""
    )
}

fun Usuario.toFBUsuario(): FBUsuario {
    val fbUsuario = FBUsuario()
    fbUsuario.nome = this.nome
    fbUsuario.email = this.email
    fbUsuario.telefone = this.telefone
    return fbUsuario
}