package com.example.ui.navigation

sealed class Screen(val route: String) {
    // Bottom Bar tabs
    data object Home : Screen("home")
    data object Accounts : Screen("accounts")
    data object Reports : Screen("reports")
    data object More : Screen("more")

    // Sub-screens
    data object AccountDetail : Screen("account_detail/{accountId}") {
        fun createRoute(accountId: Long) = "account_detail/$accountId"
    }

    data object AddEditAccount : Screen("add_edit_account?accountId={accountId}") {
        fun createRoute(accountId: Long? = null) = if (accountId != null) "add_edit_account?accountId=$accountId" else "add_edit_account"
    }

    data object AddEditTransaction : Screen("add_edit_transaction?accountId={accountId}&transId={transId}") {
        fun createRoute(accountId: Long? = null, transId: Long? = null): String {
            return when {
                accountId != null && transId != null -> "add_edit_transaction?accountId=$accountId&transId=$transId"
                accountId != null -> "add_edit_transaction?accountId=$accountId"
                transId != null -> "add_edit_transaction?transId=$transId"
                else -> "add_edit_transaction"
            }
        }
    }

    data object Transfer : Screen("transfer?sourceAccountId={sourceAccountId}") {
        fun createRoute(sourceAccountId: Long? = null) =
            if (sourceAccountId != null) "transfer?sourceAccountId=$sourceAccountId" else "transfer"
    }

    data object Vouchers : Screen("vouchers")

    data object AddVoucher : Screen("add_voucher?voucherType={voucherType}&accountId={accountId}") {
        fun createRoute(voucherType: String = "RECEIPT", accountId: Long? = null) =
            if (accountId != null) "add_voucher?voucherType=$voucherType&accountId=$accountId"
            else "add_voucher?voucherType=$voucherType"
    }

    data object AllTransactions : Screen("all_transactions")
    data object Currencies : Screen("currencies")
    data object Categories : Screen("categories")
    data object Settings : Screen("settings")
    data object BackupRestore : Screen("backup_restore")
}
