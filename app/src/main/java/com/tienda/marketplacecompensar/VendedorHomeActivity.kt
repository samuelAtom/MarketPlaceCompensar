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

class VendedorHomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ProductoAdapter
    private val productoList = mutableListOf<Producto>()
    private var currentVendedorId = ""
    private var currentVendedorNombre = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vendedor_home)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        currentVendedorId = auth.currentUser?.uid ?: ""
        currentVendedorNombre = auth.currentUser?.email ?: "Vendedor"

        val btnAddProduct = findViewById<Button>(R.id.btnAddProduct)
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)
        val rvProductos = findViewById<RecyclerView>(R.id.rvProductos)

        rvProductos.layoutManager = LinearLayoutManager(this)
        adapter = ProductoAdapter(productoList, { producto ->
            showEditProductDialog(producto)
        }, { producto ->
            deleteProduct(producto)
        })
        rvProductos.adapter = adapter

        loadProducts()

        btnAddProduct.setOnClickListener {
            showAddProductDialog()
        }

        btnCerrarSesion.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadProducts() {
        db.collection("productos")
            .whereEqualTo("vendedorId", currentVendedorId)
            .get()
            .addOnSuccessListener { documents ->
                productoList.clear()
                for (document in documents) {
                    val producto = document.toObject(Producto::class.java)
                    productoList.add(producto.copy(id = document.id))
                }
                adapter.updateList(productoList)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar productos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showAddProductDialog() {
        val dialog = android.app.AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_producto, null)
        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val etDescripcion = view.findViewById<EditText>(R.id.etDescripcion)
        val etPrecio = view.findViewById<EditText>(R.id.etPrecio)
        val etStock = view.findViewById<EditText>(R.id.etStock)
        val etCategoria = view.findViewById<EditText>(R.id.etCategoria)

        dialog.setTitle("Agregar Producto")
        dialog.setView(view)
        dialog.setPositiveButton("Guardar") { _, _ ->
            val nombre = etNombre.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()
            val precio = etPrecio.text.toString().trim().toDoubleOrNull() ?: 0.0
            val stock = etStock.text.toString().trim().toIntOrNull() ?: 0
            val categoria = etCategoria.text.toString().trim()

            if (nombre.isEmpty() || descripcion.isEmpty()) {
                Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val producto = hashMapOf(
                "nombre" to nombre,
                "descripcion" to descripcion,
                "precio" to precio,
                "stock" to stock,
                "categoria" to categoria,
                "vendedorId" to currentVendedorId,
                "vendedorNombre" to currentVendedorNombre,
                "imagenUrl" to "",
                "fechaCreacion" to System.currentTimeMillis()
            )

            db.collection("productos").add(producto)
                .addOnSuccessListener {
                    Toast.makeText(this, "Producto agregado", Toast.LENGTH_SHORT).show()
                    loadProducts()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al agregar", Toast.LENGTH_SHORT).show()
                }
        }
        dialog.setNegativeButton("Cancelar", null)
        dialog.show()
    }

    private fun showEditProductDialog(producto: Producto) {
        val dialog = android.app.AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_producto, null)
        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val etDescripcion = view.findViewById<EditText>(R.id.etDescripcion)
        val etPrecio = view.findViewById<EditText>(R.id.etPrecio)
        val etStock = view.findViewById<EditText>(R.id.etStock)
        val etCategoria = view.findViewById<EditText>(R.id.etCategoria)

        etNombre.setText(producto.nombre)
        etDescripcion.setText(producto.descripcion)
        etPrecio.setText(producto.precio.toString())
        etStock.setText(producto.stock.toString())
        etCategoria.setText(producto.categoria)

        dialog.setTitle("Editar Producto")
        dialog.setView(view)
        dialog.setPositiveButton("Guardar") { _, _ ->
            val nombre = etNombre.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()
            val precio = etPrecio.text.toString().trim().toDoubleOrNull() ?: 0.0
            val stock = etStock.text.toString().trim().toIntOrNull() ?: 0
            val categoria = etCategoria.text.toString().trim()

            val updates = mapOf(
                "nombre" to nombre,
                "descripcion" to descripcion,
                "precio" to precio,
                "stock" to stock,
                "categoria" to categoria
            )

            db.collection("productos").document(producto.id).update(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show()
                    loadProducts()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
        }
        dialog.setNegativeButton("Cancelar", null)
        dialog.show()
    }

    private fun deleteProduct(producto: Producto) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Eliminar Producto")
            .setMessage("¿Estás seguro de eliminar ${producto.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                db.collection("productos").document(producto.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show()
                        loadProducts()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}