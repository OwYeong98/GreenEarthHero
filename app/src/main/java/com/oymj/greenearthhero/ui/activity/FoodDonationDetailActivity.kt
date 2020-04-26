package com.oymj.greenearthhero.ui.activity

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.data.ClaimFood
import com.oymj.greenearthhero.data.Food
import com.oymj.greenearthhero.data.FoodDonation
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.ui.dialog.LoadingDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import kotlinx.android.synthetic.main.activity_food_donation_detail.*
import java.lang.Exception
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.ThreadPoolExecutor
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

                }

            }
        }
    }

    private lateinit var currentViewingFoodDonationId: String

    private lateinit var recyclerViewAdapter: UniversalAdapter
    private var foodOfferedList = ArrayList<Any>()

    private lateinit var donationDetailListener: ListenerRegistration
    private lateinit var foodListListener: ListenerRegistration
    private var timeLeftUpdaterThread:Thread? = null
    private var isFirstTimeLoad:Boolean= true
    private var isFirstTimeLoadFoodList:Boolean= true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_donation_detail)

        currentViewingFoodDonationId = intent.getStringExtra("foodDonationId")

        linkAllButtonWithOnClickListener()
        setupRecyclerView()

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

                    //update the UI
                    tvRestaurantName.text = foodDonation?.donateLocation?.name
                    tvAddress.text = foodDonation?.donateLocation?.address

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

                        if(isFirstTimeLoad){
                            recyclerViewAdapter.stopSkeletalLoading()
                            isFirstTimeLoadFoodList = false
                        }

                        if(success){
                            for(food in foodList!!){

                                var foundFoodInList = foodOfferedList.fold(null as ClaimFood?, {prev,obj -> if((obj as ClaimFood).food.id == food.id) obj else prev })

                                //if found then update
                                if(foundFoodInList!=null){
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
        listenToFoodListChangesAndUpdateUI()
    }

    override fun onPause() {
        super.onPause()

        donationDetailListener.remove()
        foodListListener.remove()
        timeLeftUpdaterThread?.interrupt()
    }

    private fun setupRecyclerView(){
        recyclerViewAdapter = object: UniversalAdapter(foodOfferedList,this@FoodDonationDetailActivity,foodOfferedRecyclerView){
            override fun getVerticalSpacing(): Int {
                //20px spacing
                return 20
            }
            override fun onItemClickedListener(data: Any, clickType:Int) {
                if(data is ClaimFood){

                }
            }
        }
        foodOfferedRecyclerView.layoutManager = LinearLayoutManager(this)
        foodOfferedRecyclerView.adapter = recyclerViewAdapter
    }

    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            btnBack,
            btnClaim
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