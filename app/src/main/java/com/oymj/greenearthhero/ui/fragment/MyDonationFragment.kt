package com.oymj.greenearthhero.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.adapters.recyclerview.recycleritem.RecyclerItemMyDonation
import com.oymj.greenearthhero.data.FoodDonation
import com.oymj.greenearthhero.ui.activity.FoodDonationDetailActivity
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.ui.dialog.YesOrNoDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import kotlinx.android.synthetic.main.fragment_current_request.*

class MyDonationFragment : Fragment() {

    var currentDonationList = ArrayList<Any>()
    lateinit var recyclerViewAdapter: UniversalAdapter
    lateinit var listener: ListenerRegistration


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_donation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var myRecyclerView = view.findViewById<RecyclerView>(R.id.currentDonationRecyclerView)


        recyclerViewAdapter = object: UniversalAdapter(currentDonationList,context!!,myRecyclerView){
            override fun getVerticalSpacing(): Int {
                //20px spacing
                return 10
            }

            override fun onItemClickedListener(data: Any, clickType:Int) {
                if(data is FoodDonation){
                    var intent = Intent(context!!,FoodDonationDetailActivity::class.java)
                    intent.putExtra("foodDonationId",data.id)
                    startActivity(intent)

                }
            }

            //override the view type to return -1 cause we want to choose recycler item mannually
            override fun getItemViewType(position: Int): Int {
                return if(data.get(position)::class.java.simpleName == "FoodDonation"){
                    -1
                }else {
                    super.getItemViewType(position)
                }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                return if(viewType == -1){
                    RecyclerItemMyDonation().getViewHolder(parent,context,this)
                }else{
                    super.onCreateViewHolder(parent, viewType)
                }
            }

        }
        myRecyclerView.layoutManager = LinearLayoutManager(view.context)
        myRecyclerView.adapter = recyclerViewAdapter

        swipeLayout.setOnRefreshListener {
            getFoodDonationFromFirebase()
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

        listener = db.collection("Food_Donation").whereEqualTo("donatorUserId", FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(context!!)).addSnapshotListener{
                snapshot,e->
            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null ) {
                //update the UI
                getFoodDonationFromFirebase()
            }
        }
    }

    private fun getFoodDonationFromFirebase(){
        //clear previous data first
        currentDonationList.clear()

        //show loading skeletal first while getting data from firestore
        recyclerViewAdapter.startSkeletalLoading(7, UniversalAdapter.SKELETAL_TYPE_3)

        FoodDonation.getFoodDonationListWithoutFoodListByUserFromFirebase(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(context!!)!!){
                success,message,data ->

            if(success){
                activity?.runOnUiThread {
                    recyclerViewAdapter.stopSkeletalLoading()
                    view?.findViewById<SwipeRefreshLayout>(R.id.swipeLayout)?.isRefreshing = false

                    currentDonationList.clear()
                    currentDonationList.addAll(data!!)


                    //refresh recyclerview
                    recyclerViewAdapter.notifyDataSetChanged()
                }

            }else{
                activity?.runOnUiThread {
                    recyclerViewAdapter.stopSkeletalLoading()
                    view?.findViewById<SwipeRefreshLayout>(R.id.swipeLayout)?.isRefreshing = false

                    var errorDialog = ErrorDialog(context!!,"Error when getting data from Firebase","Contact the developer. Error Code: $message")
                    errorDialog.show()
                }
            }

        }
    }
}