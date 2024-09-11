package app.sanmer.message.proxy.ktx

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun Long.toLocalDateTime(
    timeZone: TimeZone = TimeZone.currentSystemDefault()
) = Instant.fromEpochMilliseconds(this).toLocalDateTime(timeZone)