package com.oymj.greenearthhero.ui.activity

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.ImageViewCompat
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.data.User
import com.oymj.greenearthhero.ui.customxmllayout.CustomNumpad
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.ui.dialog.LoadingDialog
import com.oymj.greenearthhero.ui.dialog.ResendSMSVerificationDialog
import com.oymj.greenearthhero.ui.dialog.SuccessDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_phone_verification.*
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PhoneVerificationActivity : AppCompatActivity() {

    lateinit var verificationId:String
    lateinit var phoneNo:String
    lateinit var countDownResendButtonThread: Thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_verification)

        setupUI()

        myNumpad.onOkClickListener = {
            code->

            if(code.length < 6){
                var errorDialog = ErrorDialog(this,"Invalid format","Verification code must be 6 digit.")
                errorDialog.show()
            }else{
                verifyCode(code)
            }

        }

        sendSMSToCurrentUserPhone()

        btnResendEmail.isClickable = false
        btnResendEmail.setOnClickListener {
            var resendDialog = ResendSMSVerificationDialog(this,phoneNo){
                phoneNo->
                sendVerificationCode(phoneNo)
            }
            resendDialog.show()
        }
    }

    fun setupUI(){
        myNumpad.setIsAllKeyReadyListener{
            var btnDelete = myNumpad.getButtonReference(CustomNumpad.BUTTON_DELETE) as ImageView
            ImageViewCompat.setImageTintMode(btnDelete, PorterDuff.Mode.SRC_ATOP)
            ImageViewCompat.setImageTintList(btnDelete, ColorStateList.valueOf(Color.parseColor("#D81B60")))


            myNumpad.setAllButtonStyle(Color.WHITE,Color.WHITE,Color.parseColor("#c4c2bc"),Color.WHITE,Color.WHITE,200f,0)
            myNumpad.setSpecificButtonStyle(CustomNumpad.BUTTON_OK,Color.WHITE,Color.WHITE,Color.parseColor("#c4c2bc"),Color.parseColor("#D81B60"),Color.parseColor("#D81B60"),200f,2)
            myNumpad.setSpecificButtonStyle(CustomNumpad.BUTTON_DELETE,Color.WHITE,Color.WHITE,Color.parseColor("#c4c2bc"),Color.parseColor("#D81B60"),Color.parseColor("#D81B60"),200f,2)


            myNumpad.setSpecificButtonTextColor(CustomNumpad.BUTTON_OK,Color.parseColor("#D81B60"))
            (myNumpad.getButtonReference(CustomNumpad.BUTTON_OK) as TextView).setTextColor(Color.parseColor("#D81B60"))
        }

        btnResendEmail.background = RippleUtil.getGradientRippleButtonOutlineDrawable(this,
            Color.WHITE,
            Color.WHITE,
            resources.getColor(R.color.transparent_pressed),
            Color.parseColor("#EF5656"),
            Color.parseColor("#D81B60"),
            50f,2, GradientDrawable.Orientation.LEFT_RIGHT
        )
    }

    fun verifyCode(code:String){
        if(::verificationId.isInitialized){
            val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
            var loadingDialog = LoadingDialog(this)
            loadingDialog.show()

            FirebaseAuth.getInstance().currentUser!!.linkWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        FirebaseFirestore.getInstance().collection("Users").document(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this@PhoneVerificationActivity)!!).update(
                            mapOf("isPhoneVerified" to true, "phone" to phoneNo))
                            .addOnSuccessListener {
                                loadingDialog.dismiss()
                                var successDialog = SuccessDialog(this@PhoneVerificationActivity,"Successfully verified!","We automatically detected the verification code from your message"){
                                    var intent = Intent(this@PhoneVerificationActivity, MenuActivity::class.java)
                                    intent.putExtra("callFromLogin",true)
                                    startActivity(intent)
                                }
                                successDialog.show()
                            }.addOnFailureListener { e->
                                loadingDialog.dismiss()
                                var errorDialog = ErrorDialog(this@PhoneVerificationActivity,"Error occured","We have encountered error when connecting to firebase.")
                                errorDialog.show()
                            }

                    } else {
                        loadingDialog.dismiss()
                        if(task.exception is FirebaseAuthUserCollisionException){
                            var errorDialog = ErrorDialog(this@PhoneVerificationActivity,"Phone Number already used","Phone number is already used by other person!")
                            errorDialog.show()
                        }else if(task.exception is FirebaseAuthInvalidCredentialsException){
                            var errorDialog = ErrorDialog(this@PhoneVerificationActivity,"Invalid Verification Code","Your verification code is invalid! Please enter the code sent to your phone.")
                            errorDialog.show()
                        }else{
                            var errorDialog = ErrorDialog(this@PhoneVerificationActivity,"Phone already verified for this account","Phone already verified for this account.")
                            errorDialog.show()
                        }

                    }
                }
        }else{
            var errorDialog = ErrorDialog(this@PhoneVerificationActivity,"Verification code not sent yet!","We have encountered error when sending SMS to ur phone number.")
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
                            btnResendEmail.text = "Resend SMS in ${DecimalFormat("00").format(minutesLeft)}:${DecimalFormat("00").format(secondLeft)}"
                            btnResendEmail.isClickable = false
                        }
                    }else{
                        runOnUiThread {
                            btnResendEmail.background = RippleUtil.getGradientRippleButtonOutlineDrawable(this,
                                Color.parseColor("#EF5656"),
                                Color.parseColor("#D81B60"),
                                resources.getColor(R.color.transparent_pressed),
                                Color.TRANSPARENT,
                                Color.TRANSPARENT,
                                50f,0, GradientDrawable.Orientation.LEFT_RIGHT
                            )
                            btnResendEmail.setTextColor(Color.WHITE)
                            btnResendEmail.text = "Resend SMS"
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

    fun sendSMSToCurrentUserPhone(){
        User.getSpecificUserFromFirebase(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this)!!) { success, message, user ->
            if (success) {
                sendVerificationCode(user!!.phone)
            }
        }
    }

    fun sendVerificationCode(phone:String){
        var loadingDialog = LoadingDialog(this)
        loadingDialog.show()

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phone, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    FirebaseAuth.getInstance().currentUser!!.linkWithCredential(credential)
                        .addOnCompleteListener(this@PhoneVerificationActivity) { task ->
                            if (task.isSuccessful) {
                                FirebaseFirestore.getInstance().collection("Users").document(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this@PhoneVerificationActivity)!!).update(
                                    mapOf("isPhoneVerified" to true, "phone" to phone))
                                    .addOnSuccessListener {
                                        loadingDialog.dismiss()

                                        var successDialog = SuccessDialog(this@PhoneVerificationActivity,"Successfully verified!","We automatically detected the verification code from your message"){
                                            //redirect to menu activity
                                            var intent = Intent(this@PhoneVerificationActivity, MenuActivity::class.java)
                                            intent.putExtra("callFromLogin",true)
                                            startActivity(intent)
                                        }
                                        successDialog.show()
                                    }.addOnFailureListener {e->
                                        Log.d("wtf","error: $e")
                                        loadingDialog.dismiss()
                                        var errorDialog = ErrorDialog(this@PhoneVerificationActivity,"Error occured!","We have encountered error when connecting to firebase.")
                                        errorDialog.show()
                                    }

                            } else {
                                loadingDialog.dismiss()
                                if(task.exception is FirebaseAuthUserCollisionException){
                                    var errorDialog = ErrorDialog(this@PhoneVerificationActivity,"Phone Number already used","Phone number is already used by other person!")
                                    errorDialog.show()
                                }else if(task.exception is FirebaseAuthInvalidCredentialsException){
                                    var errorDialog = ErrorDialog(this@PhoneVerificationActivity,"Invalid Verification Code","Your verification code is invalid! Please enter the code sent to your phone.")
                                    errorDialog.show()
                                }else{
                                    var errorDialog = ErrorDialog(this@PhoneVerificationActivity,"Phone already verified for this account","Phone already verified for this account.")
                                    errorDialog.show()
                                }
                            }
                        }

                }

                override fun onVerificationFailed(e: FirebaseException) {
                    loadingDialog.dismiss()
                    Log.d("wtf","error sending code: ${e}")
                    var errorDialog = ErrorDialog(this@PhoneVerificationActivity,"Error Occured!","We have encountered error when sending SMS to ur phone number. Please make sure your phone number is valid.")
                    errorDialog.show()
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    loadingDialog.dismiss()
                    this@PhoneVerificationActivity.verificationId = verificationId
                    this@PhoneVerificationActivity.phoneNo = phone

                    countDownResendButton(Date())

                    tvSuccessSendSMS.text = "A Verification code is sent to  ${phoneNo}. Please check your SMS."

                }
            })
    }



}