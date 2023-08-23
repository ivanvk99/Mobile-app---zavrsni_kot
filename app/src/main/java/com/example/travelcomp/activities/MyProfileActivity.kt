@file:Suppress("DEPRECATION")

package com.example.travelcomp.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.travelcomp.R
import com.example.travelcomp.databinding.ActivityMyProfileBinding
import com.example.travelcomp.firebase.FirestoreClass
import com.example.travelcomp.models.User
import com.example.travelcomp.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

class MyProfileActivity : BaseActivity() {



    private var mSelectedImageFileUri : Uri? =null
    private lateinit var mUserDetails: User
    private var mProfileImageURL : String = ""

    private var binding: ActivityMyProfileBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.ivUserImage?.setOnClickListener {
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

        binding?.btnUpdate?.setOnClickListener {
            if(mSelectedImageFileUri != null){
                uploadUserImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }

        setupActionBar()

        FirestoreClass().loadUserData(this)
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
                if (grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
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
                    .with(this@MyProfileActivity)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(binding?.ivUserImage as CircleImageView)
            }catch (e:IOException){
                e.printStackTrace()
            }



        }
    }

    private fun setupActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_my_profile_activity)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            title = resources.getString(R.string.my_profile)
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }

    }

    fun setUserDataInUI(user: User){
        mUserDetails = user
        val userImage = binding?.ivUserImage!!

        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(userImage)

        binding?.etName?.setText(user.name)
        binding?.etEmail?.setText(user.email)
        if(user.mobile != 0L){
            binding?.etMobile?.setText(user.mobile.toString())
        }
    }

    private fun updateUserProfileData(){
        val userHashMap = HashMap<String, Any>()

        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image){
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }

        if(binding?.etName?.text.toString() != mUserDetails.name){
            userHashMap[Constants.NAME] = binding?.etName?.text.toString()
        }
        if (binding?.etMobile?.text.toString()!=mUserDetails.mobile.toString()){
            userHashMap[Constants.MOBILE] = binding?.etMobile?.text.toString().toLong()
        }

        FirestoreClass().updateUserProfileData(this, userHashMap)

    }

    private fun uploadUserImage(){
        showProgressDialog(resources.getString(R.string.please_wait))
        if (mSelectedImageFileUri != null){
            val sRef : StorageReference = FirebaseStorage.getInstance().reference.
            child("USER_IMAGE" + System.currentTimeMillis() + "." +
                    Constants.getFileExtension(this, mSelectedImageFileUri))
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot ->
                Log.i(
                    "Firebase Image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri->
                    Log.e("Downloadable Image URL", uri.toString())
                    mProfileImageURL = uri.toString()

                    updateUserProfileData()

                }

            }.addOnFailureListener {
                exception ->
                Toast.makeText(this,exception.message,Toast.LENGTH_LONG).show()

                hideProgressDialog()
            }
        }
    }


    fun profileUpdateSuccess(){
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()

        binding = null
    }
}