package com.oymj.greenearthhero.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.data.User
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.ui.dialog.LoadingDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.utils.FormUtils
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    lateinit var loadingDialog:LoadingDialog
    //all button action will be registered here
    private var myOnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            when (v) {
                btnSignIn -> {
                    if(validate())
                        loginFromFirebase()
                    else
                        Toast.makeText(this@LoginActivity,"Please fill in the form", Toast.LENGTH_LONG).show()
                }
                btnBack -> {
                    finish()
                }

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        inputEmail.setText("mun-ji@hotmail.com")
        inputPassword.setText("Asd@1234")

        //initialize loading dialog
        loadingDialog = LoadingDialog(this)

        linkAllButtonWithOnClickListener()
    }

    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            btnSignIn,
            btnBack
        )

        for (view in actionButtonViewList) {
            view.setOnClickListener(myOnClickListener)
        }
    }

    private fun loginFromFirebase(){
        var email = inputEmail.text.toString()
        var password = inputPassword.text.toString()

        loadingDialog.show()

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener(this) {
            task ->

            if(task.isSuccessful){


                //update the device token to firebase so we can send notification later
                FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                    innerTask->
                    var token = innerTask.token
                    var db = FirebaseFirestore.getInstance()
                    db.collection("Users").document(FirebaseAuth.getInstance().currentUser?.uid!!).update(mapOf(
                        "cloudMessagingId" to token
                    ))
                }

                User.getSpecificUserFromFirebase(task.getResult()?.user?.uid!!,callback = {
                    success,message,data->
                    if(success){
                        FirebaseUtil.currentUserDetail = data!!
                        loadingDialog.dismiss()

                        if(!task.result!!.user!!.isEmailVerified){
                            var intent = Intent(this@LoginActivity, EmailVerificationActivity::class.java)
                            startActivity(intent)
                        }else if(!data.isPhoneVerified){
                            var intent = Intent(this@LoginActivity, PhoneVerificationActivity::class.java)
                            startActivity(intent)
                        }else{
                            //redirect to menu activity
                            var intent = Intent(this@LoginActivity, MenuActivity::class.java)
                            intent.putExtra("callFromLogin",true)
                            startActivity(intent)
                        }

                    }else{
                        var errorDialog = ErrorDialog(this,"Error","Error getting user profile!")
                        errorDialog.show()
                    }
                })


            }else{
                loadingDialog.dismiss()
                var errorDialog = ErrorDialog(this,"Authentication Fail!","Either Email or password provided is invalid")
                errorDialog.show()
            }
        }
    }

    fun validate(): Boolean{
        var email = inputEmail.text.toString()
        var password = inputPassword.text.toString()

        var emailError = ""
        var passwordError = ""

        emailError+=FormUtils.isNull("Email",email)?:""

        passwordError+=FormUtils.isNull("Password",email)?:""

        if(emailError!=""){
            inputEmail.error = emailError
        }
        if(passwordError!=""){
            inputPassword.error = passwordError
        }

        return emailError+passwordError == ""
    }
}
