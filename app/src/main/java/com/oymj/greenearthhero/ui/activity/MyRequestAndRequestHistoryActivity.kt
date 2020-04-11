package com.oymj.greenearthhero.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.viewpagers.MyRequestAndRequestHistoryViewPagerAdapter
import kotlinx.android.synthetic.main.activity_my_request_and_request_history.*

class MyRequestAndRequestHistoryActivity : AppCompatActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_request_and_request_history)

        setupViewPagerAndTabLayout()
    }

    private fun setupViewPagerAndTabLayout(){

        myViewPager.adapter = MyRequestAndRequestHistoryViewPagerAdapter(this,supportFragmentManager)
        tabLayout.setupWithViewPager(myViewPager)
    }

}