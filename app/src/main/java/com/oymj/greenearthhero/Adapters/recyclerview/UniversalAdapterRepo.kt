package com.oymj.greenearthhero.adapters.recyclerview

import com.oymj.greenearthhero.adapters.recyclerview.recycleritem.RecyclerItemFeaturePlaces
import com.oymj.greenearthhero.adapters.recyclerview.UniversalRecyclerItem
import com.oymj.greenearthhero.adapters.recyclerview.recycleritem.RecyclerItemSkeletalLoading

object UniversalAdapterRepo {

    var list = arrayListOf<Pair<Int,UniversalRecyclerItem>>(
//        Pair<Int,UniversalRecyclerItem>(1,
//            RecyclerItemFeaturePlaces()
//        )
    )

    init{
        registerAllRecyclerItem()
    }

    fun registerAllRecyclerItem(){
        registerRecyclerItem(RecyclerItemFeaturePlaces())
        registerRecyclerItem(RecyclerItemSkeletalLoading())
    }

    fun registerRecyclerItem(recyclerItem: UniversalRecyclerItem){
        list.add(Pair(list.size+1, recyclerItem))

    }



}