package com.tienda.marketplacecompensar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CarritoAdapter(
    private var carritoList: List<CarritoItem>,
    private val onCantidadChange: (CarritoItem, Int) -> Unit,
    private val onEliminarClick: (CarritoItem) -> Unit
) : RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarritoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_carrito, parent, false)
        return CarritoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarritoViewHolder, position: Int) {
        val item = carritoList[position]

        holder.tvNombre.text = item.nombre
        holder.tvPrecio.text = String.format("$%,.0f", item.precio)
        holder.tvCantidad.text = item.cantidad.toString()

        holder.btnAumentar.setOnClickListener {
            onCantidadChange(item, item.cantidad + 1)
        }

        holder.btnDisminuir.setOnClickListener {
            if (item.cantidad > 1) {
                onCantidadChange(item, item.cantidad - 1)
            }
        }

        holder.btnEliminar.setOnClickListener {
            onEliminarClick(item)
        }
    }

    override fun getItemCount(): Int = carritoList.size

    fun updateList(newList: List<CarritoItem>) {
        carritoList = newList
        notifyDataSetChanged()
    }

    class CarritoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvPrecio: TextView = itemView.findViewById(R.id.tvPrecio)
        val tvCantidad: TextView = itemView.findViewById(R.id.tvCantidad)
        val btnAumentar: TextView = itemView.findViewById(R.id.btnAumentar)
        val btnDisminuir: TextView = itemView.findViewById(R.id.btnDisminuir)
        val btnEliminar: Button = itemView.findViewById(R.id.btnEliminar)
    }
}