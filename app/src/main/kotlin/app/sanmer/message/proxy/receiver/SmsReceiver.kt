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
import dev.sanmer.email.Lettre
import dev.sanmer.email.Mailbox
import dev.sanmer.email.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                    val sms = SMSMessage.tryFromIntent(context, intent) ?: return@launch
                    val preference = preference.data.first()
                    if (preference.smtp.username.isEmpty() || preference.email.isEmpty()) return@launch

                    runCatching {
                        Timber.d("${sms.from} -> ${sms.to} (${sms.dateTime})")
                        context.sendEmail(
                            smtp = preference.smtp,
                            email = preference.email,
                            sms = sms
                        )
                    }.onSuccess {
                        log.insert(sms.asLog(true))
                    }.onFailure {
                        log.insert(sms.asLog(false))
                        Timber.e(it)
                    }
                }
            }
        }
    }

    data class SMSMessage(
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
            private fun getFromIntent(context: Context, intent: Intent): SMSMessage {
                val subInfo = SubscriptionManagerCompat.getSubInfoFromIntent(context, intent)
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

                return SMSMessage(
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
        sms: SMSMessage
    ) = withContext(Dispatchers.IO) {
        val config = Lettre.Config(
            server = smtp.server,
            port = smtp.port,
            username = smtp.username,
            password = smtp.password
        )

        val message = Message(
            from = Mailbox(
                name = sms.from ?: "",
                email = smtp.email.ifEmpty { smtp.username }
            ),
            to = Mailbox(
                name = sms.to ?: "",
                email = email
            ),
            subject = sms.subject(this@sendEmail),
            body = sms.content
        )

        Lettre.send(config, message)
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