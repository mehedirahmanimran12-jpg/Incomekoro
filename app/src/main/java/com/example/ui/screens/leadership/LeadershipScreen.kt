package com.example.ui.screens.leadership

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.LeadershipTier
import com.example.data.repository.AppRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadershipScreen(
    repository: AppRepository,
    onNavigateBack: () -> Unit
) {
    BackHandler(onBack = onNavigateBack)

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentUser by repository.currentUser.collectAsState(initial = null)
    val teamMembers by repository.teamMembers.collectAsState(initial = emptyList())
    val resellingOrders by repository.resellingOrders.collectAsState(initial = emptyList())

    val userReferrals = teamMembers.size.coerceAtLeast(currentUser?.teamCount ?: 0)
    val userSales = resellingOrders.size

    val tiers = remember { repository.getLeadershipTiers() }

    var selectedTierForTarget by remember { mutableStateOf<LeadershipTier?>(null) }
    var selectedTierForReward by remember { mutableStateOf<LeadershipTier?>(null) }
    var showClaimSuccessDialog by remember { mutableStateOf(false) }
    var lastClaimedRewardAmount by remember { mutableStateOf(0.0) }
    var isClaiming by remember { mutableStateOf(false) }

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
                        modifier = Modifier.testTag("leadership_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Leadership",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FD)),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Hero Promotional Header matching Screenshot
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    // Dark Blue Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .background(Color(0xFF05112B))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_leadership_hero),
                            contentDescription = "Leadership Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            alpha = 0.45f
                        )

                        // Centered Bengali Banner Typography matching Screenshot Exactly
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Line 1: Rootva লিডারশিপ টার্গেট ও রিওয়ার্ড
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Rootva ",
                                    color = Color(0xFFFFE600),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "লিডারশিপ টার্গেট ও রিওয়ার্ড",
                                    color = Color(0xFF00E5FF),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Line 2: নেতৃত্ব দিন, আয় বাড়ান, স্বপ্ন পূরণ করুন!
                            Text(
                                text = "নেতৃত্ব দিন, আয় বাড়ান, স্বপ্ন পূরণ করুন!",
                                color = Color(0xFFFF2D75),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Line 3: এগিয়ে যান একধাপ এগিয়ে
                            Text(
                                text = "এগিয়ে যান একধাপ এগিয়ে",
                                color = Color(0xFF00E5FF),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Line 4: — আজই শুরু করুন! 🚀
                            Text(
                                text = "— আজই শুরু করুন! 🚀",
                                color = Color(0xFF00E5FF),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Floating Overlapping Circular Badge with Golden Border matching screenshot
                    Surface(
                        color = Color(0xFF081C45),
                        shape = CircleShape,
                        border = BorderStroke(2.5.dp, Color(0xFFFFC107)),
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .size(54.dp)
                            .align(Alignment.BottomCenter)
                            .offset(y = 20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // 2. Leadership Tier Cards (Rootva, Silver, Gold, Diamond, Crown)
            items(tiers, key = { it.id }) { tier ->
                LeadershipCardItem(
                    tier = tier,
                    onTargetClick = { selectedTierForTarget = tier },
                    onRewardClick = { selectedTierForReward = tier }
                )
            }
        }
    }

    // -------------------------------------------------------------
    // TARGET DETAILS DIALOG (টার্গেট বিস্তারিত)
    // -------------------------------------------------------------
    if (selectedTierForTarget != null) {
        val tier = selectedTierForTarget!!
        val isTargetReached = userReferrals >= tier.targetReferrals && userSales >= tier.targetResellingSales

        AlertDialog(
            onDismissRequest = { selectedTierForTarget = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🎯 ",
                        fontSize = 20.sp
                    )
                    Text(
                        text = "${tier.title} - টার্গেট",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0055D2)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "এই লিডারশিপ লেভেলে পৌঁছানোর শর্তাবলী:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Condition 1: Direct Referrals
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "সরাসরি লিডার রেফারেল:",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155)
                                )
                                Text(
                                    text = "$userReferrals / ${tier.targetReferrals} জন",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (userReferrals >= tier.targetReferrals) Color(0xFF10B981) else Color(0xFF0055D2)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (userReferrals.toFloat() / tier.targetReferrals).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF10B981),
                                trackColor = Color(0xFFCBD5E1)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Condition 2: Team Size
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "টিম নেটওয়ার্ক সদস্য:",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155)
                                )
                                Text(
                                    text = "$userReferrals / ${tier.targetTeamSize} জন",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (userReferrals >= tier.targetTeamSize) Color(0xFF10B981) else Color(0xFF0055D2)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (userReferrals.toFloat() / tier.targetTeamSize).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF0284C7),
                                trackColor = Color(0xFFCBD5E1)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Condition 3: Reselling Orders
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "রিসেলিং অর্ডার সম্পন্ন:",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155)
                                )
                                Text(
                                    text = "$userSales / ${tier.targetResellingSales} টি",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (userSales >= tier.targetResellingSales) Color(0xFF10B981) else Color(0xFF0055D2)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (userSales.toFloat() / tier.targetResellingSales).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFFF59E0B),
                                trackColor = Color(0xFFCBD5E1)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = if (isTargetReached) Color(0xFFECFDF5) else Color(0xFFFFFBEB),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isTargetReached) Color(0xFFA7F3D0) else Color(0xFFFDE68A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isTargetReached) Icons.Default.CheckCircle else Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = if (isTargetReached) Color(0xFF059669) else Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTargetReached) "টার্গেট সম্পন্ন হয়েছে! রিওয়ার্ড অপশনে যান।" else "টার্গেট অর্জনে কাজ চলমান রয়েছে।",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isTargetReached) Color(0xFF065F46) else Color(0xFF92400E)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedTierForTarget = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ঠিক আছে")
                }
            }
        )
    }

    // -------------------------------------------------------------
    // PERFORMANCE REWARD DIALOG (পারফরম্যান্স রিওয়ার্ড)
    // -------------------------------------------------------------
    if (selectedTierForReward != null) {
        val tier = selectedTierForReward!!
        val isEligibleToClaim = userReferrals >= tier.targetReferrals && userSales >= tier.targetResellingSales

        AlertDialog(
            onDismissRequest = { selectedTierForReward = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🏆 ", fontSize = 20.sp)
                    Text(
                        text = "${tier.title} - রিওয়ার্ড",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0055D2)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "এই লেভেল অর্জনে আপনার প্রাপ্য সুবিধাসমূহ:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Reward 1: Cash Bonus
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFF0055D2))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("এককালীন ক্যাশ বোনাস:", fontSize = 13.sp, color = Color(0xFF1E40AF))
                            }
                            Text(
                                text = "৳${String.format(java.util.Locale.US, "%,.0f", tier.cashReward)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Color(0xFF10B981)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Reward 2: Monthly Royalty
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color(0xFF059669))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("টিম রয়্যালটি কমিশন:", fontSize = 13.sp, color = Color(0xFF065F46))
                            }
                            Text(
                                text = "${tier.monthlyRoyaltyPercent}%",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Color(0xFF059669)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Reward 3: Gift Perk
                    Surface(
                        color = Color(0xFFFFFBEB),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFFD97706))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tier.giftPerk,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF92400E)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isEligibleToClaim) {
                        Button(
                            onClick = {
                                isClaiming = true
                                coroutineScope.launch {
                                    val result = repository.claimLeadershipReward(tier)
                                    isClaiming = false
                                    if (result.isSuccess) {
                                        lastClaimedRewardAmount = tier.cashReward
                                        selectedTierForReward = null
                                        showClaimSuccessDialog = true
                                    } else {
                                        Toast.makeText(context, "দাবি করতে ব্যর্থ হয়েছে", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = !isClaiming,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("রিওয়ার্ড দাবি করুন (Claim ৳${tier.cashReward.toInt()})", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = "💡 টার্গেট সম্পূর্ণ হলে আপনি এই বোনাস ও উপহারগুলো সরাসরি দাবি করতে পারবেন।",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedTierForReward = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }

    // -------------------------------------------------------------
    // CLAIM SUCCESS DIALOG
    // -------------------------------------------------------------
    if (showClaimSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showClaimSuccessDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "অভিনন্দন! 🏆",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A)
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "আপনার লিডারশিপ রিওয়ার্ড সফলভাবে ওয়ালেটে যুক্ত হয়েছে!",
                        fontSize = 13.sp,
                        color = Color(0xFF334155)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "+৳${String.format(java.util.Locale.US, "%.2f", lastClaimedRewardAmount)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color(0xFF10B981)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showClaimSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ধন্যবাদ 🌟")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// LEADERSHIP TIER CARD MATCHING SCREENSHOT EXACTLY
// -------------------------------------------------------------
@Composable
fun LeadershipCardItem(
    tier: LeadershipTier,
    onTargetClick: () -> Unit,
    onRewardClick: () -> Unit
) {
    Surface(
        color = Color(0xFFFFB300), // Vibrant Gold / Yellow matching Screenshot Exactly
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .testTag("leadership_card_${tier.id}")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header Row: Left Blue Pill Banner + Right Circular Emblem
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Blue Pill Shape Banner matching Screenshot
                Surface(
                    color = Color(0xFF0055D2),
                    shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                    modifier = Modifier.padding(end = 10.dp)
                ) {
                    Text(
                        text = tier.title,
                        color = Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                    )
                }

                // Right Circular Branding Emblem matching Screenshot
                Surface(
                    color = Color(0xFF0055D2),
                    shape = CircleShape,
                    border = BorderStroke(2.dp, Color(0xFFFFE600)),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "R",
                            color = Color(0xFFFFE600),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Center Illustration: Leader holding flag atop colorful team members matching screenshot
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                contentAlignment = Alignment.Center
            ) {
                LeaderWithTeamVectorGraphic()
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Bottom Two Action Buttons: 🎯 টার্গেট বিস্তারিত & 🏆 পারফরম্যান্স রিওয়ার্ড
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 🎯 টার্গেট বিস্তারিত Button
                Button(
                    onClick = onTargetClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("target_details_button_${tier.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "🎯", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "টার্গেট বিস্তারিত",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 🏆 পারফরম্যান্স রিওয়ার্ড Button
                Button(
                    onClick = onRewardClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("performance_reward_button_${tier.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "🏆", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "পারফরম্যান্স রিওয়ার্ড",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// LEADER WITH FLAG ATOP TEAM MEMBERS GRAPHIC
// -------------------------------------------------------------
@Composable
fun LeaderWithTeamVectorGraphic() {
    Canvas(modifier = Modifier.size(width = 110.dp, height = 85.dp)) {
        val w = size.width
        val h = size.height

        // 1. Leader (Center top)
        val leaderHeadCenter = Offset(w * 0.52f, h * 0.22f)
        val leaderHeadRadius = 8.dp.toPx()
        drawCircle(color = Color.White, radius = leaderHeadRadius, center = leaderHeadCenter)
        drawCircle(color = Color.Black, radius = leaderHeadRadius, center = leaderHeadCenter, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))

        // Leader body (Cyan/Blue #00C3FF)
        val leaderBody = Path().apply {
            moveTo(w * 0.44f, h * 0.48f)
            lineTo(w * 0.60f, h * 0.48f)
            lineTo(w * 0.56f, h * 0.30f)
            lineTo(w * 0.48f, h * 0.30f)
            close()
        }
        drawPath(leaderBody, color = Color(0xFF00C3FF))
        drawPath(leaderBody, color = Color.Black, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))

        // Flag Pole
        drawLine(
            color = Color.Black,
            start = Offset(w * 0.45f, h * 0.45f),
            end = Offset(w * 0.35f, h * 0.05f),
            strokeWidth = 2.dp.toPx()
        )

        // Orange Flag
        val flagPath = Path().apply {
            moveTo(w * 0.35f, h * 0.05f)
            lineTo(w * 0.22f, h * 0.12f)
            lineTo(w * 0.35f, h * 0.22f)
            close()
        }
        drawPath(flagPath, color = Color(0xFFFF6D00))
        drawPath(flagPath, color = Color.Black, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()))

        // 2. Team Member 1 (Bottom Left, Green)
        drawCircle(color = Color.White, radius = 6.5.dp.toPx(), center = Offset(w * 0.32f, h * 0.54f))
        drawCircle(color = Color.Black, radius = 6.5.dp.toPx(), center = Offset(w * 0.32f, h * 0.54f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()))
        val bodyGreen = Path().apply {
            moveTo(w * 0.26f, h * 0.72f)
            lineTo(w * 0.38f, h * 0.72f)
            lineTo(w * 0.35f, h * 0.60f)
            lineTo(w * 0.29f, h * 0.60f)
            close()
        }
        drawPath(bodyGreen, color = Color(0xFF00E676))
        drawPath(bodyGreen, color = Color.Black, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()))

        // 3. Team Member 2 (Bottom Center, Purple)
        drawCircle(color = Color.White, radius = 6.5.dp.toPx(), center = Offset(w * 0.52f, h * 0.56f))
        drawCircle(color = Color.Black, radius = 6.5.dp.toPx(), center = Offset(w * 0.52f, h * 0.56f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()))
        val bodyPurple = Path().apply {
            moveTo(w * 0.46f, h * 0.74f)
            lineTo(w * 0.58f, h * 0.74f)
            lineTo(w * 0.55f, h * 0.62f)
            lineTo(w * 0.49f, h * 0.62f)
            close()
        }
        drawPath(bodyPurple, color = Color(0xFFD500F9))
        drawPath(bodyPurple, color = Color.Black, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()))

        // 4. Team Member 3 (Bottom Right, Cyan)
        drawCircle(color = Color.White, radius = 6.5.dp.toPx(), center = Offset(w * 0.72f, h * 0.54f))
        drawCircle(color = Color.Black, radius = 6.5.dp.toPx(), center = Offset(w * 0.72f, h * 0.54f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()))
        val bodyCyan = Path().apply {
            moveTo(w * 0.66f, h * 0.72f)
            lineTo(w * 0.78f, h * 0.72f)
            lineTo(w * 0.75f, h * 0.60f)
            lineTo(w * 0.69f, h * 0.60f)
            close()
        }
        drawPath(bodyCyan, color = Color(0xFF00B0FF))
        drawPath(bodyCyan, color = Color.Black, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()))

        // 5. Front Team Members (Bottom row)
        // Yellow member (Far Left)
        drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(w * 0.41f, h * 0.76f))
        drawCircle(color = Color.Black, radius = 6.dp.toPx(), center = Offset(w * 0.41f, h * 0.76f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()))
        val bodyYellow = Path().apply {
            moveTo(w * 0.35f, h * 0.94f)
            lineTo(w * 0.47f, h * 0.94f)
            lineTo(w * 0.44f, h * 0.82f)
            lineTo(w * 0.38f, h * 0.82f)
            close()
        }
        drawPath(bodyYellow, color = Color(0xFFFFD600))
        drawPath(bodyYellow, color = Color.Black, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()))

        // Orange member (Far Right)
        drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(w * 0.63f, h * 0.76f))
        drawCircle(color = Color.Black, radius = 6.dp.toPx(), center = Offset(w * 0.63f, h * 0.76f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()))
        val bodyOrange = Path().apply {
            moveTo(w * 0.57f, h * 0.94f)
            lineTo(w * 0.69f, h * 0.94f)
            lineTo(w * 0.66f, h * 0.82f)
            lineTo(w * 0.60f, h * 0.82f)
            close()
        }
        drawPath(bodyOrange, color = Color(0xFFFF3D00))
        drawPath(bodyOrange, color = Color.Black, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()))
    }
}
