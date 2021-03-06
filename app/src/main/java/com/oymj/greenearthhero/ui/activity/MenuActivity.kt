package com.oymj.greenearthhero.ui.activity

import android.animation.Animator
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.data.FoodDonationHistory
import com.oymj.greenearthhero.data.Notification
import com.oymj.greenearthhero.data.RecycleRequestHistory
import com.oymj.greenearthhero.data.SecondHandItemHistory
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.ui.dialog.SuccessDialog
import com.oymj.greenearthhero.ui.dialog.YesOrNoDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.utils.LottieUtils
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_menu.*


class MenuActivity : AppCompatActivity() {

    private var isFromLogin = false

    //Better control of onClickListener
    //all button action will be registered here
    private var myOnClickListener = object: View.OnClickListener {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onClick(v: View?) {
            when (v) {
                menu_cross_icon -> {
                    this@MenuActivity.onBackPressed()
                }
                menu_notification_icon -> {
                    var intent = Intent(this@MenuActivity, NotificationActivity::class.java)
                    startActivity(intent)
                }
                menu_chat_icon -> {
                    if(!isFromLogin){
                        finish()
                    }
                    var intent = Intent(this@MenuActivity, MyChatActivity::class.java)
                    startActivity(intent)
                }
                menu_profile_icon -> {
                    if(!isFromLogin){
                        finish()
                    }
                    var intent = Intent(this@MenuActivity, ProfileActivity::class.java)
                    intent.putExtra("isFromMenuActivity",true)
                    intent.putExtra("userId","-1")
                    startActivity(intent)

                }
                menu_recycle_icon -> {
                    if(!isFromLogin){
                        finish()
                    }
                    var intent = Intent(this@MenuActivity, RecycleActivity::class.java)
                    startActivity(intent)

                }
                menu_food_donation_icon -> {
                    if(!isFromLogin){
                        finish()
                    }
                    var intent = Intent(this@MenuActivity, FoodDonationActivity::class.java)
                    startActivity(intent)
                }
                menu_good_selling_icon -> {
                    if(!isFromLogin){
                        finish()
                    }
                    var intent = Intent(this@MenuActivity, SecondHandPlatformActivity::class.java)
                    startActivity(intent)
                }
                menu_app_info_icon -> {
                    var intent = Intent(this@MenuActivity, IntroActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(
            R.anim.freeze,
            R.anim.freeze
        )
        setContentView(R.layout.activity_menu)

        isFromLogin = intent.getBooleanExtra("callFromLogin",false)

        linkAllButtonWithOnClickListener()
        if (savedInstanceState == null) {
            val viewTreeObserver: ViewTreeObserver = menu_bg.getViewTreeObserver()
            if (viewTreeObserver.isAlive) {
                viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                    override fun onGlobalLayout() {
                        circularRevealActivity()
                        menu_bg.getViewTreeObserver().removeOnGlobalLayoutListener(this)
                    }
                })
            }
        }
        animateMenuIcon()
        setupUI()
        calculateAchievementOnRecycleReuseAndReduce()

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onResume() {
        super.onResume()
        overridePendingTransition(
            R.anim.freeze,
            R.anim.freeze
        )

        menu_bg.post {
            runOnUiThread{
                circularRevealActivity()
            }
        }

        animateMenuIcon()

        getNumOfNotification()
    }

    private fun setupUI(){
        numOfNotification.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.red),
            resources.getColor(R.color.red),
            resources.getColor(R.color.transparent),
            200f,0)
    }
    private fun getNumOfNotification(){
        Notification.getNumOfNotificationOfUser(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this)!!){
            success,message,numberOfNotification->

            if(success){
                if(numberOfNotification!!>0){
                    numOfNotification.visibility=View.VISIBLE
                    numOfNotification.text = numberOfNotification.toString()
                }else{
                    numOfNotification.visibility=View.GONE
                }
            }
        }
    }

    private fun calculateAchievementOnRecycleReuseAndReduce(){

        FoodDonationHistory.getFoodDonationHistoryListByUserFromFirebase(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this)!!,callback = {
                success,message,data->
            if(success){
                var totalFoodDonated = data!!.fold(0,{prev,obj-> prev + obj.totalFoodAmount})

                runOnUiThread{
                    loading_reduced.visibility = View.GONE
                    reduced_icon.visibility = View.VISIBLE
                    reducedAmount.text = "Reduced\n$totalFoodDonated meals"
                }
            }
        })

        RecycleRequestHistory.getSpecificUserRecycleRequestHistoryFromFirebase(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this)!!,callback = {
                success, message, data ->
            if(success){
                var totalRecycled = data!!.fold(0,{prev,obj-> prev + obj.getTotalAmount()})

                runOnUiThread{
                    loading_recycled.visibility = View.GONE
                    recyled_icon.visibility = View.VISIBLE
                    recycledAmount.text = "Recycled\n$totalRecycled Kg"
                }
            }
        })


        SecondHandItemHistory.getItemSaleHistoryOfUserFromFirebase(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this)!!,callback = {
                success, message, data ->
            SecondHandItemHistory.getPurchaseHistoryOfUserFromFirebase(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this)!!,callback = {
                    innerSuccess, innerMessage, innerData ->

                if(success && innerSuccess){
                    var totalItemSale = data!!.size + innerData!!.size

                    runOnUiThread{
                        loading_reused.visibility = View.GONE
                        reused_icon.visibility = View.VISIBLE
                        reusedAmount.text = "Reused\n$totalItemSale items"
                    }
                }
            })
        })


    }

    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            menu_cross_icon,
            menu_notification_icon,
            menu_chat_icon,
            menu_profile_icon,
            menu_recycle_icon,
            menu_food_donation_icon,
            menu_good_selling_icon,
            menu_app_info_icon
        )

        for (view in actionButtonViewList) {
            view.setOnClickListener(myOnClickListener)
        }
    }


    private fun animateMenuIcon(){
        val fadeInAnimation: Animation = AnimationUtils.loadAnimation(applicationContext,
            R.anim.fade_in
        )

        val fadeInIcon = listOf(
            menu_cross_icon,
            menu_notification_icon,
            menu_chat_icon,
            menu_profile_icon
        )
        val lottieAnimationIcon = listOf(
            menu_recycle_icon,
            menu_food_donation_icon,
            menu_good_selling_icon,
            menu_app_info_icon)
        var animationDelayPreset: Long = 500

        //start lottie animation
        for (lottieView in lottieAnimationIcon){
            LottieUtils.startAnimationWithDelay(lottieView, animationDelayPreset)
            animationDelayPreset += 100
        }

        //fade in other icon
        for (view in fadeInIcon) {
            view.startAnimation(fadeInAnimation)
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun circularRevealActivity() {
        /**
         * Tips:
         * cx and cy identify the anchor point of the reveal circle
         * Final radius identify the reveal circle final size
         *
         * NOTE: we are try to get cx and cy in the center of our menu icon
         * 16 identify the margin of our menu icon
         * 56/2 identify the radius of our floating action button(floating action button has a default size of 56dp)
         */
        val cx: Int = menu_bg.left + getDips(16) + getDips(56/2)
        val cy: Int = menu_bg.top + getDips(16) + getDips(56/2)
        val finalRadius: Float = Math.max(menu_bg.width, menu_bg.height).toFloat()

        val circularReveal = ViewAnimationUtils.createCircularReveal(
            menu_bg,
            cx,
            cy, 0f,
            finalRadius
        )
        circularReveal.duration = 1000
        menu_bg.setVisibility(View.VISIBLE)
        circularReveal.start()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun revertCircularRevealActivity() {
        val cx: Int = menu_bg.left + getDips(16) + getDips(56/2)
        val cy: Int = menu_bg.top + getDips(16) + getDips(56/2)
        val finalRadius: Float = Math.max(menu_bg.width, menu_bg.height).toFloat()
        val circularReveal =
            ViewAnimationUtils.createCircularReveal(menu_bg, cx, cy, finalRadius, 0f)
        circularReveal.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}
            override fun onAnimationEnd(animator: Animator) {
                menu_bg.setVisibility(View.INVISIBLE)
                finish()
            }

            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
        circularReveal.duration = 1000
        circularReveal.start()
    }
    /**
     * @function getDips Convert dp to calculated pixels according to their device's dpi
     * @param dps required dp
     */
    private fun getDips(dps: Int): Int {
        val resources: Resources = resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dps.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    override fun onBackPressed() {
        if(isFromLogin){
            var confirmDialog = YesOrNoDialog(this,"Are you sure you want to Logout?", callback = {
                    isYesPressed->

                if (isYesPressed == true){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        revertCircularRevealActivity()
                    } else {
                        super.onBackPressed()
                    }
                }
            })
            confirmDialog.show()
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                revertCircularRevealActivity()
            } else {
                super.onBackPressed()
            }
        }

    }

}
