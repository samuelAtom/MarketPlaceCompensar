package com.tienda.marketplacecompensar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnBiometric = findViewById<Button>(R.id.btnBiometric)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        // Configurar autenticación biométrica
        iniciarAutenticacionBiometrica()

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val userId = result.user?.uid ?: ""
                    db.collection("usuarios").document(userId).get()
                        .addOnSuccessListener { document ->
                            val rol = document.getString("rol") ?: "comprador"
                            Toast.makeText(this, "Bienvenido $rol", Toast.LENGTH_SHORT).show()

                            // Guardar credenciales para autenticación biométrica
                            guardarCredenciales(email, password)

                            redirigirPorRol(rol)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
                            guardarCredenciales(email, password)
                            redirigirPorRol("comprador")
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }

        btnBiometric.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }

        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        tvForgotPassword.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Ingresa tu correo electrónico", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Correo de recuperación enviado a $email", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun iniciarAutenticacionBiometrica() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Autenticación exitosa - obtener email y password guardados
                    val sharedPref = getSharedPreferences("biometric_prefs", MODE_PRIVATE)
                    val email = sharedPref.getString("user_email", "")
                    val password = sharedPref.getString("user_password", "")

                    if (!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
                        realizarLogin(email, password)
                    } else {
                        Toast.makeText(this@LoginActivity, "No hay credenciales guardadas. Inicia sesión primero.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@LoginActivity, "Autenticación fallida", Toast.LENGTH_SHORT).show()
                }
            }
        )

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación con huella")
            .setSubtitle("Inicia sesión con tu huella digital")
            .setNegativeButtonText("Cancelar")
            .build()
    }

    private fun realizarLogin(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: ""
                db.collection("usuarios").document(userId).get()
                    .addOnSuccessListener { document ->
                        val rol = document.getString("rol") ?: "comprador"
                        Toast.makeText(this, "Bienvenido $rol", Toast.LENGTH_SHORT).show()
                        redirigirPorRol(rol)
                    }
                    .addOnFailureListener {
                        redirigirPorRol("comprador")
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun redirigirPorRol(rol: String) {
        when (rol) {
            "admin" -> {
                val intent = Intent(this, AdminHomeActivity::class.java)
                startActivity(intent)
                finish()
            }
            "vendedor" -> {
                val intent = Intent(this, VendedorHomeActivity::class.java)
                startActivity(intent)
                finish()
            }
            else -> {
                val intent = Intent(this, CompradorHomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun guardarCredenciales(email: String, password: String) {
        val sharedPref = getSharedPreferences("biometric_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("user_email", email)
            putString("user_password", password)
            apply()
        }
    }
}