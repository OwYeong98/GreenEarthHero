package com.oymj.greenearthhero.ui.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.Models.SharedPreference
import com.oymj.greenearthhero.utils.LocationUtils
import com.oymj.greenearthhero.utils.PermissionManager
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

        //get all permission needed for the app
        getPermission()

    }

    private fun checkFirstTimeUser(){
        val mySharePreferenceService: SharedPreference = SharedPreference(this)//get shared  / cache

        if (mySharePreferenceService.getValueBoolean("isFirstTimeUser", true)){
            //if first time user, navigate to intro screen
            var intent = Intent(this, IntroActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getPermission(){
        if(!PermissionManager.haveLocationPermission(this)){
            PermissionManager.requestLocationPermission(this)
        }else{
            //start update location constantly
            LocationUtils.startConstantUpdateLocation(this)
        }
    }

    //when user interact with request permission dialog
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionManager.PERMISSION_REQUEST_LOCATION) {
            var allSuccess = true
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    allSuccess = false
                    val requestAgain = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(permissions[i])
                    if (requestAgain) {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Go to settings and enable the permission", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            if(allSuccess){
                //start update location constantly
                LocationUtils.startConstantUpdateLocation(this)
            }
        }
    }


}
