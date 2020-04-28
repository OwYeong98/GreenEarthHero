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
        registerRecyclerItem(RecyclerItemSkeletalLoadingType4())
        registerRecyclerItem(RecyclerItemRecycleHistory())
        registerRecyclerItem(RecyclerItemChatRoom())
        registerRecyclerItem(RecyclerItemChatMessage())
        registerRecyclerItem(RecyclerItemFoodEditable())
        registerRecyclerItem(RecyclerItemFoodDonation())
        registerRecyclerItem(RecyclerItemClaimFood())
        registerRecyclerItem(RecyclerItemFoodDonationHistory())
    }

    fun registerRecyclerItem(recyclerItem: UniversalRecyclerItem){
        list.add(Pair(list.size+1, recyclerItem))

    }



}