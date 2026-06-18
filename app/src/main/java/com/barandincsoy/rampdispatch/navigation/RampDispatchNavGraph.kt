package com.barandincsoy.rampdispatch.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.barandincsoy.rampdispatch.ui.board.DispatchBoardScreen
import com.barandincsoy.rampdispatch.ui.board.DispatchBoardViewModel
import com.barandincsoy.rampdispatch.ui.detail.OrderDetailScreen
import com.barandincsoy.rampdispatch.ui.fueling.FuelingWizardScreen
import com.barandincsoy.rampdispatch.ui.stats.StatsScreen

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
                onBack = { navController.popBackStack() },
                onStartFueling = {
                    navController.navigate(Routes.fuelingWizard(orderId))
                }
            )
        }
        composable(route = Routes.FUELING_WIZARD) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId").orEmpty()
            FuelingWizardScreen(
                orderId = orderId,
                onFinished = {
                    // Order completed — go straight back to the board, skipping detail.
                    navController.popBackStack(Routes.BOARD, inclusive = false)
                },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}