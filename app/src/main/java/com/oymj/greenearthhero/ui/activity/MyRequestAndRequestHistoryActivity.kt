package com.oymj.greenearthhero.ui.activity

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.viewpagers.MyRequestAndRequestHistoryViewPagerAdapter
import kotlinx.android.synthetic.main.activity_my_request_and_request_history.*

class MyRequestAndRequestHistoryActivity : AppCompatActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_request_and_request_history)

        btnBackToHome.setOnClickListener{
            finish()
            overridePendingTransition(R.anim.freeze,R.anim.slide_down_slow)
        }

        setupViewPagerAndTabLayout()
    }

    override fun onEnterAnimationComplete() {
        super.onEnterAnimationComplete()

        Handler().postDelayed({
            runOnUiThread {
                showBackToHomeButton()
            }
        },200)
    }

    private fun setupViewPagerAndTabLayout(){
        myViewPager.adapter = MyRequestAndRequestHistoryViewPagerAdapter(this,supportFragmentManager)
        tabLayout.setupWithViewPager(myViewPager)
    }

    private fun showBackToHomeButton(){
        val slideUpAnimation: Animation = AnimationUtils.loadAnimation(applicationContext,
            R.anim.slide_up
        )
        btnBackToHome.visibility= View.VISIBLE
        btnBackToHome.startAnimation(slideUpAnimation)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.freeze,R.anim.slide_down_slow)
    }

}