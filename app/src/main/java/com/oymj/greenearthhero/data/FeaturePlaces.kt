package com.oymj.greenearthhero.data

import com.google.gson.annotations.SerializedName

class FeaturePlaces {
    @SerializedName("query")
    var id:String? = null
    @SerializedName("text")
    var title:String? = null
    @SerializedName("place_name")
    var fullAddress:String? = null
    @SerializedName("center")
    var latLong:List<Double>? = null

    fun getLatitude(): Double{
        return latLong!![0]
    }

    fun getLongitude(): Double{
        return latLong!![1]
    }

}