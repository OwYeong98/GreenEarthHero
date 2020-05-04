package com.oymj.greenearthhero.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.viewpagers.CurrentPostAndSalesHistoryViewPagerAdapter
import com.oymj.greenearthhero.adapters.viewpagers.CurrentPurchaseAndPurchaseHistoryViewPagerAdapter
import kotlinx.android.synthetic.main.activity_current_purchase_and_purchase_history.*

class CurrentPuchaseAndPurchaseHistoryActivity : AppCompatActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_purchase_and_purchase_history)

        btnBack.setOnClickListener {
            finish()
        }

        setupViewPagerAndTabLayout()
    }

    private fun setupViewPagerAndTabLayout(){
        myViewPager.adapter = CurrentPurchaseAndPurchaseHistoryViewPagerAdapter(this,supportFragmentManager)
        tabLayout.setupWithViewPager(myViewPager)
    }


}