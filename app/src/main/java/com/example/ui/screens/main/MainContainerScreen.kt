package com.example.ui.screens.main

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AppRepository
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.team.TeamScreen
import com.example.ui.screens.wallet.WalletScreen
import com.example.ui.screens.work.WorkScreen

enum class MainTab(val title: String, val icon: ImageVector, val showLabel: Boolean) {
    HOME("হোম", Icons.Outlined.Home, true),
    WALLET("ওয়ালেট", Icons.Outlined.Payments, false),
    SHOP("শপ", Icons.Outlined.ShoppingCart, false),
    TEAM("টিম", Icons.Outlined.Groups, false),
    PROFILE("প্রোফাইল", Icons.Outlined.AccountCircle, false)
}

@Composable
fun MainContainerScreen(
    repository: AppRepository,
    onNavigateToTaskDetail: (String) -> Unit,
    onNavigateToDeposit: () -> Unit,
    onNavigateToWithdraw: () -> Unit,
    onNavigateToReferral: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToTransactionDetail: (String) -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToAnnouncements: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }

    Scaffold(
        containerColor = Color(0xFFF4F6F9),
        bottomBar = {
            // Royal Blue Screenshot Bottom Bar
            Surface(
                color = Color(0xFF0055D2),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .testTag("screenshot_bottom_nav")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MainTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedTab = tab }
                                .padding(vertical = 4.dp)
                                .testTag("tab_${tab.name.lowercase()}")
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.75f),
                                modifier = Modifier.size(26.dp)
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                MainTab.HOME -> {
                    HomeScreen(
                        repository = repository,
                        onNavigateToTasks = { selectedTab = MainTab.SHOP },
                        onNavigateToDeposit = onNavigateToDeposit,
                        onNavigateToWithdraw = onNavigateToWithdraw,
                        onNavigateToReferral = onNavigateToReferral,
                        onNavigateToTransactions = onNavigateToTransactions,
                        onNavigateToSupport = onNavigateToSupport,
                        onNavigateToNotifications = onNavigateToNotifications,
                        onNavigateToAnnouncements = onNavigateToAnnouncements,
                        onNavigateToProfile = { selectedTab = MainTab.PROFILE },
                        onSelectTransaction = onNavigateToTransactionDetail
                    )
                }
                MainTab.WALLET -> {
                    WalletScreen(
                        repository = repository,
                        onNavigateToDeposit = onNavigateToDeposit,
                        onNavigateToWithdraw = onNavigateToWithdraw,
                        onNavigateToTransactions = onNavigateToTransactions,
                        onSelectTransaction = onNavigateToTransactionDetail
                    )
                }
                MainTab.SHOP -> {
                    WorkScreen(
                        repository = repository,
                        onSelectTask = onNavigateToTaskDetail
                    )
                }
                MainTab.TEAM -> {
                    TeamScreen(
                        repository = repository,
                        onNavigateToReferral = onNavigateToReferral
                    )
                }
                MainTab.PROFILE -> {
                    ProfileScreen(
                        repository = repository,
                        onNavigateToEditProfile = onNavigateToEditProfile,
                        onNavigateToTransactions = onNavigateToTransactions,
                        onNavigateToReferral = onNavigateToReferral,
                        onNavigateToSupport = onNavigateToSupport,
                        onNavigateToNotifications = onNavigateToNotifications,
                        onNavigateToSettings = onNavigateToSettings,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}
