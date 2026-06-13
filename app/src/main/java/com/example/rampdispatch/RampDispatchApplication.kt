package com.example.rampdispatch

import android.app.Application
import com.example.rampdispatch.data.local.RampDispatchDatabase
import com.example.rampdispatch.data.remote.NetworkModule
import com.example.rampdispatch.data.repository.DispatchRepository

/**
 * Manual dependency container for the MVP (no DI framework).
 * Lives for the entire app process, so everything created here
 * is effectively a singleton.
 */
class RampDispatchApplication : Application() {

    val repository: DispatchRepository by lazy {
        val db = RampDispatchDatabase.getInstance(this)
        DispatchRepository(
            api = NetworkModule.dispatchApi,
            orderDao = db.fuelOrderDao(),
            fuelerDao = db.fuelerDao(),
            eventDao = db.statusEventDao()
        )
    }
}