package com.barandincsoy.rampdispatch.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barandincsoy.rampdispatch.ui.theme.Dimens
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = viewModel(factory = StatsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Stats") }) }
    ) { innerPadding ->
        LazyColumn(
            Modifier.padding(innerPadding),
            contentPadding = PaddingValues(Dimens.SpacingL),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingL)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingM)) {
                    StatCard("Completed", uiState.completedCount.toString(), Modifier.weight(1f))
                    StatCard("Active", uiState.activeCount.toString(), Modifier.weight(1f))
                }
            }
            item {
                StatCard(
                    label = "Total fuel pumped",
                    value = String.format(Locale.US, "%,d lbs", uiState.totalFueledLbs),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Text(
                    "By fueler",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (uiState.fuelerStats.isEmpty()) {
                item {
                    Text(
                        "No completed orders yet. Complete an order to see stats here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(uiState.fuelerStats) { stat -> FuelerStatRow(stat) }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(Dimens.SpacingL)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Dimens.SpacingXs))
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FuelerStatRow(stat: FuelerStat) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(Dimens.SpacingL),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(stat.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    stat.deviceId,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text(
                    String.format(Locale.US, "%,d lbs", stat.totalLbs),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "${stat.completedCount} orders",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}