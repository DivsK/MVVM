package com.example.serviceyou.ui.dao

import androidx.room.*
import com.example.serviceyou.ui.data.ScannedDevices

/**
 * @author Divya Khanduri
 */
@Dao
interface ScannedDevicesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertScannedDevices(scannedDevices: ScannedDevices)

    @Update
    fun updateScannedDevices(scannedDevices: ScannedDevices)

    @Query("DELETE FROM ScannedDevices")
    fun deleteScannedDevices()

    @Query("SELECT * FROM ScannedDevices WHERE deviceName == :deviceName")
    fun getScannedDevicesByName(deviceName: String): List<ScannedDevices>

    @Query("SELECT * FROM ScannedDevices")
    fun getScannedDevices(): List<ScannedDevices>
}