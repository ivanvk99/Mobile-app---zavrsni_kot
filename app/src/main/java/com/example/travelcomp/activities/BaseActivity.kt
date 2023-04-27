@file:Suppress("DEPRECATION")

package com.example.travelcomp.activities

import android.app.Activity
import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.travelcomp.R
import com.example.travelcomp.databinding.ActivityBaseBinding
import com.example.travelcomp.databinding.DialogProgressBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

open class BaseActivity : AppCompatActivity() {


    private var binding: ActivityBaseBinding?= null
    private var doubleBackToExitPressedOnce = false
    private lateinit var mProgressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding?.root)

    }


    fun showProgressDialog(text:String){

        mProgressDialog = Dialog(this)

        mProgressDialog.setContentView(R.layout.dialog_progress)

        val binding = DialogProgressBinding.inflate(layoutInflater)
        mProgressDialog.setContentView(binding.root)

        binding.tvProgressText.text = text

        mProgressDialog.show()


    }

    fun hideProgressDialog(){
        mProgressDialog.dismiss()

    }

    fun getCurrentUserID(): String{
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    fun doubleBackToExit(){
        if(doubleBackToExitPressedOnce){
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this,
        resources.getString(R.string.please_click_back_again_to_exit), Toast.LENGTH_SHORT).show()

        Handler().postDelayed({doubleBackToExitPressedOnce = false}, 2000)


    }


    fun showErrorSnackBar(context: Context, message: String){
        val snackBar = Snackbar.make((context as Activity).findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)

        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(context, R.color.snackbar_error_color))

        snackBar.show()
    }

    override fun onDestroy() {
        super.onDestroy()

        binding = null
    }

}