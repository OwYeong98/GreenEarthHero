package com.oymj.greenearthhero.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.data.User
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.ui.dialog.LoadingDialog
import com.oymj.greenearthhero.utils.FormUtils
import kotlinx.android.synthetic.main.activity_register.*
import java.text.SimpleDateFormat
import java.util.*


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
            if(validate())
                createUserAccount()
            else
                Toast.makeText(this@RegisterActivity,"Please fill in the form",Toast.LENGTH_SHORT).show()
        }

        val builder = MaterialDatePicker.Builder.datePicker()
        val currentTimeInMillis = Calendar.getInstance().timeInMillis
        builder.setSelection(currentTimeInMillis)
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener {
                dateLong->
            val date = Date(dateLong)
            inputBirthDate.setText(SimpleDateFormat("dd/MM/yyyy").format(date))
            validate()
        }

        inputBirthDate.setOnClickListener {
            if(!picker.isVisible)
                picker.show(supportFragmentManager, picker.toString())
        }
    }

    private fun createUserAccount(){

        var email:String = inputEmail.text.toString()
        var password: String = inputPassword.text.toString()
        var firstName: String = inputFirstName.text.toString()
        var lastName: String = inputLastName.text.toString()
        var phone: String = "+60"+inputPhone.text.toString()
        var dateOfBirth: String = inputBirthDate.text.toString()

        var loadingDialog = LoadingDialog(this)
        loadingDialog.show()

        User.getUserListFromFirebase {
                success, message, userList ->

            if (success){
                var isPhoneExist = userList!!.fold(false,{prev,curr-> prev || curr.phone == phone})
                if(isPhoneExist){
                    loadingDialog.dismiss()
                    var errorDialog = ErrorDialog(this,"Phone already in use","Please try another Phone No")
                    errorDialog.show()
                }else{
                    mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this){
                                task->

                            if(task.isSuccessful){
                                var user: FirebaseUser? = mAuth.getCurrentUser()

                                val userData = hashMapOf(
                                    "email" to email,
                                    "firstName" to firstName,
                                    "lastName" to lastName,
                                    "phone" to phone,
                                    "dateOfBirth" to dateOfBirth,
                                    "isPhoneVerified" to false
                                )

                                FirebaseFirestore.getInstance().collection("Users").document(user?.uid!!)
                                    .set(userData)
                                    .addOnSuccessListener {
                                        loadingDialog.dismiss()
                                        //update the device token to firebase so we can send notification later
                                        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                                                innerTask->
                                            var token = innerTask.token
                                            var db = FirebaseFirestore.getInstance()
                                            db.collection("Users").document(FirebaseAuth.getInstance().currentUser?.uid!!).update(mapOf(
                                                "cloudMessagingId" to token
                                            ))
                                        }

                                        var intent = Intent(this@RegisterActivity,EmailVerificationActivity::class.java)
                                        startActivity(intent)
                                    }
                                    .addOnFailureListener {
                                            e -> Log.d("error", "Error writing document", e) }
                            }else{
                                loadingDialog.dismiss()
                                if(task.exception is FirebaseAuthUserCollisionException){
                                    var errorDialog = ErrorDialog(this,"Email already in use","Please try another Email")
                                    errorDialog.show()
                                }else{
                                    var errorDialog = ErrorDialog(this,"Error","We have encountered error when connecting to firebase!")
                                    errorDialog.show()
                                }
                                Log.d("error",task.exception!!.toString())
                            }
                        }
                }
            }else{
                loadingDialog.dismiss()
                var errorDialog = ErrorDialog(this,"Error","We have encountered error when connecting to firebase!")
                errorDialog.show()
            }
        }

    }

    fun validate(): Boolean{
        inputBirthDate.setError(null)
        var email:String = inputEmail.text.toString()
        var password: String = inputPassword.text.toString()
        var confirmPassword: String = inputConfirmPasword.text.toString()
        var firstName: String = inputFirstName.text.toString()
        var lastName: String = inputLastName.text.toString()
        var phone: String = inputPhone.text.toString()
        var dateOfBirth: String = inputBirthDate.text.toString()

        var emailError = ""
        var passwordError = ""
        var confirmPasswordError = ""
        var firstNameError = ""
        var lastNameError = ""
        var phoneError = ""
        var dateOfBirthError = ""

        emailError+= "${FormUtils.isNull("Email",email)?:""}|"
        emailError+= "${FormUtils.isEmail(email)?:""}|"

        passwordError+="${FormUtils.isNull("Password",password)?:""}|"
        passwordError+="${FormUtils.isMatchRegex(password,"^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}\$","Password must contain at least one uppercase,lowercase,number,symbol with minimum of 8 character")?:""}|"

        if(confirmPassword != password){
            confirmPasswordError += "Confirm password does not match your password!"
        }

        firstNameError+= "${FormUtils.isNull("First Name",firstName)?:""}|"
        firstNameError+= "${FormUtils.isLengthBetween("First Name",firstName,3,20)?:""}|"

        lastNameError+= "${FormUtils.isNull("Last Name",lastName)?:""}|"
        lastNameError+= "${FormUtils.isLengthBetween("Last Name",lastName,3,20)?:""}|"

        phoneError+= "${FormUtils.isNull("Phone No",phone)?:""}|"
        phoneError+= "${FormUtils.isOnlyNumber("Phone No",phone)?:""}|"
        phoneError+= "${FormUtils.isLengthBetween("Phone No",phone,9,10)?:""}|"

        dateOfBirthError+= "${FormUtils.isNull("Date Of Birth",dateOfBirth)?:""}|"

        Log.d("asd",emailError)
        if(emailError!=""){
            for(err in emailError.split("|")){
                if(err!=""){
                    inputEmail.error = err
                    break
                }
            }
        }
        if(passwordError!=""){
            for(err in passwordError.split("|")){
                if(err!=""){
                    inputPassword.error = err
                    break
                }
            }
        }
        if(confirmPasswordError!=""){
            inputConfirmPasword.error = confirmPasswordError
        }
        if(firstNameError!=""){
            for(err in firstNameError.split("|")){
                if(err!=""){
                    inputFirstName.error = err
                    break
                }
            }
        }
        if(lastNameError!=""){
            for(err in lastNameError.split("|")){
                if(err!=""){
                    inputLastName.error = err
                    break
                }
            }
        }
        if(phoneError!=""){
            for(err in phoneError.split("|")){
                if(err!=""){
                    inputPhone.error = err
                    break
                }
            }
        }
        if(dateOfBirthError!=""){
            for(err in dateOfBirthError.split("|")){
                if(err!=""){
                    inputBirthDate.error = err
                    break
                }
            }
        }

        var allError = emailError+passwordError+confirmPasswordError+firstNameError+lastNameError+phoneError+dateOfBirthError

        return allError.replace("|","")== ""
    }
}
