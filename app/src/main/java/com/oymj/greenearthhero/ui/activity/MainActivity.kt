package com.oymj.greenearthhero.ui.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.data.SharedPreference
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.utils.LocationUtils
import com.oymj.greenearthhero.utils.PermissionManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(checkFirstTimeUser()){
            //get all permission needed for the app
            getAllPermission()

        }else{


            getStartedButton.setOnClickListener{
                if(isAllPermissionGranted()) {
                    var intent = Intent(this, RegisterActivity::class.java)
                    startActivity(intent)
                }
            }

            loginButton.setOnClickListener{
                if(isAllPermissionGranted()){
                    var intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
            }

        }
    }

    private fun checkFirstTimeUser(): Boolean{
        val mySharePreferenceService: SharedPreference =
            SharedPreference(this)//get shared  / cache

        return mySharePreferenceService.getValueBoolean("isFirstTimeUser", true)
    }
    private fun getAllPermission(){
        var permissionNeeded = PermissionManager.PERMISSIONS_LOCATION + PermissionManager.PERMISSIONS_STORAGE
        ActivityCompat.requestPermissions(this,permissionNeeded,1)
    }

    private fun isAllPermissionGranted():Boolean{
        var isAllPermissionGranted = true
        if(!PermissionManager.haveLocationPermission(this)){
            isAllPermissionGranted=false
            var errorDialog = ErrorDialog(this,"Location Access not granted","We 've need location access to display your current location for your convenience. We wont use your location data for other purposes. Please allow location permission")
            errorDialog.show()

            PermissionManager.requestLocationPermission(this)
        }
        if(!PermissionManager.hasStorageReadWritePermission(this)){
            isAllPermissionGranted=false

            var errorDialog = ErrorDialog(this,"Storage access not granted","We 've storage access for you to upload image. We wont use it for any other purposes. Please allow storage permission")
            errorDialog.show()
            PermissionManager.requestStorageReadWritePermission(this)
        }

        return isAllPermissionGranted
    }

    //when user interact with request permission dialog
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == 1 ){
            //if first time user, navigate to intro screen
            var intent = Intent(this, IntroActivity::class.java)
            startActivity(intent)
        }

    }


}
