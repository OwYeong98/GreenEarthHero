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
    var imageUrl:String,
    var imageBitmap: Bitmap?
) {

    companion object{

        suspend fun suspendGetFoodList(foodDonationId:String): ArrayList<Food> {
            var foodListSnapshot = FirebaseFirestore.getInstance().collection("Food_Donation/$foodDonationId/Food_List").get().await()

            var foodList = ArrayList<Food>()
            for(food in foodListSnapshot){
                var id = food.id
                var foodDesc = food.getString("foodDesc")
                var foodName = food.getString("foodName")
                var foodQty = food.getLong("foodQuantity")?.toInt()
                var imgUrl = food.getString("imageUrl")

                var newFood = Food(id,foodName!!,foodDesc!!,foodQty!!,imgUrl!!,null)

                foodList.add(newFood)
            }

            return foodList
        }
    }



}