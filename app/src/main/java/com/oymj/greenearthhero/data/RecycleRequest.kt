package com.oymj.greenearthhero.data

import android.location.Location
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.oymj.greenearthhero.utils.LocationUtils
import java.text.SimpleDateFormat
import java.util.Date

class RecycleRequest(
    var id:String,
    var dateRequested: Date,
    var requestedUser: User,
    var address:String,
    var location:GeoPoint,
    var glassWeight:Int,
    var plasticWeight:Int,
    var metalWeight:Int,
    var paperWeight:Int,
    var acceptedCollectUser: User?
) {

    companion object{

        fun getRecycleRequestFromFirebase(callback: (Boolean,String?,ArrayList<RecycleRequest>?)->Unit){

            //firestore reference
            var fireStoreDB = FirebaseFirestore.getInstance()

            //empty list
            var recycleRequestList = ArrayList<RecycleRequest>()

            fireStoreDB.collection("Recycle_Request").get()
                .addOnSuccessListener {
                        recycleRequestResult->

                    //get user list
                    User.getUserListFromFirebase {
                            success,message,userList ->

                        if(success){
                            for(recycleRequest in recycleRequestResult){
                                var id = recycleRequest.id
                                var dateRequested = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(recycleRequest.getString("date_requested"))
                                var userRequestedId = recycleRequest.getString("userId")
                                var address = recycleRequest.getString("address")
                                var location = recycleRequest.getGeoPoint("location")
                                var glassWeight = recycleRequest.getLong("glass_weight")?.toInt()
                                var plasticWeight = recycleRequest.getLong("plastic_weight")?.toInt()
                                var paperWeight = recycleRequest.getLong("paper_weight")?.toInt()
                                var metalWeight = recycleRequest.getLong("metal_weight")?.toInt()
                                var acceptedCollectBy = recycleRequest.getString("accepted_collect_by")


                                var userRequestedObj: User? = null
                                var acceptedCollectUserObj: User? = null

                                for(user in userList!!){
                                    if(userRequestedObj == null || acceptedCollectUserObj == null){
                                        //match requested user id
                                        if (user.userId == userRequestedId)
                                            userRequestedObj = user

                                        //match accept collect user id
                                        if (user.userId == acceptedCollectBy)
                                            acceptedCollectUserObj = user
                                    }else{
                                        break
                                    }
                                }

                                var newRecycleRequest = RecycleRequest(id,dateRequested!!,userRequestedObj!!,address!!,location!!,glassWeight!!,plasticWeight!!, metalWeight!!, paperWeight!!, acceptedCollectUserObj)
                                recycleRequestList.add(newRecycleRequest)
                            }

                            callback(true,null,recycleRequestList)

                        }else{
                            callback(false,message,null)
                        }
                    }
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