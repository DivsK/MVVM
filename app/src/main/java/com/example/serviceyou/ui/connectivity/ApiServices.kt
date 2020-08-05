package com.example.serviceyou.ui.connectivity

import okhttp3.ResponseBody
import retrofit2.Call

import retrofit2.http.GET

/**
 * @author Divya Khanduri
 */
interface ApiServices {
    // option 1: a resource relative to your base URL
    @GET("sample.txt")
    fun downloadFileWithFixedUrl(): Call<ResponseBody?>?
}