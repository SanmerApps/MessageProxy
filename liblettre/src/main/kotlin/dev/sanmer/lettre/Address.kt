package dev.sanmer.lettre

data class Address(
    val user: String,
    val domain: String
) {
    companion object Default {
        internal fun String.toAddress(): Address {
            val (user, domain) = split('@', limit = 2)
            return Address(user, domain)
        }
    }
}
