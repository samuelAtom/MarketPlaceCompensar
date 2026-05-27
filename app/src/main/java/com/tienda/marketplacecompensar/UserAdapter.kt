package com.tienda.marketplacecompensar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(
    private var userList: List<Map<String, Any>>,
    private val onEditClick: (String, String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_usuario, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        val userId = user["userId"] as? String ?: ""
        val nombre = user["nombre"] as? String ?: "Sin nombre"
        val email = user["email"] as? String ?: "Sin email"
        val rol = user["rol"] as? String ?: "comprador"

        holder.tvNombre.text = nombre
        holder.tvEmail.text = email
        holder.tvRol.text = "Rol: $rol"

        holder.btnEditarRol.setOnClickListener {
            onEditClick(userId, rol)
        }

        holder.btnEliminar.setOnClickListener {
            onDeleteClick(userId)
        }
    }

    override fun getItemCount(): Int = userList.size

    fun updateList(newList: List<Map<String, Any>>) {
        userList = newList
        notifyDataSetChanged()
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        val tvRol: TextView = itemView.findViewById(R.id.tvRol)
        val btnEditarRol: Button = itemView.findViewById(R.id.btnEditarRol)
        val btnEliminar: Button = itemView.findViewById(R.id.btnEliminar)
    }
}