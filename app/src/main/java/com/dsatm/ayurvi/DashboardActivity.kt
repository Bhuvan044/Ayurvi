package com.dsatm.ayurvi

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class DashboardActivity : AppCompatActivity() {
    private var firebaseAuth: FirebaseAuth? = null
    var firebaseUser: FirebaseUser? = null
    var myuid: String? = null
    var actionBar: ActionBar? = null
    var navigationView: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        actionBar = supportActionBar
        actionBar!!.title = "Profile Activity"
        firebaseAuth = FirebaseAuth.getInstance()

        navigationView = findViewById<BottomNavigationView>(R.id.navigation)
        navigationView?.setOnNavigationItemSelectedListener(selectedListener)
//        actionBar!!.title = "Home"
        supportActionBar?.hide()

        // When we open the application first
        // time the fragment should be shown to the user
        // in this case it is home fragment
        val fragment = ChatListFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.content, fragment, "")
        fragmentTransaction.commit()
    }

    private val selectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
//                R.id.nav_home -> {
//                    supportActionBar?.show()
//                    actionBar!!.title = "Home"
//                    val fragment = HomeFragment()
//                    val fragmentTransaction = supportFragmentManager.beginTransaction()
//                    fragmentTransaction.replace(R.id.content, fragment, "")
//                    fragmentTransaction.commit()
//                    return@OnNavigationItemSelectedListener true
//                }

//                R.id.nav_profile -> {
//                    supportActionBar?.show()
//                    actionBar!!.setTitle("Profile")
//                    val fragment1 = ProfileFragment()
//                    val fragmentTransaction1 = supportFragmentManager.beginTransaction()
//                    fragmentTransaction1.replace(R.id.content, fragment1)
//                    fragmentTransaction1.commit()
//                    return@OnNavigationItemSelectedListener true
//                }

//                R.id.nav_users -> {
//                    supportActionBar?.show()
//                    actionBar!!.setTitle("Users")
//                    val fragment2 = UsersFragment()
//                    val fragmentTransaction2 = supportFragmentManager.beginTransaction()
//                    fragmentTransaction2.replace(R.id.content, fragment2, "")
//                    fragmentTransaction2.commit()
//                    return@OnNavigationItemSelectedListener true
//                }

                R.id.nav_chat -> {
                    supportActionBar?.hide()
                    val listFragment = ChatListFragment()
                    val fragmentTransaction3 = supportFragmentManager.beginTransaction()
                    fragmentTransaction3.replace(R.id.content, listFragment, "")
                    fragmentTransaction3.commit()
                    return@OnNavigationItemSelectedListener true
                }

//                R.id.nav_addblogs -> {
//                    supportActionBar?.show()
//                    actionBar!!.setTitle("Create")
//                    val fragment4 = AddBlogsFragment()
//                    val fragmentTransaction4 = supportFragmentManager.beginTransaction()
//                    fragmentTransaction4.replace(R.id.content, fragment4, "")
//                    fragmentTransaction4.commit()
//                    return@OnNavigationItemSelectedListener true
//                }
            }
            false
        }
}
