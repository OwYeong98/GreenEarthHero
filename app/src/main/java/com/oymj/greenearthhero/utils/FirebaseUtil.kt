package com.oymj.greenearthhero.utils

import android.content.Context
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.oymj.greenearthhero.data.User
import com.oymj.greenearthhero.ui.activity.LoginActivity

object FirebaseUtil {

    var currentUserDetail: User? = null

    fun getUserIdAndRedirectToLoginIfNotFound(context: Context): String?{
        return if(FirebaseAuth.getInstance().currentUser?.uid != null){
            FirebaseAuth.getInstance().currentUser?.uid
        }else{
            context.startActivity(Intent(context, LoginActivity::class.java))
            null
        }
    }
}