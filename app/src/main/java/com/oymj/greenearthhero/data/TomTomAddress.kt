package com.oymj.greenearthhero.data

import com.google.gson.annotations.SerializedName

class TomTomAddress {
    @SerializedName("streetName")
    var street:String? = null
    @SerializedName("postalCode")
    var postalCode:String? = null
    @SerializedName("municipalitySubdivision")
    var taman:String? = null
    @SerializedName("countrySubdivision")
    var state:String? = null
    @SerializedName("municipality")
    var city:String? = null
    @SerializedName("freeformAddress")
    var fullAddress:String? = null
}