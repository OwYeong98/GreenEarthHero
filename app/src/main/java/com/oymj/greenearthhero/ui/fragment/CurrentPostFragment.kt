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
import com.oymj.greenearthhero.adapters.recyclerview.recycleritem.RecyclerItemCurrentItemPost
import com.oymj.greenearthhero.adapters.recyclerview.recycleritem.RecyclerItemSecondHandItem
import com.oymj.greenearthhero.data.SecondHandItem
import com.oymj.greenearthhero.ui.activity.CurrentPostDetailActivity
import com.oymj.greenearthhero.ui.activity.PostNewItemActivity
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import kotlinx.android.synthetic.main.fragment_current_post.*

class CurrentPostFragment : Fragment() {

    var currentPostList = ArrayList<Any>()
    lateinit var recyclerViewAdapter: UniversalAdapter
    lateinit var listener: ListenerRegistration


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_current_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var myRecyclerView = view.findViewById<RecyclerView>(R.id.currentPostRecyclerView)


        recyclerViewAdapter = object: UniversalAdapter(currentPostList,context!!,myRecyclerView){
            override fun getVerticalSpacing(): Int {
                //20px spacing
                return 10
            }

            override fun onItemClickedListener(data: Any, clickType:Int) {
                if(data is SecondHandItem){
                    var intent = Intent(context!!,CurrentPostDetailActivity::class.java)
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
                    RecyclerItemCurrentItemPost().getViewHolder(parent,context,this)
                }else{
                    super.onCreateViewHolder(parent, viewType)
                }
            }
        }
        myRecyclerView.layoutManager = LinearLayoutManager(view.context)
        myRecyclerView.adapter = recyclerViewAdapter

        swipeLayout.setOnRefreshListener {
            getCurrentPostFromFirebase()
        }

        btnAdd.setOnClickListener {
            var intent = Intent(context!!,PostNewItemActivity::class.java)
            context!!.startActivity(intent)
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

        listener = db.collection("Second_hand_Item").whereEqualTo("postedBy", FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(context!!)).addSnapshotListener{
                snapshot,e->
            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null ) {
                //update the UI
                getCurrentPostFromFirebase()
            }
        }
    }

    private fun getCurrentPostFromFirebase(){
        zeroStateContainer.visibility = View.GONE
        currentPostRecyclerView.visibility = View.VISIBLE

        //clear previous data first
        currentPostList.clear()

        //show loading skeletal first while getting data from firestore
        recyclerViewAdapter.startSkeletalLoading(7, UniversalAdapter.SKELETAL_TYPE_3)

        SecondHandItem.getItemListOnSaleOfUserFromFirebase(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(context!!)!!){
                success,message,data ->

            if(success){
                activity?.runOnUiThread {
                    recyclerViewAdapter.stopSkeletalLoading()
                    view?.findViewById<SwipeRefreshLayout>(R.id.swipeLayout)?.isRefreshing = false

                    data!!.forEach { item-> currentPostList.add(item) }

                    currentPostList.sortByDescending { item-> if(item is SecondHandItem) item.datePosted.time else 0 }

                    if(currentPostList.size > 0){
                        //refresh recyclerview
                        recyclerViewAdapter.notifyDataSetChanged()
                    }else{
                        currentPostRecyclerView.visibility = View.GONE
                        zeroStateContainer.visibility = View.VISIBLE
                    }

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