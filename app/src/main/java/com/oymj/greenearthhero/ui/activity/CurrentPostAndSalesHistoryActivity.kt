package com.oymj.greenearthhero.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.viewpagers.CurrentPostAndSalesHistoryViewPagerAdapter
import kotlinx.android.synthetic.main.activity_current_post_and_sales_history.*

class CurrentPostAndSalesHistoryActivity : AppCompatActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_post_and_sales_history)

        btnBack.setOnClickListener {
            finish()
        }

        setupViewPagerAndTabLayout()
    }

    private fun setupViewPagerAndTabLayout(){
        myViewPager.adapter = CurrentPostAndSalesHistoryViewPagerAdapter(this,supportFragmentManager)
        tabLayout.setupWithViewPager(myViewPager)
    }


}