package com.oymj.greenearthhero.data

import com.google.gson.annotations.SerializedName

class TomTomReverseGeocodeResult {
    @SerializedName("address")
    var address:TomTomAddress? = null
    @SerializedName("position")
    var latLong:String? = null

}