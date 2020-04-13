package com.oymj.greenearthhero.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.adapters.recyclerview.recycleritem.RecyclerItemMyRequest
import com.oymj.greenearthhero.data.RecycleRequest
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import kotlinx.android.synthetic.main.activity_my_request_and_request_history.*
import kotlinx.android.synthetic.main.fragment_current_request.*

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
        //clear previous data first
        currentRequestList.clear()

        //show loading skeletal first while getting data from firestore
        recyclerViewAdapter.startSkeletalLoading(7, UniversalAdapter.SKELETAL_TYPE_3)

        RecycleRequest.getRecycleRequestFromFirebase{
                success,message,data ->

            if(success){
                recyclerViewAdapter.stopSkeletalLoading()
                swipeLayout.isRefreshing = false

                //filter only show request that are requested by the current logged in user
                for(request in data!!){
                    if(request.requestedUser.userId == FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(context!!)){
                        currentRequestList.add(request)
                    }
                }

                //refresh recyclerview
                recyclerViewAdapter.notifyDataSetChanged()


            }else{
                recyclerViewAdapter.stopSkeletalLoading()
                swipeLayout.isRefreshing = false

                var errorDialog = ErrorDialog(context!!,"Error when getting data from Firebase","Contact the developer. Error Code: $message")
                errorDialog.show()
            }

        }


    }



}