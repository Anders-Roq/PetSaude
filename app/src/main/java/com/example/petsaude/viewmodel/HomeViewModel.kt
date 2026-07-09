package com.example.petsaude.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.petsaude.db.fb.FBDatabase
import com.example.petsaude.db.fb.FBPet
import com.example.petsaude.db.fb.FBUsuario
import com.example.petsaude.db.fb.toFBPet
import com.example.petsaude.model.Pet
import com.example.petsaude.model.Usuario

class HomeViewModel(private val db: FBDatabase) : ViewModel(), FBDatabase.Listener {

    private val _pets = mutableStateListOf<Pet>()
    val pets: List<Pet> get() = _pets.toList()

    private val _usuario = mutableStateOf<Usuario?>(null)
    val usuario: Usuario? get() = _usuario.value

    init {
        db.setListener(this)
    }

    fun addPet(pet: Pet) {
        db.add(pet.toFBPet())
    }

    fun removePet(pet: Pet) {
        db.remove(pet.toFBPet())
    }

    override fun onUsuarioLoaded(usuario: FBUsuario) {
        _usuario.value = usuario.toUsuario()
    }

    override fun onUsuarioSignOut() {
        _pets.clear()
        _usuario.value = null
    }

    override fun onPetAdded(pet: FBPet) {
        _pets.add(pet.toPet())
    }

    override fun onPetUpdated(pet: FBPet) {
        val index = _pets.indexOfFirst { it.id == pet.id }
        if (index != -1) _pets[index] = pet.toPet()
    }

    override fun onPetRemoved(pet: FBPet) {
        _pets.removeAll { it.id == pet.id }
    }
}

class HomeViewModelFactory(private val db: FBDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
