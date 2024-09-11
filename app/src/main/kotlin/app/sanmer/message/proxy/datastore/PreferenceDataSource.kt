package app.sanmer.message.proxy.datastore

import androidx.datastore.core.DataStore
import app.sanmer.message.proxy.datastore.model.Preference
import app.sanmer.message.proxy.datastore.model.SmtpConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PreferenceDataSource @Inject constructor(
    private val dataStore: DataStore<Preference>
) {
    val data get() = dataStore.data

    suspend fun setSmtp(value: SmtpConfig) = withContext(Dispatchers.IO) {
        dataStore.updateData {
            it.copy(
                smtp = value
            )
        }
    }

    suspend fun setEmail(value: String) = withContext(Dispatchers.IO) {
        dataStore.updateData {
            it.copy(
                email = value
            )
        }
    }
}