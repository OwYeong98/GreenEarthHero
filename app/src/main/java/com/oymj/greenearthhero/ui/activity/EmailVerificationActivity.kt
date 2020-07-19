package com.oymj.greenearthhero.ui.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.ui.dialog.LoadingDialog
import com.oymj.greenearthhero.ui.dialog.SuccessDialog
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_email_verification.*
import java.text.DecimalFormat
import java.util.*

class EmailVerificationActivity : AppCompatActivity() {
    lateinit var countDownResendButtonThread: Thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_verification)

        setupUI()
        sendEmailVerification()

        btnBack.setOnClickListener {
            finish()
        }

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
                       finish()
                       var intent = Intent(this,PhoneVerificationActivity::class.java)
                       startActivity(intent)
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

    fun countDownResendButton(sentTime: Date){
        if (::countDownResendButtonThread.isInitialized){
            countDownResendButtonThread.interrupt()
        }

        countDownResendButtonThread = Thread{
            try{
                var keepLoop= true
                while (keepLoop){
                    var timeLeft = Date().time - sentTime.time

                    var twoMinutes = 2 * 1000 * 60
                    if(timeLeft < twoMinutes){
                        timeLeft = twoMinutes - timeLeft
                        var minutesLeft = Math.floor((timeLeft / 1000 / 60 % 60).toDouble()).toInt()
                        var secondLeft = Math.floor((timeLeft / 1000 % 60).toDouble()).toInt()

                        runOnUiThread {
                            btnResendEmail.background = RippleUtil.getGradientRippleButtonOutlineDrawable(this,
                                Color.WHITE,
                                Color.WHITE,
                                resources.getColor(R.color.transparent_pressed),
                                Color.parseColor("#EF5656"),
                                Color.parseColor("#D81B60"),
                                50f,2, GradientDrawable.Orientation.LEFT_RIGHT
                            )
                            btnResendEmail.setTextColor(Color.parseColor("#D81B60"))
                            btnResendEmail.text = "Resend Email in ${DecimalFormat("00").format(minutesLeft)}:${DecimalFormat("00").format(secondLeft)}"
                            btnResendEmail.isClickable = false
                        }
                    }else{
                        runOnUiThread {
                            btnResendEmail.background = RippleUtil.getGradientRippleButtonOutlineDrawable(this,
                                resources.getColor(R.color.yellow),
                                resources.getColor(R.color.yellow),
                                resources.getColor(R.color.transparent_pressed),
                                Color.TRANSPARENT,
                                Color.TRANSPARENT,
                                50f,0, GradientDrawable.Orientation.LEFT_RIGHT
                            )
                            btnResendEmail.setTextColor(Color.WHITE)
                            btnResendEmail.text = "Resend Email"
                            btnResendEmail.isClickable = true
                        }
                        keepLoop = false
                    }

                    Thread.sleep(1000)
                }
            }catch (ex:InterruptedException){

            }

        }
        countDownResendButtonThread!!.start()


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
                    countDownResendButton(Date())
                    successDialog.show()

                }else{
                    var errorDialog = ErrorDialog(this,"Error sending email","We've encountered error when sending email. Please try again later.")
                    Log.d("wtf","error: ${task.exception}")
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