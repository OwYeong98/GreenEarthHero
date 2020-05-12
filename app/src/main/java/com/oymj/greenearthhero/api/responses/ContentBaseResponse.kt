package com.oymj.greenearthhero.api.responses

import com.google.gson.annotations.SerializedName

class ContentBaseResponse<T> {
    @SerializedName("data")
    var data:T? = null
    @SerializedName("error")
    var error:List<String>? = listOf()
    @SerializedName("message")
    var message:String? = ""
}