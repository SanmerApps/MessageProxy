package app.sanmer.message.proxy.datastore.model

import kotlinx.serialization.Serializable

@Serializable
data class SmtpConfig(
    val server: String,
    val port: Int = 465,
    val username: String,
    val password: String,
    val email: String = username
) {
    companion object Default {
        val EMPTY = SmtpConfig(
            server = "",
            username = "",
            password = "",
        )
    }
}