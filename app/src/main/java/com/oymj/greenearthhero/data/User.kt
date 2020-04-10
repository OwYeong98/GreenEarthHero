package com.oymj.greenearthhero.data

import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class User(var userId:String, var email:String, var firstName:String,  var lastName:String, var phone:String, var dateOfBirth: Date) {

    companion object{
        fun getUserListFromFirebase(callback: (Boolean,String?,ArrayList<User>?)->Unit){
            var fireStoreDB = FirebaseFirestore.getInstance()

            var userList = ArrayList<User>()
            fireStoreDB.collection("Users").get()
                .addOnSuccessListener { usersListResult ->
                    for(userDoc in usersListResult){
                        var userId = userDoc.id
                        var email = userDoc.getString("email")
                        var firstName = userDoc.getString("firstName")
                        var lastName = userDoc.getString("lastName")
                        var phone = userDoc.getString("phone")
                        var dateOfBirth = SimpleDateFormat("dd/MM/yyyy").parse(userDoc.getString("dateOfBirth"))

                        var newUser = User(userId, email!!,firstName!!,lastName!!,phone!!,dateOfBirth!!)

                        userList.add(newUser)
                    }

                    //callback after done getting list
                    callback(true,null,userList)

                }.addOnFailureListener {
                    exception ->

                    //pass failure and exception
                    callback(false,exception.message,null)
                }
        }
    }

    fun getFullName():String{
        return lastName+" "+firstName
    }


}