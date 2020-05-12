package com.oymj.greenearthhero.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.Serializable
import java.util.ArrayList

class Location(
    var id:String,
    var userId:String,
    var name:String,
    var address:String,
    var location: TomTomPosition) : Serializable {

    companion object{

        fun getLocationList( callback: (Boolean, String?, ArrayList<Location>?)-> Unit){
            FirebaseFirestore.getInstance().collection("Location").get()
                .addOnSuccessListener {
                        donateLocSnapshot->

                    var donateLocationList = ArrayList<Location>()

                    for(donateLocation in donateLocSnapshot){
                        var id = donateLocation.id
                        var name = donateLocation.getString("name")
                        var userId = donateLocation.getString("userId")
                        var address = donateLocation.getString("address")
                        var location = donateLocation.getGeoPoint("location")

                        var tomTomPosition = TomTomPosition()
                        tomTomPosition.lat = location?.latitude!!
                        tomTomPosition.lon = location?.longitude!!

                        var newDonateLocation = Location(id,userId!!,name!!,address!!,tomTomPosition)

                        donateLocationList.add(newDonateLocation)
                    }

                    callback(true,null,donateLocationList)

                }
                .addOnFailureListener {
                        ex->
                    callback(true,ex.toString(),null)
                }
        }

        fun getLocationListOfUser(userId:String, callback: (Boolean, String?, ArrayList<Location>?)-> Unit){
            FirebaseFirestore.getInstance().collection("Location").whereEqualTo("userId",userId).get()
                .addOnSuccessListener {
                        donateLocSnapshot->

                    var donateLocationList = ArrayList<Location>()

                    for(donateLocation in donateLocSnapshot){
                        var id = donateLocation.id
                        var name = donateLocation.getString("name")
                        var userId = donateLocation.getString("userId")
                        var address = donateLocation.getString("address")
                        var location = donateLocation.getGeoPoint("location")

                        var tomTomPosition = TomTomPosition()
                        tomTomPosition.lat = location?.latitude!!
                        tomTomPosition.lon = location?.longitude!!

                        var newDonateLocation = Location(id,userId!!,name!!,address!!,tomTomPosition)

                        donateLocationList.add(newDonateLocation)
                    }

                    callback(true,null,donateLocationList)

                }
                .addOnFailureListener {
                        ex->
                    callback(true,ex.toString(),null)
                }
        }

        suspend fun getLocationById(donationLocationId:String): Location? {
            var donateLocationSnapshot = FirebaseFirestore.getInstance().collection("Location").document(donationLocationId).get().await()

            var id = donateLocationSnapshot.id
            var name = donateLocationSnapshot.getString("name")
            var userId = donateLocationSnapshot.getString("userId")
            var address = donateLocationSnapshot.getString("address")
            var location = donateLocationSnapshot.getGeoPoint("location")

            var tomTomPosition = TomTomPosition()
            tomTomPosition.lat = location?.latitude!!
            tomTomPosition.lon = location?.longitude!!

            var foundDonateLocation = Location(id,userId!!,name!!,address!!,tomTomPosition)


            return foundDonateLocation!!
        }

    }
}