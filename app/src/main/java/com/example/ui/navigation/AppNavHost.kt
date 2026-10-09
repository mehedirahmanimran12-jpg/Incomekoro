package com.example.ui.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.data.preferences.UserPreferences
import com.example.data.repository.AppRepository
import com.example.ui.screens.auth.ForgotPasswordScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.main.MainContainerScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.profile.EditProfileScreen
import com.example.ui.screens.profile.SettingsScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.support.*
import com.example.ui.screens.team.ReferralScreen
import com.example.ui.screens.wallet.DepositScreen
import com.example.ui.screens.wallet.TransactionDetailScreen
import com.example.ui.screens.wallet.TransactionsScreen
import com.example.ui.screens.wallet.WithdrawScreen
import com.example.ui.screens.work.TaskDetailScreen
import com.example.ui.screens.work.TaskSubmissionScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    preferences: UserPreferences,
    repository: AppRepository
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                preferences = preferences,
                repository = repository,
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                preferences = preferences,
                onFinishOnboarding = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                repository = repository,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                repository = repository,
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onRegisterSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Main.route) {
            MainContainerScreen(
                repository = repository,
                onNavigateToTaskDetail = { taskId ->
                    navController.navigate(Screen.TaskDetails.createRoute(taskId))
                },
                onNavigateToDeposit = { navController.navigate(Screen.Deposit.route) },
                onNavigateToWithdraw = { navController.navigate(Screen.Withdraw.route) },
                onNavigateToReferral = { navController.navigate(Screen.Referral.route) },
                onNavigateToTransactions = { navController.navigate(Screen.Transactions.route) },
                onNavigateToTransactionDetail = { trxId ->
                    navController.navigate(Screen.TransactionDetails.createRoute(trxId))
                },
                onNavigateToSupport = { navController.navigate(Screen.Support.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToAnnouncements = { navController.navigate(Screen.Announcements.route) },
                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.TaskDetails.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            TaskDetailScreen(
                taskId = taskId,
                repository = repository,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSubmission = { id ->
                    navController.navigate(Screen.TaskSubmission.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.TaskSubmission.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            TaskSubmissionScreen(
                taskId = taskId,
                repository = repository,
                onNavigateBack = { navController.popBackStack() },
                onSubmitSuccess = {
                    navController.popBackStack(Screen.Main.route, inclusive = false)
                }
            )
        }

        composable(Screen.Deposit.route) {
            DepositScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() },
                onDepositSuccess = { navController.popBackStack() }
            )
        }

        composable(Screen.Withdraw.route) {
            WithdrawScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() },
                onWithdrawSuccess = { navController.popBackStack() }
            )
        }

        composable(Screen.Transactions.route) {
            TransactionsScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() },
                onSelectTransaction = { trxId ->
                    navController.navigate(Screen.TransactionDetails.createRoute(trxId))
                }
            )
        }

        composable(
            route = Screen.TransactionDetails.route,
            arguments = listOf(navArgument("trxId") { type = NavType.StringType })
        ) { backStackEntry ->
            val trxId = backStackEntry.arguments?.getString("trxId") ?: ""
            TransactionDetailScreen(
                trxId = trxId,
                repository = repository,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Referral.route) {
            ReferralScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Support.route) {
            SupportScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNewTicket = { navController.navigate(Screen.NewTicket.route) },
                onSelectTicket = { ticketId ->
                    navController.navigate(Screen.TicketDetails.createRoute(ticketId))
                }
            )
        }

        composable(Screen.NewTicket.route) {
            NewTicketScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() },
                onCreatedSuccess = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.TicketDetails.route,
            arguments = listOf(navArgument("ticketId") { type = NavType.StringType })
        ) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
            TicketDetailScreen(
                ticketId = ticketId,
                repository = repository,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Announcements.route) {
            AnnouncementsScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() },
                onSelectAnnouncement = { annId ->
                    navController.navigate(Screen.AnnouncementDetails.createRoute(annId))
                }
            )
        }

        composable(
            route = Screen.AnnouncementDetails.route,
            arguments = listOf(navArgument("announcementId") { type = NavType.StringType })
        ) { backStackEntry ->
            val annId = backStackEntry.arguments?.getString("announcementId") ?: ""
            AnnouncementDetailScreen(
                announcementId = annId,
                repository = repository,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
