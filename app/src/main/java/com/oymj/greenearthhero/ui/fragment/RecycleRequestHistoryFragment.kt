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
import com.oymj.greenearthhero.data.RecycleRequestHistory
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import kotlinx.android.synthetic.main.fragment_recycle_request_history.*
import okhttp3.internal.notify

class RecycleRequestHistoryFragment : Fragment() {

    var recycleHistoryList = ArrayList<Any>()
    lateinit var recyclerViewAdapter: UniversalAdapter
    lateinit var listener: ListenerRegistration


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recycle_request_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var myRecyclerView = view.findViewById<RecyclerView>(R.id.recycleRequestHistoryRecyclerView)


        recyclerViewAdapter = object: UniversalAdapter(recycleHistoryList,context!!,myRecyclerView){
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
            getRecycleHistoryFromFirebase()
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

        listener = db.collection("Recycle_Request_History").whereEqualTo("user_requested.userId",FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(context!!)).addSnapshotListener{
                snapshot,e->
            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null ) {
                //update the UI
                getRecycleHistoryFromFirebase()
            }
        }
    }

    private fun getRecycleHistoryFromFirebase(){
        zeroStateContainer.visibility = View.GONE
        recycleRequestHistoryRecyclerView.visibility = View.VISIBLE

        recycleHistoryList.clear()
        recyclerViewAdapter.startSkeletalLoading(6,UniversalAdapter.SKELETAL_TYPE_3)

        RecycleRequestHistory.getRecycleRequestHistoryFromFirebase(callback = {
            success,message,data->

            if(success){
                activity?.runOnUiThread {
                    recyclerViewAdapter.stopSkeletalLoading()
                    swipeLayout.isRefreshing = false

                    var totalMetal = 0
                    var totalGlass = 0
                    var totalPlastic = 0
                    var totalPaper = 0

                    recycleHistoryList.clear()
                    for(history in data!!){
                        if(history.userRequested.userId == FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(context!!)){
                            totalMetal += history.metalWeight
                            totalGlass += history.glassWeight
                            totalPlastic += history.plasticWeight
                            totalPaper += history.paperWeight
                            recycleHistoryList.add(history)
                        }
                    }

                    //update the total recycler material
                    tvPlasticAmount.text = "$totalPlastic KG"
                    tvGlassAmount.text = "$totalGlass KG"
                    tvMetalAmount.text = "$totalMetal KG"
                    tvPaperAmount.text = "$totalPaper KG"


                    recycleHistoryList.sortBy { data-> (data as RecycleRequestHistory).dateCollected.time }

                    if(recycleHistoryList.size >0){
                        recyclerViewAdapter.notifyDataSetChanged()
                    }else{
                        zeroStateContainer.visibility = View.VISIBLE
                        recycleRequestHistoryRecyclerView.visibility = View.GONE
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
        })
    }
}