package com.example.petsaude

import android.app.Application
import android.content.Intent
import com.example.petsaude.view.HomeActivity
import com.example.petsaude.view.LoginActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class PetSaudeApp : Application() {

    private val flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or
            Intent.FLAG_ACTIVITY_NEW_TASK or
            Intent.FLAG_ACTIVITY_CLEAR_TASK

    override fun onCreate() {
        super.onCreate()
        Firebase.auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                goToHome()
            } else {
                goToLogin()
            }
        }
    }

    private fun goToHome() {
        startActivity(Intent(this, HomeActivity::class.java).setFlags(flags))
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).setFlags(flags))
    }
}