package com.oymj.greenearthhero.api

import android.content.Context
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.api.responses.SearchAddressResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApisImplementation {

    companion object{

        fun getApi():apis{
            val retrofit = Retrofit.Builder()
                .baseUrl("")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            var service = retrofit.create(apis::class.java)

            return service
        }
    }

    fun searchAddress(context: Context, keyword:String, callback:(Boolean, SearchAddressResponse?)->Unit){
        var mapBoxToken = context.resources.getString(R.string.mapbox_access_token)

        var call = ApisImplementation.getApi().searchAddress(keyword,mapBoxToken)

        call.enqueue(object : Callback<SearchAddressResponse> {
            override fun onResponse(call: Call<SearchAddressResponse>, response: Response<SearchAddressResponse>) {
                if (response.code() == 200) {
                    callback(true,response.body())
                }else{
                    callback(false,response.body())
                }
            }
            override fun onFailure(call: Call<SearchAddressResponse>, t: Throwable) {
                callback(false,null)
            }
        })

    }
}