package com.example.serviceyou.ui.base

import android.app.Application
import com.example.serviceyou.ui.connectivity.ApiServices
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * @author Divya Khanduri
 */
class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        create()
    }

    companion object {
        fun create(): ApiServices {

            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(
                    RxJava2CallAdapterFactory.create()
                )
                .addConverterFactory(
                    GsonConverterFactory.create()
                )
                .baseUrl("http://25.io/toau/audio/")
                .build()

            return retrofit.create(ApiServices::class.java)
        }
    }
}