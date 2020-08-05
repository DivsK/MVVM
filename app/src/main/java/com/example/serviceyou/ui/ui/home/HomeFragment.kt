package com.example.serviceyou.ui.ui.home

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.serviceyou.R
import com.example.serviceyou.databinding.HomeFragmentBinding
import com.example.serviceyou.ui.base.AppDatabase
import com.example.serviceyou.ui.data.ScannedDevices
import com.example.serviceyou.ui.repo.HomeRepo
import java.io.File

/**
 * @author Divya Khanduri
 */

class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    val TAG = HomeFragment::class.simpleName

    private lateinit var binding: HomeFragmentBinding
    private lateinit var viewModel: HomeViewModel

    private val STORAGE_PERMISSION_RESULT_CODE = 1
    private val COARSE_LOCATION_PERMISSION_RESULT_CODE: Int = 2
    private val BLUETOOTH_REQUEST_CODE: Int = 3
    private var mBTA: BluetoothAdapter? = null

    var repoModel: HomeRepo? = null
    private var db: AppDatabase? = null

    var mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context?, intent: Intent?) {
            val action = intent?.action
            when {
                BluetoothDevice.ACTION_FOUND == action -> {
                    // Get the BluetoothDevice object from the Intent
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    // Add the name and address to an array adapter to show in a Toast
                    val derp = device.name + " - " + device.address
                    Log.d(TAG, "Scanned devices" + derp)
                    Toast.makeText(ctxt, "Scanned devices - " + derp, Toast.LENGTH_LONG).show()
                    repoModel?.saveInDb(
                        device.name,
                        device.address,
                        object : HomeRepo.OnDataReadyCallback {
                            override fun onDataReady(data: String) {}
                            override fun onDataReady(data: ArrayList<ScannedDevices>?) {
                                viewModel._scannedDevicesList?.postValue(data)
                            }
                        })
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED == action -> {
                    Toast.makeText(
                        ctxt,
                        "Scanning started,Only devices which are discoverable will be detected",
                        Toast.LENGTH_LONG
                    ).show()
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action -> {
                    Toast.makeText(ctxt, "Scanning Finished", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.home_fragment, container, false)
        val view: View = binding.getRoot()
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        binding.homeViewModel = viewModel
        binding.lifecycleOwner = this

        db = AppDatabase.getAppDataBase(context = this.activity!!)
        repoModel = HomeRepo(null, db?.scannedDevicesDao()!!, this.activity!!)

        viewModel.locationPermissionRequest.observe(this.activity!!, Observer { permission ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !permission) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    COARSE_LOCATION_PERMISSION_RESULT_CODE
                )
            } else if (permission) {
                checkBluetoothState()
            }
        })

        viewModel.storagePermissionRequest.observe(this.activity!!, Observer { permission ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    STORAGE_PERMISSION_RESULT_CODE
                )
            }
        })

        activity?.registerReceiver(mReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        activity?.registerReceiver(
            mReceiver,
            IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        )
        activity?.registerReceiver(
            mReceiver,
            IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        )

        viewModel.fileData?.observe(this.activity!!, Observer { fileText ->
            binding.tvFileData
        })
        //Get value from DB
        repoModel!!.getScannedDevicesFromDb(object : HomeRepo.OnDataReadyCallback {
            override fun onDataReady(data: String) {}

            override fun onDataReady(data: ArrayList<ScannedDevices>?) {
                if (!data?.isEmpty()!!)
                    viewModel._scannedDevicesList?.postValue(
                        db?.scannedDevicesDao()!!.getScannedDevices() as ArrayList<ScannedDevices>?
                    )
            }
        })
    }

    private fun checkBluetoothState() {
        val duration = Toast.LENGTH_SHORT
        mBTA = BluetoothAdapter.getDefaultAdapter()
        //check to see if there is BT on the Android device at all
        if (mBTA == null) {
            Toast.makeText(activity, "No Bluetooth on this handset", duration).show()
            return
        } else {
            //let's make the user enable BT if it isn't already
            if (mBTA!!.isEnabled()) {
                if (mBTA!!.isDiscovering) {
                    Toast.makeText(activity, "Scanning Bluetooth Devices", duration).show()
                } else {
                    Toast.makeText(activity, "Bluetooth is Enabled", duration).show()
                    mBTA!!.startDiscovery()
                }
            } else {
                Toast.makeText(activity, "Enable Bluetooth Device", duration).show()
                val enableBT = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBT, BLUETOOTH_REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            COARSE_LOCATION_PERMISSION_RESULT_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Loc permission granted")
                    checkBluetoothState()
                } else {
                    Log.d(TAG, "Loc permission denied")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ),
                            COARSE_LOCATION_PERMISSION_RESULT_CODE
                        )
                    }
                }
            }
            STORAGE_PERMISSION_RESULT_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "granted")
                    viewModel.startFileDownload(this.activity!!)
                } else {
                    Log.d(TAG, "denied")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(
                            arrayOf(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            ),
                            STORAGE_PERMISSION_RESULT_CODE
                        )
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            BLUETOOTH_REQUEST_CODE -> {
                if (resultCode == 1) {
                    Log.d(TAG, "BLE granted")
                    mBTA!!.startDiscovery()
                } else {
                    Log.d(TAG, "BLE denied")
                    checkBluetoothState()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.unregisterReceiver(mReceiver)
    }
}
