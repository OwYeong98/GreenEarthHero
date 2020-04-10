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
import com.oymj.greenearthhero.ui.dialog.LoadingDialog
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

        inputEmail.setText("hahaha@gmail.com")
        inputPassword.setText("123456")

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
                loadingDialog.hide()

                //update the device token to firebase so we can send notification later
                FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                    innerTask->
                    var token = innerTask.token
                    var db = FirebaseFirestore.getInstance()
                    db.collection("Users").document(FirebaseAuth.getInstance().currentUser?.uid!!).update(mapOf(
                        "cloudMessagingId" to token
                    ))
                }
                //redirect to menu activity
                var intent = Intent(this@LoginActivity, MenuActivity::class.java)
                startActivity(intent)
            }else{
                loadingDialog.hide()
                Toast.makeText(this,"Authentication Fail! Crediential provided is not valid", Toast.LENGTH_SHORT).show()
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
