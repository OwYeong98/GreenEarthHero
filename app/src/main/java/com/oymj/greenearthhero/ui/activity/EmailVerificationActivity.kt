package com.oymj.greenearthhero.ui.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.ui.dialog.LoadingDialog
import com.oymj.greenearthhero.ui.dialog.SuccessDialog
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_email_verification.*

class EmailVerificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_verification)

        setupUI()
        sendEmailVerification()

        btnResendEmail.setOnClickListener {
            sendEmailVerification()
        }

        btnReloadCheckEmail.setOnClickListener {
            checkIsEmailVerified()
        }
    }

    private fun setupUI(){
        btnReloadCheckEmail.background = RippleUtil.getGradientRippleButtonOutlineDrawable(this,
            Color.parseColor("#7CDF75"),
            Color.parseColor("#6BF261"),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            resources.getColor(R.color.transparent),
            15f,0, GradientDrawable.Orientation.LEFT_RIGHT
        )

        btnResendEmail.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.yellow),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            15f,0)
    }

    private fun checkIsEmailVerified(){
        var user = FirebaseAuth.getInstance().currentUser

        if(user != null){
            var loadingDialog = LoadingDialog(this)
            loadingDialog.show()
           user.reload().addOnCompleteListener {
                task ->

               loadingDialog.dismiss()

               if(user.isEmailVerified){
                   var successDialog = SuccessDialog(this,"Email is verified successfully","Thank you for verifying your email."){

                   }
                   successDialog.show()
               }else{
                   var errorDialog = ErrorDialog(this,"Email is not verified","Please check your email for the verification email and\n follow the link in email for verifcation.")
                   errorDialog.show()
               }
            }
        }else{
            var errorDialog = ErrorDialog(this,"You are not logged in","Please log in your account first"){
                var intent = Intent(this,LoginActivity::class.java)
                startActivity(intent)
            }
            errorDialog.show()
        }
    }

    private fun sendEmailVerification(){
        var user = FirebaseAuth.getInstance().currentUser

        if(user != null){
            var loadingDialog = LoadingDialog(this)
            loadingDialog.show()
            user.sendEmailVerification().addOnCompleteListener {
                task ->

                loadingDialog.dismiss()

                if(task.isSuccessful){
                    var successDialog = SuccessDialog(this,"Verification Email sent successfully","An email is send to ${user.email}.\nPlease check your email inbox to verify your email.\nNote: Email may goes to Spam folder.")
                    successDialog.show()
                }else{
                    var errorDialog = ErrorDialog(this,"Error sending email","We've encountered error when sending email. Please try again later.")
                    errorDialog.show()
                }
            }
        }else{
            var errorDialog = ErrorDialog(this,"You are not logged in","Please log in your account first"){
                var intent = Intent(this,LoginActivity::class.java)
                startActivity(intent)
            }
            errorDialog.show()
        }
    }




}