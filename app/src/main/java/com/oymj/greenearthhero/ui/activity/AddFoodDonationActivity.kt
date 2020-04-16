package com.oymj.greenearthhero.ui.activity

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.oymj.greenearthhero.R
import kotlinx.android.synthetic.main.activity_add_food_donation.*

class AddFoodDonationActivity : AppCompatActivity() {


    //Better control of onClickListener
    //all button action will be registered here
    private var myOnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            when (v) {


            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_food_donation)


        linkAllButtonWithOnClickListener()
    }

    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            tvTitle
        )

        for (view in actionButtonViewList) {
            view.setOnClickListener(myOnClickListener)
        }
    }

}