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
import com.oymj.greenearthhero.data.User
import com.oymj.greenearthhero.utils.FormUtils
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.dialog_resend_sms_verification.*

class ResendSMSVerificationDialog(context: Context,var phoneNo:String, var callback: (String)->Unit ) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_resend_sms_verification)

        //set default background color to transparent
        getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        var layoutParam = getWindow().getAttributes()
        layoutParam.dimAmount = 0.7f
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        setCanceledOnTouchOutside(false)

        inputPhoneNo.setText(phoneNo.substring(3))


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

            //callback and show the result
            if(validatePhoneNo()){
                var phoneNumber = "+60"+inputPhoneNo.text.toString()

                btnConfirm.isClickable = false
                User.getUserListFromFirebase {
                        success, message, userList ->

                    if (success){
                        var isPhoneExist = userList!!.fold(false,{prev,curr-> prev || curr.phone == phoneNumber})
                        if(isPhoneExist){
                            inputPhoneNo.error = "Phone No already exist! Please use another phone"
                            inputPhoneNo.requestFocus()
                            btnConfirm.isClickable = true
                        }else{
                            callback(phoneNumber)
                            this.dismiss()
                        }
                    }else{
                        btnConfirm.isClickable = true
                    }
                }
            }
        }

        btnCancel.setOnClickListener{
            this.dismiss()
        }
    }

    fun validatePhoneNo():Boolean{
        var phone: String = inputPhoneNo.text.toString()
        var phoneError = ""

        phoneError+= "${FormUtils.isNull("Phone No",phone)?:""}|"
        phoneError+= "${FormUtils.isOnlyNumber("Phone No",phone)?:""}|"
        phoneError+= "${FormUtils.isLengthBetween("Phone No",phone,9,10)?:""}|"

        if(phoneError.replace("|","")!=""){
            for(err in phoneError.split("|")){
                if(err!=""){
                    inputPhoneNo.error = err
                    inputPhoneNo.requestFocus()
                    break
                }
            }

            return false
        }else{
            return true
        }
    }

}