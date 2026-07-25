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
    private var usuarioListReg: ListenerRegistration? = null
    private var listener: Listener? = null

    init {
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser == null) {
                petsListReg?.remove()
                usuarioListReg?.remove()
                listener?.onUsuarioSignOut()
                return@addAuthStateListener
            }

            val refCurrUsuario = db.collection("usuarios").
            document(firebaseAuth.currentUser!!.uid)

            usuarioListReg = refCurrUsuario.addSnapshotListener { snapshot, ex ->
                if (ex != null) return@addSnapshotListener
                snapshot?.toObject(FBUsuario::class.java)?.let { usuario ->
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

    //Pets
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

    fun getPets(onResult: (List<FBPet>) -> Unit) {

        if (auth.currentUser == null)
            throw RuntimeException("Usuário não está logado!")

        val uid = auth.currentUser!!.uid

        db.collection("usuarios")
            .document(uid)
            .collection("pets")
            .get()
            .addOnSuccessListener { documents ->

                val lista = mutableListOf<FBPet>()

                for (document in documents) {

                    val pet = document.toObject(FBPet::class.java)

                    pet.id = document.id

                    lista.add(pet)

                }

                onResult(lista)

            }

    }


    //Consultas
    fun addConsulta(consulta: FBConsulta) {

        if (auth.currentUser == null)
            throw RuntimeException("Usuário não está logado!")

        val uid = auth.currentUser!!.uid

        if (consulta.id.isNullOrBlank()) {

            consulta.id =
                db.collection("usuarios")
                    .document(uid)
                    .collection("consultas")
                    .document()
                    .id
        }

        db.collection("usuarios")
            .document(uid)
            .collection("consultas")
            .document(consulta.id!!)
            .set(consulta)
    }


    fun listenConsultas(
        onChange: (List<FBConsulta>) -> Unit
    ) {
        if (auth.currentUser == null)
            return
        val uid = auth.currentUser!!.uid
        db.collection("usuarios")
            .document(uid)
            .collection("consultas")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null)
                    return@addSnapshotListener
                val lista = mutableListOf<FBConsulta>()
                for (doc in snapshot.documents) {
                    val consulta = doc.toObject(FBConsulta::class.java)
                    if (consulta != null) {
                        consulta.id = doc.id
                        lista.add(consulta)
                    }
                }
                onChange(lista)
            }
    }

    fun updateConsulta(consulta: FBConsulta) {
        if (auth.currentUser == null)
            throw RuntimeException("Usuário não está logado!")
        if (consulta.id.isNullOrBlank())
            throw RuntimeException("Consulta sem ID!")
        val uid = auth.currentUser!!.uid
        db.collection("usuarios")
            .document(uid)
            .collection("consultas")
            .document(consulta.id!!)
            .set(consulta)
    }

    fun removeConsulta(consulta: FBConsulta) {
        if (auth.currentUser == null)
            throw RuntimeException("Usuário não está logado!")
        if (consulta.id.isNullOrBlank())
            throw RuntimeException("Consulta sem ID!")
        val uid = auth.currentUser!!.uid
        db.collection("usuarios")
            .document(uid)
            .collection("consultas")
            .document(consulta.id!!)
            .delete()
    }

    //Vacinas
    fun addVacina(vacina: FBVacina) {
        if (auth.currentUser == null) throw RuntimeException("Usuário não está logado!")
        val uid = auth.currentUser!!.uid

        if (vacina.id.isNullOrBlank()) {
            vacina.id = db.collection("usuarios").document(uid).collection("vacinas").document().id
        }

        db.collection("usuarios").document(uid).collection("vacinas").document(vacina.id!!).set(vacina)
    }

    fun listenVacinas(onChange: (List<FBVacina>) -> Unit) {
        if (auth.currentUser == null) return
        val uid = auth.currentUser!!.uid
        db.collection("usuarios").document(uid).collection("vacinas")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener
                val lista = mutableListOf<FBVacina>()
                for (doc in snapshot.documents) {
                    val vacina = doc.toObject(FBVacina::class.java)
                    if (vacina != null) {
                        vacina.id = doc.id
                        lista.add(vacina)
                    }
                }
                onChange(lista)
            }
    }

    fun updateVacina(vacina: FBVacina) {
        if (auth.currentUser == null) throw RuntimeException("Usuário não está logado!")
        if (vacina.id.isNullOrBlank()) throw RuntimeException("Vacina sem ID!")
        val uid = auth.currentUser!!.uid
        db.collection("usuarios").document(uid).collection("vacinas").document(vacina.id!!).set(vacina)
    }

    fun removeVacina(vacina: FBVacina) {
        if (auth.currentUser == null) throw RuntimeException("Usuário não está logado!")
        if (vacina.id.isNullOrBlank()) throw RuntimeException("Vacina sem ID!")
        val uid = auth.currentUser!!.uid
        db.collection("usuarios").document(uid).collection("vacinas").document(vacina.id!!).delete()
    }


    fun updateUsuario(usuario: FBUsuario) {
        if (auth.currentUser == null)
            throw RuntimeException("Usuário não está logado!")

        val uid = auth.currentUser!!.uid

        db.collection("usuarios")
            .document(uid)
            .set(usuario)
    }
}