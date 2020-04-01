package com.oymj.greenearthhero.api.responses

import com.google.gson.annotations.SerializedName
import com.oymj.greenearthhero.data.FeaturePlaces

class SearchAddressResponse{
    @SerializedName("query")
    var queryKeywords:List<String>? = null
    @SerializedName("features")
    var resultList:List<FeaturePlaces>? = null
}