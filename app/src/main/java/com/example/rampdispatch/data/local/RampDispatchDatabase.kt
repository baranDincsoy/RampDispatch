package com.example.rampdispatch.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.rampdispatch.data.local.dao.FuelOrderDao
import com.example.rampdispatch.data.local.dao.FuelerDao
import com.example.rampdispatch.data.local.dao.StatusEventDao
import com.example.rampdispatch.data.local.entity.FuelOrderEntity
import com.example.rampdispatch.data.local.entity.FuelerEntity
import com.example.rampdispatch.data.local.entity.StatusEventEntity

@Database(
    entities = [FuelOrderEntity::class, FuelerEntity::class, StatusEventEntity::class],
    version = 2,
    exportSchema = false
)
abstract class RampDispatchDatabase : RoomDatabase() {

    abstract fun fuelOrderDao(): FuelOrderDao
    abstract fun fuelerDao(): FuelerDao
    abstract fun statusEventDao(): StatusEventDao

    companion object {
        @Volatile
        private var instance: RampDispatchDatabase? = null

        fun getInstance(context: Context): RampDispatchDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    RampDispatchDatabase::class.java,
                    "ramp_dispatch.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}