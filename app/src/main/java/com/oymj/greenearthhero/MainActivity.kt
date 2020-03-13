package com.oymj.greenearthhero

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.oymj.greenearthhero.Models.SharedPreference
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkFirstTimeUser()

        getStartedButton.setOnClickListener{
            var intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener{
            var intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkFirstTimeUser(){
        val mySharePreferenceService: SharedPreference = SharedPreference(this)//get shared  / cache

        if (mySharePreferenceService.getValueBoolean("isFirstTimeUser", true)){
            //if first time user, navigate to intro screen
            var intent = Intent(this, IntroActivity::class.java)
            startActivity(intent)
        }
    }
}
