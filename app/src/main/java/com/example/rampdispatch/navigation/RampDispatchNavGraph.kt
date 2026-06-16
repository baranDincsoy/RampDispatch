package com.example.rampdispatch.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.rampdispatch.ui.board.DispatchBoardScreen
import com.example.rampdispatch.ui.board.DispatchBoardViewModel
import com.example.rampdispatch.ui.detail.OrderDetailScreen
import com.example.rampdispatch.ui.stats.StatsScreen

@Composable
fun RampDispatchNavGraph(
    navController: NavHostController,
    onLogout: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = Routes.BOARD
    ) {
        composable(route = Routes.BOARD) {
            val viewModel: DispatchBoardViewModel =
                viewModel(factory = DispatchBoardViewModel.Factory)
            DispatchBoardScreen(
                viewModel = viewModel,
                onOrderClick = { orderId ->
                    navController.navigate(Routes.orderDetail(orderId))
                },
                onLogout = onLogout
            )
        }

        composable(route = Routes.STATS) {
            StatsScreen()
        }

        composable(route = Routes.ORDER_DETAIL) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId").orEmpty()
            OrderDetailScreen(
                orderId = orderId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}