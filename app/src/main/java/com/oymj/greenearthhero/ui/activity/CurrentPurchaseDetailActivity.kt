package com.oymj.greenearthhero.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.data.ChatMessage
import com.oymj.greenearthhero.data.ChatRoom
import com.oymj.greenearthhero.data.SecondHandItem
import com.oymj.greenearthhero.data.User
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.ui.dialog.LoadingDialog
import com.oymj.greenearthhero.ui.dialog.SuccessDialog
import com.oymj.greenearthhero.ui.dialog.YesOrNoDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_current_purchase_detail.*
import kotlinx.android.synthetic.main.activity_current_purchase_detail.btnBack
import kotlinx.android.synthetic.main.activity_current_purchase_detail.tvDate
import kotlinx.android.synthetic.main.activity_current_purchase_detail.tvItemName
import kotlinx.android.synthetic.main.activity_current_purchase_detail.tvStatus
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CurrentPurchaseDetailActivity: AppCompatActivity() {

    lateinit var currentViewingItemDetail: SecondHandItem
    lateinit var currentViewingItemId:String
    lateinit var listener: ListenerRegistration

    //Better control of onClickListener
    //all button action will be registered here
    private var myOnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            when (v) {
                btnBack -> {
                    finish()
                }
                btnChatWithSeller->{
                    var loadingDialog = LoadingDialog(this@CurrentPurchaseDetailActivity)
                    loadingDialog.show()

                    var sellerUserId = currentViewingItemDetail.postedByUser?.userId!!
                    ChatRoom.getSpecificChatRoomProvidingTwoUser(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this@CurrentPurchaseDetailActivity)!!,sellerUserId, callback = {
                            success,message,chatRoomRef->

                        GlobalScope.launch {
                            if(chatRoomRef!=null){
                                loadingDialog.dismiss()
                                //if found we return previous activity
                                var intent = Intent(this@CurrentPurchaseDetailActivity,ChatRoomActivity::class.java)
                                intent.putExtra("chatRoom",chatRoomRef)
                                startActivity(intent)
                            }else{
                                loadingDialog.dismiss()

                                var user1 = User.suspendGetSpecificUserFromFirebase(FirebaseAuth.getInstance().currentUser?.uid!!) //currentlogged in user
                                var user2 = currentViewingItemDetail.postedByUser

                                var id = "-1"
                                var lastMessage = ""
                                var lastMessageSendBy = ""
                                var messageList = ArrayList<ChatMessage>()

                                var newChatRoom = ChatRoom(id, user1!!, user2!!, messageList, lastMessage,lastMessageSendBy)
                                var intent = Intent(this@CurrentPurchaseDetailActivity,ChatRoomActivity::class.java)
                                intent.putExtra("chatRoom",newChatRoom)
                                startActivity(intent)
                            }
                        }
                    })
                }
                btnMarkItemAsReceived->{
                    var confirmDialog = YesOrNoDialog(this@CurrentPurchaseDetailActivity,"Are you sure you want to mark this item as receivied?") {
                            isYes->

                        if(isYes)
                            updateFirebaseSalesIsDone(currentViewingItemDetail)
                    }

                    confirmDialog.show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_purchase_detail)

        currentViewingItemId = intent.getStringExtra("itemId")

        linkAllButtonWithOnClickListener()
        setupUI()

    }

    override fun onStart() {
        super.onStart()
        listenToItemDetailChangesAndUpdateUI()
    }

    override fun onStop() {
        super.onStop()
        listener.remove()
    }

    private fun listenToItemDetailChangesAndUpdateUI(){
        var db = FirebaseFirestore.getInstance()

        listener = db.collection("Second_Hand_Item").document(currentViewingItemId).addSnapshotListener{
                snapshot,e->
            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null ) {
                //update the UI
                getItemDetailFromFirebase()
            }
        }
    }

    private fun getItemDetailFromFirebase(){
        var loadingDialog = LoadingDialog(this)
        if(!::currentViewingItemDetail.isInitialized){
            loadingDialog.show()
        }

        SecondHandItem.getItemByIdFromFirebase(currentViewingItemId){
                success,message,data->

            if(success){
                if(!::currentViewingItemDetail.isInitialized){
                    loadingDialog.dismiss()
                }

                currentViewingItemDetail = data!!

                runOnUiThread{
                    tvDate.text = "Date: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(data.datePosted)}"
                    tvItemName.text = data.itemName
                    tvPrice.text = "RM ${String.format("%.2f",data.itemPrice)}"
                    tvDeliveryCompany.text = if(data.courierCompany == "") "N/A" else data.courierCompany
                    tvTrackingNo.text = if(data.trackingNo == "") "N/A" else data.trackingNo

                    if(data.boughtByUser != null){
                        if(data.trackingNo == ""){
                            tvStatus.text = "Pending For Delivery"
                            tvStatus.background = RippleUtil.getRippleButtonOutlineDrawable(this,
                                resources.getColor(R.color.yellow),
                                resources.getColor(R.color.transparent_pressed),
                                resources.getColor(R.color.transparent),
                                0f, 0)

                        }else{
                            tvStatus.text = "On Delivery"
                            tvStatus.background = RippleUtil.getRippleButtonOutlineDrawable(this,
                                resources.getColor(R.color.slightdarkgreen),
                                resources.getColor(R.color.transparent_pressed),
                                resources.getColor(R.color.transparent),
                                0f, 0)
                        }
                    }
                }
            }else{
                var errorDialog = ErrorDialog(this,"Error","We have encountered some error when contacting with firebase. Contact the developer")
                errorDialog.show()
            }
        }
    }

    private fun updateFirebaseSalesIsDone(data: SecondHandItem){
        if(data.trackingNo == ""){
            var errorDialog = ErrorDialog(this,"Delivery detail not added!","You can only mark this purchase as received when seller added delivery detail.")
            errorDialog.show()
        }else{
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            dateFormat.timeZone = TimeZone.getTimeZone("GMT+8:00")
            val currentDateTime: String = dateFormat.format(Date()) // Find todays date

            //create a firebase document
            val itemHistoryDocument = hashMapOf(
                "date" to currentDateTime,
                "user_posted" to data.postedByUser,
                "user_bought" to data.boughtByUser,
                "delivery_location" to data.deliveryLocation,
                "courierCompany" to data.courierCompany,
                "trackingNo" to data.trackingNo,
                "imageUrl" to data.imageUrl,
                "itemName" to data.itemName,
                "itemDesc" to data.itemDesc,
                "itemPrice" to data.itemPrice
            )

            var loadingDialog = LoadingDialog(this)
            loadingDialog.show()

            FirebaseFirestore.getInstance().collection("Second_Hand_Item_History").add(itemHistoryDocument)
                .addOnSuccessListener {

                    var successDialog = SuccessDialog(this,"Success","The item is marked as received Successfully!")
                    successDialog.show()
//                FirebaseFirestore.getInstance().collection("Second_Hand_Item").document(data.id).delete()
//                    .addOnSuccessListener {
//                        loadingDialog.hide()
//
//                        var successDialog = SuccessDialog(this,"Success","The item is marked as received Successfully!")
//                        successDialog.show()
//                    }
//                    .addOnFailureListener {
//                        loadingDialog.hide()
//
//                        var errorDialog = ErrorDialog(this,"Oops","Sorry, We have encountered some error when connecting with Firebase.")
//                        errorDialog.show()
//                    }
                }
                .addOnFailureListener {
                        e ->
                    loadingDialog.hide()

                    var errorDialog = ErrorDialog(this,"Oops","Sorry, We have encountered some error when connecting with Firebase.")
                    errorDialog.show()
                }
        }
    }

    private fun setupUI(){

        btnChatWithSeller.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.darkgrey),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            50f, 0)

        btnMarkItemAsReceived.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.slightdarkgreen),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            50f, 0)

    }

    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            btnBack,
            btnMarkItemAsReceived,
            btnChatWithSeller
        )

        for (view in actionButtonViewList) {
            view.setOnClickListener(myOnClickListener)
        }
    }
}