package app.sanmer.message.proxy.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import app.sanmer.message.proxy.Const
import app.sanmer.message.proxy.R
import app.sanmer.message.proxy.ktx.viewUrl
import app.sanmer.message.proxy.ui.ktx.navigateSingleTopTo
import app.sanmer.message.proxy.ui.main.Screen
import app.sanmer.message.proxy.ui.screen.home.component.EmailItem
import app.sanmer.message.proxy.ui.screen.home.component.SmtpItem
import app.sanmer.message.proxy.ui.screen.home.component.StateItem
import app.sanmer.message.proxy.viewmodel.HomeViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    navController: NavController
) {
    val count by viewModel.countFlow.collectAsStateWithLifecycle(0)
    val permissionsState = rememberMultiplePermissionsState(viewModel.permissions)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopBar(scrollBehavior = scrollBehavior)
        },
        floatingActionButton = {
            ActionButton(navController = navController)
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .imePadding()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            StateItem(
                count = count,
                granted = permissionsState.allPermissionsGranted,
                launchRequest = permissionsState::launchMultiplePermissionRequest
            )

            SmtpItem(
                smtp = viewModel.smtp,
                update = viewModel::updateSmtp
            )

            EmailItem(
                email = viewModel.email,
                update = viewModel::updateEmail
            )
        }
    }
}

@Composable
private fun ActionButton(
    navController: NavController
) = FloatingActionButton(
    onClick = { navController.navigateSingleTopTo(Screen.Log()) }
) {
    Icon(
        painter = painterResource(R.drawable.inbox),
        contentDescription = null
    )
}

@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior
) = TopAppBar(
    title = { Text(text = stringResource(id = R.string.app_name)) },
    actions = {
        val context = LocalContext.current
        IconButton(
            onClick = { context.viewUrl(Const.GITHUB_URL) }
        ) {
            Icon(
                painter = painterResource(R.drawable.brand_github),
                contentDescription = null
            )
        }
    },
    scrollBehavior = scrollBehavior
)