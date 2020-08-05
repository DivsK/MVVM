package com.example.serviceyou.ui.data

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.serviceyou.BR

/**
 * @author Divya Khanduri
 */

@Entity
data class ScannedDevices(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    var deviceName: String,
    var deviceAddress: String
) : BaseObservable() {
    var _deviceName: String?
        @Bindable get() = deviceName
        set(value) {
            deviceName = value!!
            notifyPropertyChanged(BR._deviceName)
        }
    var _deviceAddress: String?
        @Bindable get() = deviceAddress
        set(value) {
            deviceAddress = value!!
            notifyPropertyChanged(BR._deviceAddress)
        }
}