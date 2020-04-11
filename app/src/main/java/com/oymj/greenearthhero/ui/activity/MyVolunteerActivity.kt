package com.oymj.greenearthhero.ui.activity

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapterRepo
import com.oymj.greenearthhero.adapters.recyclerview.recycleritem.RecyclerItemMyVolunteerCollectionRequest
import com.oymj.greenearthhero.data.RecycleRequest
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import kotlinx.android.synthetic.main.activity_my_volunteer.*

class MyVolunteerActivity : AppCompatActivity() {

    private lateinit var recyclerViewAdapter: UniversalAdapter

    private var myVolunteerList = ArrayList<Any>()

    //Better control of onClickListener
    //all button action will be registered here
    private var myOnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            when (v) {
                btnBackToHome->{
                    finish()
                    overridePendingTransition(R.anim.freeze, R.anim.slide_down_slow)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_volunteer)


        setupRecyclerView()
        linkAllButtonWithOnClickListener()
        getRecyclerRequestFromFirebase()

    }

    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            btnBackToHome
        )

        for (view in actionButtonViewList) {
            view.setOnClickListener(myOnClickListener)
        }
    }

    private fun getRecyclerRequestFromFirebase(){
        //clear previous data first
        myVolunteerList.clear()

        //show loading skeletal first while getting data from firestore
        recyclerViewAdapter.startSkeletalLoading(7, UniversalAdapter.SKELETAL_TYPE_3)

        RecycleRequest.getRecycleRequestFromFirebase{
                success,message,data ->

            if(success){
                recyclerViewAdapter.stopSkeletalLoading()

                //filter only show request that are requested by the current logged in user
                for(request in data!!){
                    if(request.requestedUser.userId == FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this)){
                        myVolunteerList.add(request)
                    }
                }

                //sort by near to far
                myVolunteerList.sortBy { obj-> (obj as RecycleRequest).getDistanceBetween() }

                //refresh recyclerview
                recyclerViewAdapter.notifyDataSetChanged()


            }else{
                recyclerViewAdapter.stopSkeletalLoading()

                var errorDialog = ErrorDialog(this,"Error when getting data from Firebase","Contact the developer. Error Code: $message")
                errorDialog.show()
            }

        }


    }


    private fun setupRecyclerView(){
        recyclerViewAdapter = object: UniversalAdapter(myVolunteerList,this@MyVolunteerActivity,myVolunteerRecyclerView){
            override fun getVerticalSpacing(): Int {
                //20px spacing
                return 10
            }
            override fun onItemClickedListener(data: Any, clickType:Int) {
                if(data is RecycleRequest){
                    //clicktype 1 is cancel request
                    if(clickType == 1){

                    }else if(clickType ==2){
                        //click type 2 is chat with requester

                    }

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
                    RecyclerItemMyVolunteerCollectionRequest().getViewHolder(parent,context,this)
                }else{
                    super.onCreateViewHolder(parent, viewType)
                }
            }
        }
        myVolunteerRecyclerView.layoutManager = LinearLayoutManager(this)
        myVolunteerRecyclerView.adapter = recyclerViewAdapter
    }

    override fun onEnterAnimationComplete() {
        super.onEnterAnimationComplete()

        Handler().postDelayed({
            runOnUiThread {
                showBackToHomeButton()
            }
        },200)

    }

    private fun showBackToHomeButton(){
        val slideUpAnimation: Animation = AnimationUtils.loadAnimation(applicationContext,
            R.anim.slide_up
        )
        btnBackToHome.visibility= View.VISIBLE
        btnBackToHome.startAnimation(slideUpAnimation)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.freeze,R.anim.slide_down_slow)
    }
}