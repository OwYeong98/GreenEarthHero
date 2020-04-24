package com.oymj.greenearthhero.data

import android.location.Location
import android.webkit.ValueCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.oymj.greenearthhero.utils.LocationUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FoodDonation(
    var id:String,
    var donatorUser: User,
    var datePosted: Date,
    var donateLocation:DonateLocation,
    var minutesAvailable:Int,
    var foodList: ArrayList<Food>,
    var totalFoodAmount:Int
    ) {

    companion object{

        fun getFoodDonationListFromFirebase(callback: (Boolean,String?,ArrayList<FoodDonation>?)->Unit){
            var foodDonationList = ArrayList<FoodDonation>()
            FirebaseFirestore.getInstance().collection("Food_Donation").get()
                .addOnSuccessListener {
                        foodDonationSnapshot->
                    GlobalScope.launch {
                        for(foodDonation in foodDonationSnapshot!!){
                            var id = foodDonation.id
                            var datePosted = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(foodDonation.getString("datePosted"))
                            var donateLocation = DonateLocation.getDonateLocationById(foodDonation.getString("donateLocationId")!!)
                            var donatorUserRef = User.suspendGetSpecificUserFromFirebase(foodDonation.getString("donatorUserId")!!)
                            var minutesAvailable = foodDonation.getLong("minutesAvailable")?.toInt()
                            var totalFoodAmount = foodDonation.getLong("totalFoodAmount")?.toInt()

                            var foodList = Food.suspendGetFoodList(foodDonation.id)

                            var newFoodDonation = FoodDonation(id,donatorUserRef,datePosted,donateLocation!!,minutesAvailable!!,foodList,totalFoodAmount!!)

                            foodDonationList.add(newFoodDonation)
                        }

                        callback(true,null,foodDonationList)
                    }
                }
                .addOnFailureListener {
                        ex->
                    callback(false,ex.toString(),null)
                }
        }

        fun getFoodDonationListWithoutFoodListFromFirebase(callback: (Boolean,String?,ArrayList<FoodDonation>?)->Unit){
            var foodDonationList = ArrayList<FoodDonation>()
            FirebaseFirestore.getInstance().collection("Food_Donation").get()
                .addOnSuccessListener {
                        foodDonationSnapshot->
                    DonateLocation.getDonateLocationListOfUser(FirebaseAuth.getInstance().currentUser?.uid!!,callback = {
                        success,message,donateLocationList->

                        if(success){
                            GlobalScope.launch {
                                for(foodDonation in foodDonationSnapshot!!){
                                    var id = foodDonation.id
                                    var datePosted = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(foodDonation.getString("datePosted"))
                                    var donateLocationId = foodDonation.getString("donateLocationId")!!
                                    var donateLocation  = donateLocationList?.fold(null as DonateLocation?,{prev,curr-> if(curr.id == donateLocationId) curr else prev})

                                    var donatorUserRef = User.suspendGetSpecificUserFromFirebase(foodDonation.getString("donatorUserId")!!)
                                    var minutesAvailable = foodDonation.getLong("minutesAvailable")?.toInt()
                                    var totalFoodAmount = foodDonation.getLong("totalFoodAmount")!!.toInt()

                                    var foodList = ArrayList<Food>()

                                    var newFoodDonation = FoodDonation(id,donatorUserRef,datePosted,donateLocation!!,minutesAvailable!!,foodList,totalFoodAmount!!)

                                    foodDonationList.add(newFoodDonation)
                                }

                                callback(true,null,foodDonationList)
                            }
                        }else{
                            callback(false,message,null)
                        }
                    })

                }
                .addOnFailureListener {
                        ex->
                    callback(false,ex.toString(),null)
                }
        }

    }


    fun getDistanceBetween(): Float{
        if(LocationUtils.getLastKnownLocation() != null){
            var userCurrentLoc = LocationUtils.getLastKnownLocation()
            var results: FloatArray = FloatArray(2)
            Location.distanceBetween(userCurrentLoc?.latitude!!,userCurrentLoc?.longitude!!,
                donateLocation.location.lat!!,donateLocation.location.lon!!,results)
            return results[0]
        }else{
            return -1f
        }
    }

    fun getDonationEndTime():Date{
        var calendar = Calendar.getInstance()
        calendar.time = datePosted
        calendar.add(Calendar.MINUTE,minutesAvailable)

        return calendar.time
    }


}