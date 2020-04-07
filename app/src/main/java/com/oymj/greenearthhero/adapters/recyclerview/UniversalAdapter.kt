package com.oymj.greenearthhero.adapters.recyclerview

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.oymj.greenearthhero.data.SkeletalEmptyModel
import java.lang.Exception

open class UniversalAdapter(val data : ArrayList<Any>, val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
            data.add(0,SkeletalEmptyModel())
        }
        notifyDataSetChanged()
    }

    fun stopSkeletalLoading(){
        var iterator = data.iterator()

        while (iterator.hasNext()){
            if(iterator.next() is SkeletalEmptyModel)
                iterator.remove()
        }
    }

}
