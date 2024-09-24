package dev.sanmer.lettre

object Lettre {
    init {
        System.loadLibrary("lettre_jni")
    }

    external fun send(config: Config, message: Message)

    data class Config(
        val server: String,
        val port: Int,
        val username: String,
        val password: String
    )
}