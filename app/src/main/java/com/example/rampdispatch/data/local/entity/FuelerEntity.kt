package com.example.rampdispatch.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuelers")
data class FuelerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val deviceId: String
)