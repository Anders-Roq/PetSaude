package com.example.petsaude.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.petsaude.db.fb.FBDatabase
import com.example.petsaude.db.fb.FBPet
import com.example.petsaude.db.fb.FBUsuario
import com.example.petsaude.db.fb.toFBUsuario
import com.example.petsaude.model.Usuario

class ProfileViewModel(
    private val fbDatabase: FBDatabase
) : ViewModel(), FBDatabase.Listener {

    var usuario by mutableStateOf<Usuario?>(null)
        private set

    init {
        fbDatabase.setListener(this)
    }

    override fun onUsuarioLoaded(usuario: FBUsuario) {
        this.usuario = usuario.toUsuario()
    }

    override fun onUsuarioSignOut() {}

    override fun onPetAdded(pet: FBPet) {}

    override fun onPetUpdated(pet: FBPet) {}

    override fun onPetRemoved(pet: FBPet) {}

    fun salvar(nome: String, email: String, telefone: String) {

        val usuario = Usuario(
            nome = nome,
            email = email,
            telefone = telefone,
            senha = ""
        )

        fbDatabase.updateUsuario(usuario.toFBUsuario())
    }
}

class ProfileViewModelFactory(
    private val fbDatabase: FBDatabase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(fbDatabase) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}