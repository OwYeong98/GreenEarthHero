package com.oymj.greenearthhero.ui.activity

import android.app.Notification
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import kotlinx.android.synthetic.main.activity_notification.*

class NotificationActivity : AppCompatActivity() {

    private lateinit var recyclerViewAdapter: UniversalAdapter
    private lateinit var listener: ListenerRegistration

    private var notificationList = ArrayList<Any>()

    //Better control of onClickListener
    //all button action will be registered here
    private var myOnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            when (v) {
                btnBack->{
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)


        setupRecyclerView()
        linkAllButtonWithOnClickListener()

        swipeLayout.setOnRefreshListener {
            getNotificationFromFirebase()
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
            btnBack
        )

        for (view in actionButtonViewList) {
            view.setOnClickListener(myOnClickListener)
        }
    }

    private fun listenToFirebaseCollectionChangesAndUpdateUI(){
        var db = FirebaseFirestore.getInstance()

        listener = db.collection("Notification").whereEqualTo("userId", FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this)).addSnapshotListener{
                snapshot,e->
            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null ) {
                //update the UI
                getNotificationFromFirebase()
            }
        }
    }

    private fun getNotificationFromFirebase(){
        //clear previous data first
        notificationList.clear()

        //show loading skeletal first while getting data from firestore
        recyclerViewAdapter.startSkeletalLoading(7, UniversalAdapter.SKELETAL_TYPE_3)

        com.oymj.greenearthhero.data.Notification.getNotificationListOfUser(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this)!!){
                success,message,data ->

            if(success){
                updateNotificationRead()
                recyclerViewAdapter.stopSkeletalLoading()
                //clear previous data first
                notificationList.clear()
                swipeLayout.isRefreshing = false

                notificationList.clear()
                notificationList.addAll(data!!)

                //sort by near to far
                notificationList.sortByDescending { obj-> (obj as com.oymj.greenearthhero.data.Notification).date.time }

                runOnUiThread {
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
        recyclerViewAdapter = object: UniversalAdapter(notificationList,this@NotificationActivity,notificationRecyclerView){
            override fun getVerticalSpacing(): Int {
                //20px spacing
                return 10
            }
            override fun onItemClickedListener(data: Any, clickType:Int) {
                if(data is com.oymj.greenearthhero.data.Notification){
                    data.redirectToPage(this@NotificationActivity)
                }
            }
        }
        notificationRecyclerView.layoutManager = LinearLayoutManager(this)
        notificationRecyclerView.adapter = recyclerViewAdapter
    }

    private fun updateNotificationRead(){
        FirebaseFirestore.getInstance().collection("Notification").whereEqualTo("userId",FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this)!!).get()
            .addOnSuccessListener {
                notificationsSnapshot->

                FirebaseFirestore.getInstance().runBatch{
                    batch->

                    notificationsSnapshot.forEach { snapshot->
                        batch.update(snapshot.reference, mapOf("isRead" to true))
                    }
                }
            }
    }
}