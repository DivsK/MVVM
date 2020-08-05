package com.example.serviceyou.ui.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.example.serviceyou.ui.adapters.BleDevicesListAdapter
import com.example.serviceyou.ui.adapters.RoutineListAdapter
import com.example.serviceyou.ui.base.AppDatabase
import com.example.serviceyou.ui.base.BaseApplication
import com.example.serviceyou.ui.data.HomeScreenData
import com.example.serviceyou.ui.data.ScannedDevices
import com.example.serviceyou.ui.repo.HomeRepo
import com.example.serviceyou.ui.utils.CommonUtils.isInternetAvailable
import com.example.serviceyou.ui.utils.ItemDecorator
import java.io.*


@BindingAdapter(value = ["setUpHomeDataAdapter"])
fun setUpHomeDataAdapter(recyclerView: RecyclerView, records: MutableList<HomeScreenData>?) {
    if (records != null) {
        if (recyclerView.adapter == null) {
            recyclerView.addItemDecoration(ItemDecorator(-150))
            recyclerView.adapter = RoutineListAdapter(records)
        } else {
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }
}

@BindingAdapter(value = ["setUpBleDevicesListAdapter"])
fun setUpBleDevicesListAdapter(recyclerView: RecyclerView, devices: MutableList<ScannedDevices>?) {
    if (devices != null) {
        if (recyclerView.adapter == null) {
            recyclerView.adapter = BleDevicesListAdapter(devices)
        } else {
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }
}

/**
 * @author Divya Khanduri
 */

class HomeViewModel : ViewModel() {
    private val TAG = HomeViewModel::class.simpleName
    private var _homeScreenDataList: MutableLiveData<List<HomeScreenData>>? = null
    private var homeScreenDataList: ArrayList<HomeScreenData>? = null

    var _scannedDevicesList: MutableLiveData<ArrayList<ScannedDevices>>? = null
    val scannedDevicesLiveData: MutableLiveData<ArrayList<ScannedDevices>>?
        get() = _scannedDevicesList

    val storagePermissionRequest = MutableLiveData<Boolean>()
    val locationPermissionRequest = MutableLiveData<Boolean>()

    private var isEnabled: MutableLiveData<Boolean>? = null
    private val apiService by lazy {
        BaseApplication.create()
    }
    var context: Context? = null
    private var fileText: MutableLiveData<String>? = null
    private var _wordFrequency: MutableLiveData<String>? = null
    private var repoModel: HomeRepo? = null

    val onFragmentAddClick = MutableLiveData<Boolean>()

    init {
        _homeScreenDataList = MutableLiveData()
        isEnabled = MutableLiveData<Boolean>(true)
        populateList()
        _homeScreenDataList?.value = homeScreenDataList
        _scannedDevicesList = MutableLiveData()
        _wordFrequency = MutableLiveData()
        fileText = MutableLiveData()
    }

    val homeMutableLiveData: MutableLiveData<List<HomeScreenData>>?
        get() = _homeScreenDataList

    val fileData: MutableLiveData<String>?
        get() = fileText

    val wordFrequency: MutableLiveData<String>?
        get() = _wordFrequency

    private fun populateList() {
        var homeScreenData: Any?
        homeScreenDataList = arrayListOf()
        homeScreenData = HomeScreenData("Dental Care")
        homeScreenDataList?.add(homeScreenData)
        homeScreenData = HomeScreenData("Eye Care")
        homeScreenDataList?.add(homeScreenData)
        homeScreenData = HomeScreenData("Heart Care")
        homeScreenDataList?.add(homeScreenData)
        homeScreenData = HomeScreenData("Mental Health Care")
        homeScreenDataList?.add(homeScreenData)
        homeScreenData = HomeScreenData("Orthopedic Care")
        homeScreenDataList?.add(homeScreenData)
    }

    fun onAddDownloadFileClick(view: View) {
        context = view.context
        Log.d(TAG, "onAddDownloadFileClick")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (view.context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || view.context.checkSelfPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_DENIED
            ) {
                storagePermissionRequest.postValue(false)
            } else {
                startFileDownload(view.context)
            }
        } else {
            startFileDownload(view.context)
        }
    }

    fun onMenuClick() {
        onFragmentAddClick.value = true
    }

    fun onBleScan(view: View) {
        checkForLocationPermissions(view.context)
    }

    fun startFileDownload(context: Context) {
        Log.d(TAG, "startFileDownload")
        if (isInternetAvailable(context)) {
            Toast.makeText(context, "Please wait...", Toast.LENGTH_LONG).show()
            isEnabled?.value = false
            repoModel = HomeRepo(apiService, null, context)
            repoModel?.getFileFromUrl(object : HomeRepo.OnDataReadyCallback {
                override fun onDataReady(data: String) {
                    readFromFile(context, data)
                }

                override fun onDataReady(data: ArrayList<ScannedDevices>?) {
                    // not fo use here
                }
            })
        } else {
            Toast.makeText(context, "Internet not available!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun readFromFile(context: Context, fileName: String) {
        try {
            val inputStream: InputStream? = context.openFileInput(fileName)
            if (inputStream != null) {
                val inputStreamReader = InputStreamReader(inputStream)
                val bufferedReader = BufferedReader(inputStreamReader)
                var receiveString: String?
                val stringBuilder = StringBuilder()
                while (bufferedReader.readLine().also { receiveString = it } != null) {
                    stringBuilder.append("\n").append(receiveString)
                }
                val hm: HashMap<String, Int?> = HashMap()
                val str = stringBuilder.toString()

                for (word in str.split(" ")) {
                    var count = 1
                    if (hm[word] != null) {
                        count += hm[word] ?: 0
                    }
                    hm[word] = count
                }
                val freq = StringBuilder()
                for ((key, value) in hm) {
                    freq.append("${key}->${value}").append("\n")
                }
                _wordFrequency?.postValue(freq.toString())
                inputStream.close()
                fileText?.postValue(stringBuilder.toString())
            }
        } catch (e: FileNotFoundException) {
            Log.e(" activity", "File not found: $e")
        } catch (e: IOException) {
            Log.e(" activity", "Can not read file: $e")
        }
    }

    private fun checkForLocationPermissions(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED || context.checkSelfPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_DENIED
            ) {
                locationPermissionRequest.postValue(false)
            } else {
                locationPermissionRequest.postValue(true)
            }
        } else {
            locationPermissionRequest.postValue(true)
        }
    }
}
