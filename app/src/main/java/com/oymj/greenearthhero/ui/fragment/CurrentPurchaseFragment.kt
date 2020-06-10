package com.oymj.greenearthhero.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.oymj.greenearthhero.adapters.recyclerview.recycleritem.RecyclerItemCurrentItemPurchase
import com.oymj.greenearthhero.data.SecondHandItem
import com.oymj.greenearthhero.ui.activity.CurrentPurchaseDetailActivity
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import kotlinx.android.synthetic.main.fragment_current_purchase.*
import kotlin.collections.ArrayList

class CurrentPurchaseFragment : Fragment() {

    var currentPurchaseList = ArrayList<Any>()
    lateinit var recyclerViewAdapter: UniversalAdapter
    lateinit var listener: ListenerRegistration


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_current_purchase, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var myRecyclerView = view.findViewById<RecyclerView>(R.id.currentPurchaseRecyclerView)


        recyclerViewAdapter = object: UniversalAdapter(currentPurchaseList,context!!,myRecyclerView){
            override fun getVerticalSpacing(): Int {
                //20px spacing
                return 10
            }

            override fun onItemClickedListener(data: Any, clickType:Int) {
                if(data is SecondHandItem){
                    var intent = Intent(context!!,CurrentPurchaseDetailActivity::class.java)
                    intent.putExtra("itemId",data.id)
                    activity!!.startActivity(intent)
                }
            }

            //override the view type to return -1 cause we want to choose recycler item mannually
            override fun getItemViewType(position: Int): Int {
                return if(data.get(position)::class.java.simpleName == "SecondHandItem"){
                    -1
                }else {
                    super.getItemViewType(position)
                }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                return if(viewType == -1){
                    RecyclerItemCurrentItemPurchase().getViewHolder(parent,context,this)
                }else{
                    super.onCreateViewHolder(parent, viewType)
                }
            }
        }
        myRecyclerView.layoutManager = LinearLayoutManager(view.context)
        myRecyclerView.adapter = recyclerViewAdapter

        swipeLayout.setOnRefreshListener {
            getCurrentPurchaseListFromFirebase()
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

        listener = db.collection("Second_Hand_Item").whereEqualTo("boughtBy", FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(context!!)).addSnapshotListener{
                snapshot,e->
            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null ) {
                //update the UI
                getCurrentPurchaseListFromFirebase()
            }
        }
    }

    private fun getCurrentPurchaseListFromFirebase(){
        zeroStateContainer.visibility = View.GONE
        currentPurchaseRecyclerView.visibility = View.VISIBLE

        //clear previous data first
        currentPurchaseList.clear()

        //show loading skeletal first while getting data from firestore
        recyclerViewAdapter.startSkeletalLoading(7, UniversalAdapter.SKELETAL_TYPE_3)

        SecondHandItem.getItemListBoughtByUserFromFirebase(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(context!!)!!){
                success,message,data ->

            if(success){
                activity?.runOnUiThread {
                    recyclerViewAdapter.stopSkeletalLoading()
                    swipeLayout.isRefreshing = false

                    currentPurchaseList.clear()
                    //filter only show request that are requested by the current logged in user
                    data!!.forEach { item-> currentPurchaseList.add(item) }

                    if(currentPurchaseList.size > 0){
                        //refresh recyclerview
                        recyclerViewAdapter.notifyDataSetChanged()
                    }else{
                        zeroStateContainer.visibility = View.VISIBLE
                        currentPurchaseRecyclerView.visibility = View.GONE
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

}