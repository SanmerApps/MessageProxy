package app.sanmer.message.proxy.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.sanmer.message.proxy.ktx.toLocalDateTime

@Entity(tableName = "log")
data class Log(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val ok: Boolean,
    val from: String?,
    val to: String?
) {
    val dateTime by lazy { timestamp.toLocalDateTime() }
}
