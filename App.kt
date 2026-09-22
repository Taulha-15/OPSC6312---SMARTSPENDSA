package com.example.smartspendsa.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.smartspendsa.viewmodel.SmartSpendViewModel

@Composable
fun SmartSpendApp(vm: SmartSpendViewModel) {
    val user by vm.user.collectAsState()
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "login") {
        composable("login") {
            LoginScreen(
                vm = vm,
                onLogin = {
                    nav.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegister = { nav.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(vm) { nav.popBackStack() }
        }
        composable("home") {
            if (user == null) {
                LaunchedEffect(Unit) { nav.navigate("login") { popUpTo("home") { inclusive = true } } }
            } else {
                MainShell(vm, nav)
            }
        }
    }
}

@Composable
fun MainShell(vm: SmartSpendViewModel, nav: NavHostController) {
    var selected by remember { mutableIntStateOf(0) }
    var showAdd by remember { mutableStateOf(false) }
    var showBudget by remember { mutableStateOf(false) }
    val titles = listOf("Dashboard", "Transactions", "Analytics", "Settings")

    Scaffold(
        topBar = { TopAppBar(title = { Text(titles[selected]) }) },
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    Icons.Default.Home to "Home",
                    Icons.Default.List to "Transactions",
                    Icons.Default.PieChart to "Analytics",
                    Icons.Default.Settings to "Settings"
                )
                items.forEachIndexed { index, (icon, label) ->
                    NavigationBarItem(
                        selected = selected == index,
                        onClick = { selected = index },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (selected) {
                0 -> DashboardScreen(
                    vm = vm,
                    onAdd = { showAdd = true },
                    onBudget = { showBudget = true }
                )
                1 -> TransactionsScreen(vm, onAdd = { showAdd = true })
                2 -> AnalyticsScreen(vm)
                3 -> SettingsScreen(vm) {
                    nav.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddTransactionDialog(vm) { showAdd = false }
    }

    if (showBudget) {
        BudgetDialog(vm) { showBudget = false }
    }
}
