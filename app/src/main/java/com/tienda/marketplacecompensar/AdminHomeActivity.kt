package com.tienda.marketplacecompensar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminHomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: UserAdapter
    private val userList = mutableListOf<Map<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etSearchUser = findViewById<EditText>(R.id.etSearchUser)
        val btnAddUser = findViewById<Button>(R.id.btnAddUser)
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)
        val rvUsuarios = findViewById<RecyclerView>(R.id.rvUsuarios)

        rvUsuarios.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter(userList, { userId, currentRol ->
            showEditRolDialog(userId, currentRol)
        }, { userId ->
            deleteUser(userId)
        })
        rvUsuarios.adapter = adapter

        loadUsers()

        btnAddUser.setOnClickListener {
            showAddUserDialog()
        }

        btnCerrarSesion.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        etSearchUser.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchUsers(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun loadUsers() {
        db.collection("usuarios").get()
            .addOnSuccessListener { documents ->
                userList.clear()
                for (document in documents) {
                    val user = document.data.toMutableMap()
                    user["userId"] = document.id
                    userList.add(user)
                }
                adapter.updateList(userList)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar usuarios", Toast.LENGTH_SHORT).show()
            }
    }

    private fun searchUsers(query: String) {
        if (query.isEmpty()) {
            loadUsers()
            return
        }
        db.collection("usuarios")
            .whereEqualTo("email", query)
            .get()
            .addOnSuccessListener { documents ->
                userList.clear()
                for (document in documents) {
                    val user = document.data.toMutableMap()
                    user["userId"] = document.id
                    userList.add(user)
                }
                adapter.updateList(userList)
            }
    }

    private fun showAddUserDialog() {
        val dialog = android.app.AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_usuario, null)
        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)

        dialog.setTitle("Agregar Usuario")
        dialog.setView(view)
        dialog.setPositiveButton("Guardar") { _, _ ->
            val nombre = etNombre.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val userId = result.user?.uid ?: ""
                    val usuario = hashMapOf(
                        "nombre" to nombre,
                        "email" to email,
                        "rol" to "comprador",
                        "fechaRegistro" to System.currentTimeMillis()
                    )
                    db.collection("usuarios").document(userId).set(usuario)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Usuario creado", Toast.LENGTH_SHORT).show()
                            loadUsers()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
        dialog.setNegativeButton("Cancelar", null)
        dialog.show()
    }

    private fun showEditRolDialog(userId: String, currentRol: String) {
        val roles = arrayOf("comprador", "vendedor", "admin")
        val selected = roles.indexOf(currentRol)

        android.app.AlertDialog.Builder(this)
            .setTitle("Editar Rol")
            .setSingleChoiceItems(roles, selected) { dialog, which ->
                val newRol = roles[which]
                db.collection("usuarios").document(userId)
                    .update("rol", newRol)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Rol actualizado a $newRol", Toast.LENGTH_SHORT).show()
                        loadUsers()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al actualizar rol", Toast.LENGTH_SHORT).show()
                    }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteUser(userId: String) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Eliminar Usuario")
            .setMessage("¿Estás seguro de eliminar este usuario?")
            .setPositiveButton("Eliminar") { _, _ ->
                db.collection("usuarios").document(userId).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                        loadUsers()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}