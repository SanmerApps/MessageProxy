package dev.sanmer.email

object Lettre {
    init {
        System.loadLibrary("lettre-jni")
    }

    external fun send(config: Config, message: Message)

    data class Config(
        val server: String,
        val port: Int,
        val username: String,
        val password: String
    )
}