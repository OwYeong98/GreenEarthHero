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
import com.oymj.greenearthhero.data.SecondHandItemHistory
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
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

        listener = db.collection("Second_Hand_Item_History").whereEqualTo("user_posted.userId", FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(context!!)).addSnapshotListener{
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
        zeroStateContainer.visibility = View.GONE
        salesHistoryRecyclerView.visibility = View.VISIBLE

        //clear previous data first
        salesHistoryList.clear()

        //show loading skeletal first while getting data from firestore
        recyclerViewAdapter.startSkeletalLoading(7, UniversalAdapter.SKELETAL_TYPE_3)

        SecondHandItemHistory.getItemSaleHistoryOfUserFromFirebase(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(context!!)!!){
                success,message,data ->

            if(success){
                recyclerViewAdapter.stopSkeletalLoading()
                swipeLayout.isRefreshing = false

                salesHistoryList.clear()
                salesHistoryList.addAll(data!!)

                activity?.runOnUiThread {

                    if(salesHistoryList.size > 0){
                        //refresh recyclerview
                        recyclerViewAdapter.notifyDataSetChanged()
                    }else{
                        zeroStateContainer.visibility = View.VISIBLE
                        salesHistoryRecyclerView.visibility = View.GONE
                    }
                }

            }else{
                recyclerViewAdapter.stopSkeletalLoading()
                swipeLayout.isRefreshing = false

                var errorDialog = ErrorDialog(context!!,"Error when getting data from Firebase","Contact the developer. Error Code: $message")
                errorDialog.show()
            }

        }
    }

}