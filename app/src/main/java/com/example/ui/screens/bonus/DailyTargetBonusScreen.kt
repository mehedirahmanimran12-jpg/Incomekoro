package com.example.ui.screens.bonus

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.AppRepository
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale

enum class WinnerAvatarType {
    ISMAIL,
    SAJIDUL,
    RONJU
}

data class LiveRankingItem(
    val rank: Int,
    val name: String,
    val verificationCount: Int,
    val progress: Float,
    val avatarType: WinnerAvatarType
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyTargetBonusScreen(
    repository: AppRepository,
    onNavigateBack: () -> Unit
) {
    BackHandler(onBack = onNavigateBack)

    var showInfoDialog by remember { mutableStateOf(false) }

    // Live remaining countdown timer calculating seconds until midnight
    var remainingSeconds by remember {
        val now = Calendar.getInstance()
        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }
        val diff = ((midnight.timeInMillis - now.timeInMillis) / 1000).coerceAtLeast(0)
        // If calculated time is around typical remaining day time, match screenshot range ~5hr 48m or live
        val initialSecs = if (diff > 0) diff else (5 * 3600 + 48 * 60 + 21)
        mutableLongStateOf(initialSecs)
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            if (remainingSeconds > 0) {
                remainingSeconds -= 1
            } else {
                remainingSeconds = 24 * 3600 - 1
            }
        }
    }

    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

    // Yesterday's Winner from Screenshot
    val yesterdayWinnerName = "Mohammad Ismail"
    val yesterdayDate = "2026-09-03"
    val yesterdayCount = 11

    // Today's Live Rankings matching Screenshot
    val rankings = remember {
        listOf(
            LiveRankingItem(
                rank = 1,
                name = "1. Mohammad Ismail",
                verificationCount = 9,
                progress = 0.90f,
                avatarType = WinnerAvatarType.ISMAIL
            ),
            LiveRankingItem(
                rank = 2,
                name = "2. SAJIDUL TM™",
                verificationCount = 2,
                progress = 0.38f,
                avatarType = WinnerAvatarType.SAJIDUL
            ),
            LiveRankingItem(
                rank = 3,
                name = "3. \"LEADER RONJU\"",
                verificationCount = 1,
                progress = 0.22f,
                avatarType = WinnerAvatarType.RONJU
            )
        )
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FD),
        topBar = {
            Surface(
                color = Color(0xFF0055D2),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 6.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("daily_bonus_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Daily Live Winners",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = Color.White,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    )

                    // Right Speech Bubble Info Icon matching Screenshot Exactly
                    IconButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier.testTag("daily_bonus_info_button")
                    ) {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Rules & Info",
                                    tint = Color(0xFF0055D2),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FD)),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Promotional Hero Banner matching Screenshot
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_daily_winner_banner),
                        contentDescription = "Daily Winner Bonus",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(185.dp)
                    )
                }
            }

            // 2. "গতকালের বিজয়ী হলো" Header
            item {
                Text(
                    text = "গতকালের বিজয়ী হলো",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(start = 2.dp, top = 2.dp)
                )
            }

            // 3. Yesterday's Winner Card matching Screenshot
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEDEEF8)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("yesterday_winner_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WinnerAvatarView(
                            type = WinnerAvatarType.ISMAIL,
                            size = 54.dp
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = yesterdayWinnerName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = yesterdayDate,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF64748B)
                            )
                        }

                        Text(
                            text = yesterdayCount.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF22C55E)
                        )
                    }
                }
            }

            // 4. Countdown Timer Box matching Screenshot
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEDEEF8)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("countdown_timer_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "সময় বাকি: $timeFormatted",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom Progress Bar matching Screenshot (Green portion on left, grey on right)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFE2E8F0))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = 0.25f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF22C55E))
                            )
                        }
                    }
                }
            }

            // 5. "আজকের লাইভ র‍্যাংকিং:" Header
            item {
                Text(
                    text = "আজকের লাইভ র‍্যাংকিং:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(start = 2.dp, top = 4.dp)
                )
            }

            // 6. Live Ranking Cards (1. Mohammad Ismail, 2. SAJIDUL TM™, 3. "LEADER RONJU")
            items(rankings, key = { it.rank }) { item ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ranking_card_${item.rank}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            WinnerAvatarView(
                                type = item.avatarType,
                                size = 50.dp
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = item.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "ভেরিফিকেশন: ${item.verificationCount} জন",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF475569)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Green Progress Bar underneath matching Screenshot
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFFE2E8F0))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = item.progress)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFF22C55E))
                            )
                        }
                    }
                }
            }
        }
    }

    // Rules & Guidelines Info Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text(
                    text = "দৈনিক লাইভ উইনার ও বোনাস নিয়মাবলী",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF0055D2)
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🏆 প্রথম পুরস্কার: সর্বোচ্চ ভেরিফিকেশনকারী পাবেন নগদ ৫০ টাকা।",
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "🥈 দ্বিতীয় পুরস্কার: ২য় স্থানাধিকারী পাবেন নগদ ৩০ টাকা।",
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "🥉 তৃতীয় পুরস্কার: ৩য় স্থানাধিকারী পাবেন নগদ ২০ টাকা।",
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )
                    Divider(color = Color(0xFFE2E8F0), modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "⏰ সময়সীমা: প্রতিদিন রাত ১১:৫৯:৫৯ মিনিট পর্যন্ত লাইভ র‍্যাংকিং কার্যকর থাকে।",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = "💰 বোনাস ক্রেডিট: রাত ১২:০০ টায় ফলাফল চূড়ান্ত হয়ে স্বয়ংক্রিয়ভাবে ওয়ালেট ব্যালেন্সে বোনাস জমা হয়ে যায়।",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = "👥 নিয়ম: আপনার রেফারেল লিংকের মাধ্যমে যেকোনো মেম্বার ভেরিফাই হলেই আপনার ভেরিফিকেশন পয়েন্ট বৃদ্ধি পাবে।",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ঠিক আছে", color = Color.White)
                }
            }
        )
    }
}

/**
 * Beautiful Custom Canvas-based Avatars representing the exact visual identity of the 3 winners
 */
@Composable
fun WinnerAvatarView(
    type: WinnerAvatarType,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(1.5.dp, Color(0xFFCBD5E1), CircleShape)
    ) {
        when (type) {
            WinnerAvatarType.ISMAIL -> {
                // Young professional in navy suit, white shirt, dark tie matching screenshot
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = this.size.width
                    val h = this.size.height

                    // Background Office Lighting
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8))
                        )
                    )

                    // Navy Suit Shoulders
                    val suitPath = Path().apply {
                        moveTo(0f, h)
                        lineTo(0f, h * 0.72f)
                        cubicTo(w * 0.2f, h * 0.65f, w * 0.35f, h * 0.65f, w * 0.5f, h * 0.70f)
                        cubicTo(w * 0.65f, h * 0.65f, w * 0.8f, h * 0.65f, w, h * 0.72f)
                        lineTo(w, h)
                        close()
                    }
                    drawPath(suitPath, Color(0xFF0F254B))

                    // White Shirt Collar Triangle
                    val shirtPath = Path().apply {
                        moveTo(w * 0.40f, h * 0.64f)
                        lineTo(w * 0.50f, h * 0.80f)
                        lineTo(w * 0.60f, h * 0.64f)
                        close()
                    }
                    drawPath(shirtPath, Color.White)

                    // Dark Navy/Black Tie
                    val tiePath = Path().apply {
                        moveTo(w * 0.47f, h * 0.70f)
                        lineTo(w * 0.53f, h * 0.70f)
                        lineTo(w * 0.54f, h * 0.95f)
                        lineTo(w * 0.50f, h)
                        lineTo(w * 0.46f, h * 0.95f)
                        close()
                    }
                    drawPath(tiePath, Color(0xFF050E1E))

                    // Face / Head
                    drawOval(
                        color = Color(0xFFF1C27D),
                        topLeft = Offset(w * 0.32f, h * 0.28f),
                        size = Size(w * 0.36f, h * 0.40f)
                    )

                    // Black Hair
                    val hairPath = Path().apply {
                        moveTo(w * 0.28f, h * 0.38f)
                        cubicTo(w * 0.28f, h * 0.18f, w * 0.72f, h * 0.18f, w * 0.72f, h * 0.38f)
                        cubicTo(w * 0.65f, h * 0.28f, w * 0.35f, h * 0.28f, w * 0.28f, h * 0.38f)
                        close()
                    }
                    drawPath(hairPath, Color(0xFF1E293B))

                    // Gentle Smile
                    drawArc(
                        color = Color(0xFF78350F),
                        startAngle = 20f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(w * 0.44f, h * 0.52f),
                        size = Size(w * 0.12f, h * 0.08f),
                        style = Stroke(width = 1.5f)
                    )
                }
            }

            WinnerAvatarType.SAJIDUL -> {
                // Circular logo "ROOTVA BUSINESS PLATFORM SAJIDUL TM MEMBERS" with cyan & navy rings
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = this.size.width
                    val h = this.size.height

                    // Deep Royal Blue Background
                    drawCircle(Color(0xFF0A2558))

                    // Cyan Accent Ring
                    drawCircle(
                        color = Color(0xFF00E5FF),
                        radius = w * 0.44f,
                        style = Stroke(width = 2.5f)
                    )

                    // Inner White Ring
                    drawCircle(
                        color = Color.White,
                        radius = w * 0.32f
                    )

                    // Character Inside
                    drawCircle(
                        color = Color(0xFF0A2558),
                        radius = w * 0.28f
                    )

                    // Person Head
                    drawOval(
                        color = Color(0xFFF1C27D),
                        topLeft = Offset(w * 0.38f, h * 0.32f),
                        size = Size(w * 0.24f, h * 0.30f)
                    )

                    // Hair
                    drawArc(
                        color = Color(0xFF1E293B),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(w * 0.37f, h * 0.26f),
                        size = Size(w * 0.26f, h * 0.22f)
                    )

                    // Cyan Tie / Badge
                    val badgePath = Path().apply {
                        moveTo(w * 0.44f, h * 0.65f)
                        lineTo(w * 0.56f, h * 0.65f)
                        lineTo(w * 0.50f, h * 0.85f)
                        close()
                    }
                    drawPath(badgePath, Color(0xFF00E5FF))
                }
            }

            WinnerAvatarType.RONJU -> {
                // Office Desk setting with "LEADER RONJU" in black shirt & silver round badge
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = this.size.width
                    val h = this.size.height

                    // Office Background
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                        )
                    )

                    // Black Casual Polo/Shirt
                    val shirtPath = Path().apply {
                        moveTo(0f, h)
                        lineTo(0f, h * 0.74f)
                        cubicTo(w * 0.25f, h * 0.65f, w * 0.75f, h * 0.65f, w, h * 0.74f)
                        lineTo(w, h)
                        close()
                    }
                    drawPath(shirtPath, Color(0xFF0F172A))

                    // Silver Round Badge on Left Chest
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFF8FAFC), Color(0xFF94A3B8))
                        ),
                        radius = w * 0.12f,
                        center = Offset(w * 0.24f, h * 0.52f)
                    )
                    drawCircle(
                        color = Color(0xFF38BDF8),
                        radius = w * 0.12f,
                        center = Offset(w * 0.24f, h * 0.52f),
                        style = Stroke(width = 1.5f)
                    )

                    // Head / Face
                    drawOval(
                        color = Color(0xFFE0BB95),
                        topLeft = Offset(w * 0.34f, h * 0.30f),
                        size = Size(w * 0.32f, h * 0.36f)
                    )

                    // Dark Hair
                    val hairPath = Path().apply {
                        moveTo(w * 0.32f, h * 0.38f)
                        cubicTo(w * 0.32f, h * 0.20f, w * 0.68f, h * 0.20f, w * 0.68f, h * 0.38f)
                        cubicTo(w * 0.60f, h * 0.28f, w * 0.40f, h * 0.28f, w * 0.32f, h * 0.38f)
                        close()
                    }
                    drawPath(hairPath, Color(0xFF0F172A))
                }
            }
        }
    }
}
