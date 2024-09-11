package app.sanmer.message.proxy.repository

import app.sanmer.message.proxy.database.dao.LogDao
import app.sanmer.message.proxy.database.entity.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogRepository @Inject constructor(
    private val log: LogDao
) {
    val countFlow get() = log.getCountAsFlow()

    val allFlow get() = log.getAllAsFlow()

    suspend fun insert(value: Log) = withContext(Dispatchers.IO) {
        log.insert(value)
    }

    suspend fun insert(values: List<Log>) = withContext(Dispatchers.IO) {
        log.insert(values)
    }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        log.deleteAll()
    }
}