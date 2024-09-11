package app.sanmer.message.proxy.compat

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.telephony.PhoneNumberUtils
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.annotation.RequiresPermission
import androidx.core.content.getSystemService

object SubscriptionManagerCompat {
    @SuppressLint("MissingPermission")
    fun getSubInfoFromIntent(context: Context, intent: Intent): SubscriptionInfoCompat? {
        val subId = intent.getIntExtra(
            SubscriptionManager.EXTRA_SUBSCRIPTION_INDEX,
            SubscriptionManager.INVALID_SUBSCRIPTION_ID
        )

        return if (subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            getSubInfo(context, subId)
        } else {
            null
        }
    }

    @RequiresPermission(
        allOf = [
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_PHONE_NUMBERS
        ]
    )
    fun getSubInfo(context: Context, subId: Int): SubscriptionInfoCompat? {
        val subscriptionManager = context.getSystemService<SubscriptionManager>() ?: return null
        val subscriptionInfo = subscriptionManager.getActiveSubscriptionInfo(subId)
            ?.let(::SubscriptionInfoCompat) ?: return null

        return if (BuildCompat.atLeastT) {
            val number = subscriptionManager.getPhoneNumber(subId)
            subscriptionInfo.copy(
                number = number.formatNumber(subscriptionInfo.countryIso ?: "US")
            )
        } else {
            subscriptionInfo
        }
    }

    data class SubscriptionInfoCompat(
        val subId: Int,
        val displayName: String,
        val number: String?,
        val countryIso: String?
    ) {
        @Suppress("DEPRECATION")
        constructor(original: SubscriptionInfo) : this(
            subId = original.subscriptionId,
            displayName = original.displayName.toString(),
            number = original.number?.formatNumber(original.countryIso),
            countryIso = original.countryIso.ifEmpty { null }
        )
    }

    fun String.formatNumber(countryIso: String): String? {
        val number = PhoneNumberUtils.formatNumber(this, countryIso) ?: return null
        val numberStr = number.split("\\s+".toRegex()).toMutableList().apply {
            removeIf { it.startsWith("+") }
        }
        return numberStr.joinToString(separator = "").ifEmpty { null }
    }
}