package com.oymj.greenearthhero.ui.activity

import android.content.Intent
import android.net.sip.SipSession
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapterRepo
import com.oymj.greenearthhero.adapters.recyclerview.recycleritem.RecyclerItemMyVolunteerCollectionRequest
import com.oymj.greenearthhero.data.ChatMessage
import com.oymj.greenearthhero.data.ChatRoom
import com.oymj.greenearthhero.data.RecycleRequest
import com.oymj.greenearthhero.data.User
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.ui.dialog.LoadingDialog
import com.oymj.greenearthhero.ui.dialog.SuccessDialog
import com.oymj.greenearthhero.ui.dialog.YesOrNoDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import kotlinx.android.synthetic.main.activity_my_volunteer.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MyVolunteerActivity : AppCompatActivity() {

    private lateinit var recyclerViewAdapter: UniversalAdapter
    private lateinit var listener: ListenerRegistration

    private var myVolunteerList = ArrayList<Any>()

    //Better control of onClickListener
    //all button action will be registered here
    private var myOnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            when (v) {
                btnBackToHome->{
                    finish()
                    overridePendingTransition(R.anim.freeze, R.anim.slide_down_slow)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_volunteer)


        setupRecyclerView()
        linkAllButtonWithOnClickListener()
        listenToFirebaseCollectionChangesAndUpdateUI()

        swipeLayout.setOnRefreshListener {
            getRecyclerRequestFromFirebase()
        }
    }

    override fun onStart() {
        super.onStart()
        listenToFirebaseCollectionChangesAndUpdateUI()
    }

    override fun onStop() {
        super.onStop()
        listener.remove()
    }



    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            btnBackToHome
        )

        for (view in actionButtonViewList) {
            view.setOnClickListener(myOnClickListener)
        }
    }

    private fun listenToFirebaseCollectionChangesAndUpdateUI(){
        var db = FirebaseFirestore.getInstance()

        listener = db.collection("Recycle_Request").whereEqualTo("accepted_collect_by",FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this)).addSnapshotListener{
                snapshot,e->
            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null ) {
                //update the UI
                getRecyclerRequestFromFirebase()
            }
        }
    }

    private fun getRecyclerRequestFromFirebase(){
        //clear previous data first
        myVolunteerList.clear()

        //show loading skeletal first while getting data from firestore
        recyclerViewAdapter.startSkeletalLoading(7, UniversalAdapter.SKELETAL_TYPE_3)

        RecycleRequest.getRecycleRequestFromFirebase{
                success,message,data ->

            if(success){
                recyclerViewAdapter.stopSkeletalLoading()
                //clear previous data first
                myVolunteerList.clear()
                swipeLayout.isRefreshing = false


                //filter only show request that are volunteer by the current logged in user
                for(request in data!!){
                    if(request.acceptedCollectUser != null){
                        if(request.acceptedCollectUser!!.userId == FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this)){
                            myVolunteerList.add(request)
                        }
                    }
                }

                runOnUiThread {
                    if (myVolunteerList.size >0){
                        //sort by near to far
                        myVolunteerList.sortBy { obj-> (obj as RecycleRequest).getDistanceBetween() }
                        myVolunteerRecyclerView.visibility = View.VISIBLE
                        zeroStateContainer.visibility = View.GONE
                    }else{
                        myVolunteerRecyclerView.visibility = View.GONE
                        zeroStateContainer.visibility = View.VISIBLE
                    }
                    //refresh recyclerview
                    recyclerViewAdapter.notifyDataSetChanged()
                }
            }else{
                recyclerViewAdapter.stopSkeletalLoading()
                swipeLayout.isRefreshing = false

                var errorDialog = ErrorDialog(this,"Error when getting data from Firebase","Contact the developer. Error Code: $message")
                errorDialog.show()
            }

        }


    }


    private fun setupRecyclerView(){
        recyclerViewAdapter = object: UniversalAdapter(myVolunteerList,this@MyVolunteerActivity,myVolunteerRecyclerView){
            override fun getVerticalSpacing(): Int {
                //20px spacing
                return 10
            }
            override fun onItemClickedListener(data: Any, clickType:Int) {
                if(data is RecycleRequest){
                    //clicktype 1 is cancel volunteer
                    if(clickType == 1){
                        var confirmDialog = YesOrNoDialog(this@MyVolunteerActivity,"Are you sure you want to cancel volunteer collection for this request?",callback = {
                            isYesPressed->

                            if(isYesPressed)
                                updateFirebaseCancelVolunteer(data)
                        })
                        confirmDialog.show()
                    }else if(clickType ==2){
                        //click type 2 is mark as collected
                        var confirmDialog = YesOrNoDialog(this@MyVolunteerActivity,"Are you sure you want to mark this request as collected?",callback = {
                            updateFirebaseRequestIsDone(data)
                        })
                        confirmDialog.show()

                    }else if(clickType == 3){
                        //click type 3 is chat with requester
                        var loadingDialog = LoadingDialog(this@MyVolunteerActivity)
                        loadingDialog.show()

                        var recycleRequest = data
                        var requesterUserId = recycleRequest.requestedUser.userId
                        Log.d("wtf","user1: ${FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this@MyVolunteerActivity)!!} | user2: ${requesterUserId}")
                        ChatRoom.getSpecificChatRoomProvidingTwoUser(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this@MyVolunteerActivity)!!,requesterUserId, callback = {
                                success,message,chatRoomRef->

                            GlobalScope.launch {
                                if(chatRoomRef!=null){
                                    loadingDialog.dismiss()
                                    //if found we return previous activity
                                    var intent = Intent(this@MyVolunteerActivity,ChatRoomActivity::class.java)
                                    intent.putExtra("chatRoom",chatRoomRef)
                                    startActivity(intent)
                                }else{
                                    loadingDialog.dismiss()

                                    var user1 = User.suspendGetSpecificUserFromFirebase(FirebaseAuth.getInstance().currentUser?.uid!!) //currentlogged in user
                                    var user2 = recycleRequest.requestedUser

                                    var id = "-1"
                                    var lastMessage = ""
                                    var lastMessageSendBy = ""
                                    var messageList = ArrayList<ChatMessage>()

                                    var newChatRoom = ChatRoom(id, user1!!, user2!!, messageList, lastMessage,lastMessageSendBy)
                                    var intent = Intent(this@MyVolunteerActivity,ChatRoomActivity::class.java)
                                    intent.putExtra("chatRoom",newChatRoom)
                                    startActivity(intent)
                                }
                            }
                        })

                    }else if(clickType == 4){
                        //click type 4 is share location
                        var loadingDialog = LoadingDialog(this@MyVolunteerActivity)
                        loadingDialog.show()


                        FirebaseFirestore.getInstance().collection("Recycle_Request").document(data.id)
                            .update(mapOf("isLocationShared" to !data.isLocationShared))
                            .addOnSuccessListener {
                                loadingDialog.dismiss()
                                var successDialog = SuccessDialog(this@MyVolunteerActivity,"Shared Location Successfully","The Requester will now be able to view your location!")
                                successDialog.show()
                            }.addOnFailureListener {
                                loadingDialog.dismiss()
                                var errorDialog = ErrorDialog(this@MyVolunteerActivity,"Oops","Sorry, We have encountered some error when connecting with Firebase.")
                                errorDialog.show()
                            }
                    }

                }
            }

            //override the view type to return -1 cause we want to choose recycler item mannually
            override fun getItemViewType(position: Int): Int {
                return if(data.get(position)::class.java.simpleName == "RecycleRequest"){
                    -1
                }else {
                    super.getItemViewType(position)
                }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                return if(viewType == -1){
                    RecyclerItemMyVolunteerCollectionRequest().getViewHolder(parent,context,this)
                }else{
                    super.onCreateViewHolder(parent, viewType)
                }
            }
        }
        myVolunteerRecyclerView.layoutManager = LinearLayoutManager(this)
        myVolunteerRecyclerView.adapter = recyclerViewAdapter
    }

    private fun updateFirebaseRequestIsDone(data: RecycleRequest){
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        dateFormat.timeZone = TimeZone.getTimeZone("GMT+8:00")
        val currentDateTime: String = dateFormat.format(Date()) // Find todays date


        //create a firebase document
        val recycleHistoryDocument = hashMapOf(
            "date_collected" to currentDateTime,
            "user_requested" to data.requestedUser,
            "user_collected" to data.acceptedCollectUser,
            "address" to data.address,
            "location" to GeoPoint(data.location.latitude,data.location.longitude),
            "glass_weight" to data.glassWeight,
            "metal_weight" to data.metalWeight,
            "plastic_weight" to data.plasticWeight,
            "paper_weight" to data.paperWeight
        )

        var loadingDialog = LoadingDialog(this)
        loadingDialog.show()

        FirebaseFirestore.getInstance().collection("Recycle_Request_History").add(recycleHistoryDocument)
            .addOnSuccessListener {


                FirebaseFirestore.getInstance().collection("Recycle_Request").document(data.id).delete()
                    .addOnSuccessListener {
                        loadingDialog.hide()

                        var successDialog = SuccessDialog(this,"Success","The request is marked as Collected Successfully!")
                        successDialog.show()
                    }
                    .addOnFailureListener {
                        loadingDialog.hide()

                        var errorDialog = ErrorDialog(this,"Oops","Sorry, We have encountered some error when connecting with Firebase.")
                        errorDialog.show()
                    }
            }
            .addOnFailureListener {
                    e ->
                Log.d("error", "Error writing document", e)
                loadingDialog.hide()

                var errorDialog = ErrorDialog(this,"Oops","Sorry, We have encountered some error when connecting with Firebase.")
                errorDialog.show()
            }
    }

    private fun updateFirebaseCancelVolunteer(data:RecycleRequest){
        var loadingDialog = LoadingDialog(this)
        loadingDialog.show()
        //update firebase that this user cancel volunteer
        FirebaseFirestore.getInstance().collection("Recycle_Request").document(data.id).update(mapOf(
            "accepted_collect_by" to ""
        )).addOnSuccessListener {
            loadingDialog.hide()
            var successDialog = SuccessDialog(this@MyVolunteerActivity,"Success","Successfully notify the request owner that you are going to collect her request!")
            successDialog.show()
        }.addOnFailureListener {
                exception ->
            loadingDialog.hide()
            var failureDialog = ErrorDialog(this@MyVolunteerActivity,"Error","We have encountered some error when connecting to Firebase! Please check ur internet connection.")
            failureDialog.show()
        }
    }

    override fun onEnterAnimationComplete() {
        super.onEnterAnimationComplete()

        Handler().postDelayed({
            runOnUiThread {
                showBackToHomeButton()
            }
        },200)
    }

    private fun showBackToHomeButton(){
        val slideUpAnimation: Animation = AnimationUtils.loadAnimation(applicationContext,
            R.anim.slide_up
        )
        btnBackToHome.visibility= View.VISIBLE
        btnBackToHome.startAnimation(slideUpAnimation)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.freeze,R.anim.slide_down_slow)
    }
}