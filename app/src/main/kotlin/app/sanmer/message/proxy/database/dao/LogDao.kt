package app.sanmer.message.proxy.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import app.sanmer.message.proxy.database.entity.Log
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT COUNT(*) FROM log WHERE ok = 1")
    fun getCountAsFlow(): Flow<Int>

    @Query("SELECT * FROM log")
    fun getAllAsFlow(): Flow<List<Log>>

    @Insert
    suspend fun insert(log: Log)

    @Insert
    suspend fun insert(logs: List<Log>)

    @Query("DELETE FROM log")
    suspend fun deleteAll()
}