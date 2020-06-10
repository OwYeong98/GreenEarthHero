package com.oymj.greenearthhero.ui.fragment

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
import com.oymj.greenearthhero.data.FoodDonationHistory
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import kotlinx.android.synthetic.main.fragment_food_donation_history.*

class FoodDonationHistoryFragment : Fragment() {

    var foodDonationHistoryList = ArrayList<Any>()
    lateinit var recyclerViewAdapter: UniversalAdapter
    lateinit var listener: ListenerRegistration


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_food_donation_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var myRecyclerView = view.findViewById<RecyclerView>(R.id.foodDonationHistoryRecyclerView)


        recyclerViewAdapter = object: UniversalAdapter(foodDonationHistoryList,context!!,myRecyclerView){
            override fun getVerticalSpacing(): Int {
                //20px spacing
                return 10
            }

            override fun onItemClickedListener(data: Any, clickType:Int) {
                if(data is FoodDonationHistory){

                }
            }

        }
        myRecyclerView.layoutManager = LinearLayoutManager(view.context)
        myRecyclerView.adapter = recyclerViewAdapter

        swipeLayout.setOnRefreshListener {
            getFoodDonationHistoryFromFirebase()
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

        listener = db.collection("Food_Donation_History").whereEqualTo("user_requested.userId",FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(context!!)).addSnapshotListener{
                snapshot,e->
            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null ) {
                //update the UI
                getFoodDonationHistoryFromFirebase()
            }
        }
    }

    private fun getFoodDonationHistoryFromFirebase(){
        zeroStateContainer.visibility = View.GONE
        foodDonationHistoryRecyclerView.visibility = View.VISIBLE

        foodDonationHistoryList.clear()
        recyclerViewAdapter.startSkeletalLoading(6,UniversalAdapter.SKELETAL_TYPE_3)

        FoodDonationHistory.getFoodDonationHistoryListByUserFromFirebase(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(context!!)!!,callback = {
                success,message,data->

            if(success){
                activity?.runOnUiThread {
                    recyclerViewAdapter.stopSkeletalLoading()
                    view?.findViewById<SwipeRefreshLayout>(R.id.swipeLayout)?.isRefreshing = false


                    var totalFoodDonated = data!!.fold(0,{prev,obj-> prev + obj.totalFoodAmount})

                    foodDonationHistoryList.clear()
                    for(history in data!!){
                        if(history.donatorUser.userId == FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(context!!)){

                            foodDonationHistoryList.add(history)
                        }
                    }

                    //update the total recycler material
                    tvTotalFoodContributed.text = totalFoodDonated.toString()

                    if(foodDonationHistoryList.size>0){
                        recyclerViewAdapter.notifyDataSetChanged()
                    }else{
                        zeroStateContainer.visibility = View.VISIBLE
                        foodDonationHistoryRecyclerView.visibility = View.GONE
                    }
                }
            }else{
                activity?.runOnUiThread {
                    recyclerViewAdapter.stopSkeletalLoading()
                    view?.findViewById<SwipeRefreshLayout>(R.id.swipeLayout)?.isRefreshing = false
                }

                var errorDialog = ErrorDialog(context!!,"Error when getting data from Firebase","Contact the developer. Error Code: $message")
                errorDialog.show()

            }
        })
    }
}