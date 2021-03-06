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
import kotlinx.android.synthetic.main.dialog_success_error.*

class ErrorDialog(context: Context,var title:String,var desc:String,var callback: ()->Unit = {}) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_success_error)

        //set default background color to transparent
        getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        var layoutParam = getWindow().getAttributes()
        layoutParam.dimAmount = 0.7f
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        setCanceledOnTouchOutside(false)

        ivIcon.setImageDrawable(context.resources.getDrawable(R.drawable.ic_rounded_cross))
        tvTitle.text = title
        tvTitle.setTextColor(Color.parseColor("#EF431B"))
        tvDesc.text = desc

        btnAction.text = "Try Again"
        btnAction.background = RippleUtil.getGradientRippleButtonOutlineDrawable(context,
            Color.parseColor("#EF431B"),
            Color.parseColor("#F92E06"),
            context.resources.getColor(R.color.transparent_pressed),
            context.resources.getColor(R.color.transparent),
            context.resources.getColor(R.color.transparent),
            50f,0, GradientDrawable.Orientation.LEFT_RIGHT
        )

        btnAction.setOnClickListener{
            this.dismiss()
            callback()
        }

    }
}