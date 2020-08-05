package com.example.serviceyou.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.serviceyou.databinding.RvBleDevicesRowItemBinding
import com.example.serviceyou.ui.data.ScannedDevices

/**
 * @author Divya Khanduri
 */

class BleDevicesListAdapter(val listBleDevices: List<ScannedDevices>) :
    RecyclerView.Adapter<BleDevicesListAdapter.BleDevicesViewHolder>() {
    inner class BleDevicesViewHolder(var rvBleDevicesRowItemBinding: RvBleDevicesRowItemBinding) :
        RecyclerView.ViewHolder(rvBleDevicesRowItemBinding.root) {
        fun bindData(scannedDevice: ScannedDevices) {
            rvBleDevicesRowItemBinding.scannedDevicesData = scannedDevice
            rvBleDevicesRowItemBinding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BleDevicesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RvBleDevicesRowItemBinding.inflate(layoutInflater, parent, false)

        return BleDevicesViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listBleDevices.size
    }

    override fun onBindViewHolder(holder: BleDevicesViewHolder, position: Int) {
        val currentItem = listBleDevices.get(position)
        holder.bindData(currentItem)
    }
}