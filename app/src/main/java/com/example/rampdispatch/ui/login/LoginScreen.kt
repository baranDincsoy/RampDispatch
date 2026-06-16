package com.example.rampdispatch.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rampdispatch.ui.theme.Dimens

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
) {
    val fuelers by viewModel.fuelers.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Dimens.SpacingL),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM)
        ) {
            item {
                Column(Modifier.padding(vertical = Dimens.SpacingXl)) {
                    Text(
                        "RampDispatch",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Sign in to continue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Text(
                    "Dispatch",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                LoginCard(
                    title = "Team Leader",
                    subtitle = "Plan and assign all orders",
                    icon = { Icon(Icons.Default.SupervisorAccount, contentDescription = null) },
                    onClick = viewModel::loginAsTeamLeader
                )
            }

            item {
                Text(
                    "Fuelers",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Dimens.SpacingM)
                )
            }
            items(fuelers, key = { it.id }) { fueler ->
                LoginCard(
                    title = fueler.name,
                    subtitle = fueler.deviceId,
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    onClick = { viewModel.loginAsFueler(fueler) }
                )
            }
        }
    }
}

@Composable
private fun LoginCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(Dimens.SpacingL),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingM)
        ) {
            icon()
            Column {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}