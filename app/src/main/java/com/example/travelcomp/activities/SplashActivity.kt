@file:Suppress("DEPRECATION")

package com.example.travelcomp.activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import com.example.travelcomp.databinding.ActivitySplashBinding
import com.example.travelcomp.firebase.FirestoreClass


class SplashActivity : AppCompatActivity() {
    private var binding:ActivitySplashBinding?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        Handler(Looper.getMainLooper()).postDelayed({
            var currentUserID = FirestoreClass().getCurrentUserId()
            if (currentUserID.isNotEmpty()){
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }else{
                val intent = Intent(this, IntroActivity::class.java)
                startActivity(intent)
            }
            finish()
        }, 3000)


        binding?.tvAppName?.typeface = Typeface.createFromAsset(this.assets, "titlovi/carbon bl.ttf")

    }

    override fun onDestroy() {
        super.onDestroy()

        binding = null
    }
}