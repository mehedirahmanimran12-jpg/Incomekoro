package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Main : Screen("main")

    // Sub-screens
    object TaskDetails : Screen("task_details/{taskId}") {
        fun createRoute(taskId: String) = "task_details/$taskId"
    }
    object TaskSubmission : Screen("task_submission/{taskId}") {
        fun createRoute(taskId: String) = "task_submission/$taskId"
    }
    object Deposit : Screen("deposit")
    object Withdraw : Screen("withdraw")
    object Transactions : Screen("transactions")
    object TransactionDetails : Screen("transaction_details/{trxId}") {
        fun createRoute(trxId: String) = "transaction_details/$trxId"
    }
    object Referral : Screen("referral")
    object Notifications : Screen("notifications")
    object Support : Screen("support")
    object NewTicket : Screen("new_ticket")
    object TicketDetails : Screen("ticket_details/{ticketId}") {
        fun createRoute(ticketId: String) = "ticket_details/$ticketId"
    }
    object Announcements : Screen("announcements")
    object AnnouncementDetails : Screen("announcement_details/{announcementId}") {
        fun createRoute(announcementId: String) = "announcement_details/$announcementId"
    }
    object EditProfile : Screen("edit_profile")
    object Settings : Screen("settings")
}
