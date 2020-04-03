package com.oymj.greenearthhero.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.utils.FormUtils
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
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

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener(this) {
            task ->

            if(task.isSuccessful){
                //redirect to menu activity
                var intent = Intent(this@LoginActivity, MenuActivity::class.java)
                startActivity(intent)
            }else{
                Toast.makeText(this,"Authentication Fail", Toast.LENGTH_SHORT).show()
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
