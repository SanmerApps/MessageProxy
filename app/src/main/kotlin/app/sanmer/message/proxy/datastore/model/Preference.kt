package app.sanmer.message.proxy.datastore.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class Preference(
    val smtp: SmtpConfig,
    val email: String
) {
    fun encodeToStream(output: OutputStream) = output.write(
        ProtoBuf.encodeToByteArray(this)
    )

    companion object Default {
        fun decodeFromStream(input: InputStream): Preference =
            ProtoBuf.decodeFromByteArray(input.readBytes())

        val EMPTY = Preference(
            smtp = SmtpConfig.EMPTY,
            email = ""
        )
    }
}
