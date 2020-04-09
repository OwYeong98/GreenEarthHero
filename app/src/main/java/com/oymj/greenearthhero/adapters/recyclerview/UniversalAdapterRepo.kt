package com.oymj.greenearthhero.adapters.recyclerview

import com.oymj.greenearthhero.adapters.recyclerview.recycleritem.RecyclerItemFeaturePlaces
import com.oymj.greenearthhero.adapters.recyclerview.UniversalRecyclerItem
import com.oymj.greenearthhero.adapters.recyclerview.recycleritem.RecyclerItemRecycleRequest
import com.oymj.greenearthhero.adapters.recyclerview.recycleritem.RecyclerItemSkeletalLoading
import com.oymj.greenearthhero.adapters.recyclerview.recycleritem.RecyclerItemSkeletalLoadingType2

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
    }

    fun registerRecyclerItem(recyclerItem: UniversalRecyclerItem){
        list.add(Pair(list.size+1, recyclerItem))

    }



}