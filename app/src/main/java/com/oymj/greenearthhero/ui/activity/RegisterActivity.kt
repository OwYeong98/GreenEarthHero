package com.oymj.greenearthhero.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.oymj.greenearthhero.R
import kotlinx.android.synthetic.main.activity_register.*


class RegisterActivity : AppCompatActivity() {
    lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()

        tvHaveAccount.setOnClickListener{
            finish()
            startActivity(Intent(this,
                LoginActivity::class.java))
        }

        btnRegister.setOnClickListener{

            var email:String = inputEmail.text.toString()
            var password: String = inputPassword.text.toString()
            var firstName: String = inputFirstName.text.toString()
            var lastName: String = inputLastName.text.toString()
            var phone: String = inputPhone.text.toString()
            var dateOfBirth: String = inputBirthDate.text.toString()

            createUserAccount()
        }


    }

    private fun createUserAccount(){

        var email:String = inputEmail.text.toString()
        var password: String = inputPassword.text.toString()
        var firstName: String = inputFirstName.text.toString()
        var lastName: String = inputLastName.text.toString()
        var phone: String = inputPhone.text.toString()
        var dateOfBirth: String = inputBirthDate.text.toString()

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, object: OnCompleteListener<AuthResult> {

                override fun onComplete(task: Task<AuthResult>) {
                    if(task.isSuccessful){
                        var user: FirebaseUser? = mAuth.getCurrentUser()

                        val userData = hashMapOf(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "phone" to phone,
                            "dateOfBirth" to dateOfBirth
                        )

                        FirebaseFirestore.getInstance().collection("Users").document(user?.uid!!)
                            .set(userData)
                            .addOnSuccessListener {
                                Log.d("sucess", "yay success")
                            }
                            .addOnFailureListener {
                                    e -> Log.d("error", "Error writing document", e) }
                    }else{
                        Log.d("error",task.exception!!.toString())
                    }
                }
            });
    }
}
