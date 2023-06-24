package com.example.travelcomp.activities

import androidx.databinding.DataBindingUtil
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.SyncStateContract
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.databinding.ViewDataBinding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travelcomp.R
import com.example.travelcomp.adapters.StatusAdapter
import com.example.travelcomp.databinding.ActivityMainBinding
import com.example.travelcomp.databinding.MainContentBinding
import com.example.travelcomp.databinding.NavHeaderMainBinding
import com.example.travelcomp.firebase.FirestoreClass
import com.example.travelcomp.models.Board
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
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }


    private lateinit var mUserName: String

    private val boardLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == Activity.RESULT_OK){
            FirestoreClass().getTravelsList(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setupActionBar()
        binding?.navView?.setNavigationItemSelectedListener(this)


        FirestoreClass().loadUserData(this@MainActivity, true)
        val fab = findViewById<FloatingActionButton>(R.id.fab_create_board)
        fab.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            boardLauncher.launch(intent)
        }


    }





    fun populateTravelListToUI(boardsList: ArrayList<Board>) {
        hideProgressDialog()
        val recyclerView = binding?.mainAppBarLayout?.mainContent?.rvBoardsList
        val noTravelsAvailable = binding?.root?.findViewById<TextView>(R.id.no_travels_available)
        val adapter = StatusAdapter(this@MainActivity, boardsList)
        recyclerView?.adapter = adapter // Attach the adapter to the recyclerView.

        if (boardsList.isNotEmpty()) {
            recyclerView?.visibility = View.VISIBLE
            noTravelsAvailable?.visibility = View.GONE

            val layoutManager = LinearLayoutManager(this)
            recyclerView?.layoutManager = layoutManager
            recyclerView?.setHasFixedSize(true)



            adapter.setOnClickListener(object :
                StatusAdapter.ItemClickListener {
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }
            })

        } else {
            recyclerView?.visibility = View.GONE
            noTravelsAvailable?.visibility = View.VISIBLE
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




    fun updateNavigationUserDetails(user: User, readTravelsList: Boolean){
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
        if (readTravelsList){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getTravelsList(this)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE) {
            FirestoreClass().loadUserData(this)
        }else if(
            resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE){
            FirestoreClass().getTravelsList(this)

            }

        else {
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