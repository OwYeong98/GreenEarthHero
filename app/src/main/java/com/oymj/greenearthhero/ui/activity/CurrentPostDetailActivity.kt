package com.oymj.greenearthhero.ui.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.data.ChatMessage
import com.oymj.greenearthhero.data.ChatRoom
import com.oymj.greenearthhero.data.SecondHandItem
import com.oymj.greenearthhero.data.User
import com.oymj.greenearthhero.ui.dialog.*
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.utils.ImageStorageManager
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_add_food.*
import kotlinx.android.synthetic.main.activity_current_post_detail.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Error
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CurrentPostDetailActivity: AppCompatActivity() {

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
                btnChatWithBuyer->{
                    var loadingDialog = LoadingDialog(this@CurrentPostDetailActivity)
                    loadingDialog.show()

                    var buyerUserId = currentViewingItemDetail.boughtByUser?.userId!!
                    ChatRoom.getSpecificChatRoomProvidingTwoUser(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this@CurrentPostDetailActivity)!!,buyerUserId, callback = {
                            success,message,chatRoomRef->

                        GlobalScope.launch {
                            if(chatRoomRef!=null){
                                loadingDialog.dismiss()
                                //if found we return previous activity
                                var intent = Intent(this@CurrentPostDetailActivity,ChatRoomActivity::class.java)
                                intent.putExtra("chatRoom",chatRoomRef)
                                startActivity(intent)
                            }else{
                                loadingDialog.dismiss()

                                var user1 = User.suspendGetSpecificUserFromFirebase(FirebaseAuth.getInstance().currentUser?.uid!!) //currentlogged in user
                                var user2 = currentViewingItemDetail.boughtByUser

                                var id = "-1"
                                var lastMessage = ""
                                var lastMessageSendBy = ""
                                var messageList = ArrayList<ChatMessage>()

                                var newChatRoom = ChatRoom(id, user1!!, user2!!, messageList, lastMessage,lastMessageSendBy)
                                var intent = Intent(this@CurrentPostDetailActivity,ChatRoomActivity::class.java)
                                intent.putExtra("chatRoom",newChatRoom)
                                startActivity(intent)
                            }
                        }
                    })
                }
                btnEditDetail->{
                    var intent = Intent(this@CurrentPostDetailActivity,PostNewItemActivity::class.java)
                    intent.putExtra("id",currentViewingItemDetail.id)
                    intent.putExtra("name",currentViewingItemDetail.itemName)
                    intent.putExtra("desc",currentViewingItemDetail.itemDesc)
                    intent.putExtra("price",currentViewingItemDetail.itemPrice)

                    var loadingDialog = LoadingDialog(this@CurrentPostDetailActivity)
                    loadingDialog.show()
                    var storageRef = FirebaseStorage.getInstance().reference
                    var foodImageRef = storageRef.child(currentViewingItemDetail.imageUrl)
                    val TEN_MEGABYTE: Long = 1024 * 1024 * 10
                    foodImageRef.getBytes(TEN_MEGABYTE)
                        .addOnSuccessListener {
                                imageData->

                            if(imageData != null){

                                var image = BitmapFactory.decodeByteArray(imageData!!,0,imageData.size)
                                var imageFileName = "tempImg.jpg"
                                ImageStorageManager.saveImgToInternalStorage(this@CurrentPostDetailActivity,image,imageFileName,callback={
                                    loadingDialog.dismiss()
                                    intent.putExtra("foodImageUrl",imageFileName)
                                    startActivity(intent)
                                })

                            }
                        }.addOnFailureListener {
                            loadingDialog.dismiss()
                            var errorDialog = ErrorDialog(this@CurrentPostDetailActivity,"Error while getting Image from firebase","Sorry, we have encountered error when connecting to firebase!")
                            errorDialog.show()
                        }
                }
                btnAddDeliveryDetail->{
                    var addDeliveryDetailDialog = AddDeliveryDetailDialog(this@CurrentPostDetailActivity,currentViewingItemDetail,callback = {
                        deliveryCom, trackingNo ->

                        var updateDetail = mapOf("courierCompany" to deliveryCom, "trackingNo" to trackingNo)
                        FirebaseFirestore.getInstance().collection("Second_Hand_Item").document(currentViewingItemId).update(updateDetail)
                            .addOnSuccessListener {
                                var successDialog = SuccessDialog(this@CurrentPostDetailActivity,"Successfully Updated","Successfully updated the delivery detail.")
                                successDialog.show()
                            }
                            .addOnFailureListener {
                                var errorDialog = ErrorDialog(this@CurrentPostDetailActivity,"Error while getting Image from firebase","Sorry, we have encountered error when connecting to firebase!")
                                errorDialog.show()
                            }
                    })
                    addDeliveryDetailDialog.show()
                }
                btnCancelSelling->{
                    var confirmDialog = YesOrNoDialog(this@CurrentPostDetailActivity,"Are you sure you want to cancel this item selling post?") {
                            isYes->
                        if(isYes){
                            //remove listener first to not listen to update for this post because we want to delete it from database
                            listener.remove()

                            var loadingDialog = LoadingDialog(this@CurrentPostDetailActivity)
                            loadingDialog.show()
                            FirebaseFirestore.getInstance().collection("Second_Hand_Item").document(currentViewingItemId).delete()
                                .addOnSuccessListener {
                                    loadingDialog.dismiss()
                                    var successDialog = SuccessDialog(this@CurrentPostDetailActivity,"Cancelled Successfully!","Your Item Post has been canceled successfully."){
                                        finish()
                                    }
                                    successDialog.show()
                                }
                                .addOnFailureListener {
                                    loadingDialog.dismiss()
                                    var errorDialog = ErrorDialog(this@CurrentPostDetailActivity,"Error","We have encountered some error when contacting with firebase. Contact the developer")
                                    errorDialog.show()
                                }
                        }
                    }

                    confirmDialog.show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_post_detail)

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
                    tvItemDesc.text = data.itemDesc
                    tvItemPrice.text = "RM ${String.format("%.2f",data.itemPrice)}"

                    if(data.boughtByUser != null){
                        tvDeliveryTitle.visibility = View.VISIBLE
                        tvDeliveryAddress.text = data.deliveryLocation?.address
                        if(data.trackingNo == ""){
                            tvStatus.text = "Sold. Please Delivery"
                            tvStatus.background = RippleUtil.getRippleButtonOutlineDrawable(this,
                                resources.getColor(R.color.yellow),
                                resources.getColor(R.color.transparent_pressed),
                                resources.getColor(R.color.transparent),
                                0f, 0)


                            btnEditDetail.text = "Add Delivery Detail"
                            btnEditDetail.visibility = View.GONE
                            btnCancelSelling.visibility = View.GONE
                            btnChatWithBuyer.visibility = View.VISIBLE
                            btnAddDeliveryDetail.visibility = View.VISIBLE
                        }else{
                            tvStatus.text = "On Delivery"
                            tvStatus.background = RippleUtil.getRippleButtonOutlineDrawable(this,
                                resources.getColor(R.color.yellow),
                                resources.getColor(R.color.transparent_pressed),
                                resources.getColor(R.color.transparent),
                                0f, 0)


                            btnEditDetail.text = "Edit Delivery Detail"
                            btnEditDetail.visibility = View.GONE
                            btnCancelSelling.visibility = View.GONE
                            btnChatWithBuyer.visibility = View.VISIBLE
                            btnAddDeliveryDetail.visibility = View.VISIBLE
                        }

                    }else{
                        tvDeliveryTitle.visibility = View.GONE
                        tvDeliveryAddress.visibility = View.GONE

                        tvStatus.text = "On Sale"
                        tvStatus.background = RippleUtil.getRippleButtonOutlineDrawable(this,
                            resources.getColor(R.color.mapboxGreen),
                            resources.getColor(R.color.transparent_pressed),
                            resources.getColor(R.color.transparent),
                            0f, 0)

                        btnEditDetail.visibility = View.VISIBLE
                        btnCancelSelling.visibility = View.VISIBLE
                        btnChatWithBuyer.visibility = View.GONE
                        btnAddDeliveryDetail.visibility = View.GONE
                    }
                }
            }else{
                var errorDialog = ErrorDialog(this,"Error","We have encountered some error when contacting with firebase. Contact the developer")
                errorDialog.show()
            }
        }
    }

    private fun setupUI(){
        btnEditDetail.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.darkgrey),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            50f, 0)

        btnChatWithBuyer.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.darkgrey),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            50f, 0)

        btnAddDeliveryDetail.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.darkgrey),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            50f, 0)

        btnCancelSelling.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.red),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            50f, 0)


    }

    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            btnBack,
            btnEditDetail,
            btnAddDeliveryDetail,
            btnCancelSelling,
            btnChatWithBuyer
        )

        for (view in actionButtonViewList) {
            view.setOnClickListener(myOnClickListener)
        }
    }
}