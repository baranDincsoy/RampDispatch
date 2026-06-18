package com.barandincsoy.rampdispatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.barandincsoy.rampdispatch.navigation.RampDispatchNavGraph
import com.barandincsoy.rampdispatch.navigation.Routes
import com.barandincsoy.rampdispatch.ui.login.LoginScreen
import com.barandincsoy.rampdispatch.ui.theme.RampDispatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RampDispatchTheme {
                val app = applicationContext as RampDispatchApplication
                val currentUser by app.sessionManager.currentUser.collectAsStateWithLifecycle()

                if (currentUser == null) {
                    LoginScreen()
                } else {
                    MainScaffold()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RampDispatchTheme {
        Greeting("Android")
    }
}

@Composable
private fun MainScaffold() {
    val context = LocalContext.current
    val app = context.applicationContext as RampDispatchApplication
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Bottom bar only on top-level screens, not on detail.
    val showBottomBar = currentRoute == Routes.BOARD || currentRoute == Routes.STATS

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Routes.BOARD,
                        onClick = {
                            navController.navigate(Routes.BOARD) {
                                popUpTo(Routes.BOARD) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.List, contentDescription = null) },
                        label = { Text("Board") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.STATS,
                        onClick = {
                            navController.navigate(Routes.STATS) {
                                popUpTo(Routes.BOARD)
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.Assessment, contentDescription = null) },
                        label = { Text("Stats") }

                    )
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            RampDispatchNavGraph(navController = navController)
        }
        Box(Modifier.padding(innerPadding)) {
            RampDispatchNavGraph(
                navController = navController,
                onLogout = { app.sessionManager.logout() }
            )
        }
    }
}