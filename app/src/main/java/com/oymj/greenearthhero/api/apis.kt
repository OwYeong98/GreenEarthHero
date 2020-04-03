package com.oymj.greenearthhero.api

import com.oymj.greenearthhero.api.responses.GeocodingTomTomResponse
import com.oymj.greenearthhero.api.responses.SearchAddressResponse
import com.oymj.greenearthhero.api.responses.ReverseGeocodingTomTomResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface apis {
    @GET("geocoding/v5/mapbox.places/{keyword}.json")
    fun searchAddress(@Path(value = "keyword", encoded = false) keyword:String, @Query("access_token") token: String,@Query("country") country: String): Call<SearchAddressResponse>

    @GET("search/2/geocode/{keyword}.json")
    fun geocodingFromTomTom(@Path(value = "keyword", encoded = false) keyword:String, @Query("key") key:String, @Query("countrySet") countryFilter:String = "MY"): Call<GeocodingTomTomResponse>

    @GET("search/2/reverseGeocode/{lat}%2C{long}.json")
    fun reverseGeocodingFromTomTom(@Path(value = "lat", encoded = false) lat:String,@Path(value = "long", encoded = false) long:String, @Query("key") key:String): Call<ReverseGeocodingTomTomResponse>
}