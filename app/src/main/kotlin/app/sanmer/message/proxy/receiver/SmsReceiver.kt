package app.sanmer.message.proxy.receiver

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Telephony
import app.sanmer.message.proxy.BuildConfig
import app.sanmer.message.proxy.R
import app.sanmer.message.proxy.compat.SubscriptionManagerCompat
import app.sanmer.message.proxy.database.entity.Log
import app.sanmer.message.proxy.datastore.model.SmtpConfig
import app.sanmer.message.proxy.ktx.toLocalDateTime
import app.sanmer.message.proxy.repository.LogRepository
import app.sanmer.message.proxy.repository.PreferenceRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.mail2.jakarta.SimpleEmail
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {
    @Inject
    lateinit var preference: PreferenceRepository

    @Inject
    lateinit var log: LogRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Telephony.Sms.Intents.SMS_RECEIVED_ACTION -> {
                scope.launch {
                    val message = Message.tryFromIntent(context, intent) ?: return@launch
                    val preference = preference.data.first()
                    if (preference.smtp.username.isEmpty() || preference.email.isEmpty()) return@launch

                    runCatching {
                        Timber.d("${message.from} -> ${message.to} (${message.dateTime})")
                        context.sendEmail(
                            smtp = preference.smtp,
                            email = preference.email,
                            message = message
                        )
                    }.onSuccess {
                        log.insert(message.asLog(true))
                    }.onFailure {
                        log.insert(message.asLog(false))
                        Timber.e(it)
                    }
                }
            }
        }
    }

    data class Message(
        val content: String,
        val timestamp: Long,
        val from: String?,
        val to: String?,
    ) {
        val dateTime by lazy { timestamp.toLocalDateTime() }

        fun subject(context: Context) =
            if (to.isNullOrBlank()) {
                context.getString(R.string.subject_unknown)
            } else {
                context.getString(R.string.subject, to)
            }

        fun asLog(ok: Boolean) = Log(
            timestamp = timestamp,
            ok = ok,
            from = from,
            to = to
        )

        companion object Default {
            private fun getFromIntent(context: Context, intent: Intent): Message {
                val subInfo = SubscriptionManagerCompat.getSubInfoFromIntent(context, intent)
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

                return Message(
                    content = messages.joinToString(separator = "") { it.messageBody },
                    timestamp = messages.first().timestampMillis,
                    from = messages.first().originatingAddress,
                    to = subInfo?.number ?: subInfo?.displayName
                )
            }

            fun tryFromIntent(context: Context, intent: Intent) = runCatching {
                getFromIntent(context, intent)
            }.onFailure {
                Timber.w(it)
            }.getOrNull()
        }
    }

    private suspend fun Context.sendEmail(
        smtp: SmtpConfig,
        email: String,
        message: Message
    ) = withContext(Dispatchers.IO) {
        smtp.asSimpleEmail(message.from).also {
            it.setSubject(message.subject(this@sendEmail))
            it.setMsg(message.content)
            it.addTo(email)
        }.send()
    }

    private fun SmtpConfig.asSimpleEmail(name: String? = null) = SimpleEmail().also {
        it.setHostName(server)
        it.setSmtpPort(port)
        it.setAuthentication(username, password)
        it.setSSLOnConnect(true)
        it.setFrom(email.ifBlank { username }, name)
    }

    companion object Default {
        private val componentName = ComponentName(
            BuildConfig.APPLICATION_ID,
            SmsReceiver::class.java.name
        )

        fun isEnable(context: Context): Boolean {
            val pm = context.packageManager
            return pm.getComponentEnabledSetting(componentName) ==
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
        }

        fun setEnable(context: Context, enable: Boolean) {
            val pm = context.packageManager
            val state = when {
                enable -> PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
                else -> PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            pm.setComponentEnabledSetting(componentName, state, PackageManager.DONT_KILL_APP)
        }
    }
}