package com.example.petsaude.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petsaude.db.fb.FBDatabase
import com.example.petsaude.db.fb.toFBUsuario
import com.example.petsaude.model.Usuario
import com.example.petsaude.ui.theme.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetSaudeTheme {
                RegisterPage(
                    onRegisterClick = { nome, email, telefone, senha ->
                        registrarUsuario(nome, email, telefone, senha)
                    },
                    onBackClick = { finish() }
                )
            }
        }
    }

    private fun registrarUsuario(nome: String, email: String, telefone: String, senha: String) {
        Firebase.auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val usuario = Usuario(nome = nome, email = email, telefone = telefone, senha = "")
                    FBDatabase().register(usuario.toFBUsuario())
                    Toast.makeText(this, "Registro realizado!", Toast.LENGTH_LONG).show()
                    // Não chamamos finish() nem navegamos manualmente:
                    // o PetSaudeApp detecta o login e leva para a HomeActivity.
                } else {
                    Toast.makeText(this, "Registro FALHOU!", Toast.LENGTH_LONG).show()
                }
            }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterPage(
    onRegisterClick: (nome: String, email: String, telefone: String, senha: String) -> Unit = { _, _, _, _ -> },
    onBackClick: () -> Unit = {}
) {
    var nome by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var telefone by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var confirmarSenha by rememberSaveable { mutableStateOf("") }
    var senhaVisivel by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Teal50, White)))
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 20.dp)
        ) {
            // Top bar
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Navy900)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Criar conta",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )
            Text(
                text = "Preencha os campos abaixo para se cadastrar",
                fontSize = 13.sp,
                color = GrayText
            )

            Spacer(modifier = Modifier.height(28.dp))

            SectionLabel("INFORMAÇÕES PESSOAIS")

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome completo") },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = Teal500) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = petFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                leadingIcon = { Icon(Icons.Default.Email, null, tint = Teal500) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = petFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = telefone,
                onValueChange = { telefone = it },
                label = { Text("Telefone") },
                leadingIcon = { Icon(Icons.Default.Phone, null, tint = Teal500) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = petFieldColors()
            )

            Spacer(modifier = Modifier.height(20.dp))

            SectionLabel("SEGURANÇA")

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                label = { Text("Senha") },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = Teal500) },
                trailingIcon = {
                    IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                        Icon(
                            if (senhaVisivel) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            null, tint = GrayText
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                colors = petFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmarSenha,
                onValueChange = { confirmarSenha = it },
                label = { Text("Confirmar senha") },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = Teal500) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(),
                colors = petFieldColors()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onRegisterClick(nome, email, telefone, senha) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal500)
            ) {
                Text("Cadastrar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Já tem conta? ", color = GrayText, fontSize = 14.sp)
                TextButton(onClick = onBackClick, contentPadding = PaddingValues(0.dp)) {
                    Text("Entrar", color = Teal500, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
