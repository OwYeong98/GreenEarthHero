package com.oymj.greenearthhero.data

import android.location.Location
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.oymj.greenearthhero.utils.LocationUtils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class RecycleRequestHistory(
    var id:String,
    var dateCollected: Date,
    var userRequested: User,
    var userCollected: User,
    var address:String,
    var location: GeoPoint,
    var glassWeight:Int,
    var plasticWeight:Int,
    var metalWeight:Int,
    var paperWeight:Int
) {

    companion object{

        fun getRecycleRequestHistoryFromFirebase(callback: (Boolean,String?,ArrayList<RecycleRequestHistory>?)->Unit){

            //firestore reference
            var fireStoreDB = FirebaseFirestore.getInstance()

            //empty list
            var recycleHistoryList = ArrayList<RecycleRequestHistory>()

            fireStoreDB.collection("Recycle_Request_History").get()
                .addOnSuccessListener {
                        recycleRequestHistoryResult->

                    for(recycleHistory in recycleRequestHistoryResult){
                        var id = recycleHistory.id
                        var dateCollected = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(recycleHistory.getString("date_collected"))
                        var address = recycleHistory.getString("address")
                        var location = recycleHistory.getGeoPoint("location")
                        var glassWeight = recycleHistory.getLong("glass_weight")?.toInt()
                        var plasticWeight = recycleHistory.getLong("plastic_weight")?.toInt()
                        var paperWeight = recycleHistory.getLong("paper_weight")?.toInt()
                        var metalWeight = recycleHistory.getLong("metal_weight")?.toInt()

                        var userRequestedData = recycleHistory.get("user_requested") as HashMap<String,String>
                        var userCollectedData = recycleHistory.get("user_collected") as HashMap<String,String>

                        var userRequestedObj = User(userRequestedData["userId"]!!,userRequestedData["email"]!!,userRequestedData["firstName"]!!
                            ,userRequestedData["lastName"]!!,userRequestedData["phone"]!!,(userRequestedData["dateOfBirth"] as Timestamp).toDate())

                        var userCollectedObj = User(userCollectedData["userId"]!!,userCollectedData["email"]!!,userCollectedData["firstName"]!!
                            ,userCollectedData["lastName"]!!,userCollectedData["phone"]!!,(userCollectedData["dateOfBirth"] as Timestamp).toDate())

                        var newRecycleHistory = RecycleRequestHistory(id,dateCollected,userRequestedObj,userCollectedObj,address!!,location!!,glassWeight!!,plasticWeight!!,metalWeight!!,paperWeight!!)
                        recycleHistoryList.add(newRecycleHistory)
                    }

                    callback(true,null,recycleHistoryList)

                }.addOnFailureListener {
                        exception ->
                    callback(false,exception.message,null)
                }
        }
    }

    fun getTotalAmount(): Int{
        return metalWeight+paperWeight+plasticWeight+glassWeight
    }

    fun getDistanceBetween(): Float{
        if(LocationUtils.getLastKnownLocation() != null){
            var userCurrentLoc = LocationUtils.getLastKnownLocation()
            var results: FloatArray = FloatArray(2)
            Location.distanceBetween(userCurrentLoc?.latitude!!,userCurrentLoc?.longitude!!,
                location.latitude,location.longitude,results)
            return results[0]
        }else{
            return -1f
        }
    }
}