package com.oymj.greenearthhero.ui.activity

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.data.ChatRoom
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_my_chat.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyChatActivity : AppCompatActivity() {

    private var chatRoomList = ArrayList<Any>()
    private lateinit var recyclerViewAdapter: UniversalAdapter
    private var myOnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            when (v) {
                btnMenu->{
                    var intent = Intent(this@MyChatActivity, MenuActivity::class.java)
                    startActivity(intent)
                }
                btnNewChat->{
                    var intent = Intent(this@MyChatActivity, NewChatActivity::class.java)
                    startActivity(intent)
                }
                btnSearch->{

                }

            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_chat)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            revertMenuPageCircularRevealActivity()
        }

        setupUI()
        setupRecyclerView()
        linkAllButtonWithOnClickListener()

        getChatRoomFromFirebase()

        swipeLayout.setOnRefreshListener {
            getChatRoomFromFirebase()
        }


    }

    fun setupUI(){
        btnNewChat.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.mapboxGreen),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            20f,0)
    }

    fun getChatRoomFromFirebase(){
        GlobalScope.launch {
            chatRoomList.clear()
            recyclerViewAdapter.startSkeletalLoading(6,UniversalAdapter.SKELETAL_TYPE_4)

            ChatRoom.getChatRoomListByUserId(callback = {
                    success,message,data->

                if(success){
                    recyclerViewAdapter.stopSkeletalLoading()
                    swipeLayout.isRefreshing = false

                    chatRoomList.addAll(data!!)
                    runOnUiThread{
                        recyclerViewAdapter.notifyDataSetChanged()
                    }

                }else{
                    recyclerViewAdapter.stopSkeletalLoading()
                    swipeLayout.isRefreshing = false

                    var errorDialog = ErrorDialog(this@MyChatActivity,"Oops","Sorry, We have encountered some error when connecting with Firebase.")
                    errorDialog.show()
                }
            })
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            revertMenuPageCircularRevealActivity()
        }
    }

    private fun linkAllButtonWithOnClickListener() {
        //all button with onClick listener should be registered in this list
        val actionButtonViewList = listOf(
            btnMenu,
            btnNewChat,
            btnSearch
        )

        for (view in actionButtonViewList) {
            view.setOnClickListener(myOnClickListener)
        }
    }

    private fun setupRecyclerView(){
        recyclerViewAdapter = object: UniversalAdapter(chatRoomList,this@MyChatActivity,chatRoomRecyclerView){
            override fun getVerticalSpacing(): Int {
                //5px spacing
                return 5
            }
            override fun onItemClickedListener(data: Any, clickType:Int) {
                if(data is ChatRoom){


                }
            }
        }
        chatRoomRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRoomRecyclerView.adapter = recyclerViewAdapter
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