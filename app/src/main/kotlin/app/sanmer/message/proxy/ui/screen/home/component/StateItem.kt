package app.sanmer.message.proxy.ui.screen.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.sanmer.message.proxy.R
import app.sanmer.message.proxy.receiver.SmsReceiver
import app.sanmer.message.proxy.ui.component.Logo

@Composable
fun StateItem(
    count: Int,
    granted: Boolean,
    launchRequest: () -> Unit
) {
    val context = LocalContext.current
    var enable by remember { mutableStateOf(SmsReceiver.isEnable(context)) }

    Surface(
        enabled = !granted,
        onClick = launchRequest,
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(all = 20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Logo(
                icon = if (granted && enable) R.drawable.mood_wink else R.drawable.mood_xd,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(40.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 15.dp)
            ) {
                Text(
                    text = stringResource(
                        if (granted && enable) R.string.proxy_running else R.string.proxy_not_running
                    ),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = when {
                        granted -> stringResource(R.string.messages_forwarded, count)
                        else -> stringResource(R.string.permissions_denied)
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (granted) {
                Switch(
                    checked = enable,
                    onCheckedChange = {
                        enable = it
                        SmsReceiver.setEnable(context, enable)
                    }
                )
            }
        }
    }
}