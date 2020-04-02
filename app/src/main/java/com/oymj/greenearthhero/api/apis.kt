package com.oymj.greenearthhero.api

import com.oymj.greenearthhero.api.responses.SearchAddressResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface apis {
    @GET("geocoding/v5/mapbox.places/{keyword}.json")
    fun searchAddress(@Path(value = "keyword", encoded = false) keyword:String, @Query("access_token") token: String,@Query("country") country: String): Call<SearchAddressResponse>
}