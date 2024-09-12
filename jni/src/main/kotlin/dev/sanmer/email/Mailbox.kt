package dev.sanmer.email

import dev.sanmer.email.Address.Default.toAddress

data class Mailbox(
    val name: String,
    val email: Address
) {
    constructor(
        name: String,
        email: String
    ) : this(
        name = name,
        email = email.toAddress()
    )
}
