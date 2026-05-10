package com.tienda.marketplacecompensar

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class CompradorHomeActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comprador_home)

        bottomNav = findViewById(R.id.bottom_navigation)

        // Cargar el fragment de Home al iniciar
        cargarFragment(HomeFragment())

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    cargarFragment(HomeFragment())
                    true
                }
                R.id.nav_search -> {
                    cargarFragment(SearchFragment())
                    true
                }
                R.id.nav_cart -> {
                    cargarFragment(CartFragment())
                    true
                }
                R.id.nav_profile -> {
                    cargarFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun cargarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}