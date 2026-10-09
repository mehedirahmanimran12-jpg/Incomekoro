package com.example.ui.screens.main

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.repository.AppRepository
import com.example.ui.components.SideMenuDrawerContent
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.team.TeamScreen
import com.example.ui.screens.wallet.WalletScreen
import com.example.ui.screens.work.WorkScreen
import kotlinx.coroutines.launch

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
    val currentUser by repository.currentUser.collectAsState(initial = null)
    val user = currentUser ?: User(
        name = "MEHEDI",
        referralCode = "250829",
        balance = 1450.0
    )

    var selectedTab by remember { mutableStateOf(MainTab.HOME) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var infoDialogTitle by remember { mutableStateOf<String?>(null) }
    var infoDialogMessage by remember { mutableStateOf<String?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            SideMenuDrawerContent(
                user = user,
                onNavigateToHome = {
                    selectedTab = MainTab.HOME
                    coroutineScope.launch { drawerState.close() }
                },
                onNavigateToAbout = {
                    infoDialogTitle = "আমাদের সম্পর্কে (About Us)"
                    infoDialogMessage = "INCOME KORO হলো বাংলাদেশের সবচেয়ে বিশ্বস্ত ও আধুনিক ডিজিটাল আর্নিং এবং বিজনেস নেটওয়ার্ক। এখানে মাইক্রো জব, রেফারেল নেটওয়ার্কিং ও টিম আর্নিংয়ের মাধ্যমে ঘরে বসেই নিশ্চিত আয় করতে পারবেন।"
                },
                onNavigateToPassiveIncome = {
                    infoDialogTitle = "প্যাসিভ ইনকাম (Passive Income)"
                    infoDialogMessage = "আপনার অ্যাফিলিয়েট টিম বৃদ্ধি করে প্রতি মাসে রয়্যালটি ও আজীবন প্যাসিভ ইনকাম উপভোগ করুন। আপনার রেফারেল লিংক শেয়ার করে আজই বড় টিম তৈরি করুন!"
                },
                onNavigateToSupport = {
                    coroutineScope.launch { drawerState.close() }
                    onNavigateToSupport()
                },
                onNavigateToMeeting = {
                    infoDialogTitle = "জয়েন মিটিং শিডিউল"
                    infoDialogMessage = "প্রতিদিন রাত ৯:০০ টায় Zoom এবং Google Meet-এ ফ্রি ট্রেনিং ক্লাস অনুষ্ঠিত হয়। লিংক আমাদের অফিশিয়াল টেলিগ্রাম ও হোয়াটসঅ্যাপ গ্রুপে শেয়ার করা হয়।"
                },
                onNavigateToSocial = { name ->
                    Toast.makeText(context, "$name এ জয়েন করা হচ্ছে...", Toast.LENGTH_SHORT).show()
                },
                onNavigateToFaq = {
                    infoDialogTitle = "সাধারণ জিজ্ঞাসা (FAQ)"
                    infoDialogMessage = "১. কিভাবে কাজ শুরু করব?\nশপ/টাস্ক অপশন থেকে টাস্ক গ্রহণ করে নির্দেশিকা মেনে সাবমিট করুন।\n\n২. কিভাবে উইথড্র করব?\nওয়ালেট অপশনে গিয়ে বিকাশ/নগদ/রকেটের মাধ্যমে সর্বনিম্ন ৫০ টাকা হলেই উইথড্র করতে পারবেন।"
                },
                onNavigateToPrivacy = {
                    infoDialogTitle = "প্রাইভেসি পলিসি"
                    infoDialogMessage = "INCOME KORO আপনার তথ্যের সর্বোচ্চ নিরাপত্তা নিশ্চিত করে। কোনো পাসওয়ার্ড বা ব্যক্তিগত সংবেদনশীল তথ্য তৃতীয় পক্ষের সাথে শেয়ার করা হয় না।"
                },
                onRateApp = {
                    Toast.makeText(context, "ধন্যবাদ! ৫ স্টার রেটিং সফল হয়েছে ⭐⭐⭐⭐⭐", Toast.LENGTH_LONG).show()
                },
                onLogout = {
                    coroutineScope.launch { drawerState.close() }
                    onLogout()
                },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
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
                            onOpenMenu = { coroutineScope.launch { drawerState.open() } },
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

    if (infoDialogTitle != null) {
        AlertDialog(
            onDismissRequest = { infoDialogTitle = null },
            title = {
                Text(
                    text = infoDialogTitle.orEmpty(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = infoDialogMessage.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { infoDialogTitle = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2))
                ) {
                    Text("ঠিক আছে")
                }
            }
        )
    }
}
