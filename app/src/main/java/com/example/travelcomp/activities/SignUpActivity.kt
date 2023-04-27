@file:Suppress("DEPRECATION")

package com.example.travelcomp.activities

import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.example.travelcomp.R
import com.example.travelcomp.databinding.ActivitySignUpBinding
import com.example.travelcomp.firebase.FirestoreClass
import com.example.travelcomp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class SignUpActivity : BaseActivity() {
    private var binding:ActivitySignUpBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setupActionBar()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    fun userRegisteredSuccess() {
        Toast.makeText(
            this,
            "you have sucesfully reg",
            Toast.LENGTH_LONG
        ).show()

        hideProgressDialog()

        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun setupActionBar(){

        setSupportActionBar(binding?.toolbarSignUpActivity)


        if(supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        binding?.toolbarSignUpActivity?.setNavigationOnClickListener { onBackPressed() }

        binding?.btnSignUp?.setOnClickListener { registerUser() }


    }




    private fun registerUser(){
        val name: String = binding?.etName?.text.toString().trim{it <= ' '}
        val email: String = binding?.etEmail?.text.toString().trim{it <= ' '}
        val password: String = binding?.etPassword?.text.toString().trim{it <= ' '}

        if(validateForm(name, email, password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    val registeredEmail = firebaseUser.email!!
                    val user = User(firebaseUser.uid, name, registeredEmail)
                    FirestoreClass().registerUser(this, user)
                } else {
                    Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                }
            }


        }
    }


    private fun validateForm(name: String, email: String, password: String): Boolean{
        return when {
            TextUtils.isEmpty(name)-> {
                (this@SignUpActivity)?.let { BaseActivity().showErrorSnackBar(it, "Please enter a name") }
                false
            }
            TextUtils.isEmpty(email)-> {
                (this@SignUpActivity)?.let { BaseActivity().showErrorSnackBar(it, "Please enter an email") }
                false
            }
            TextUtils.isEmpty(password)->{
                (this@SignUpActivity)?.let { BaseActivity().showErrorSnackBar(it, "Please enter password") }
                false
            }
            else-> true
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        binding = null
    }

}