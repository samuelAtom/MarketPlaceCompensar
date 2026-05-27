package com.tienda.marketplacecompensar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale
/**
 * LocationHelper - Clase auxiliar para geolocalización
 *
 * Esta clase proporciona métodos para obtener la ubicación actual del usuario.
 * Utiliza FusedLocationProviderClient de Google Play Services.
 *
 * Funcionalidades:
 * - Verificar si el usuario ha concedido permisos de ubicación
 * - Obtener la ubicación actual (latitud, longitud)
 * - Convertir coordenadas a dirección legible usando Geocoder
 * - Manejar errores y permisos de forma segura
 */

class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getCurrentLocation(onLocationResult: (Double, Double, String) -> Unit, onError: (String) -> Unit) {
        if (!hasLocationPermission()) {
            onError("Permiso de ubicación no concedido")
            return
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lng = location.longitude
                    val direccion = obtenerDireccion(lat, lng)
                    onLocationResult(lat, lng, direccion)
                } else {
                    onError("No se pudo obtener la ubicación")
                }
            }
            .addOnFailureListener {
                onError("Error al obtener ubicación: ${it.message}")
            }
    }

    private fun obtenerDireccion(lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val direcciones = geocoder.getFromLocation(lat, lng, 1)
            if (direcciones != null && direcciones.isNotEmpty()) {
                val direccion = direcciones[0]
                val calle = direccion.thoroughfare ?: ""
                val numero = direccion.subThoroughfare ?: ""
                val colonia = direccion.subLocality ?: ""
                val ciudad = direccion.locality ?: ""
                val pais = direccion.countryName ?: ""

                when {
                    calle.isNotEmpty() && numero.isNotEmpty() -> "$calle #$numero, $ciudad"
                    calle.isNotEmpty() -> "$calle, $ciudad"
                    colonia.isNotEmpty() -> "$colonia, $ciudad"
                    else -> "$ciudad, $pais"
                }
            } else {
                "Ubicación: $lat, $lng"
            }
        } catch (e: Exception) {
            "Ubicación: $lat, $lng"
        }
    }
}