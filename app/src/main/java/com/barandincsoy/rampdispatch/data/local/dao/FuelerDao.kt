package com.barandincsoy.rampdispatch.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.barandincsoy.rampdispatch.data.local.entity.FuelerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelerDao {

    @Query("SELECT * FROM fuelers ORDER BY name ASC")
    fun observeFuelers(): Flow<List<FuelerEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(fuelers: List<FuelerEntity>)

}