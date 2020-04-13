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
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.dialog_yes_or_no.*

class YesOrNoDialog(context: Context, var message:String, var callback: (Boolean)->Unit ) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_yes_or_no)

        //set default background color to transparent
        getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        var layoutParam = getWindow().getAttributes()
        layoutParam.dimAmount = 0.7f
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        setCanceledOnTouchOutside(false)

        tvMessage.text = message

        btnYes.background = RippleUtil.getGradientRippleButtonOutlineDrawable(context,
            Color.parseColor("#37B734"),
            Color.parseColor("#45D641"),
            context.resources.getColor(R.color.transparent_pressed),
            context.resources.getColor(R.color.transparent),
            context.resources.getColor(R.color.transparent),
            20f,0, GradientDrawable.Orientation.LEFT_RIGHT
        )

        btnNo.background = RippleUtil.getGradientRippleButtonOutlineDrawable(context,
            Color.parseColor("#606060"),
            Color.parseColor("#6F706F"),
            context.resources.getColor(R.color.transparent_pressed),
            context.resources.getColor(R.color.transparent),
            context.resources.getColor(R.color.transparent),
            20f,0, GradientDrawable.Orientation.LEFT_RIGHT
        )

        btnYes.setOnClickListener{
            this.dismiss()

            //callback and show the result
            callback(true)
        }

        btnNo.setOnClickListener{
            this.dismiss()

            //callback and show the result
            callback(false)
        }

    }


}