package com.oymj.greenearthhero.api

import android.content.Context
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.api.responses.SearchAddressResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.logging.Level

class ApisImplementation {

    companion object{

        fun getApi():apis{
            val logging = HttpLoggingInterceptor()

            logging.setLevel(HttpLoggingInterceptor.Level.BODY)

            val httpClient = OkHttpClient.Builder().addInterceptor(logging).build()


            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.mapbox.com/")
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            var service = retrofit.create(apis::class.java)

            return service
        }
    }

    fun searchAddress(context: Context, keyword:String, callback:(Boolean, SearchAddressResponse?)->Unit){
        var mapBoxToken = context.resources.getString(R.string.mapbox_access_token)

        var call = ApisImplementation.getApi().searchAddress(keyword,mapBoxToken,"MY")

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