@file:Suppress("DEPRECATION")

package com.example.travelcomp.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import com.example.travelcomp.R
import com.example.travelcomp.databinding.ActivityLogInBinding
import com.example.travelcomp.models.User
import com.google.firebase.auth.FirebaseAuth


class LogInActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private var binding:ActivityLogInBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setupActionBar()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding?.btnSignIn?.setOnClickListener { loginRegisteredUser() }
    }

    fun signInSuccess(user: User){
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    private fun setupActionBar(){

        setSupportActionBar(binding?.toolbarLogInActivity)


        if(supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        binding?.toolbarLogInActivity?.setNavigationOnClickListener { onBackPressed() }
    }

    private fun loginRegisteredUser(){
        val email: String = binding?.etEmail?.text.toString().trim{it <= ' '}
        val password: String = binding?.etPassword?.text.toString().trim{it <= ' '}

        if(validateForm(email, password)){
            showProgressDialog(resources.getString(R.string.please_wait))

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Sign In", "createUserWithEmail:success")
                        val user = auth.currentUser
                        startActivity(Intent(this, MainActivity::class.java))
                        finishAffinity()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Sign In", "createUserWithEmail:failure", task.exception)
                        (this@LogInActivity)?.let { BaseActivity().showErrorSnackBar(it, "Authentication failed: ${task.exception?.message}") }
                    }
                }
        }
    }

    private fun validateForm(email: String, password: String): Boolean{
        return when {
            TextUtils.isEmpty(email)-> {
                (this@LogInActivity)?.let { BaseActivity().showErrorSnackBar(it, "Please enter an email") }
                false
            }
            TextUtils.isEmpty(password)->{
                (this@LogInActivity)?.let { BaseActivity().showErrorSnackBar(it, "Please enter password") }
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