package com.oymj.greenearthhero.data

import com.google.gson.annotations.SerializedName

class TomTomPlacesResult {
    @SerializedName("address")
    var address:TomTomAddress? = null
    @SerializedName("position")
    var latLong:TomTomPosition? = null

}