package app.sanmer.message.proxy.ui.screen.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.sanmer.message.proxy.R
import app.sanmer.message.proxy.ui.component.Logo

@Composable
fun EmailItem(
    email: String,
    update: (String) -> Unit
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
                icon = R.drawable.at,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(40.dp)
            )

            Text(
                text = stringResource(R.string.email_config),
                style = MaterialTheme.typography.titleMedium
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = update,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            placeholder = { Text(text = stringResource(R.string.demo_email)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            )
        )
    }
}