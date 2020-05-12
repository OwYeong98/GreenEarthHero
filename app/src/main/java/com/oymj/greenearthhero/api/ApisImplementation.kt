package com.oymj.greenearthhero.api

import android.content.Context
import android.util.Log
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.oymj.greenearthhero.R
import com.oymj.greenearthhero.api.responses.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApisImplementation {

    companion object{

        fun getMapBoxApi():apis{
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

        fun getTomTomApi():apis{
            val logging = HttpLoggingInterceptor()

            logging.setLevel(HttpLoggingInterceptor.Level.BODY)

            val httpClient = OkHttpClient.Builder().addInterceptor(logging).build()


            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.tomtom.com/")
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            var service = retrofit.create(apis::class.java)

            return service
        }

        fun getPaymentServer():apis{
            val logging = HttpLoggingInterceptor()

            logging.setLevel(HttpLoggingInterceptor.Level.BODY)

            val httpClient = OkHttpClient.Builder().addInterceptor(logging).build()


            val retrofit = Retrofit.Builder()
                .baseUrl("https://greenearthhero-nodejs-server.herokuapp.com/api/")
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            var service = retrofit.create(apis::class.java)

            return service
        }

        fun <T> parseError(response:Response<out Any>) : BaseResponse<T>? {
            Log.d("wtf","error : ${response.errorBody().toString()}")
            val gson = Gson()
            val type = object : TypeToken<BaseResponse<Any>>() {}.type
            var errorResponse: BaseResponse<T>? = gson.fromJson(response.errorBody()!!.charStream(), type)

            return errorResponse
        }
    }

    fun searchAddress(context: Context, keyword:String, callback:(Boolean, SearchAddressResponse?)->Unit){
        var mapBoxToken = context.resources.getString(R.string.mapbox_access_token)

        var call = ApisImplementation.getMapBoxApi().searchAddress(keyword,mapBoxToken,"MY")

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

    fun geocodingFromTomTom(context: Context,keyword: String, callback:(Boolean, GeocodingTomTomResponse?)->Unit){
        var tomtomToken = context.resources.getString(R.string.tomtom_key)

        var call = ApisImplementation.getTomTomApi().geocodingFromTomTom(keyword,tomtomToken)

        call.enqueue(object : Callback<GeocodingTomTomResponse> {
            override fun onResponse(call: Call<GeocodingTomTomResponse>, response: Response<GeocodingTomTomResponse>) {
                if (response.code() == 200) {
                    callback(true,response.body())
                }else{
                    callback(false,response.body())
                }
            }
            override fun onFailure(call: Call<GeocodingTomTomResponse>, t: Throwable) {
                callback(false,null)
            }
        })
    }

    fun reverseGeocodingFromTomTom(context: Context,lat:Double, long:Double, callback:(Boolean, ReverseGeocodingTomTomResponse?)->Unit){
        var tomtomToken = context.resources.getString(R.string.tomtom_key)

        var call = ApisImplementation.getTomTomApi().reverseGeocodingFromTomTom(lat.toString(),long.toString(),tomtomToken)

        call.enqueue(object : Callback<ReverseGeocodingTomTomResponse> {
            override fun onResponse(call: Call<ReverseGeocodingTomTomResponse>, response: Response<ReverseGeocodingTomTomResponse>) {
                if (response.code() == 200) {
                    callback(true,response.body())
                }else{
                    callback(false,response.body())
                }
            }
            override fun onFailure(call: Call<ReverseGeocodingTomTomResponse>, t: Throwable) {
                callback(false,null)
            }
        })
    }

    fun initializePaymentIntent(context: Context,firebaseToken:String,itemIdToPurchase:String,locationId:String, callback:(Boolean, BaseResponse<InitializePaymentIntentResponse>?)->Unit){

        var call = ApisImplementation.getPaymentServer().initializePaymentIntent(firebaseToken,itemIdToPurchase,locationId)

        call.enqueue(object : Callback<BaseResponse<InitializePaymentIntentResponse>> {
            override fun onResponse(call: Call<BaseResponse<InitializePaymentIntentResponse>>, response: Response<BaseResponse<InitializePaymentIntentResponse>>) {
                if (response.isSuccessful) {
                    callback(true,response.body())
                }else{
                    var errorResponse:BaseResponse<InitializePaymentIntentResponse> = parseError(response)!!
                    callback(false,errorResponse!!)
                }
            }
            override fun onFailure(call: Call<BaseResponse<InitializePaymentIntentResponse>>, t: Throwable) {
                callback(false,null)
            }
        })
    }
}