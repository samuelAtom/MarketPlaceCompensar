package com.tienda.marketplacecompensar

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val stock: Int = 0,
    val categoria: String = "",
    val imagenUrl: String = "",
    val vendedorId: String = "",
    val vendedorNombre: String = "",
    val fechaCreacion: Long = System.currentTimeMillis()
)