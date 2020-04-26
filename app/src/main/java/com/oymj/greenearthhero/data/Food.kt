package com.oymj.greenearthhero.data

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.ArrayList

class Food(
    var id:String,
    var foodName:String,
    var foodDesc:String,
    var foodQuantity:Int,
    var claimedFoodQuantity:Int,
    var imageUrl:String,
    var imageBitmap: Bitmap?
) {

    companion object{

        fun getFoodListOfFoodDonation(foodDonationId:String, callback:(Boolean,String?, ArrayList<Food>?)->Unit) {

            var foodListSnapshot = FirebaseFirestore.getInstance().collection("Food_Donation/$foodDonationId/Food_List").get()
                .addOnSuccessListener {
                    foodListSnapshot->

                    var foodList = ArrayList<Food>()
                    for(food in foodListSnapshot){
                        var id = food.id
                        var foodDesc = food.getString("foodDesc")
                        var foodName = food.getString("foodName")
                        var foodQty = food.getLong("foodQuantity")?.toInt()
                        var claimedFoodQty = food.getLong("claimedFoodQuantity")?.toInt()
                        var imgUrl = food.getString("imageUrl")

                        var newFood = Food(id,foodName!!,foodDesc!!,foodQty!!,claimedFoodQty!!,imgUrl!!,null)

                        foodList.add(newFood)
                    }

                    callback(true,null,foodList)
                }
                .addOnFailureListener {
                    ex->
                    callback(false,ex.toString(),null)

                }
        }

        suspend fun suspendGetFoodList(foodDonationId:String): ArrayList<Food> {
            var foodListSnapshot = FirebaseFirestore.getInstance().collection("Food_Donation/$foodDonationId/Food_List").get().await()

            var foodList = ArrayList<Food>()
            for(food in foodListSnapshot){
                var id = food.id
                var foodDesc = food.getString("foodDesc")
                var foodName = food.getString("foodName")
                var foodQty = food.getLong("foodQuantity")?.toInt()
                var claimedFoodQty = food.getLong("claimedFoodQuantity")?.toInt()
                var imgUrl = food.getString("imageUrl")

                var newFood = Food(id,foodName!!,foodDesc!!,foodQty!!,claimedFoodQty!!,imgUrl!!,null)

                foodList.add(newFood)
            }

            return foodList
        }
    }



}