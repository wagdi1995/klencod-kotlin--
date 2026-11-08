package com.example.ui

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.PremiumBottomBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.accounts.AccountDetailScreen
import com.example.ui.screens.accounts.AccountsScreen
import com.example.ui.screens.accounts.AddEditAccountScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.more.BackupRestoreScreen
import com.example.ui.screens.more.CategoriesScreen
import com.example.ui.screens.more.CurrenciesScreen
import com.example.ui.screens.more.MoreScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.transactions.AddEditTransactionScreen
import com.example.ui.screens.transactions.AllTransactionsScreen
import com.example.ui.screens.transfer.TransferScreen
import com.example.ui.screens.vouchers.AddVoucherScreen
import com.example.ui.screens.vouchers.VouchersScreen
import com.example.ui.theme.AppBg
import com.example.ui.viewmodel.AccountDetailViewModel
import com.example.ui.viewmodel.AccountsViewModel
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.ReportsViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.TransactionsViewModel
import com.example.ui.viewmodel.TransferViewModel
import com.example.ui.viewmodel.ViewModelFactory
import com.example.ui.viewmodel.VouchersViewModel

@Composable
fun HisabatiMainApp() {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val isTopLevelDestination = currentRoute in listOf(
        Screen.Home.route,
        Screen.Accounts.route,
        Screen.Reports.route,
        Screen.More.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppBg,
        bottomBar = {
            AnimatedVisibility(
                visible = isTopLevelDestination,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                PremiumBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Home Screen
            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = viewModel(factory = ViewModelFactory.createFactory(app))
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            // 2. Accounts List Screen
            composable(Screen.Accounts.route) {
                val accountsViewModel: AccountsViewModel = viewModel(factory = ViewModelFactory.createFactory(app))
                AccountsScreen(
                    viewModel = accountsViewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            // 3. Reports Screen
            composable(Screen.Reports.route) {
                val reportsViewModel: ReportsViewModel = viewModel(factory = ViewModelFactory.createFactory(app))
                ReportsScreen(
                    viewModel = reportsViewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            // 4. More Screen
            composable(Screen.More.route) {
                MoreScreen(
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            // Sub-screen: Account Details & Statement
            composable(
                route = Screen.AccountDetail.route,
                arguments = listOf(navArgument("accountId") { type = NavType.LongType })
            ) { backStackEntry ->
                val accountId = backStackEntry.arguments?.getLong("accountId") ?: 0L
                val detailViewModel: AccountDetailViewModel = viewModel(
                    factory = ViewModelFactory.createFactory(app, extraId = accountId)
                )
                AccountDetailScreen(
                    viewModel = detailViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            // Sub-screen: Add / Edit Account
            composable(
                route = Screen.AddEditAccount.route,
                arguments = listOf(
                    navArgument("accountId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val accountId = backStackEntry.arguments?.getLong("accountId")
                val actualId = if (accountId != null && accountId > 0) accountId else null
                val accountsViewModel: AccountsViewModel = viewModel(factory = ViewModelFactory.createFactory(app))
                AddEditAccountScreen(
                    viewModel = accountsViewModel,
                    accountId = actualId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Sub-screen: Add / Edit Transaction
            composable(
                route = Screen.AddEditTransaction.route,
                arguments = listOf(
                    navArgument("accountId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument("transId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val acctId = backStackEntry.arguments?.getLong("accountId")
                val transId = backStackEntry.arguments?.getLong("transId")
                val transactionsViewModel: TransactionsViewModel = viewModel(factory = ViewModelFactory.createFactory(app))
                AddEditTransactionScreen(
                    viewModel = transactionsViewModel,
                    initialAccountId = if (acctId != null && acctId > 0) acctId else null,
                    transactionId = if (transId != null && transId > 0) transId else null,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Sub-screen: Transfer Between Accounts
            composable(
                route = Screen.Transfer.route,
                arguments = listOf(
                    navArgument("sourceAccountId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val srcId = backStackEntry.arguments?.getLong("sourceAccountId")
                val transferViewModel: TransferViewModel = viewModel(
                    factory = ViewModelFactory.createFactory(app, extraId = if (srcId != null && srcId > 0) srcId else null)
                )
                TransferScreen(
                    viewModel = transferViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Sub-screen: All Vouchers
            composable(Screen.Vouchers.route) {
                val vouchersViewModel: VouchersViewModel = viewModel(factory = ViewModelFactory.createFactory(app))
                VouchersScreen(
                    viewModel = vouchersViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            // Sub-screen: Add Voucher
            composable(
                route = Screen.AddVoucher.route,
                arguments = listOf(
                    navArgument("voucherType") {
                        type = NavType.StringType
                        defaultValue = "RECEIPT"
                    },
                    navArgument("accountId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val vType = backStackEntry.arguments?.getString("voucherType") ?: "RECEIPT"
                val acctId = backStackEntry.arguments?.getLong("accountId")
                val vouchersViewModel: VouchersViewModel = viewModel(factory = ViewModelFactory.createFactory(app))
                AddVoucherScreen(
                    viewModel = vouchersViewModel,
                    initialType = vType,
                    initialAccountId = if (acctId != null && acctId > 0) acctId else null,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Sub-screen: All Transactions
            composable(Screen.AllTransactions.route) {
                val transactionsViewModel: TransactionsViewModel = viewModel(factory = ViewModelFactory.createFactory(app))
                AllTransactionsScreen(
                    viewModel = transactionsViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            // Sub-screen: Currencies
            composable(Screen.Currencies.route) {
                val settingsViewModel: SettingsViewModel = viewModel(factory = ViewModelFactory.createFactory(app))
                CurrenciesScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Sub-screen: Categories
            composable(Screen.Categories.route) {
                val settingsViewModel: SettingsViewModel = viewModel(factory = ViewModelFactory.createFactory(app))
                CategoriesScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Sub-screen: Backup & Restore
            composable(Screen.BackupRestore.route) {
                val settingsViewModel: SettingsViewModel = viewModel(factory = ViewModelFactory.createFactory(app))
                BackupRestoreScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
