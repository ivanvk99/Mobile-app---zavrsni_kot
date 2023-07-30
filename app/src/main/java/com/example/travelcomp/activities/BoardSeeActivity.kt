package com.example.travelcomp.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowInsets
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.example.travelcomp.R
import com.example.travelcomp.databinding.ActivityBoardSeeBinding
import com.example.travelcomp.firebase.FirestoreClass
import com.example.travelcomp.models.Board
import com.example.travelcomp.utils.Constants


class BoardSeeActivity : BaseActivity() {

    private var binding: ActivityBoardSeeBinding? = null
    private lateinit var mTravelDetail : Board

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardSeeBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // This is used to hide the status bar and make the splash screen as a full screen activity.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        var boardDocumentId = ""
        if (intent.hasExtra(Constants.DOCUMENT_ID)){
            boardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this, boardDocumentId)


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_members -> {
                val intent = Intent(this, MembersActivity::class.java)
                intent.putExtra(Constants.TRAVEL_DETAIL, mTravelDetail)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun setupActionBar(title : String) {

        setSupportActionBar(binding?.toolbarTaskListActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mTravelDetail.name
        }

        binding?.toolbarTaskListActivity?.setNavigationOnClickListener {
            onBackPressed() }
    }

    fun travelDetails(board: Board){
        hideProgressDialog()
        setupActionBar(board.name)
        mTravelDetail = board
        val userImage = binding?.ivBoardImage!!

        Glide
            .with(this@BoardSeeActivity)
            .load(board.image)
            .centerCrop()
            .placeholder(R.drawable.ic_board_place_holder)
            .into(userImage)

        binding?.textViewCreatedBy?.text = board.createdBy
        binding?.textViewDescription?.text=board.description
        binding?.textViewCity?.text=board.city
        binding?.textViewStartDate?.text = board.date
    }


    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}