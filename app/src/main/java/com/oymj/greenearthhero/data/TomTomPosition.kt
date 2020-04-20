package com.oymj.greenearthhero.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class TomTomPosition : Serializable {
    @SerializedName("lat")
    var lat:Double? = null
    @SerializedName("lon")
    var lon:Double? = null

}