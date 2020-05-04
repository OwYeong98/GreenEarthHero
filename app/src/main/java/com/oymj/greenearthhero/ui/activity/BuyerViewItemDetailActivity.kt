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
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.ui.dialog.LoadingDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_buyer_view_item_detail.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class BuyerViewItemDetailActivity: AppCompatActivity() {

    lateinit var currentViewingItemDetail:SecondHandItem
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
                btnBuyNow->{

                }
                btnChatWithSeller->{
                    var loadingDialog = LoadingDialog(this@BuyerViewItemDetailActivity)
                    loadingDialog.show()

                    var sellerUserId = currentViewingItemDetail.postedByUser.userId
                    ChatRoom.getSpecificChatRoomProvidingTwoUser(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this@BuyerViewItemDetailActivity)!!,sellerUserId, callback = {
                            success,message,chatRoomRef->

                        GlobalScope.launch {
                            if(chatRoomRef!=null){
                                loadingDialog.dismiss()
                                //if found we return previous activity
                                var intent = Intent(this@BuyerViewItemDetailActivity,ChatRoomActivity::class.java)
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
                                var intent = Intent(this@BuyerViewItemDetailActivity,ChatRoomActivity::class.java)
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
        setContentView(R.layout.activity_buyer_view_item_detail)

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
                    tvItemPrice.text = "RM ${String.format("%.2f",data!!.itemPrice)}"
                    tvDatePosted.text = "Date posted: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(data!!.datePosted)}"
                    tvItemName.text = data!!.itemName
                    tvItemDesc.text = data!!.itemDesc
                    tvSellerName.text = data!!.postedByUser.getFullName()

                    ivItemImage.setImageResource(R.drawable.skeleton_rounded_square)
                    shimmerLayout.startShimmerAnimation()


                    var storageRef = FirebaseStorage.getInstance().reference
                    var itemImageRef = storageRef.child(data.imageUrl)
                    val TEN_MEGABYTE: Long = 1024 * 1024 * 10
                    itemImageRef.getBytes(TEN_MEGABYTE)
                        .addOnSuccessListener {
                                imageData->

                            if(imageData != null){
                                shimmerLayout.stopShimmerAnimation()
                                var itemImage = BitmapFactory.decodeByteArray(imageData!!,0,imageData.size)
                                ivItemImage.setImageBitmap(itemImage)
                            }

                        }

                }
            }else{
                var errorDialog = ErrorDialog(this,"Error","We have encountered some error when contacting with firebase. Contact the developer")
                errorDialog.show()
            }
        }


    }


    private fun setupUI(){
        btnBuyNow.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.slightdarkgreen),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            50f, 0)

        btnChatWithSeller.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.darkgrey),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            50f, 0)


    }

    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            btnBack,
            btnBuyNow,
            btnChatWithSeller
        )

        for (view in actionButtonViewList) {
            view.setOnClickListener(myOnClickListener)
        }
    }
}