package dev.sanmer.lettre

data class Message(
    val from: Mailbox,
    val to: Mailbox,
    val subject: String,
    val body: String
)