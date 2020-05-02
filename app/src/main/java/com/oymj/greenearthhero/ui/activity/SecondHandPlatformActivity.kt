package com.oymj.greenearthhero.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.data.SecondHandItem
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_second_hand_platform.*

class SecondHandPlatformActivity : AppCompatActivity() {

    private var itemOnSaleList = ArrayList<Any>()
    private lateinit var recyclerViewAdapter: UniversalAdapter
    lateinit var listener: ListenerRegistration

    //Better control of onClickListener
    //all button action will be registered here
    private var myOnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            when (v) {
                btnMyShop->{
                    var intent = Intent(this@SecondHandPlatformActivity, CurrentPostAndSalesHistoryActivity::class.java)
                    startActivity(intent)
                }
                btnPurchaseHistory->{

                }

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second_hand_platform)

        linkAllButtonWithOnClickListener()
        setupUI()
        setupRecyclerView()

        swipeLayout.setOnRefreshListener {
            getItemListFromFirebase()
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

        listener = db.collection("Second_Hand_Item").whereEqualTo("userId", FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this)).addSnapshotListener{
                snapshot,e->
            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null ) {
                //update the UI
                getItemListFromFirebase()
            }
        }
    }

    private fun getItemListFromFirebase(){
        //clear previous data first
        itemOnSaleList.clear()

        //show loading skeletal first while getting data from firestore
        recyclerViewAdapter.startSkeletalLoading(7, UniversalAdapter.SKELETAL_TYPE_3)

        SecondHandItem.getItemListFromFirebase {
                success,message,data->

            if(success){
                recyclerViewAdapter.stopSkeletalLoading()
                swipeLayout.isRefreshing = false

                data!!.forEach { i-> itemOnSaleList.add(i)}

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

    fun setupRecyclerView(){
        recyclerViewAdapter = object: UniversalAdapter(itemOnSaleList,this@SecondHandPlatformActivity,itemOnSaleRecyclerView){
            override fun getVerticalSpacing(): Int {
                //20px spacing
                return 10
            }

            override fun onItemClickedListener(data: Any, clickType:Int) {
                if(data is SecondHandItem){

                }
            }
        }
        itemOnSaleRecyclerView.layoutManager = GridLayoutManager(this,2)
        itemOnSaleRecyclerView.adapter = recyclerViewAdapter
    }

    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            btnMyShop,
            btnPurchaseHistory
        )

        for (view in actionButtonViewList) {
            view.setOnClickListener(myOnClickListener)
        }
    }

    private fun setupUI(){
        btnPurchaseHistory.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.inputContainerGrey),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            25f, 0)

        btnMyShop.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.inputContainerGrey),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            25f, 0)

        sortBySpinner.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.darkgrey),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            10f, 0)

        btnMenu.background = RippleUtil.getRippleButtonOutlineDrawable(this,
        resources.getColor(R.color.transparent),
        resources.getColor(R.color.transparent_pressed),
        resources.getColor(R.color.transparent),
        100f, 0)

        btnSearch.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.transparent),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            100f, 0)
    }

}