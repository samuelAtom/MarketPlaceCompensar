package com.tienda.marketplacecompensar

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CompradorHomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ProductoAdapterComprador
    private val productoList = mutableListOf<Producto>()
    private lateinit var locationHelper: LocationHelper
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comprador_home)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        locationHelper = LocationHelper(this)

        val etBuscar = findViewById<EditText>(R.id.etBuscar)
        val btnVerCarrito = findViewById<Button>(R.id.btnVerCarrito)
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)
        val btnMiUbicacion = findViewById<Button>(R.id.btnMiUbicacion)
        val rvProductos = findViewById<RecyclerView>(R.id.rvProductos)

        rvProductos.layoutManager = LinearLayoutManager(this)
        adapter = ProductoAdapterComprador(productoList) { producto ->
            agregarAlCarrito(producto)
        }
        rvProductos.adapter = adapter

        cargarProductos()

        btnMiUbicacion.setOnClickListener {
            obtenerMiUbicacion()
        }

        btnVerCarrito.setOnClickListener {
            val intent = Intent(this, CarritoActivity::class.java)
            startActivity(intent)
        }

        btnCerrarSesion.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        etBuscar.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                buscarProductos(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun cargarProductos() {
        db.collection("productos")
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

    private fun buscarProductos(query: String) {
        if (query.isEmpty()) {
            cargarProductos()
            return
        }
        db.collection("productos")
            .whereEqualTo("nombre", query)
            .get()
            .addOnSuccessListener { documents ->
                productoList.clear()
                for (document in documents) {
                    val producto = document.toObject(Producto::class.java)
                    productoList.add(producto.copy(id = document.id))
                }
                adapter.updateList(productoList)
            }
    }

    private fun agregarAlCarrito(producto: Producto) {
        val userId = auth.currentUser?.uid ?: return

        val carritoItem = hashMapOf(
            "productoId" to producto.id,
            "nombre" to producto.nombre,
            "precio" to producto.precio,
            "cantidad" to 1,
            "imagenUrl" to producto.imagenUrl,
            "vendedorId" to producto.vendedorId,
            "usuarioId" to userId
        )

        db.collection("carritos").add(carritoItem)
            .addOnSuccessListener {
                Toast.makeText(this, "Producto agregado al carrito", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al agregar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerMiUbicacion()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun obtenerMiUbicacion() {
        if (!locationHelper.hasLocationPermission()) {
            requestLocationPermission()
            return
        }

        Toast.makeText(this, "Obteniendo ubicación...", Toast.LENGTH_SHORT).show()
        locationHelper.getCurrentLocation(
            onLocationResult = { lat, lng, direccion ->
                Toast.makeText(this, "📍 $direccion", Toast.LENGTH_LONG).show()
            },
            onError = { error ->
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        )
    }
}