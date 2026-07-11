package com.example.petsaude.db.fb
import com.example.petsaude.model.Consulta




data class FBConsulta(

    var id: String? = null,

    var petId: String = "",

    var nomePet: String = "",

    var veterinario: String = "",

    var motivo: String = "",

    var data: String = "",

    var horario: String = "",

    var endereco: String = ""

)
fun Consulta.toFBConsulta(): FBConsulta {

    return FBConsulta(

        id = id,

        petId = petId,

        nomePet = nomePet,

        veterinario = veterinario,

        motivo = motivo,

        data = data,

        horario = horario,

        endereco = endereco

    )

}



