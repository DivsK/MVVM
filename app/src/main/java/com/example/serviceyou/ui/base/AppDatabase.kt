package com.example.serviceyou.ui.base

import android.app.Activity
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.serviceyou.ui.dao.ScannedDevicesDao
import com.example.serviceyou.ui.data.ScannedDevices

/**
 * @author Divya Khanduri
 */

@Database(entities = [ScannedDevices::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scannedDevicesDao(): ScannedDevicesDao

    companion object {
        var INSTANCE: AppDatabase? = null

        fun getAppDataBase(context: Activity): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "myDB"
                    ).build()
                }
            }
            return INSTANCE
        }

        fun destroyDataBase() {
            INSTANCE = null
        }
    }
}