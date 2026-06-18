package com.barandincsoy.rampdispatch.ui.board

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barandincsoy.rampdispatch.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DispatchBoardScreen(
    viewModel: DispatchBoardViewModel,
    onOrderClick: (String) -> Unit = {},
    onLogout: () -> Unit = {}
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
        topBar = {
            TopAppBar(
                title = { Text("Dispatch Board") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Log out")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {

            FilterRow(
                selected = uiState.selectedFilter,
                onSelect = viewModel::onFilterSelected
            )

            SortRow(
                selected = uiState.selectedSort,
                onSelect = viewModel::onSortSelected
            )

            ConcourseRow(
                concourses = uiState.availableConcourses,
                selected = uiState.selectedConcourse,
                onSelect = viewModel::onConcourseSelected
            )

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize()
            ) {
                if (uiState.items.isEmpty() && !uiState.isRefreshing) {
                    EmptyState(Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(Dimens.SpacingL),
                        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM)
                    ) {
                        items(uiState.items, key = { it.order.id }) { item ->
                            OrderCard(item = item, onClick = { onOrderClick(item.order.id) })
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
private fun SortRow(
    selected: BoardSort,
    onSelect: (BoardSort) -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = Dimens.SpacingL, vertical = Dimens.SpacingS),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS)
    ) {
        Text(
            "Sort:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        BoardSort.entries.forEach { sort ->
            FilterChip(
                selected = sort == selected,
                onClick = { onSelect(sort) },
                label = { Text(sort.label) }
            )
        }
    }
}

@Composable
private fun ConcourseRow(
    concourses: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit
) {
    if (concourses.isEmpty()) return   // nothing to filter yet

    Row(
        modifier = Modifier.padding(horizontal = Dimens.SpacingL, vertical = Dimens.SpacingS),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS)
    ) {
        Text(
            "Concourse:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // "All" chip resets the concourse filter (null).
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text("All") }
        )
        concourses.forEach { concourse ->
            FilterChip(
                selected = concourse == selected,
                onClick = { onSelect(concourse) },
                label = { Text(concourse) }
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