package com.tienda.marketplacecompensar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CarritoActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: CarritoAdapter
    private val carritoList = mutableListOf<CarritoItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrito)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val rvCarrito = findViewById<RecyclerView>(R.id.rvCarrito)
        val tvTotal = findViewById<TextView>(R.id.tvTotal)
        val btnComprar = findViewById<Button>(R.id.btnComprar)
        val btnVolver = findViewById<Button>(R.id.btnVolver)

        btnVolver.setOnClickListener {
            finish()
        }

        rvCarrito.layoutManager = LinearLayoutManager(this)
        adapter = CarritoAdapter(carritoList,
            { item, nuevaCantidad ->
                actualizarCantidad(item, nuevaCantidad)
            },
            { item ->
                eliminarDelCarrito(item)
            }
        )
        rvCarrito.adapter = adapter

        cargarCarrito { total ->
            tvTotal.text = String.format("$%,.0f", total)
        }

        btnComprar.setOnClickListener {
            val total = carritoList.sumOf { it.precio * it.cantidad }
            if (carritoList.isEmpty()) {
                Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, PagoActivity::class.java)
            intent.putExtra("total", total)
            startActivityForResult(intent, 1001)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            val pagoExitoso = data?.getBooleanExtra("pago_exitoso", false) ?: false
            if (pagoExitoso) {
                Toast.makeText(this, "¡Compra completada con éxito!", Toast.LENGTH_LONG).show()
                vaciarCarrito()
            }
        }
    }

    private fun cargarCarrito(onTotalCalculado: (Double) -> Unit) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("carritos")
            .whereEqualTo("usuarioId", userId)
            .get()
            .addOnSuccessListener { documents ->
                carritoList.clear()
                var total = 0.0
                for (document in documents) {
                    val item = document.toObject(CarritoItem::class.java)
                    val itemConId = item.copy(id = document.id)
                    carritoList.add(itemConId)
                    total += item.precio * item.cantidad
                }
                adapter.updateList(carritoList)
                onTotalCalculado(total)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar carrito", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarCantidad(item: CarritoItem, nuevaCantidad: Int) {
        db.collection("carritos").document(item.id)
            .update("cantidad", nuevaCantidad)
            .addOnSuccessListener {
                cargarCarrito { total ->
                    findViewById<TextView>(R.id.tvTotal).text = String.format("$%,.0f", total)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarDelCarrito(item: CarritoItem) {
        db.collection("carritos").document(item.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show()
                cargarCarrito { total ->
                    findViewById<TextView>(R.id.tvTotal).text = String.format("$%,.0f", total)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun vaciarCarrito() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("carritos")
            .whereEqualTo("usuarioId", userId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("carritos").document(document.id).delete()
                }
                carritoList.clear()
                adapter.updateList(carritoList)
                findViewById<TextView>(R.id.tvTotal).text = "$0"
                finish()
            }
    }
}