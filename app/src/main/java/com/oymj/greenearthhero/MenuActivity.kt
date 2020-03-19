package com.oymj.greenearthhero

import android.animation.Animator
import android.content.Intent
import android.content.res.Resources
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
import com.oymj.greenearthhero.Utils.LottieUtils
import kotlinx.android.synthetic.main.activity_menu.*


class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.freeze, R.anim.freeze)
        setContentView(R.layout.activity_menu)

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

        menu_cross_icon.setOnClickListener(object : View.OnClickListener {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onClick(v: View?) {
                revertCircularRevealActivity()
            }
        })

        menu_app_info_icon.setOnClickListener (object : View.OnClickListener {

            override fun onClick(v: View?) {
                var intent = Intent(this@MenuActivity, IntroActivity::class.java)
                startActivity(intent)
            }
        })


    }

    private fun animateMenuIcon(){
        val fadeInAnimation: Animation = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in)

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
            menu_request_icon,
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
         */
        val cx: Int = menu_bg.right
        val cy: Int = menu_bg.bottom
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
        val finalRadius: Float = Math.max(menu_bg.width, menu_bg.height).toFloat()
        val circularReveal =
            ViewAnimationUtils.createCircularReveal(menu_bg, menu_bg.right, menu_bg.bottom, finalRadius, 0f)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            revertCircularRevealActivity()
        } else {
            super.onBackPressed()
        }
    }

}
