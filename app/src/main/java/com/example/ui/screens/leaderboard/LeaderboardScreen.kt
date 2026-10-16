package com.example.ui.screens.leaderboard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeaderboardUser
import com.example.data.repository.AppRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    repository: AppRepository,
    onNavigateBack: () -> Unit
) {
    BackHandler(onBack = onNavigateBack)

    val leaderboardUsers = remember { repository.getLeaderboardUsers() }
    val top1 = remember(leaderboardUsers) { leaderboardUsers.find { it.rank == 1 } ?: leaderboardUsers[0] }
    val top2 = remember(leaderboardUsers) { leaderboardUsers.find { it.rank == 2 } ?: leaderboardUsers.getOrNull(1) ?: top1 }
    val top3 = remember(leaderboardUsers) { leaderboardUsers.find { it.rank == 3 } ?: leaderboardUsers.getOrNull(2) ?: top1 }
    val remainingUsers = remember(leaderboardUsers) { leaderboardUsers.filter { it.rank > 3 } }

    var selectedUserForDetails by remember { mutableStateOf<LeaderboardUser?>(null) }

    Scaffold(
        containerColor = Color(0xFF3362F0),
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
                        modifier = Modifier.testTag("leaderboard_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Leaderboard",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0055D2),
                            Color(0xFF2855E5),
                            Color(0xFF3B67F2),
                            Color(0xFF4C75F2)
                        )
                    )
                )
        ) {
            // 1. TOP 3 PODIUM SECTION MATCHING SCREENSHOT EXACTLY
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Rank #2 (Left Podium)
                    PodiumCard(
                        user = top2,
                        podiumHeight = 200.dp,
                        podiumWidth = 104.dp,
                        rankDisplay = "#2",
                        rankColor = Color.White.copy(alpha = 0.75f),
                        avatarBorderColor = Color(0xFF60A5FA),
                        avatarSize = 58.dp,
                        hasCrown = false,
                        onClick = { selectedUserForDetails = top2 }
                    )

                    // Rank #1 (Center Podium - Champion)
                    PodiumCard(
                        user = top1,
                        podiumHeight = 236.dp,
                        podiumWidth = 118.dp,
                        rankDisplay = "#1",
                        rankColor = Color(0xFFFDE047),
                        avatarBorderColor = Color(0xFFFACC15),
                        avatarSize = 70.dp,
                        hasCrown = true,
                        onClick = { selectedUserForDetails = top1 }
                    )

                    // Rank #3 (Right Podium)
                    PodiumCard(
                        user = top3,
                        podiumHeight = 180.dp,
                        podiumWidth = 100.dp,
                        rankDisplay = "#3",
                        rankColor = Color(0xFFFB923C),
                        avatarBorderColor = Color(0xFFFB923C),
                        avatarSize = 54.dp,
                        hasCrown = false,
                        onClick = { selectedUserForDetails = top3 }
                    )
                }
            }

            // 2. BOTTOM ROUNDED SHEET CONTAINING RANKS 4+ MATCHING SCREENSHOT
            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(remainingUsers, key = { it.rank }) { user ->
                        LeaderboardListItemCard(
                            user = user,
                            onClick = { selectedUserForDetails = user }
                        )
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------
    // LEADER PROFILE DETAILS BOTTOM SHEET
    // -------------------------------------------------------------
    if (selectedUserForDetails != null) {
        val user = selectedUserForDetails!!
        ModalBottomSheet(
            onDismissRequest = { selectedUserForDetails = null },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Rank Indicator
                Surface(
                    color = if (user.rank == 1) Color(0xFFFDE047) else Color(0xFF0055D2),
                    shape = CircleShape,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "#${user.rank}",
                            color = if (user.rank == 1) Color(0xFF0F172A) else Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = user.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )

                Text(
                    text = "অ্যাফিলিয়েট আইডি: ${user.affiliateId}",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = Color(0xFFF3E8FF),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFDDD6FE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "মোট অ্যাক্টিভ মেম্বার:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF6B21A8)
                        )
                        Text(
                            text = user.memberCountText,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color(0xFF9333EA)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { selectedUserForDetails = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Text("বন্ধ করুন", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PODIUM CARD COMPONENT FOR RANKS 1, 2, 3
// -------------------------------------------------------------
@Composable
fun PodiumCard(
    user: LeaderboardUser,
    podiumHeight: androidx.compose.ui.unit.Dp,
    podiumWidth: androidx.compose.ui.unit.Dp,
    rankDisplay: String,
    rankColor: Color,
    avatarBorderColor: Color,
    avatarSize: androidx.compose.ui.unit.Dp,
    hasCrown: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = if (hasCrown) 0.22f else 0.16f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = if (hasCrown) 1.5.dp else 1.dp,
            color = if (hasCrown) Color(0xFFFDE047).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .width(podiumWidth)
            .height(podiumHeight)
            .clickable { onClick() }
            .testTag("podium_card_${user.rank}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Crown if rank 1 + Avatar
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (hasCrown) {
                    // Golden Crown Illustration
                    Icon(
                        imageVector = Icons.Default.MilitaryTech,
                        contentDescription = "Golden Crown",
                        tint = Color(0xFFFACC15),
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Avatar Box with styled border
                Box(
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .border(BorderStroke(2.5.dp, avatarBorderColor), CircleShape)
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    LeaderboardAvatarGraphic(avatarType = user.avatarType)
                }
            }

            // Middle: Name & Member Count
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = user.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (hasCrown) 12.5.sp else 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = user.memberCountText,
                    color = if (hasCrown) Color(0xFFFDE047) else Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Bottom: Rank #1, #2, #3
            Text(
                text = rankDisplay,
                color = rankColor,
                fontWeight = FontWeight.Black,
                fontSize = if (hasCrown) 22.sp else 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

// -------------------------------------------------------------
// LEADERBOARD LIST ITEM CARD (RANKS 4+) MATCHING SCREENSHOT
// -------------------------------------------------------------
@Composable
fun LeaderboardListItemCard(
    user: LeaderboardUser,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("leaderboard_card_${user.rank}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Rank Number Blue Badge matching Screenshot
            Surface(
                color = Color(0xFF0055D2),
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${user.rank}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 2. Avatar Thumbnail
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9))
                    .border(BorderStroke(1.dp, Color(0xFFCBD5E1)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                LeaderboardAvatarGraphic(avatarType = user.avatarType)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 3. Leader Name & Affiliate ID
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Affiliate ID: ${user.affiliateId}",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            }

            // 4. Purple Member Count Pill Badge matching Screenshot
            Surface(
                color = Color(0xFFF3E8FF),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFDDD6FE)),
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = user.memberCountText,
                    color = Color(0xFF8B5CF6),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// STYLIZED AVATAR GRAPHICS MATCHING USERS IN SCREENSHOT
// -------------------------------------------------------------
@Composable
fun LeaderboardAvatarGraphic(avatarType: Int) {
    when (avatarType) {
        1 -> {
            // Rank 1: Mohammad Ismail (Man in Suit)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1E3A8A), Color(0xFF1E293B))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFFFDE047),
                    modifier = Modifier.fillMaxSize(0.75f)
                )
            }
        }
        2 -> {
            // Rank 2: SAJIDUL TM
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0284C7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize(0.7f)
                )
            }
        }
        3 -> {
            // Rank 3: H.M Jahedol
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF7C2D12)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = Color(0xFFFDBA74),
                    modifier = Modifier.fillMaxSize(0.75f)
                )
            }
        }
        4 -> {
            // Rank 4: freelancer Likhon
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F766E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = null,
                    tint = Color(0xFFCCFBF1),
                    modifier = Modifier.fillMaxSize(0.75f)
                )
            }
        }
        5 -> {
            // Rank 5: Rootva Qween
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFBE185D)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Face4,
                    contentDescription = null,
                    tint = Color(0xFFFCE7F3),
                    modifier = Modifier.fillMaxSize(0.75f)
                )
            }
        }
        6 -> {
            // Rank 6: Amina Khatun
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF065F46)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PersonOutline,
                    contentDescription = null,
                    tint = Color(0xFFA7F3D0),
                    modifier = Modifier.fillMaxSize(0.75f)
                )
            }
        }
        7 -> {
            // Rank 7: LEADER RONJU
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF374151)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color(0xFF93C5FD),
                    modifier = Modifier.fillMaxSize(0.7f)
                )
            }
        }
        8 -> {
            // Rank 8: Rootva default
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.fillMaxSize(0.75f)
                )
            }
        }
        else -> {
            // Default avatar
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF3B82F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize(0.75f)
                )
            }
        }
    }
}
