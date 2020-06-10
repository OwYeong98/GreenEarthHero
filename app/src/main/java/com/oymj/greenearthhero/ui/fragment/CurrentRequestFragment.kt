package com.oymj.greenearthhero.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.adapters.recyclerview.recycleritem.RecyclerItemMyRequest
import com.oymj.greenearthhero.data.ChatMessage
import com.oymj.greenearthhero.data.ChatRoom
import com.oymj.greenearthhero.data.RecycleRequest
import com.oymj.greenearthhero.data.User
import com.oymj.greenearthhero.ui.activity.ChatRoomActivity
import com.oymj.greenearthhero.ui.activity.ViewVolunteerLocationActivity
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.ui.dialog.LoadingDialog
import com.oymj.greenearthhero.ui.dialog.SuccessDialog
import com.oymj.greenearthhero.ui.dialog.YesOrNoDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import kotlinx.android.synthetic.main.activity_my_request_and_request_history.*
import kotlinx.android.synthetic.main.fragment_current_request.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Error

class CurrentRequestFragment : Fragment() {

    var currentRequestList = ArrayList<Any>()
    lateinit var recyclerViewAdapter: UniversalAdapter
    lateinit var listener: ListenerRegistration


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_current_request, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var myRecyclerView = view.findViewById<RecyclerView>(R.id.currentRequestRecyclerView)


        recyclerViewAdapter = object: UniversalAdapter(currentRequestList,context!!,myRecyclerView){
            override fun getVerticalSpacing(): Int {
                //20px spacing
                return 10
            }

            override fun onItemClickedListener(data: Any, clickType:Int) {
                if(data is RecycleRequest){

                    if(clickType == 1){
                        //cancel request
                        var confirmDialog = YesOrNoDialog(context!!,"Are you sure you want to delete this request?",callback = {
                            isYes->

                            if(isYes)
                                deleteRequestFromFirebase(data)

                        })
                        confirmDialog.show()
                    }else if(clickType == 2){
                        //chat with volunteer pressed
                        var loadingDialog = LoadingDialog(context!!)
                        loadingDialog.show()

                        var recycleRequest = data
                        var collectorUserId = recycleRequest.acceptedCollectUser!!.userId
                        ChatRoom.getSpecificChatRoomProvidingTwoUser(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(context!!)!!,collectorUserId, callback = {
                                success,message,chatRoomRef->

                            GlobalScope.launch {
                                if(chatRoomRef!=null){
                                    loadingDialog.dismiss()
                                    //if found we return previous activity
                                    var intent = Intent(context!!, ChatRoomActivity::class.java)
                                    intent.putExtra("chatRoom",chatRoomRef)
                                    startActivity(intent)
                                }else{
                                    loadingDialog.dismiss()

                                    var user1 = User.suspendGetSpecificUserFromFirebase(FirebaseAuth.getInstance().currentUser?.uid!!) //currentlogged in user
                                    var user2 = recycleRequest.acceptedCollectUser

                                    var id = "-1"
                                    var lastMessage = ""
                                    var lastMessageSendBy = ""
                                    var messageList = ArrayList<ChatMessage>()

                                    var newChatRoom = ChatRoom(id, user1!!, user2!!, messageList, lastMessage,lastMessageSendBy)
                                    var intent = Intent(context!!,ChatRoomActivity::class.java)
                                    intent.putExtra("chatRoom",newChatRoom)
                                    startActivity(intent)
                                }
                            }
                        })

                    }else if(clickType == 3){
                        var intent = Intent(context!!,ViewVolunteerLocationActivity::class.java)
                        intent.putExtra("volunteerUserId",data.acceptedCollectUser?.userId)
                        intent.putExtra("destLat",data.location.latitude)
                        intent.putExtra("destLong",data.location.longitude)
                        startActivity(intent)
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
                    RecyclerItemMyRequest().getViewHolder(parent,context,this)
                }else{
                    super.onCreateViewHolder(parent, viewType)
                }
            }

        }
        myRecyclerView.layoutManager = LinearLayoutManager(view.context)
        myRecyclerView.adapter = recyclerViewAdapter

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

    private fun listenToFirebaseCollectionChangesAndUpdateUI(){
        var db = FirebaseFirestore.getInstance()

        listener = db.collection("Recycle_Request").whereEqualTo("userId",FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(context!!)).addSnapshotListener{
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
        zeroStateContainer.visibility = View.GONE
        currentRequestRecyclerView.visibility = View.VISIBLE

        //clear previous data first
        currentRequestList.clear()

        //show loading skeletal first while getting data from firestore
        recyclerViewAdapter.startSkeletalLoading(7, UniversalAdapter.SKELETAL_TYPE_3)

        RecycleRequest.getRecycleRequestFromFirebase{
                success,message,data ->

            if(success){
                recyclerViewAdapter.stopSkeletalLoading()
                swipeLayout.isRefreshing = false

                currentRequestList.clear()
                //filter only show request that are requested by the current logged in user
                for(request in data!!){
                    if(request.requestedUser.userId == FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(context!!)){
                        currentRequestList.add(request)
                    }
                }

                activity?.runOnUiThread {
                    if(currentRequestList.size >0){
                        //refresh recyclerview
                        recyclerViewAdapter.notifyDataSetChanged()
                    }else{
                        zeroStateContainer.visibility = View.VISIBLE
                        currentRequestRecyclerView.visibility = View.GONE
                    }
                }
            }else{
                activity?.runOnUiThread {
                    recyclerViewAdapter.stopSkeletalLoading()
                    swipeLayout.isRefreshing = false

                    var errorDialog = ErrorDialog(context!!,"Error when getting data from Firebase","Contact the developer. Error Code: $message")
                    errorDialog.show()
                }
            }

        }
    }

    fun deleteRequestFromFirebase(request:RecycleRequest){

        var loadingDialog = LoadingDialog(context!!)
        loadingDialog.show()
        FirebaseFirestore.getInstance().collection("Recycle_Request").document(request.id).delete()
            .addOnSuccessListener {
                loadingDialog.dismiss()

                var successDialog = SuccessDialog(context!!,"Successfully deleted","Your request is removed successfully!")
                successDialog.show()
            }
            .addOnFailureListener {
                loadingDialog.dismiss()

                var errorDialog = ErrorDialog(context!!,"Error Occured when connecting to firebase","We ve encountered some error when connecting to firebase. Please check your internet connection!")
                errorDialog.show()
            }

    }



}