package com.tienda.marketplacecompensar

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
/**
 * PagoActivity - Pasarela de pagos simulada
 *
 * Esta actividad simula el proceso de pago de una compra.
 * Funcionalidades principales:
 * - Ingresar dirección de envío (calle, ciudad, código postal)
 * - Seleccionar método de pago (Tarjeta o PSE)
 * - Completar datos de pago según método seleccionado:
 *   - Tarjeta: número, fecha, CVV, nombre del titular
 *   - PSE: banco, tipo de cuenta, número de cuenta
 * - Confirmar pago y recibir mensaje de éxito
 *
 * Es una simulación académica, no procesa pagos reales.
 * Al confirmar, envía resultado exitoso a CarritoActivity para vaciar el carrito
 */

class PagoActivity : AppCompatActivity() {

    private var total = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pago)

        total = intent.getDoubleExtra("total", 0.0)

        val tvSubtotal = findViewById<TextView>(R.id.tvSubtotal)
        val tvEnvio = findViewById<TextView>(R.id.tvEnvio)
        val tvTotalPago = findViewById<TextView>(R.id.tvTotalPago)
        val rgMetodoPago = findViewById<RadioGroup>(R.id.rgMetodoPago)
        val layoutTarjeta = findViewById<LinearLayout>(R.id.layoutTarjeta)
        val layoutPSE = findViewById<LinearLayout>(R.id.layoutPSE)
        val btnConfirmarPago = findViewById<Button>(R.id.btnConfirmarPago)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)

        val etDireccion = findViewById<EditText>(R.id.etDireccion)
        val etCiudad = findViewById<EditText>(R.id.etCiudad)
        val etCodigoPostal = findViewById<EditText>(R.id.etCodigoPostal)
        val etNumeroTarjeta = findViewById<EditText>(R.id.etNumeroTarjeta)
        val etFechaVencimiento = findViewById<EditText>(R.id.etFechaVencimiento)
        val etCVV = findViewById<EditText>(R.id.etCVV)
        val etNombreTitular = findViewById<EditText>(R.id.etNombreTitular)
        val etBanco = findViewById<EditText>(R.id.etBanco)
        val etTipoCuenta = findViewById<EditText>(R.id.etTipoCuenta)
        val etNumeroCuenta = findViewById<EditText>(R.id.etNumeroCuenta)

        // Valores de ejemplo para pruebas
        etNumeroTarjeta.hint = "Ej: 4111111111111111"
        etFechaVencimiento.hint = "Ej: 12/28"
        etCVV.hint = "Ej: 123"
        etBanco.hint = "Ej: Bancolombia"
        etTipoCuenta.hint = "Ej: Ahorros"
        etNumeroCuenta.hint = "Ej: 123456789"
        etDireccion.hint = "Ej: Cra 45 # 67-89"
        etCiudad.hint = "Ej: Bogotá"
        etCodigoPostal.hint = "Ej: 110111"
        etNombreTitular.hint = "Ej: Juan Perez"

        // Calcular total con envío
        val envio = 8000.0
        val subtotal = total
        val totalConEnvio = subtotal + envio

        tvSubtotal.text = String.format("$%,.0f", subtotal)
        tvEnvio.text = String.format("$%,.0f", envio)
        tvTotalPago.text = String.format("$%,.0f", totalConEnvio)

        // Mostrar/ocultar campos según método de pago
        rgMetodoPago.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbTarjeta -> {
                    layoutTarjeta.visibility = View.VISIBLE
                    layoutPSE.visibility = View.GONE
                }
                R.id.rbPSE -> {
                    layoutTarjeta.visibility = View.GONE
                    layoutPSE.visibility = View.VISIBLE
                }
            }
        }

        btnConfirmarPago.setOnClickListener {
            val direccion = etDireccion.text.toString().trim()
            val ciudad = etCiudad.text.toString().trim()
            val codigoPostal = etCodigoPostal.text.toString().trim()

            if (direccion.isEmpty() || ciudad.isEmpty()) {
                Toast.makeText(this, "Completa la dirección de entrega", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val metodoSeleccionado = rgMetodoPago.checkedRadioButtonId
            when (metodoSeleccionado) {
                R.id.rbTarjeta -> {
                    val numeroTarjeta = etNumeroTarjeta.text.toString().trim()
                    val fechaVencimiento = etFechaVencimiento.text.toString().trim()
                    val cvv = etCVV.text.toString().trim()
                    val nombreTitular = etNombreTitular.text.toString().trim()

                    if (numeroTarjeta.isEmpty() || fechaVencimiento.isEmpty() || cvv.isEmpty() || nombreTitular.isEmpty()) {
                        Toast.makeText(this, "Completa todos los datos de la tarjeta", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    // Simulación - no se valida que la tarjeta sea real
                    procesarPago("Tarjeta de crédito/débito", direccion, ciudad)
                }
                R.id.rbPSE -> {
                    val banco = etBanco.text.toString().trim()
                    val tipoCuenta = etTipoCuenta.text.toString().trim()
                    val numeroCuenta = etNumeroCuenta.text.toString().trim()

                    if (banco.isEmpty() || tipoCuenta.isEmpty() || numeroCuenta.isEmpty()) {
                        Toast.makeText(this, "Completa todos los datos bancarios", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    procesarPago("PSE - Transferencia bancaria", direccion, ciudad)
                }
                else -> {
                    Toast.makeText(this, "Selecciona un método de pago", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }
        }

        btnCancelar.setOnClickListener {
            finish()
        }
    }

    private fun procesarPago(metodoPago: String, direccion: String, ciudad: String) {
        Toast.makeText(this, "Procesando pago...", Toast.LENGTH_SHORT).show()

        android.os.Handler().postDelayed({
            Toast.makeText(this, "✅ Pago exitoso" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "" +
                    "\n📦 Envío a: $direccion, $ciudad\n💳 Método: $metodoPago", Toast.LENGTH_LONG).show()

            val intent = Intent()
            intent.putExtra("pago_exitoso", true)
            setResult(RESULT_OK, intent)
            finish()
        }, 1500)
    }
}