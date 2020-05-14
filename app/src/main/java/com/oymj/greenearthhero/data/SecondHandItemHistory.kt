package com.oymj.greenearthhero.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SecondHandItemHistory(
    var id:String,
    var date: Date,
    var userBought:User,
    var userPosted:User,
    var deliveryLocation:Location,
    var itemName:String,
    var itemDesc:String,
    var itemPrice:Double,
    var imageUrl:String,
    var trackingNo:String,
    var courierCompany:String
){
    companion object{
        fun getItemSaleHistoryOfUserFromFirebase(userId:String,callback:(Boolean,String?,ArrayList<SecondHandItemHistory>?)->Unit){
            var itemList = ArrayList<SecondHandItemHistory>()
            FirebaseFirestore.getInstance().collection("Second_Hand_Item_History").whereEqualTo("user_posted.userId",userId).get()
                .addOnSuccessListener {
                        itemSnapshot->

                    GlobalScope.launch {
                        for(item in itemSnapshot!!){
                            var id = item.id
                            var datePosted = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(item.getString("date"))
                            var itemName = item.getString("itemName")!!
                            var itemDesc = item.getString("itemDesc")!!
                            var itemPrice = item.getLong("itemPrice")!!.toDouble()
                            var imageUrl = item.getString("imageUrl")!!
                            var courierCompany = item.getString("courierCompany")
                            var trackingNo = item.getString("trackingNo")

                            var userBoughtData = item.get("user_bought") as HashMap<String,String>
                            var userBoughtObj = User(userBoughtData["userId"]!!,userBoughtData["email"]!!,userBoughtData["firstName"]!!
                                ,userBoughtData["lastName"]!!,userBoughtData["phone"]!!,(userBoughtData["dateOfBirth"] as Timestamp).toDate())

                            var userPostedData = item.get("user_posted") as HashMap<String,String>
                            var userPostedObj = User(userPostedData["userId"]!!,userPostedData["email"]!!,userPostedData["firstName"]!!
                                ,userPostedData["lastName"]!!,userPostedData["phone"]!!,(userPostedData["dateOfBirth"] as Timestamp).toDate())

                            var deliveryLocationData = item.get("delivery_location") as HashMap<String,String>
                            var positionData = item.get("delivery_location.location") as HashMap<String,Double>

                            var location = TomTomPosition()
                            location.lat = positionData["lat"]!!
                            location.lon = positionData["lon"]!!

                            var deliveryLocationObj = Location(deliveryLocationData["id"]!!,deliveryLocationData["userId"]!!,deliveryLocationData["name"]!!
                                ,deliveryLocationData["address"]!!,location)


                            var newItemHistory = SecondHandItemHistory(id,datePosted,userBoughtObj,userPostedObj,deliveryLocationObj,itemName,itemDesc,itemPrice,imageUrl,trackingNo!!,courierCompany!!)

                            itemList.add(newItemHistory)
                        }

                        callback(true,null,itemList)
                    }
                }
                .addOnFailureListener {
                        ex->
                    callback(false,ex.toString(),null)
                }
        }

        fun getPurchaseHistoryOfUserFromFirebase(userId:String,callback:(Boolean,String?,ArrayList<SecondHandItemHistory>?)->Unit){
            var itemList = ArrayList<SecondHandItemHistory>()
            FirebaseFirestore.getInstance().collection("Second_Hand_Item_History").whereEqualTo("user_bought.userId",userId).get()
                .addOnSuccessListener {
                        itemSnapshot->

                    GlobalScope.launch {
                        for(item in itemSnapshot!!){
                            var id = item.id
                            var datePosted = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(item.getString("date"))
                            var itemName = item.getString("itemName")!!
                            var itemDesc = item.getString("itemDesc")!!
                            var itemPrice = item.getLong("itemPrice")!!.toDouble()
                            var imageUrl = item.getString("imageUrl")!!
                            var courierCompany = item.getString("courierCompany")
                            var trackingNo = item.getString("trackingNo")

                            var userBoughtData = item.get("user_bought") as HashMap<String,String>
                            var userBoughtObj = User(userBoughtData["userId"]!!,userBoughtData["email"]!!,userBoughtData["firstName"]!!
                                ,userBoughtData["lastName"]!!,userBoughtData["phone"]!!,(userBoughtData["dateOfBirth"] as Timestamp).toDate())

                            var userPostedData = item.get("user_posted") as HashMap<String,String>
                            var userPostedObj = User(userPostedData["userId"]!!,userPostedData["email"]!!,userPostedData["firstName"]!!
                                ,userPostedData["lastName"]!!,userPostedData["phone"]!!,(userPostedData["dateOfBirth"] as Timestamp).toDate())

                            var deliveryLocationData = item.get("delivery_location") as HashMap<String,String>
                            var positionData = item.get("delivery_location.location") as HashMap<String,Double>

                            var location = TomTomPosition()
                            location.lat = positionData["lat"]!!
                            location.lon = positionData["lon"]!!

                            var deliveryLocationObj = Location(deliveryLocationData["id"]!!,deliveryLocationData["userId"]!!,deliveryLocationData["name"]!!
                                ,deliveryLocationData["address"]!!,location)


                            var newItemHistory = SecondHandItemHistory(id,datePosted,userBoughtObj,userPostedObj,deliveryLocationObj,itemName,itemDesc,itemPrice,imageUrl,trackingNo!!,courierCompany!!)

                            itemList.add(newItemHistory)
                        }

                        callback(true,null,itemList)
                    }
                }
                .addOnFailureListener {
                        ex->
                    callback(false,ex.toString(),null)
                }
        }
    }
}