package com.oymj.greenearthhero.adapters.recyclerview

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.core.view.marginLeft
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.data.SkeletalEmptyModel
import com.oymj.greenearthhero.data.SkeletalEmptyModel2
import java.lang.Exception

open class UniversalAdapter(val data : ArrayList<Any>, val context: Context,val recyclerView:RecyclerView) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object{
        const val SKELETAL_TYPE_1:Int = 1
        const val SKELETAL_TYPE_2:Int = 2
    }

//    lateinit var shimmerEffectFrameLayout: ShimmerFrameLayout

    init{
//        var parent = recyclerView.parent as ViewGroup
//
//        if(parent != null){
//
//            //create a facebook shimmer framelayout to wrap the recycler view so that we start shimmer animation later
//            shimmerEffectFrameLayout = ShimmerFrameLayout(context)
//            shimmerEffectFrameLayout.duration = 700
//            shimmerEffectFrameLayout.layoutParams = recyclerView.layoutParams
//            parent.removeView(recyclerView)
//            recyclerView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
//            shimmerEffectFrameLayout.addView(recyclerView)
//            parent.addView(shimmerEffectFrameLayout)
//        }

        //add spacing
        recyclerView.addItemDecoration(SpacingItemDecorator(
            getHorizontalSpacing(),getVerticalSpacing()
        ))
    }

    open fun getHorizontalSpacing():Int{
        return 0
    }

    open fun getVerticalSpacing():Int{
        return 0
    }

    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        var itemId = -1
        for((id,recyclerItem) in UniversalAdapterRepo.list){
            if(data.get(position)::class.java.simpleName == recyclerItem.dataClassName){
                itemId = id
            }
        }

        if(itemId != -1){
            return itemId
        }else{
            throw RecyclerItemNotRegisteredInUniversalAdapterRepoException()
        }
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var foundRecyclerItem: UniversalRecyclerItem? = null

        for((id, recyclerItem) in UniversalAdapterRepo.list){
            if(id == viewType){
                foundRecyclerItem = recyclerItem
            }
        }

        if(foundRecyclerItem != null){
            return foundRecyclerItem!!.getViewHolder(parent,context,this)!!
        }else{
            throw Exception()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var currentData = data[position]
        (holder as UniversalViewHolder).onBindViewHolder(currentData)

    }

    //can be override to get onclick
    open fun onItemClickedListener(data:Any,clickType:Int):Unit{
    }

    fun startSkeletalLoading(numberOfSkeletal:Int,skeletalType:Int){
        for(x in 0..numberOfSkeletal){

            var skeletalModel:Any = SkeletalEmptyModel()

            when(skeletalType){
                SKELETAL_TYPE_1-> skeletalModel = SkeletalEmptyModel()
                SKELETAL_TYPE_2-> skeletalModel = SkeletalEmptyModel2()
            }


            data.add(0,skeletalModel)
        }
        notifyDataSetChanged()

//        shimmerEffectFrameLayout.startShimmerAnimation()


    }

    fun stopSkeletalLoading(){
        var iterator = data.iterator()

        while (iterator.hasNext()){
            var currentItem =iterator.next()
            if(currentItem is SkeletalEmptyModel || currentItem is SkeletalEmptyModel2)
                iterator.remove()
        }

//        shimmerEffectFrameLayout.stopShimmerAnimation()
    }

    inner class SpacingItemDecorator(private val horizontalSpacing: Int,private val verticalSpacing: Int): RecyclerView.ItemDecoration(){
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)

            outRect.right = horizontalSpacing
            outRect.left = horizontalSpacing

            if(parent.getChildLayoutPosition(view) == 0){
                outRect.top = verticalSpacing
            }

            outRect.bottom = verticalSpacing
        }
    }

    inner class RecyclerItemNotRegisteredInUniversalAdapterRepoException: Exception()

}
