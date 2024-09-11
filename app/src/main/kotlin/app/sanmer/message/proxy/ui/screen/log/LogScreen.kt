package app.sanmer.message.proxy.ui.screen.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import app.sanmer.message.proxy.R
import app.sanmer.message.proxy.database.entity.Log
import app.sanmer.message.proxy.ui.ktx.plus
import app.sanmer.message.proxy.viewmodel.LogViewModel

@Composable
fun LogScreen(
    viewModel: LogViewModel = hiltViewModel(),
    navController: NavController
) {
    val logs by viewModel.allFlow.collectAsStateWithLifecycle(emptyList())
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopBar(
                deleteAll = viewModel::deleteAll,
                navController = navController,
                scrollBehavior = scrollBehavior
            )
        },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = contentPadding + PaddingValues(all = 15.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            items(logs) {
                LogItem(it)
            }
        }
    }
}

@Composable
private fun LogItem(
    log: Log
) = Surface(
    shape = MaterialTheme.shapes.large,
    tonalElevation = 1.dp,
    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .padding(all = 15.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 15.dp)
        ) {
            Text(
                text = buildString {
                    append(log.from ?: context.getString(R.string.unknown))
                    append(" -> ")
                    append(log.to ?: context.getString(R.string.unknown))
                },
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = log.dateTime.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Icon(
            painter = painterResource(
                if (log.ok) R.drawable.circle_check_filled else R.drawable.circle_x_filled
            ),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.let { if (log.ok) it.primary else it.error }
        )
    }
}

@Composable
private fun TopBar(
    deleteAll: () -> Unit,
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior
) = TopAppBar(
    title = { Text(text = stringResource(id = R.string.log_title)) },
    navigationIcon = {
        IconButton(
            onClick = { navController.navigateUp() }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_left),
                contentDescription = null
            )
        }
    },
    actions = {
        IconButton(
            onClick = deleteAll
        ) {
            Icon(
                painter = painterResource(id = R.drawable.clear_all),
                contentDescription = null
            )
        }
    },
    scrollBehavior = scrollBehavior
)