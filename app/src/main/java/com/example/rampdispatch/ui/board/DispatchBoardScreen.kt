package com.example.rampdispatch.ui.board

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rampdispatch.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DispatchBoardScreen(
    viewModel: DispatchBoardViewModel,
    onOrderClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // One-shot side effect: show the error once, then tell the VM it was shown.
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onErrorShown()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Dispatch Board") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {

            FilterRow(
                selected = uiState.selectedFilter,
                onSelect = viewModel::onFilterSelected
            )

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize()
            ) {
                if (uiState.orders.isEmpty() && !uiState.isRefreshing) {
                    EmptyState(Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(Dimens.SpacingL),
                        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM)
                    ) {
                        items(uiState.orders, key = { it.id }) { order ->
                            OrderCard(order = order, onClick = { onOrderClick(order.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    selected: BoardFilter,
    onSelect: (BoardFilter) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Dimens.SpacingL),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS)
    ) {
        items(BoardFilter.entries) { filter ->
            FilterChip(
                selected = filter == selected,
                onClick = { onSelect(filter) },
                label = { Text(filter.name.replace('_', ' ')) }
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("All clear", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(Dimens.SpacingS))
        Text(
            "No orders match this filter.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}