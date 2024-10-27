package com.technogenis.alarammaster

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView

class DashboardScreen : AppCompatActivity() {

    private lateinit var navMenu: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var fragmentManager: FragmentManager
    private var fragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_dashboard_screen)

        val toolbar: Toolbar = findViewById(R.id.Toolbar)
        setSupportActionBar(toolbar)

        navMenu = findViewById(R.id.navMenu)
        drawerLayout = findViewById(R.id.drawerlayout)

        // Load the initial fragment
//        supportFragmentManager.beginTransaction().replace(R.id.main_frame, HomeFragment()).commit()

        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        fragmentManager = supportFragmentManager

        navMenu.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menuHome -> {
//                    fragment = HomeFragment()
                }
                R.id.menuSilentMode -> {
                    val logIntent = Intent(this@DashboardScreen, MainActivity::class.java)
                    logIntent.putExtra("title","Set Silent Mode")
                    logIntent.putExtra("mode","silent")
                    startActivity(logIntent)
                }
                R.id.menuRinging -> {
                    val logIntent = Intent(this@DashboardScreen, MainActivity::class.java)
                    logIntent.putExtra("title","Set Ringing Mode")
                    logIntent.putExtra("mode","normal")
                    startActivity(logIntent)
                }
                R.id.menuMap -> {
                    val mapIntent = Intent(this@DashboardScreen, MapScreen::class.java)
                    startActivity(mapIntent)
                }
                R.id.menuExit -> {
                    finishAffinity() // Better way to exit the app
                    return@setNavigationItemSelectedListener true
                }
                R.id.menu_logout -> {
                    val logIntent = Intent(this@DashboardScreen, MainActivity::class.java)
                    startActivity(logIntent)
                    finish()
                    Toast.makeText(this@DashboardScreen, "Logout", Toast.LENGTH_SHORT).show()
                    return@setNavigationItemSelectedListener true
                }
                else -> return@setNavigationItemSelectedListener false
            }

            drawerLayout.closeDrawer(GravityCompat.START)

            fragment?.let {
                val transaction: FragmentTransaction = fragmentManager.beginTransaction()
                transaction.replace(R.id.main_frame, it)
                transaction.addToBackStack(null) // Optional: add fragment to back stack
                transaction.commit()
            }

            true
        }
    }
}