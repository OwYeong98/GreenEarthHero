package com.oymj.greenearthhero.ui.activity

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.inputmethod.EditorInfo
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.adapters.recyclerview.UniversalAdapter
import com.oymj.greenearthhero.data.ChatRoom
import com.oymj.greenearthhero.ui.dialog.ErrorDialog
import com.oymj.greenearthhero.utils.FirebaseUtil
import com.oymj.greenearthhero.utils.RippleUtil
import kotlinx.android.synthetic.main.activity_my_chat.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyChatActivity : AppCompatActivity() {

    private var chatRoomList = ArrayList<Any>()
    private lateinit var recyclerViewAdapter: UniversalAdapter
    private lateinit var listener: ListenerRegistration
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
                    getChatRoomFromFirebase()
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

        listenToNewChatRoomListFromFirebase()

        swipeLayout.setOnRefreshListener {
            getChatRoomFromFirebase()
        }

        tvSearch.setOnEditorActionListener { v, actionId, event ->
            var handled=false
            if(actionId == EditorInfo.IME_ACTION_SEND){
                handled = true
                getChatRoomFromFirebase()
            }
            handled
        }


    }

    fun setupUI(){
        btnNewChat.background = RippleUtil.getRippleButtonOutlineDrawable(this,
            resources.getColor(R.color.mapboxGreen),
            resources.getColor(R.color.transparent_pressed),
            resources.getColor(R.color.transparent),
            20f,0)
    }


    fun listenToNewChatRoomListFromFirebase(){
        var db = FirebaseFirestore.getInstance()

        listener = db.collection("Chat_Room").whereArrayContains("chatUsers",FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this)!!).addSnapshotListener{
                snapshot,e->
            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null ) {
                getChatRoomFromFirebase()
            }
        }
    }

    fun getChatRoomFromFirebase(){
        GlobalScope.launch {
            chatRoomList.clear()
            runOnUiThread{
                recyclerViewAdapter.startSkeletalLoading(6,UniversalAdapter.SKELETAL_TYPE_4)
            }

            ChatRoom.getChatRoomListByUserIdWithoutMessages(FirebaseUtil.getUserIdAndRedirectToLoginIfNotFound(this@MyChatActivity)!!,callback = {
                    success,message,data->

                if(success){
                    recyclerViewAdapter.stopSkeletalLoading()
                    swipeLayout.isRefreshing = false

                    for(chatRoom in data!!){
                        if(chatRoom.chatUser1.getFullName().toLowerCase().trim().contains(tvSearch.text.toString().toLowerCase().trim())||
                            chatRoom.chatUser2.getFullName().toLowerCase().trim().contains(tvSearch.text.toString().toLowerCase().trim())){
                            chatRoomList.add(chatRoom)
                        }
                    }

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
                    var intent = Intent(this@MyChatActivity, ChatRoomActivity::class.java)
                    intent.putExtra("chatRoom",data)
                    startActivity(intent)
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