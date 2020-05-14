package com.oymj.greenearthhero.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SecondHandItem(
    var id:String,
    var itemName:String,
    var itemDesc:String,
    var itemPrice:Double,
    var imageUrl:String,
    var datePosted: Date,
    var postedByUser: User,
    var boughtByUser: User?,
    var deliveryLocation:Location?,
    var courierCompany: String?,
    var trackingNo:String?
) {

    companion object{

        fun getItemOnSaleListFromFirebase(callback:(Boolean,String?,ArrayList<SecondHandItem>?)->Unit){
            var itemList = ArrayList<SecondHandItem>()
            FirebaseFirestore.getInstance().collection("Second_Hand_Item").whereEqualTo("boughtBy","").get()
                .addOnSuccessListener {
                        itemSnapshot->

                    GlobalScope.launch {
                        for(item in itemSnapshot!!){
                            var id = item.id
                            var datePosted = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(item.getString("datePosted"))
                            var itemName = item.getString("itemName")!!
                            var itemDesc = item.getString("itemDesc")!!
                            var itemPrice = item.getLong("itemPrice")!!.toDouble()
                            var imageUrl = item.getString("imageUrl")!!
                            var postedByUser  = User.suspendGetSpecificUserFromFirebase(item.getString("postedBy")!!)
                            var boughtByUser = if(item.getString("boughtBy")!="") User.suspendGetSpecificUserFromFirebase(item.getString("boughtBy")!!) else null
                            var deliveryLocation = if(item.getString("deliveryLocationId")!="")Location.suspendGetLocationById(item.getString("deliveryLocationId")!!) else null
                            var courierCompany = item.getString("courierCompany")
                            var trackingNo = item.getString("trackingNo")


                            var newItem = SecondHandItem(id,itemName!!,itemDesc!!,itemPrice!!,imageUrl!!,datePosted,postedByUser!!,boughtByUser,deliveryLocation,courierCompany,trackingNo)

                            itemList.add(newItem)
                        }

                        callback(true,null,itemList)
                    }
                }
                .addOnFailureListener {
                        ex->
                    callback(false,ex.toString(),null)
                }
        }

        fun getItemListOnSaleOfUserFromFirebase(userId:String,callback:(Boolean,String?,ArrayList<SecondHandItem>?)->Unit){
            var itemList = ArrayList<SecondHandItem>()
            FirebaseFirestore.getInstance().collection("Second_Hand_Item").whereEqualTo("postedBy",userId).get()
                .addOnSuccessListener {
                        itemSnapshot->

                    GlobalScope.launch {
                        for(item in itemSnapshot!!){
                            var id = item.id
                            var datePosted = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(item.getString("datePosted"))
                            var itemName = item.getString("itemName")!!
                            var itemDesc = item.getString("itemDesc")!!
                            var itemPrice = item.getLong("itemPrice")!!.toDouble()
                            var imageUrl = item.getString("imageUrl")!!
                            var postedByUser  = User.suspendGetSpecificUserFromFirebase(item.getString("postedBy")!!)
                            var boughtByUser = if(item.getString("boughtBy")!="") User.suspendGetSpecificUserFromFirebase(item.getString("boughtBy")!!) else null
                            var deliveryLocation = if(item.getString("deliveryLocationId")!="")Location.suspendGetLocationById(item.getString("deliveryLocationId")!!) else null
                            var courierCompany = item.getString("courierCompany")
                            var trackingNo = item.getString("trackingNo")


                            var newItem = SecondHandItem(id,itemName!!,itemDesc!!,itemPrice!!,imageUrl!!,datePosted,postedByUser!!,boughtByUser,deliveryLocation,courierCompany,trackingNo)

                            itemList.add(newItem)
                        }

                        callback(true,null,itemList)
                    }
                }
                .addOnFailureListener {
                        ex->
                    callback(false,ex.toString(),null)
                }
        }

        fun getItemListBoughtByUserFromFirebase(userId:String,callback:(Boolean,String?,ArrayList<SecondHandItem>?)->Unit){
            var itemList = ArrayList<SecondHandItem>()
            FirebaseFirestore.getInstance().collection("Second_Hand_Item").whereEqualTo("boughtBy",userId).get()
                .addOnSuccessListener {
                        itemSnapshot->

                    GlobalScope.launch {
                        for(item in itemSnapshot!!){
                            var id = item.id
                            var datePosted = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(item.getString("datePosted"))
                            var itemName = item.getString("itemName")!!
                            var itemDesc = item.getString("itemDesc")!!
                            var itemPrice = item.getLong("itemPrice")!!.toDouble()
                            var imageUrl = item.getString("imageUrl")!!
                            var postedByUser  = User.suspendGetSpecificUserFromFirebase(item.getString("postedBy")!!)
                            var boughtByUser = if(item.getString("boughtBy")!="") User.suspendGetSpecificUserFromFirebase(item.getString("boughtBy")!!) else null
                            var deliveryLocation = if(item.getString("deliveryLocationId")!="")Location.suspendGetLocationById(item.getString("deliveryLocationId")!!) else null
                            var courierCompany = item.getString("courierCompany")
                            var trackingNo = item.getString("trackingNo")


                            var newItem = SecondHandItem(id,itemName!!,itemDesc!!,itemPrice!!,imageUrl!!,datePosted,postedByUser!!,boughtByUser,deliveryLocation,courierCompany,trackingNo)

                            itemList.add(newItem)
                        }

                        callback(true,null,itemList)
                    }
                }
                .addOnFailureListener {
                        ex->
                    callback(false,ex.toString(),null)
                }
        }

        fun getItemByIdFromFirebase(itemId:String,callback:(Boolean,String?,SecondHandItem?)->Unit){
            FirebaseFirestore.getInstance().collection("Second_Hand_Item").document(itemId).get()
                .addOnSuccessListener {
                        itemSnapshot->

                    if(itemSnapshot.exists()){
                        GlobalScope.launch {
                            var item = itemSnapshot
                            var id = item.id
                            var datePosted = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(item.getString("datePosted"))
                            var itemName = item.getString("itemName")!!
                            var itemDesc = item.getString("itemDesc")!!
                            var itemPrice = item.getLong("itemPrice")!!.toDouble()
                            var imageUrl = item.getString("imageUrl")!!
                            var postedByUser  = User.suspendGetSpecificUserFromFirebase(item.getString("postedBy")!!)
                            var boughtByUser = if(item.getString("boughtBy")!="") User.suspendGetSpecificUserFromFirebase(item.getString("boughtBy")!!) else null
                            var deliveryLocation = if(item.getString("deliveryLocationId")!="")Location.suspendGetLocationById(item.getString("deliveryLocationId")!!) else null
                            var courierCompany = item.getString("courierCompany")
                            var trackingNo = item.getString("trackingNo")


                            var newItem = SecondHandItem(id,itemName!!,itemDesc!!,itemPrice!!,imageUrl!!,datePosted,postedByUser!!,boughtByUser,deliveryLocation,courierCompany,trackingNo)

                            callback(true,null,newItem)
                        }
                    }else{
                        callback(false,"Item not exist!",null)
                    }

                }
                .addOnFailureListener {
                        ex->
                    callback(false,ex.toString(),null)
                }
        }
    }
}