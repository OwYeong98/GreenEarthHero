package com.oymj.greenearthhero.adapters.recyclerview

import com.oymj.greenearthhero.adapters.recyclerview.UniversalRecyclerItem
import com.oymj.greenearthhero.adapters.recyclerview.recycleritem.*

object UniversalAdapterRepo {

    var list = arrayListOf<Pair<Int,UniversalRecyclerItem>>(

    )

    init{
        registerAllRecyclerItem()
    }

    fun registerAllRecyclerItem(){
        registerRecyclerItem(RecyclerItemFeaturePlaces())
        registerRecyclerItem(RecyclerItemSkeletalLoading())
        registerRecyclerItem(RecyclerItemRecycleRequest())
        registerRecyclerItem(RecyclerItemSkeletalLoadingType2())
        registerRecyclerItem(RecyclerItemSkeletalLoadingType3())
    }

    fun registerRecyclerItem(recyclerItem: UniversalRecyclerItem){
        list.add(Pair(list.size+1, recyclerItem))

    }



}