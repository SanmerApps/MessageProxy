package app.sanmer.message.proxy.repository

import app.sanmer.message.proxy.datastore.PreferenceDataSource
import app.sanmer.message.proxy.datastore.model.SmtpConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceRepository @Inject constructor(
    private val dataSource: PreferenceDataSource
) {
    val data get() = dataSource.data

    suspend fun setSmtp(value: SmtpConfig) {
        dataSource.setSmtp(value)
    }

    suspend fun setEmail(value: String) {
        dataSource.setEmail(value)
    }
}