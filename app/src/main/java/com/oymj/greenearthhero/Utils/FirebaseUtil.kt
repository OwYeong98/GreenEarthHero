package com.oymj.greenearthhero.Utils

import android.content.Context
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.oymj.greenearthhero.ui.activity.LoginActivity

object FirebaseUtil {

    fun getUserIdAndRedirectToLoginIfNotFound(context: Context): String?{
        return if(FirebaseAuth.getInstance().currentUser?.uid != null){
            FirebaseAuth.getInstance().currentUser?.uid
        }else{
            context.startActivity(Intent(context, LoginActivity::class.java))
            null
        }
    }
}