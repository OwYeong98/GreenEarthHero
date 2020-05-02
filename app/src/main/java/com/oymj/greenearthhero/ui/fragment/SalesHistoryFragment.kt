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
import com.oymj.greenearthhero.utils.FirebaseUtil
import kotlinx.android.synthetic.main.fragment_sales_history.*

class SalesHistoryFragment : Fragment() {

    var salesHistoryList = ArrayList<Any>()
    lateinit var recyclerViewAdapter: UniversalAdapter
    lateinit var listener: ListenerRegistration


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sales_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var myRecyclerView = view.findViewById<RecyclerView>(R.id.salesHistoryRecyclerView)


        recyclerViewAdapter = object: UniversalAdapter(salesHistoryList,context!!,myRecyclerView){
            override fun getVerticalSpacing(): Int {
                //20px spacing
                return 10
            }

            override fun onItemClickedListener(data: Any, clickType:Int) {
//                if(data is RecycleRequest){
//
//                }
            }
        }
        myRecyclerView.layoutManager = LinearLayoutManager(view.context)
        myRecyclerView.adapter = recyclerViewAdapter

        swipeLayout.setOnRefreshListener {
            getSalesHistoryFromFirebase()
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

        listener = db.collection("Current_Post").whereEqualTo("userId", FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(context!!)).addSnapshotListener{
                snapshot,e->
            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null ) {
                //update the UI
                getSalesHistoryFromFirebase()
            }
        }
    }

    private fun getSalesHistoryFromFirebase(){
//        //clear previous data first
//        currentRequestList.clear()
//
//        //show loading skeletal first while getting data from firestore
//        recyclerViewAdapter.startSkeletalLoading(7, UniversalAdapter.SKELETAL_TYPE_3)
//
//        RecycleRequest.getRecycleRequestFromFirebase{
//                success,message,data ->
//
//            if(success){
//                recyclerViewAdapter.stopSkeletalLoading()
//                swipeLayout.isRefreshing = false
//
//                //filter only show request that are requested by the current logged in user
//                for(request in data!!){
//                    if(request.requestedUser.userId == FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(context!!)){
//                        currentRequestList.add(request)
//                    }
//                }
//
//                //refresh recyclerview
//                recyclerViewAdapter.notifyDataSetChanged()
//
//
//            }else{
//                recyclerViewAdapter.stopSkeletalLoading()
//                swipeLayout.isRefreshing = false
//
//                var errorDialog = ErrorDialog(context!!,"Error when getting data from Firebase","Contact the developer. Error Code: $message")
//                errorDialog.show()
//            }
//
//        }
    }

}