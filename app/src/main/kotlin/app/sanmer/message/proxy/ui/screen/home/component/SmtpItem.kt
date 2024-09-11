package app.sanmer.message.proxy.ui.screen.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import app.sanmer.message.proxy.R
import app.sanmer.message.proxy.datastore.model.SmtpConfig
import app.sanmer.message.proxy.ui.component.Logo

@Composable
fun SmtpItem(
    smtp: SmtpConfig,
    update: ((SmtpConfig) -> SmtpConfig) -> Unit
) = Surface(
    shape = MaterialTheme.shapes.extraLarge,
    tonalElevation = 1.dp
) {
    Column(
        modifier = Modifier.padding(all = 20.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Row(
            modifier = Modifier.padding(bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Logo(
                icon = R.drawable.mail_forward,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(40.dp)
            )

            Text(
                text = stringResource(R.string.smtp_config),
                style = MaterialTheme.typography.titleMedium
            )
        }

        OutlinedTextField(
            value = smtp.server,
            onValueChange = { value -> update { it.copy(server = value) } },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            label = { Text(text = stringResource(R.string.smtp_server)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Next
            )
        )

        OutlinedTextField(
            value = smtp.username,
            onValueChange = { value -> update { it.copy(username = value) } },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            label = { Text(text = stringResource(R.string.smtp_username)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        var hidden by rememberSaveable { mutableStateOf(true) }
        OutlinedTextField(
            value = smtp.password,
            onValueChange = { value -> update { it.copy(password = value) } },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            label = { Text(text = stringResource(R.string.smtp_password)) },
            trailingIcon = {
                IconButton(
                    onClick = { hidden = !hidden }
                ) {
                    Icon(
                        painter = painterResource(
                            if (hidden) R.drawable.eye_closed else R.drawable.eye
                        ),
                        contentDescription = null
                    )
                }
            },
            visualTransformation = when {
                hidden -> PasswordVisualTransformation()
                else -> VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )
    }
}