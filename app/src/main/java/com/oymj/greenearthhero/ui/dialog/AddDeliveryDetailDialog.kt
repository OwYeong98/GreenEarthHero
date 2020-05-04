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
import com.oymj.greenearthhero.data.SecondHandItem
import com.oymj.greenearthhero.utils.FormUtils
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.dialog_add_delivery_detail.*

class AddDeliveryDetailDialog(context: Context,var itemDetail:SecondHandItem, var callback: (deliveryCom:String,trackingNo:String)->Unit) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_add_delivery_detail)

        //set default background color to transparent
        getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        var layoutParam = getWindow().getAttributes()
        layoutParam.dimAmount = 0.7f
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        setCanceledOnTouchOutside(false)

        inputTrackingNo.setText(itemDetail?.trackingNo?:"")
        inputDeliveryCompany.setText(itemDetail?.courierCompany?:"")

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
            if(validate()){
                this.dismiss()
                callback(inputDeliveryCompany.text.toString(),inputTrackingNo.text.toString())
            }
        }

        btnCancel.setOnClickListener{
            this.dismiss()
        }
    }

    fun validate():Boolean{
        var delCompany:String = inputDeliveryCompany.text.toString()
        var trackingNo: String = inputTrackingNo.text.toString().trim()

        var delComError = ""
        var trackingNoError = ""

        delComError+= "${FormUtils.isNull("Company Name: ",delCompany)?:""}|"
        delComError+= "${FormUtils.isLengthBetween("Company Name",delCompany,5,20)?:""}|"


        trackingNoError+= "${FormUtils.isNull("Tracking No",trackingNo)?:""}|"
        trackingNoError+= "${FormUtils.isLengthBetween("Tracking No",trackingNo,5,30)?:""}|"

        if(delComError!=""){
            for(err in delComError.split("|")){
                if(err!=""){
                    inputDeliveryCompany.error = err
                    break
                }
            }
        }

        if(trackingNoError!=""){
            for(err in trackingNoError.split("|")){
                if(err!=""){
                    inputTrackingNo.error = err
                    break
                }
            }
        }

        var allError = delComError+trackingNoError

        return allError.replace("|","")== ""
    }
}