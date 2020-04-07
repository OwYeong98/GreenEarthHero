package com.oymj.greenearthhero.adapters.recyclerview

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.view.marginLeft
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.data.SkeletalEmptyModel
import java.lang.Exception

open class UniversalAdapter(val data : ArrayList<Any>, val context: Context,val recyclerView:RecyclerView) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var shimmerEffectFrameLayout: ShimmerFrameLayout

    init{
        var parent = recyclerView.parent as ViewGroup

        if(parent != null){

            //create a facebook shimmer framelayout to wrap the recycler view so that we start shimmer animation later
            shimmerEffectFrameLayout = ShimmerFrameLayout(context)
            shimmerEffectFrameLayout.duration = 700
            shimmerEffectFrameLayout.layoutParams = recyclerView.layoutParams
            parent.removeView(recyclerView)
            shimmerEffectFrameLayout.addView(recyclerView)
            parent.addView(shimmerEffectFrameLayout)
            shimmerEffectFrameLayout.startShimmerAnimation()

        }
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
            throw Exception()
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
    open fun onItemClickedListener(data:Any):Unit{
    }

    fun startSkeletalLoading(numberOfSkeletal:Int){
        for(x in 0..numberOfSkeletal){
            var skeletalModel = SkeletalEmptyModel()
            data.add(0,skeletalModel)
        }
        notifyDataSetChanged()

        shimmerEffectFrameLayout.startShimmerAnimation()


    }

    fun stopSkeletalLoading(){
        var iterator = data.iterator()

        while (iterator.hasNext()){
            if(iterator.next() is SkeletalEmptyModel)
                iterator.remove()
        }

        shimmerEffectFrameLayout.stopShimmerAnimation()
    }

}
