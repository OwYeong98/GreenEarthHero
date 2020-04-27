package com.oymj.greenearthhero.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.spinner.TimeAvailableSpinnerAdapter
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.dialog_extends_food_donation_dialog.*


class ExtendsFoodDonationDialog(context: Context, var callback: (Int)->Unit ) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_extends_food_donation_dialog)

        //set default background color to transparent
        getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        var layoutParam = getWindow().getAttributes()
        layoutParam.dimAmount = 0.7f
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        setCanceledOnTouchOutside(false)

        setupSpinner()


        btnConfirm.background = RippleUtil.getGradientRippleButtonOutlineDrawable(context,
            Color.parseColor("#37B734"),
            Color.parseColor("#45D641"),
            context.resources.getColor(R.color.transparent_pressed),
            context.resources.getColor(R.color.transparent),
            context.resources.getColor(R.color.transparent),
            20f,0, GradientDrawable.Orientation.LEFT_RIGHT
        )

        btnCancel.background = RippleUtil.getGradientRippleButtonOutlineDrawable(context,
            Color.parseColor("#606060"),
            Color.parseColor("#6F706F"),
            context.resources.getColor(R.color.transparent_pressed),
            context.resources.getColor(R.color.transparent),
            context.resources.getColor(R.color.transparent),
            20f,0, GradientDrawable.Orientation.LEFT_RIGHT
        )

        btnConfirm.setOnClickListener{
            this.dismiss()

            var minutes = (timeAvailableSpinner.selectedItem as String).replace("hours","").trim().toInt() * 60

            //callback and show the result
            callback(minutes)
            this.dismiss()
        }

        btnCancel.setOnClickListener{
            this.dismiss()
        }
    }

    fun setupSpinner(){
        var timeAvailable = arrayListOf<String>()
        for(hour in 1..10){
            timeAvailable.add("$hour hours")
        }
        timeAvailableSpinner.adapter = TimeAvailableSpinnerAdapter(context,timeAvailable)
    }


}