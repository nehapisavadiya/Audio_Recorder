package com.example.audiorecorder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        this.supportActionBar?.hide()
        window.statusBarColor = this.resources.getColor(android.R.color.white)

        Handler().postDelayed({
            startActivity(Intent(applicationContext,MainActivity::class.java))
            this.finish()
        },4000)

    }
}