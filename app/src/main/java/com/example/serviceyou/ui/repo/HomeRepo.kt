package com.example.serviceyou.ui.repo

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import com.example.serviceyou.ui.connectivity.ApiServices
import com.example.serviceyou.ui.dao.ScannedDevicesDao
import com.example.serviceyou.ui.data.ScannedDevices
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

/**
 * @author Divya Khanduri
 */

class HomeRepo(
    private val apiService: ApiServices?,
    private val scannedDevicesDao: ScannedDevicesDao?,
    private val context: Context
) {

    fun getScannedDevicesFromDb(onDataReadyCallback: OnDataReadyCallback): List<ScannedDevices> {
        if (scannedDevicesDao != null) {
            object : AsyncTask<Void?, Long?, Void?>() {
                private lateinit var sd: List<ScannedDevices>

                override fun doInBackground(vararg params: Void?): Void? {
                    onDataReadyCallback.onDataReady(scannedDevicesDao.getScannedDevices() as ArrayList<ScannedDevices>)
                    return null
                }
            }.execute()
        }
        return emptyList()
    }

    fun getFileFromUrl(onDataReadyCallback: OnDataReadyCallback) {
        val call =
            apiService?.downloadFileWithFixedUrl()?.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>?,
                    response: Response<ResponseBody?>
                ) {
                    if (response.isSuccessful()) {
                        Log.d("TAG", "Got the body for the file")
                        object : AsyncTask<Void?, Long?, Void?>() {
                            override fun doInBackground(vararg params: Void?): Void? {
                                saveToDisk(response.body(), context, onDataReadyCallback)
                                return null
                            }
                        }.execute()
                    } else {
                        Log.d("TAG", "Connection failed " + response.errorBody())
                    }
                }

                override fun onFailure(call: Call<ResponseBody?>?, t: Throwable) {
                    t.printStackTrace()
                    Log.e("TAG", t.message)
                }
            })
        return call!!
    }

    fun saveToDisk(
        body: ResponseBody?,
        context: Context,
        onDataReadyCallback: OnDataReadyCallback
    ) {
        try {
            val fileName = "Sample_${System.currentTimeMillis()}.txt"
            val destinationFile = File(context.filesDir, "/${fileName}")
            var `is`: InputStream? = null
            var os: OutputStream? = null
            try {
                Log.d("TAG", "File Size=" + body?.contentLength())
                `is` = body?.byteStream()
                os = FileOutputStream(destinationFile)
                val data = ByteArray(4096)
                var count = 0
                var progress = 0
                while (`is`?.read(data).also({ count = it!! }) != -1) {
                    os.write(data, 0, count)
                    progress += count
                    Log.d(
                        "TAG",
                        "Progress: " + progress + "/" + body?.contentLength() + " >>>> " + progress.toFloat() / body?.contentLength()!!
                    )
                }
                os.flush()
                Log.d("TAG", "File saved successfully!")
                onDataReadyCallback.onDataReady(fileName)
                return
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d("TAG", "Failed to save the file!")
                return
            } finally {
                if (`is` != null) `is`.close()
                if (os != null) os.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("TAG", "Failed to save the file!")
            return
        }
    }

    fun saveInDb(
        name: String?,
        address: String?,
        onDataReadyCallback: OnDataReadyCallback
    ) {
        val scannedDevicesList: ArrayList<ScannedDevices>? = arrayListOf()
        Observable.fromCallable {
            val dao = scannedDevicesDao

            val scannedDevices =
                ScannedDevices(deviceName = name.toString(), deviceAddress = address.toString())
            scannedDevicesList?.add(scannedDevices)
            with(dao) {
                if (!this?.getScannedDevices()?.isEmpty()!!) {
                    this.deleteScannedDevices()
                }
                this.insertScannedDevices(scannedDevices)
            }
            dao?.getScannedDevices()
        }.doOnNext({ list ->
            var finalString = ""
            list?.map {
                finalString += it.deviceName + " - " + it.deviceAddress
            }
            onDataReadyCallback.onDataReady(scannedDevicesList)
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    interface OnDataReadyCallback {
        fun onDataReady(data: String)
        fun onDataReady(data: ArrayList<ScannedDevices>?)
    }
}