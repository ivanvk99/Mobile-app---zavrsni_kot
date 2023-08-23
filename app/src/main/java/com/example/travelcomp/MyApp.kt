package com.example.travelcomp

import android.app.Application

class MyApp : Application() {
    val allCityList: ArrayList<String> = arrayListOf()

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: MyApp
            private set
    }
}