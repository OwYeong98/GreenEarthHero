package com.oymj.greenearthhero.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FoodDonationHistory(
    var id:String,
    var donatorUser: User,
    var dateStarted: Date,
    var dateEnded: Date,
    var donateLocation:DonateLocation,
    var minutesAvailable:Int,
    var foodList: ArrayList<Food>,
    var totalFoodAmount:Int
) {

    companion object{
        fun getFoodDonationHistoryListByUserFromFirebase(userId:String, callback: (Boolean,String?,ArrayList<FoodDonationHistory>?)->Unit){
            var foodDonationHistoryList = ArrayList<FoodDonationHistory>()
            FirebaseFirestore.getInstance().collection("Food_Donation_History").whereEqualTo("donatorUser.userId",userId).get()
                .addOnSuccessListener {
                        foodDonationHistorySnapshot->
                    GlobalScope.launch {
                        for(foodDonation in foodDonationHistorySnapshot!!){
                            var id = foodDonation.id
                            var dateStarted = SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(foodDonation.getString("date_started"))
                            var dateEnded = SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(foodDonation.getString("date_ended"))
                            var minutesAvailable =foodDonation.getLong("minutesAvailable")!!.toInt()
                            var totalFoodAmount =foodDonation.getLong("totalFoodAmount")!!.toInt()

                            var donateLocationData = foodDonation.get("donateLocation") as HashMap<String,String>
                            var positionData = foodDonation.get("donateLocation.location") as HashMap<String,Double>
                            var donatorUserData = foodDonation.get("donatorUser") as HashMap<String,String>
                            var foodList = Food.suspendGetHistoryFoodList(id)

                            var location = TomTomPosition()
                            location.lat = positionData["lat"]!!
                            location.lon = positionData["lon"]!!

                            var donateLocationObj = DonateLocation(donateLocationData["id"]!!,donateLocationData["userId"]!!,donateLocationData["name"]!!
                                ,donateLocationData["address"]!!,location)

                            var donatorUserObj = User(donatorUserData["userId"]!!,donatorUserData["email"]!!,donatorUserData["firstName"]!!
                                ,donatorUserData["lastName"]!!,donatorUserData["phone"]!!,(donatorUserData["dateOfBirth"] as Timestamp).toDate())

                            var newFoodHistory = FoodDonationHistory(id,donatorUserObj,dateStarted,dateEnded!!,donateLocationObj!!,minutesAvailable,foodList,totalFoodAmount!!)

                            foodDonationHistoryList.add(newFoodHistory)
                        }

                        callback(true,null,foodDonationHistoryList)
                    }
                }
                .addOnFailureListener {
                        ex->
                    callback(false,ex.toString(),null)
                }
        }
    }
}