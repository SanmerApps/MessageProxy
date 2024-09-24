package dev.sanmer.lettre

import dev.sanmer.lettre.Address.Default.toAddress

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
