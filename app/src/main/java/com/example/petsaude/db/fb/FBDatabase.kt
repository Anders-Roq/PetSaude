package com.example.petsaude.db.fb

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore

class FBDatabase {

    interface Listener {
        fun onUsuarioLoaded(usuario: FBUsuario)
        fun onUsuarioSignOut()
        fun onPetAdded(pet: FBPet)
        fun onPetUpdated(pet: FBPet)
        fun onPetRemoved(pet: FBPet)
    }

    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private var petsListReg: ListenerRegistration? = null
    private var listener: Listener? = null

    init {
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser == null) {
                petsListReg?.remove()
                listener?.onUsuarioSignOut()
                return@addAuthStateListener
            }

            val refCurrUsuario = db.collection("usuarios").document(firebaseAuth.currentUser!!.uid)

            refCurrUsuario.get().addOnSuccessListener {
                it.toObject(FBUsuario::class.java)?.let { usuario ->
                    listener?.onUsuarioLoaded(usuario)
                }
            }

            petsListReg = refCurrUsuario.collection("pets")
                .addSnapshotListener { snapshots, ex ->
                    if (ex != null) return@addSnapshotListener
                    snapshots?.documentChanges?.forEach { change ->
                        val fbPet = change.document.toObject(FBPet::class.java)
                        when (change.type) {
                            DocumentChange.Type.ADDED -> listener?.onPetAdded(fbPet)
                            DocumentChange.Type.MODIFIED -> listener?.onPetUpdated(fbPet)
                            DocumentChange.Type.REMOVED -> listener?.onPetRemoved(fbPet)
                        }
                    }
                }
        }
    }

    fun setListener(listener: Listener? = null) {
        this.listener = listener
    }

    fun register(usuario: FBUsuario) {
        if (auth.currentUser == null)
            throw RuntimeException("Usuário não está logado!")
        val uid = auth.currentUser!!.uid
        db.collection("usuarios").document(uid).set(usuario)
    }

    fun add(pet: FBPet) {
        if (auth.currentUser == null)
            throw RuntimeException("Usuário não está logado!")
        if (pet.id.isNullOrBlank())
            throw RuntimeException("Pet sem id definido!")
        val uid = auth.currentUser!!.uid
        db.collection("usuarios").document(uid).collection("pets")
            .document(pet.id!!).set(pet)
    }

    fun remove(pet: FBPet) {
        if (auth.currentUser == null)
            throw RuntimeException("Usuário não está logado!")
        if (pet.id.isNullOrBlank())
            throw RuntimeException("Pet sem id definido!")
        val uid = auth.currentUser!!.uid
        db.collection("usuarios").document(uid).collection("pets")
            .document(pet.id!!).delete()
    }
}