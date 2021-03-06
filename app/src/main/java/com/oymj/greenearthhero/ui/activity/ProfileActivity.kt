package com.oymj.greenearthhero.ui.activity

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.data.*
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.ui.dialog.LoadingDialog
import com.oymj.greenearthhero.ui.dialog.SuccessDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.utils.FormUtils
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_menu.menu_bg
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ProfileActivity : AppCompatActivity(){
    private var isFromMenuActivity:Boolean = false
    private var isSelfProfile:Boolean = false
    lateinit var currentViewingUserId:String

    private var myOnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            when (v) {
                btnEditProfile->{
                    if(btnEditProfile.text == "Edit Info")
                        enableEdit()
                    else{
                        if(validate()){
                            saveDetailToFirebase()
                        }
                    }

                }
                btnMenu->{
                    var intent = Intent(this@ProfileActivity, MenuActivity::class.java)
                    startActivity(intent)
                }
                btnBack->{
                    finish()
                }
                btnMessageHim->{
                    var loadingDialog = LoadingDialog(this@ProfileActivity)
                    loadingDialog.show()

                    ChatRoom.getSpecificChatRoomProvidingTwoUser(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this@ProfileActivity)!!,currentViewingUserId, callback = {
                            success,message,chatRoomRef->

                        GlobalScope.launch {
                            if(chatRoomRef!=null){
                                loadingDialog.dismiss()
                                //if found we return previous activity
                                var intent = Intent(this@ProfileActivity,ChatRoomActivity::class.java)
                                intent.putExtra("chatRoom",chatRoomRef)
                                startActivity(intent)
                            }else{
                                loadingDialog.dismiss()

                                var user1 = User.suspendGetSpecificUserFromFirebase(FirebaseAuth.getInstance().currentUser?.uid!!) //currentlogged in user
                                var user2 = User.suspendGetSpecificUserFromFirebase(currentViewingUserId)

                                var id = "-1"
                                var lastMessage = ""
                                var lastMessageSendBy = ""
                                var messageList = ArrayList<ChatMessage>()

                                var newChatRoom = ChatRoom(id, user1!!, user2!!, messageList, lastMessage,lastMessageSendBy)
                                var intent = Intent(this@ProfileActivity,ChatRoomActivity::class.java)
                                intent.putExtra("chatRoom",newChatRoom)
                                startActivity(intent)
                            }
                        }
                    })
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setupUI()

        isFromMenuActivity = intent.getBooleanExtra("isFromMenuActivity",false)
        if(isFromMenuActivity){
            btnMenu.visibility = View.VISIBLE
            btnBack.visibility = View.GONE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                revertMenuPageCircularRevealActivity()
            }
        }else{
            btnMenu.visibility = View.GONE
            btnBack.visibility = View.VISIBLE
        }


        var userId = intent.getStringExtra("userId")


        //if userID is -1 means view self profile
        if(userId == "-1"){
            isSelfProfile = true
            btnMessageHim.visibility = View.GONE
            btnEditProfile.visibility = View.VISIBLE
            currentViewingUserId = FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this)!!
            displayUserDataFromFirebase(currentViewingUserId)
        }else{
            btnMessageHim.visibility = View.VISIBLE
            btnEditProfile.visibility = View.GONE
            currentViewingUserId = userId
            displayUserDataFromFirebase(userId)
        }

        linkAllButtonWithOnClickListener()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            revertMenuPageCircularRevealActivity()
        }
    }


    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            btnEditProfile,
            btnMenu,
            btnBack,
            btnMessageHim
        )

        for (view in actionButtonViewList) {
            view.setOnClickListener(myOnClickListener)
        }
    }

    private fun displayUserDataFromFirebase(userId: String){
        var loadingDialog = LoadingDialog(this)
        loadingDialog.show()

        calculateAchievementOnRecycleReuseAndReduce(userId)
        User.getSpecificUserFromFirebase(userId, callback = {
            success,message,data->

            RecycleRequestHistory.getSpecificUserRecycleRequestHistoryFromFirebase(userId,callback = {
                recycleHisSuccess,recycleHisMessage,recycleHisResult->
                if(success && recycleHisSuccess){
                    loadingDialog.dismiss()

                    if(isSelfProfile)
                        tvTitle.text = "My Profile"
                    else
                        tvTitle.text = "${data?.firstName}'s Profile"


                    tvFullName.text = data?.getFullName()
                    tvFirstName.setText(data?.firstName!!)
                    tvLastName.setText(data?.lastName!!)
                    tvEmail.setText(data?.email!!)
                    tvPhone.setText(data?.phone!!)

                    //cal total recycled material
                    var totalRecycle = 0
                    for(his in recycleHisResult!!){
                        totalRecycle+= his.getTotalAmount()
                    }
                    tvRecycledAmount.text = "$totalRecycle kg"

                }else{
                    loadingDialog.dismiss()
                    var errorDialog = ErrorDialog(this,"Error","We have encountered some error when contacting with firebase. Contact the developer")
                    errorDialog.show()
                }
            })
        })
    }

    private fun calculateAchievementOnRecycleReuseAndReduce(userId: String){

        FoodDonationHistory.getFoodDonationHistoryListByUserFromFirebase(userId,callback = {
                success,message,data->
            if(success){
                var totalFoodDonated = data!!.fold(0,{prev,obj-> prev + obj.totalFoodAmount})

                runOnUiThread{
                    loading_reduced.visibility = View.GONE
                    reduced_icon.visibility = View.VISIBLE
                    tvReducedAmount.text = "$totalFoodDonated meals"
                }
            }
        })

        RecycleRequestHistory.getSpecificUserRecycleRequestHistoryFromFirebase(userId,callback = {
                success, message, data ->
            if(success){
                var totalRecycled = data!!.fold(0,{prev,obj-> prev + obj.getTotalAmount()})

                runOnUiThread{
                    loading_recycled.visibility = View.GONE
                    recyled_icon.visibility = View.VISIBLE
                    tvRecycledAmount.text = "$totalRecycled Kg"
                }
            }
        })


        SecondHandItemHistory.getItemSaleHistoryOfUserFromFirebase(userId,callback = {
                success, message, data ->
            SecondHandItemHistory.getPurchaseHistoryOfUserFromFirebase(userId,callback = {
                    innerSuccess, innerMessage, innerData ->

                if(success && innerSuccess){
                    var totalItemSale = data!!.size + innerData!!.size

                    runOnUiThread{
                        loading_reused.visibility = View.GONE
                        reused_icon.visibility = View.VISIBLE
                        tvReusedAmount.text = "$totalItemSale items"
                    }
                }
            })
        })


    }

    private fun setupUI(){
        btnEditProfile.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.mapboxGreen),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            20f,0
        )

        btnMessageHim.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            Color.parseColor("#3D90F4"),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            20f,0
        )

    }

    private fun enableEdit(){
        btnEditProfile.text = "Save"

        tvFirstName.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.white),
            resources.getColor(R.color.white),
            resources.getColor(R.color.black),
            20f,2
        )
        tvFirstName.isEnabled = true
        tvFirstName.setPadding(5,20,0,20)

        tvLastName.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.white),
            resources.getColor(R.color.white),
            resources.getColor(R.color.black),
            20f,2
        )
        tvLastName.isEnabled = true
        tvLastName.setPadding(5,20,0,20)

    }

    private fun disableEdit(){
        btnEditProfile.text = "Edit Info"

        tvFirstName.background = null
        tvFirstName.isEnabled = false
        tvFirstName.setPadding(0,0,0,0)

        tvLastName.background = null
        tvLastName.isEnabled = false
        tvLastName.setPadding(0,0,0,0)

    }

    private fun saveDetailToFirebase(){
        //update firebase that this user accept the request
        var db = FirebaseFirestore.getInstance()
        db.collection("Users").document(currentViewingUserId).update(mapOf(
            "firstName" to tvFirstName.text.toString(),
            "lastName" to tvLastName.text.toString()
        )).addOnSuccessListener {
            disableEdit()
            displayUserDataFromFirebase(currentViewingUserId)
            var successDialog = SuccessDialog(this,"Success","Successfully Updated your profile!")
            successDialog.show()
        }.addOnFailureListener {
                exception ->
            var failureDialog = ErrorDialog(this,"Error","We have encountered some error when connecting to Firebase! Please check ur internet connection.")
            failureDialog.show()
        }
    }

    fun validate(): Boolean{
        var firstName: String = tvFirstName.text.toString()
        var lastName: String = tvLastName.text.toString()

        var firstNameError = ""
        var lastNameError = ""


        firstNameError+= "${FormUtils.isNull("First Name",firstName)?:""}|"
        firstNameError+= "${FormUtils.isLengthBetween("First Name",firstName,3,20)?:""}|"

        lastNameError+= "${FormUtils.isNull("Last Name",lastName)?:""}|"
        lastNameError+= "${FormUtils.isLengthBetween("Last Name",lastName,3,20)?:""}|"


        if(firstNameError!=""){
            for(err in firstNameError.split("|")){
                if(err!=""){
                    tvFirstName.error = err
                    break
                }
            }
        }
        if(lastNameError!=""){
            for(err in lastNameError.split("|")){
                if(err!=""){
                    tvLastName.error = err
                    break
                }
            }
        }

        var allError = firstNameError+lastNameError

        return allError.replace("|","")== ""
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun revertMenuPageCircularRevealActivity() {

        //add menu view so that we can revert reveal it
        var layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var inflatedView = layoutInflater.inflate(R.layout.activity_menu,null) as ConstraintLayout
        inflatedView.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT)
        rootLayout.addView(inflatedView)

        inflatedView.post {
            runOnUiThread{
                val cx: Int = menu_bg.left + getDips(16) + getDips(56/2)
                val cy: Int = menu_bg.top + getDips(16) + getDips(56/2)
                val finalRadius: Float = Math.max(inflatedView.width, inflatedView.height).toFloat()
                val circularReveal =
                    ViewAnimationUtils.createCircularReveal(inflatedView, cx, cy, finalRadius, 0f)
                circularReveal.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animator: Animator) {}
                    override fun onAnimationEnd(animator: Animator) {
                        rootLayout.removeView(inflatedView)
                    }

                    override fun onAnimationCancel(animator: Animator) {}
                    override fun onAnimationRepeat(animator: Animator) {}
                })
                circularReveal.duration = 1000
                circularReveal.start()
            }
        }
    }

    private fun getDips(dps: Int): Int {
        val resources: Resources = resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dps.toFloat(),
            resources.displayMetrics
        ).toInt()
    }


}