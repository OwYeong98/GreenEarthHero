package com.oymj.greenearthhero.api.responses

import com.google.gson.annotations.SerializedName
import com.oymj.greenearthhero.data.TomTomPlacesResult

class InitializePaymentIntentResponse {
    @SerializedName("secret")
    var secretKey:String? = ""
}