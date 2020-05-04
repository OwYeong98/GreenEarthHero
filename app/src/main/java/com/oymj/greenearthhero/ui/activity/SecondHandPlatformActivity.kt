package com.oymj.greenearthhero.ui.activity

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.adapters.recyclerview.recycleritem.RecyclerItemSecondHandItem
import com.oymj.greenearthhero.adapters.spinner.TimeAvailableSpinnerAdapter
import com.oymj.greenearthhero.data.SecondHandItem
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_food_donation.*
import kotlinx.android.synthetic.main.activity_second_hand_platform.*
import kotlinx.android.synthetic.main.activity_second_hand_platform.btnMenu
import kotlinx.android.synthetic.main.activity_second_hand_platform.rootLayout

class SecondHandPlatformActivity : AppCompatActivity() {

    private var itemOnSaleFullList = ArrayList<SecondHandItem>()
    private var itemOnSaleList = ArrayList<Any>()
    private lateinit var recyclerViewAdapter: UniversalAdapter
    lateinit var listener: ListenerRegistration

    //Better control of onClickListener
    //all button action will be registered here
    private var myOnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            when (v) {
                btnMenu->{
                    var intent = Intent(this@SecondHandPlatformActivity , MenuActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    startActivity(intent)
                }
                btnMyShop->{
                    var intent = Intent(this@SecondHandPlatformActivity, CurrentPostAndSalesHistoryActivity::class.java)
                    startActivity(intent)
                }
                btnPurchaseHistory->{

                }
                btnSearch->{
                    showSearchContainer()
                }
                btnCancel->{
                    hideSearchContainer()
                }
                btnConfirmSearch->{
                    itemOnSaleList.clear()

                    itemOnSaleFullList.forEach({item-> if(item.itemName.toLowerCase().trim().replace(" ","").contains(inputSearch.text.toString().toLowerCase().trim().replace(" ",""))) itemOnSaleList.add(item)})
                    recyclerViewAdapter.notifyDataSetChanged()

                    tvSearchedKeyword.visibility = View.VISIBLE
                    tvSearchedKeyword.text = "Search Result for ${inputSearch.text}"

                    hideSearchContainer()
                }
                btnClearSearch->{
                    tvSearchedKeyword.visibility = View.GONE
                    inputSearch.setText("")
                    itemOnSaleList.clear()

                    //if the item is not posted by current logged in user then add to the list
                    itemOnSaleFullList.forEach{item-> if(item.postedByUser.userId != FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this@SecondHandPlatformActivity)) itemOnSaleList.add(item)}
                    recyclerViewAdapter.notifyDataSetChanged()
                }

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second_hand_platform)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            revertMenuPageCircularRevealActivity()
        }

        linkAllButtonWithOnClickListener()
        setupUI()
        setupSpinner()
        setupRecyclerView()


        swipeLayout.setOnRefreshListener {
            getItemListFromFirebase()
        }

        inputSearch.setOnEditorActionListener { v, actionId, event ->
            var handled=false
            if(actionId == EditorInfo.IME_ACTION_SEND){
                handled = true
                btnConfirmSearch.performClick()
            }
            handled
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

    private fun showSearchContainer(){
        val slideDownAnimation: Animation = AnimationUtils.loadAnimation(applicationContext,
            R.anim.slide_down_show
        )

        searchContainer.visibility= View.VISIBLE
        btnSearch.visibility = View.GONE
        searchContainer.startAnimation(slideDownAnimation)
    }

    private fun hideSearchContainer(){
        val slideUpAnimation: Animation = AnimationUtils.loadAnimation(applicationContext,
            R.anim.slide_up_hide
        )

        slideUpAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                searchContainer.visibility = View.GONE
            }

            override fun onAnimationStart(animation: Animation?) {
            }
        })

        searchContainer.startAnimation(slideUpAnimation)
        btnSearch.visibility = View.VISIBLE
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
        itemOnSaleFullList.clear()

        //show loading skeletal first while getting data from firestore
        recyclerViewAdapter.startSkeletalLoading(7, UniversalAdapter.SKELETAL_TYPE_5)

        SecondHandItem.getItemListFromFirebase {
                success,message,data->

            if(success){
                recyclerViewAdapter.stopSkeletalLoading()
                swipeLayout.isRefreshing = false

                itemOnSaleFullList.addAll(data!!)
                data!!.forEach { i-> itemOnSaleList.add(i)}

                var sortBy = sortBySpinner.selectedItem as String
                when(sortBy){
                    "Latest Post"->{
                        itemOnSaleList.sortBy { current-> if(current is SecondHandItem) current.datePosted.time else 0 }
                    }
                    "Oldest Post"->{
                        itemOnSaleList.sortByDescending { current-> if(current is SecondHandItem) current.datePosted.time else 0}
                    }
                    "Price Asc"->{
                        itemOnSaleList.sortBy { current-> if(current is SecondHandItem) current.itemPrice else 0.0 }
                    }
                    "Price Desc"->{
                        itemOnSaleList.sortByDescending { current-> if(current is SecondHandItem) current.itemPrice else 0.0 }
                    }
                }

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

    private fun setupRecyclerView(){
        recyclerViewAdapter = object: UniversalAdapter(itemOnSaleList,this@SecondHandPlatformActivity,itemOnSaleRecyclerView){
            override fun getVerticalSpacing(): Int {
                //20px spacing
                return 10
            }

            override fun getHorizontalSpacing(): Int {
                return 10
            }

            override fun onItemClickedListener(data: Any, clickType:Int) {
                if(data is SecondHandItem){
                    var intent = Intent(this@SecondHandPlatformActivity,BuyerViewItemDetailActivity::class.java)
                    intent.putExtra("itemId",data.id)
                    startActivity(intent)
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
                    RecyclerItemSecondHandItem().getViewHolder(parent,context,this)
                }else{
                    super.onCreateViewHolder(parent, viewType)
                }
            }
        }
        itemOnSaleRecyclerView.layoutManager = GridLayoutManager(this,2)
        itemOnSaleRecyclerView.adapter = recyclerViewAdapter
    }

    private fun setupSpinner(){
        var sortByList = arrayListOf<String>()
        sortByList.add("Latest Post")
        sortByList.add("Oldest Post")
        sortByList.add("Price Asc")
        sortByList.add("Price Desc")

        sortBySpinner.adapter = TimeAvailableSpinnerAdapter(this,sortByList)

        //refresh when sort by selected
        sortBySpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                var sortBy = sortBySpinner.selectedItem as String
                when(sortBy){
                    "Latest Post"->{
                        itemOnSaleList.sortByDescending { current-> if(current is SecondHandItem) current.datePosted.time else 0 }
                        recyclerViewAdapter.notifyDataSetChanged()
                    }
                    "Oldest Post"->{
                        itemOnSaleList.sortBy { current-> if(current is SecondHandItem) current.datePosted.time else 0 }
                        recyclerViewAdapter.notifyDataSetChanged()
                    }
                    "Price Asc"->{
                        itemOnSaleList.sortBy { current-> if(current is SecondHandItem) current.itemPrice else 0.0 }
                        recyclerViewAdapter.notifyDataSetChanged()
                    }
                    "Price Desc"->{
                        itemOnSaleList.sortByDescending { current-> if(current is SecondHandItem) current.itemPrice else 0.0 }
                        recyclerViewAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        sortBySpinner.setSelection(0)
    }


    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            btnMyShop,
            btnPurchaseHistory,
            btnMenu,
            btnSearch,
            btnCancel,
            btnConfirmSearch,
            btnClearSearch
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
        200f, 0)

        btnSearch.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.transparent),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            200f, 0)

        btnCancel.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.transparent),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            200f, 0)

        btnConfirmSearch.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.transparent),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            200f, 0)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun revertMenuPageCircularRevealActivity() {


        //add menu view so that we can revert reveal it
        var layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var inflatedView = layoutInflater.inflate(R.layout.activity_menu,null) as ConstraintLayout
        inflatedView.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT)
        rootLayout.addView(inflatedView)

        inflatedView.post {
            runOnUiThread{
                val cx: Int = inflatedView.left + getDips(16) + getDips(56/2)
                val cy: Int = inflatedView.top + getDips(16) + getDips(56/2)
                val finalRadius: Float = Math.max(inflatedView.width, inflatedView.height).toFloat()
                val circularReveal =
                    ViewAnimationUtils.createCircularReveal(inflatedView, cx, cy, finalRadius, 0f)
                circularReveal.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animator: Animator) {}
                    override fun onAnimationEnd(animator: Animator) {
                        rootLayout.removeView(inflatedView)
                    }

                    override fun onAnimationCancel(animator: Animator) {}
                    override fun onAnimationRepeat(animator: Animator) {}
                })
                circularReveal.duration = 1000
                circularReveal.start()
            }
        }
    }

    private fun getDips(dps: Int): Int {
        val resources: Resources = resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dps.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

}