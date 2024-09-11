package app.sanmer.message.proxy.datastore

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import app.sanmer.message.proxy.datastore.model.Preference
import app.sanmer.message.proxy.ktx.dpContext
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.SerializationException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

class PreferenceSerializer @Inject constructor() : Serializer<Preference> {
    override val defaultValue = Preference.EMPTY

    override suspend fun readFrom(input: InputStream) =
        try {
            Preference.decodeFromStream(input)
        } catch (e: SerializationException) {
            throw CorruptionException("Failed to read proto", e)
        }

    override suspend fun writeTo(t: Preference, output: OutputStream) {
        t.encodeToStream(output)
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object Provider {
        @Provides
        @Singleton
        fun DataStore(
            @ApplicationContext context: Context,
            serializer: PreferenceSerializer
        ): DataStore<Preference> =
            DataStoreFactory.create(
                serializer = serializer
            ) {
                context.dpContext.dataStoreFile("preference.pb")
            }
    }
}