package com.oymj.greenearthhero.api.responses

import com.google.gson.annotations.SerializedName
import com.oymj.greenearthhero.data.TomTomPlacesResult

class BaseResponse<T> {

    @SerializedName("code")
    var code:String? = ""
    @SerializedName("http_code")
    var httpCode:Int? = 0
    @SerializedName("content")
    var content:ContentBaseResponse<T>? = null
}