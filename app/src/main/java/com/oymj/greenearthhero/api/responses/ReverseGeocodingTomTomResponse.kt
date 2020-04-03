package com.oymj.greenearthhero.api.responses

import com.google.gson.annotations.SerializedName
import com.oymj.greenearthhero.data.TomTomPlacesResult
import com.oymj.greenearthhero.data.TomTomReverseGeocodeResult

class ReverseGeocodingTomTomResponse {
    @SerializedName("addresses")
    var addressResult:List<TomTomReverseGeocodeResult>? = null
}