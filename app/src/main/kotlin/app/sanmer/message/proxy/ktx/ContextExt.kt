package app.sanmer.message.proxy.ktx

import android.content.Context
import android.content.Intent

val Context.dpContext: Context
    inline get() = createDeviceProtectedStorageContext()

fun Context.viewUrl(url: String) {
    startActivity(
        Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
    )
}