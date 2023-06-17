package com.example.travelcomp.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.SyncStateContract
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.example.travelcomp.R
import com.example.travelcomp.databinding.ActivityMainBinding
import com.example.travelcomp.databinding.NavHeaderMainBinding
import com.example.travelcomp.firebase.FirestoreClass
import com.example.travelcomp.models.User
import com.example.travelcomp.utils.Constants
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

@Suppress("DEPRECATION")
class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var binding: ActivityMainBinding? = null

    companion object {
        const val MY_PROFILE_REQUEST_CODE : Int = 11
    }


    private lateinit var mUserName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setupActionBar()
        binding?.navView?.setNavigationItemSelectedListener(this)

        FirestoreClass().loadUserData(this@MainActivity)
        val fab = findViewById<FloatingActionButton>(R.id.fab_create_board)
        fab.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)

            startActivity(intent)
        }

    }

    private fun setupActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_main_activity)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_action_navigation_menu)
        }
        title = getString(R.string.app_name)
        toolbar.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer(){
        if(binding?.drawerLayout?.isDrawerOpen(GravityCompat.START)==true){
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        }else{
            binding?.drawerLayout?.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if(binding?.drawerLayout?.isDrawerOpen(GravityCompat.START)==true){
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        }else{
            doubleBackToExit()
        }
    }




    fun updateNavigationUserDetails(user: User){
        val headerView = binding?.navView?.getHeaderView(0)
        val bindingNavHeader = NavHeaderMainBinding.bind(headerView!!)

        mUserName = user.name

        Glide
            .with(this@MainActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(bindingNavHeader.navUserImage)

        bindingNavHeader.tvUsername.text = user.name
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE) {
            FirestoreClass().loadUserData(this)
        } else {
            Log.e("Canceled", "Cancelled")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.nav_my_profile -> {
                startActivityForResult(Intent(this, MyProfileActivity::class.java), MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out ->{
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }

        }
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)

        return true
    }



    override fun onDestroy() {
        super.onDestroy()

        binding = null
    }
}