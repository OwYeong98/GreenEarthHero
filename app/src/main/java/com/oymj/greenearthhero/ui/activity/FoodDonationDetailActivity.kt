package com.oymj.greenearthhero.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.adapters.recyclerview.recycleritem.RecyclerItemClaimFood
import com.oymj.greenearthhero.adapters.recyclerview.recycleritem.RecyclerItemFoodEditableAmount
import com.oymj.greenearthhero.data.ClaimFood
import com.oymj.greenearthhero.data.Food
import com.oymj.greenearthhero.data.FoodDonation
import com.oymj.greenearthhero.ui.dialog.*
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_food_donation_detail.*
import java.lang.Exception
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FoodDonationDetailActivity : AppCompatActivity() {

    //Better control of onClickListener
    //all button action will be registered here
    private var myOnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            when (v) {
                btnBack->{
                    this@FoodDonationDetailActivity.onBackPressed()
                }
                btnClaim->{
                    var totalClaimFoodAmt = foodOfferedList.fold(0,{prev,obj-> prev + (obj as ClaimFood).claimAmount})

                    if(Date().after(foodDonationDetail.getDonationEndTime())){
                        var errorDialog = ErrorDialog(this@FoodDonationDetailActivity,"Offer Ended","Sorry this food donation is already ended! It will be closed soon unless owner extends it.")
                        errorDialog.show()
                    }else{
                        if(totalClaimFoodAmt > 0){
                            if(isOwner){
                                var loadingDialog = LoadingDialog(this@FoodDonationDetailActivity)
                                loadingDialog.show()
                                FirebaseFirestore.getInstance().runBatch {
                                        batch->
                                    for(claimFood in foodOfferedList){
                                        var claimFood = claimFood as ClaimFood

                                        if(claimFood.claimAmount >0){
                                            val docRef = FirebaseFirestore.getInstance().collection("Food_Donation/$currentViewingFoodDonationId/Food_List").document(claimFood.food.id)
                                            batch.update(docRef, "foodQuantity", claimFood.food.foodQuantity + claimFood.claimAmount)
                                        }
                                    }
                                }.addOnSuccessListener {
                                    loadingDialog.dismiss()
                                    for(claimFood in foodOfferedList){
                                        (claimFood as ClaimFood).claimAmount = 0
                                    }
                                    recyclerViewAdapter.notifyDataSetChanged()
                                    var successDialog = SuccessDialog(this@FoodDonationDetailActivity,"Successfully added amount of the Food","Thank for participating in reducing food waste.")
                                    successDialog.show()
                                }.addOnFailureListener {
                                        ex->
                                    loadingDialog.dismiss()
                                    var failureDialog = ErrorDialog(this@FoodDonationDetailActivity,"Error","We have encountered some error when connecting to Firebase! Please check ur internet connection.")
                                    failureDialog.show()
                                }
                            }else{
                                var loadingDialog = LoadingDialog(this@FoodDonationDetailActivity)
                                loadingDialog.show()
                                FirebaseFirestore.getInstance().runBatch {
                                        batch->
                                    for(claimFood in foodOfferedList){
                                        var claimFood = claimFood as ClaimFood

                                        if(claimFood.claimAmount >0){
                                            val docRef = FirebaseFirestore.getInstance().collection("Food_Donation/$currentViewingFoodDonationId/Food_List").document(claimFood.food.id)
                                            batch.update(docRef, "claimedFoodQuantity", claimFood.claimAmount + claimFood.food.claimedFoodQuantity)
                                        }
                                    }
                                }.addOnSuccessListener {
                                    loadingDialog.dismiss()
                                    for(claimFood in foodOfferedList){
                                        (claimFood as ClaimFood).claimAmount = 0
                                    }
                                    recyclerViewAdapter.notifyDataSetChanged()
                                    var successDialog = SuccessDialog(this@FoodDonationDetailActivity,"Successfully claimed the Food","Thank for participating in reducing food waste.")
                                    successDialog.show()
                                }.addOnFailureListener {
                                        ex->
                                    loadingDialog.dismiss()
                                    var failureDialog = ErrorDialog(this@FoodDonationDetailActivity,"Error","We have encountered some error when connecting to Firebase! Please check ur internet connection.")
                                    failureDialog.show()
                                }
                            }



                        }else{
                            var errorDialog = ErrorDialog(this@FoodDonationDetailActivity, "No Food selected","You should at least add 1 amount to any food")
                            errorDialog.show()
                        }
                    }
                }
                btnExtends->{
                    var dialog = ExtendsFoodDonationDialog(this@FoodDonationDetailActivity,callback = {
                        minutesAdded->

                        var newMinutes = foodDonationDetail.minutesAvailable + minutesAdded
                        FirebaseFirestore.getInstance().collection("Food_Donation").document(currentViewingFoodDonationId).update("minutesAvailable",newMinutes)
                            .addOnSuccessListener {
                                var successDialog = SuccessDialog(this@FoodDonationDetailActivity,"Successfully extended the hour","Your food donation will be available to the public!")
                                successDialog.show()
                            }.addOnFailureListener {
                                var failureDialog = ErrorDialog(this@FoodDonationDetailActivity,"Error","We have encountered some error when connecting to Firebase! Please check ur internet connection.")
                                failureDialog.show()
                            }
                    })
                    dialog.show()

                }
                btnEndDonation->{
                    var confirmDialog = YesOrNoDialog(this@FoodDonationDetailActivity,"Are you sure you want to end this donation?",callback = {
                        isYesPressed->

                        if(isYesPressed)
                            updateFirebaseDonationIsDone(foodDonationDetail)
                    })
                    confirmDialog.show()
                }
            }
        }
    }

    private lateinit var currentViewingFoodDonationId: String
    private lateinit var foodDonationDetail:FoodDonation

    private lateinit var recyclerViewAdapter: UniversalAdapter
    private var foodOfferedList = ArrayList<Any>()

    private lateinit var donationDetailListener: ListenerRegistration
    private lateinit var foodListListener: ListenerRegistration
    private var timeLeftUpdaterThread:Thread? = null
    private var isFirstTimeLoad:Boolean= true
    private var isFirstTimeLoadFoodList:Boolean= true
    private var isOwner:Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_donation_detail)

        currentViewingFoodDonationId = intent.getStringExtra("foodDonationId")

        linkAllButtonWithOnClickListener()
        setupUI()
    }

    private fun setupUI(){
        btnClaim.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.mapboxGreen),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            20f,0
        )

        btnEndDonation.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.colorAccent),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            20f,0
        )

        btnExtends.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            Color.parseColor("#EFA037"),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            20f,0
        )
    }

    private fun listenToDonationDetailChangesAndUpdateUI(){
        var db = FirebaseFirestore.getInstance()

        donationDetailListener = db.collection("Food_Donation").document(currentViewingFoodDonationId)
            .addSnapshotListener{
                snapshot,e->
            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null ) {
                var loadingDialog:LoadingDialog? = null
                if(isFirstTimeLoad){
                    loadingDialog = LoadingDialog(this@FoodDonationDetailActivity)
                    loadingDialog.show()
                }
                FoodDonation.getFoodDonationDetailByIdFromFirebase(currentViewingFoodDonationId,callback = {
                    success,message,foodDonation->
                    if(isFirstTimeLoad){
                        loadingDialog!!.dismiss()
                        isFirstTimeLoad=false
                    }
                    foodDonationDetail = foodDonation!!
                    isOwner = foodDonation?.donatorUser?.userId == FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this@FoodDonationDetailActivity)!!

                    runOnUiThread {
                        setupRecyclerView(isOwner)
                        listenToFoodListChangesAndUpdateUI()

                        if(isOwner){
                            btnClaim.text = "Edit Food Amount"
                            btnExtends.visibility = View.VISIBLE
                        }else{
                            btnClaim.text = "Claim Food"
                            btnExtends.visibility = View.GONE
                        }

                        //update the UI
                        tvRestaurantName.text = foodDonation?.location?.name
                        tvAddress.text = foodDonation?.location?.address
                    }

                    //end previous thread if there are already one
                    if (timeLeftUpdaterThread != null){
                        try{
                            timeLeftUpdaterThread!!.interrupt()
                        }catch (ex:Exception){

                        }
                    }

                    timeLeftUpdaterThread = Thread{
                        try{
                            var keepLoop= true
                            while (keepLoop){
                                var timeLeft = foodDonation?.getDonationEndTime()?.time!! - Date()?.time!!

                                if(timeLeft > 0){
                                    var hourLeft = Math.floor((timeLeft / 1000 / 60 / 60).toDouble()).toInt()
                                    var minutesLeft = Math.floor((timeLeft / 1000 / 60 % 60).toDouble()).toInt()
                                    var secondLeft = Math.floor((timeLeft / 1000 % 60).toDouble()).toInt()

                                    runOnUiThread {
                                        topContainer.setBackgroundColor(resources.getColor(R.color.slightdarkgreen))
                                        tvTimeLeft.text = "${DecimalFormat("00").format(hourLeft)}:${DecimalFormat("00").format(minutesLeft)}:${DecimalFormat("00").format(secondLeft)} until this offer ends"
                                    }
                                }else{
                                    runOnUiThread {
                                        topContainer.setBackgroundColor(resources.getColor(R.color.red))
                                        tvTimeLeft.text = "Sorry Offer had ended, This donation will be closed soon unless owner extend its"
                                    }
                                    keepLoop = false
                                }

                                Thread.sleep(1000)
                            }
                        }catch (ex:InterruptedException){

                        }

                    }
                    timeLeftUpdaterThread!!.start()

                })
            }
        }
    }

    private fun listenToFoodListChangesAndUpdateUI(){
        var db = FirebaseFirestore.getInstance()

        foodListListener = db.collection("Food_Donation/$currentViewingFoodDonationId/Food_List")
            .addSnapshotListener{
                    snapshot,e->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null ) {
                    var loadingDialog:LoadingDialog? = null
                    if(isFirstTimeLoadFoodList){
                        recyclerViewAdapter.startSkeletalLoading(1,UniversalAdapter.SKELETAL_TYPE_4)
                    }

                    Food.getFoodListOfFoodDonation(currentViewingFoodDonationId,callback = {
                        success,message,foodList->

                        if(isFirstTimeLoadFoodList){
                            recyclerViewAdapter.stopSkeletalLoading()
                            isFirstTimeLoadFoodList = false
                        }

                        if(success){
                            foodDonationDetail.foodList = foodList!!
                            for(food in foodList!!){

                                var foundFoodInList = foodOfferedList.fold(null as ClaimFood?, {prev,obj -> if((obj as ClaimFood).food.id == food.id) obj else prev })

                                //if found then update
                                if(foundFoodInList!=null){
                                    var availableFood = food.foodQuantity - food.claimedFoodQuantity
                                    if(isOwner){
                                        if(foundFoodInList.claimAmount * -1 > availableFood){
                                            foundFoodInList.claimAmount = availableFood *-1
                                        }
                                    }else{
                                        if(foundFoodInList.claimAmount > availableFood)
                                            foundFoodInList.claimAmount = availableFood
                                    }


                                    foundFoodInList.food = food
                                }else{
                                    foodOfferedList.add(ClaimFood(food,0))
                                }
                            }
                            recyclerViewAdapter.notifyDataSetChanged()
                        }else{
                            var errorDialog = ErrorDialog(this,"Error","We have encountered some error when contacting with firebase. Contact the developer")
                            errorDialog.show()
                        }
                    })
                }
            }
    }

    override fun onResume() {
        super.onResume()

        listenToDonationDetailChangesAndUpdateUI()
    }

    override fun onPause() {
        super.onPause()

        donationDetailListener.remove()
        foodListListener.remove()
        timeLeftUpdaterThread?.interrupt()
    }

    private fun setupRecyclerView(isOwner:Boolean){
        recyclerViewAdapter = object: UniversalAdapter(foodOfferedList,this@FoodDonationDetailActivity,foodOfferedRecyclerView){
            override fun getVerticalSpacing(): Int {
                //20px spacing
                return 20
            }
            override fun onItemClickedListener(data: Any, clickType:Int) {
                if(data is ClaimFood){

                }
            }

            override fun getItemViewType(position: Int): Int {
                return if(data.get(position)::class.java.simpleName == "ClaimFood"){
                    -1
                }else {
                    super.getItemViewType(position)
                }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                return if(viewType == -1 && isOwner){
                    RecyclerItemFoodEditableAmount().getViewHolder(parent,context,this)
                }else if (viewType == -1 && !isOwner){
                    RecyclerItemClaimFood().getViewHolder(parent,context,this)
                }else{
                    super.onCreateViewHolder(parent, viewType)
                }
            }
        }
        foodOfferedRecyclerView.layoutManager = LinearLayoutManager(this)
        foodOfferedRecyclerView.adapter = recyclerViewAdapter
    }

    private fun updateFirebaseDonationIsDone(data: FoodDonation){
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        dateFormat.timeZone = TimeZone.getTimeZone("GMT+8:00")
        val currentDateTime: String = dateFormat.format(Date()) // Find todays date


        //create a firebase document
        val donationHistoryDocument = hashMapOf(
            "date_started" to dateFormat.format(data.datePosted),
            "date_ended" to currentDateTime,
            "donateLocation" to data.location,
            "donatorUser" to data.donatorUser,
            "minutesAvailable" to data.minutesAvailable,
            "totalFoodAmount" to data.totalFoodAmount
        )

        var loadingDialog = LoadingDialog(this)
        loadingDialog.show()

        //stop listener
        foodListListener.remove()
        donationDetailListener.remove()

        FirebaseFirestore.getInstance().collection("Food_Donation_History").add(donationHistoryDocument)
            .addOnSuccessListener { docRef ->

                var docId = docRef.id

                FirebaseFirestore.getInstance().runBatch { batch ->
                    //add food to subcollection
                    for (index in data.foodList.indices) {
                        if (data.foodList[index] is Food) {
                            var food = data.foodList[index] as Food

                            val foodImageData = hashMapOf(
                                "foodName" to food.foodName,
                                "foodDesc" to food.foodDesc,
                                "foodQuantity" to food.foodQuantity,
                                "claimedFoodQuantity" to food.claimedFoodQuantity,
                                "imageUrl" to food.imageUrl
                            )

                            val docRef = FirebaseFirestore.getInstance()
                                .collection("Food_Donation_History/$docId/Food_List").document()
                            batch.set(docRef, foodImageData)
                        }
                    }

                }.addOnSuccessListener {
                    //remove the food donation in Food_Donation

                    FirebaseFirestore.getInstance().runBatch { batch ->
                        for(food in data.foodList){
                            val docRef = FirebaseFirestore.getInstance().collection("Food_Donation/${data.id}/Food_List").document(food.id)
                            batch.delete(docRef)
                        }

                    }.addOnSuccessListener {
                        FirebaseFirestore.getInstance().collection("Food_Donation").document(data.id)
                            .delete()
                            .addOnSuccessListener {
                                loadingDialog.dismiss()
                                var successDialog = SuccessDialog(
                                    this@FoodDonationDetailActivity,
                                    "Successfully End the donation",
                                    "The donation is now ended, It will not be available to the public for claiming.",
                                    callback = {
                                        finish()
                                    }
                                )
                                successDialog.show()
                            }
                            .addOnFailureListener {
                                loadingDialog.dismiss()
                                var failureDialog = ErrorDialog(
                                    this@FoodDonationDetailActivity,
                                    "Error",
                                    "We have encountered some error when connecting to Firebase! Please check ur internet connection."
                                )
                                failureDialog.show()
                            }
                    }.addOnFailureListener {
                        loadingDialog.dismiss()
                        var failureDialog = ErrorDialog(
                            this@FoodDonationDetailActivity,
                            "Error",
                            "We have encountered some error when connecting to Firebase! Please check ur internet connection."
                        )
                        failureDialog.show()
                    }




                }.addOnFailureListener { ex ->
                    loadingDialog.dismiss()
                    var failureDialog = ErrorDialog(
                        this@FoodDonationDetailActivity,
                        "Error",
                        "We have encountered some error when connecting to Firebase! Please check ur internet connection."
                    )
                    failureDialog.show()
                }
            }.addOnFailureListener {
                loadingDialog.dismiss()
                var failureDialog = ErrorDialog(this@FoodDonationDetailActivity,
                    "Error",
                    "We have encountered some error when connecting to Firebase! Please check ur internet connection."
                )
                failureDialog.show()
            }
    }

    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            btnBack,
            btnClaim,
            btnExtends,
            btnEndDonation
        )

        for (view in actionButtonViewList) {
            view.setOnClickListener(myOnClickListener)
        }
    }

    private fun showBackToListButton(){
        val slideUpAnimation: Animation = AnimationUtils.loadAnimation(applicationContext,
            R.anim.slide_up
        )
        btnBack.visibility=View.VISIBLE
        btnBack.startAnimation(slideUpAnimation)

    }

    private fun hideBackToListButton(callback:()->Unit){
        val slideDownAnimation: Animation = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_down)
        slideDownAnimation.setAnimationListener(object: Animation.AnimationListener{
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                btnBack.visibility=View.GONE
                callback()
            }

            override fun onAnimationStart(animation: Animation?) {
            }
        })

        btnBack.startAnimation(slideDownAnimation)

    }

    override fun onEnterAnimationComplete() {
        super.onEnterAnimationComplete()
        showBackToListButton()
    }

    override fun onBackPressed() {
        hideBackToListButton {
            super.onBackPressed()
            overridePendingTransition(R.anim.freeze, R.anim.slide_down_slow)
        }
    }

}