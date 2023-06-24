package com.example.travelcomp.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.travelcomp.R
import com.example.travelcomp.databinding.ActivityCreateBoardBinding
import com.example.travelcomp.firebase.FirestoreClass
import com.example.travelcomp.models.Board
import com.example.travelcomp.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException


class CreateBoardActivity : BaseActivity(){

    private var mSelectedImageFileUri : Uri? = null
    private var binding: ActivityCreateBoardBinding? = null

    private lateinit var mUserName: String

    private var mBoardImageURL : String =""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if(intent.hasExtra(Constants.NAME)){
            mUserName = intent.getStringExtra(Constants.NAME) ?: ""
        }

        binding?.ivBoardImage?.setOnClickListener{
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                ==PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)

            }else{
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }


        setupActionBar()

        binding?.btnCreate?.setOnClickListener {
            if(mSelectedImageFileUri != null){
                uploadBoardImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }
    }


    private fun createBoard(){
        val membersArrayList: ArrayList<String> = ArrayList()
        membersArrayList.add(getCurrentUserID())

        val board = Board(
            binding?.etBoardName?.text.toString(),
            mBoardImageURL,
            mUserName,
            membersArrayList
        )

        FirestoreClass().createBoard(this, board)
    }


    private fun uploadBoardImage(){
        showProgressDialog(resources.getString(R.string.please_wait))

        val sRef : StorageReference = FirebaseStorage.getInstance().reference.
        child("BOARD_IMAGE" + System.currentTimeMillis() + "." +
                Constants.getFileExtension(this, mSelectedImageFileUri))
        sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot ->
            Log.i(
                "Board Image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
            )

            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri->
                Log.e("Downloadable Image URL", uri.toString())
                mBoardImageURL = uri.toString()

                createBoard()

            }

        }.addOnFailureListener {
                exception ->
            Toast.makeText(this,exception.message,Toast.LENGTH_LONG).show()

            hideProgressDialog()
        }
    }


    fun boardCreatedSuccessfully() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            Constants.showImageChooser(this)
        }else{
            if(requestCode == Constants.READ_STORAGE_PERMISSION_CODE){
                if (grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    Constants.showImageChooser(this)
                }else{
                    Toast.makeText(this, "You denied the permission for storage.", Toast.LENGTH_LONG).show()
                }
            }
        }

    }




    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null){
            mSelectedImageFileUri = data.data


            try {
                Glide
                    .with(this@CreateBoardActivity)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(binding?.ivBoardImage as CircleImageView)
            }catch (e: IOException){
                e.printStackTrace()
            }



        }
    }

    private fun setupActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_create_board_activity)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            title = resources.getString(R.string.create_board_title)
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }

    }

}