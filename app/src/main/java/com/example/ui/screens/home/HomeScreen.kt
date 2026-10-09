package com.example.ui.screens.home

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.User
import com.example.data.repository.AppRepository
import com.example.ui.components.ServiceIconRenderer
import com.example.ui.components.ServiceType
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    repository: AppRepository,
    onNavigateToTasks: () -> Unit,
    onNavigateToDeposit: () -> Unit,
    onNavigateToWithdraw: () -> Unit,
    onNavigateToReferral: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToAnnouncements: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onSelectTransaction: (String) -> Unit
) {
    val currentUser by repository.currentUser.collectAsState(initial = null)
    val notifications by repository.notifications.collectAsState(initial = emptyList())
    val unreadNotifsCount = notifications.count { !it.isRead }
    val context = LocalContext.current

    val user = currentUser ?: User(
        name = "MEHEDI",
        referralCode = "250829",
        balance = 1450.0
    )

    val displayName = if (user.name.isNotBlank()) user.name else "MEHEDI"
    val affiliateId = if (user.referralCode.isNotBlank()) user.referralCode else "250829"

    var selectedServiceModal by remember { mutableStateOf<ServiceType?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6F9))
            .testTag("screen_home")
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Top Bar Header (Full width)
            item(span = { GridItemSpan(3) }) {
                TopAppBarScreenshotStyle(
                    name = displayName,
                    affiliateId = affiliateId,
                    unreadCount = unreadNotifsCount,
                    onProfileClick = onNavigateToProfile,
                    onNotificationClick = onNavigateToNotifications
                )
            }

            // 2. Promotional Banner (Full width)
            item(span = { GridItemSpan(3) }) {
                PromotionalBannerSection(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    onClick = onNavigateToAnnouncements
                )
            }

            // 3. Social Media Communities Row (Full width)
            item(span = { GridItemSpan(3) }) {
                SocialCommunityCard(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    onSocialClick = { platform ->
                        Toast.makeText(context, "Opening $platform...", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // 4. "Our Services" Section Title (Full width)
            item(span = { GridItemSpan(3) }) {
                Text(
                    text = "Our Services",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = Color(0xFF0055D2),
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                )
            }

            // 5. Grid of 21 Services (3 columns)
            items(ServiceType.values().toList(), key = { it.name }) { service ->
                ScreenshotServiceCard(
                    service = service,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    onClick = {
                        when (service) {
                            ServiceType.MICRO_JOB, ServiceType.JOB_POST, ServiceType.REVIEW_JOB -> onNavigateToTasks()
                            ServiceType.DAILY_TARGET_BONUS, ServiceType.WEEKLY_BONUS, ServiceType.MONTHLY_SALARY -> onNavigateToDeposit()
                            ServiceType.DOLLAR_INCOME -> onNavigateToWithdraw()
                            ServiceType.LEADERSHIP -> onNavigateToReferral()
                            else -> selectedServiceModal = service
                        }
                    }
                )
            }

            // 6. Leadership Achiever Banner (Full width)
            item(span = { GridItemSpan(3) }) {
                Surface(
                    color = Color(0xFF0055D2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp)
                        .clickable { onNavigateToReferral() }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Leadership Achiever",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Floating Customer Service Agent
        FloatingAgentButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 90.dp),
            onClick = onNavigateToSupport
        )
    }

    if (selectedServiceModal != null) {
        val s = selectedServiceModal!!
        AlertDialog(
            onDismissRequest = { selectedServiceModal = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ServiceIconRenderer(type = s, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(s.title.replace("\n", " "), fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Welcome to ${s.title.replace("\n", " ")}! Complete active tasks or orders and receive direct earnings to your wallet balance.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedServiceModal = null
                        onNavigateToTasks()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2))
                ) {
                    Text("Explore Jobs")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedServiceModal = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun TopAppBarScreenshotStyle(
    name: String,
    affiliateId: String,
    unreadCount: Int,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    Surface(
        color = Color(0xFF0055D2),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile circular badge with verified checkmark
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .clickable { onProfileClick() }
            ) {
                // Outer circle with colorful ring
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(color = Color(0xFFE11D48), radius = size.width * 0.48f, center = Offset(size.width * 0.3f, size.height * 0.4f))
                        drawCircle(color = Color(0xFFFBBF24), radius = size.width * 0.45f, center = Offset(size.width * 0.7f, size.height * 0.3f))
                        drawCircle(color = Color(0xFF2563EB), radius = size.width * 0.45f, center = Offset(size.width * 0.5f, size.height * 0.8f))
                    }
                    Text(
                        text = "রবি",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White
                    )
                }

                // Verified checkmark badge
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF38BDF8))
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Verified",
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Affiliate ID: $affiliateId",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    ),
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            // App Brand Logo on Right (Golden/Yellow monogram)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Golden stylized emblem
                        drawRoundRect(
                            color = Color(0xFFFBBF24),
                            topLeft = Offset(size.width * 0.15f, size.height * 0.1f),
                            size = androidx.compose.ui.geometry.Size(size.width * 0.7f, size.height * 0.8f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                        )
                        drawRoundRect(
                            color = Color(0xFF0055D2),
                            topLeft = Offset(size.width * 0.35f, size.height * 0.25f),
                            size = androidx.compose.ui.geometry.Size(size.width * 0.35f, size.height * 0.5f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                        )
                    }
                }
                Text(
                    text = "INCOME",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 8.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color(0xFFFBBF24)
                )
            }

            // Notification Bell
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("btn_top_notifications")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge(
                                containerColor = Color(0xFFEF4444),
                                contentColor = Color.White
                            ) {
                                Text(unreadCount.toString())
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PromotionalBannerSection(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("promo_banner"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A2540))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF003882),
                            Color(0xFF0055D2),
                            Color(0xFF0284C7)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFBBF24)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "IK",
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF003882),
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "INCOME KORO",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White
                            )
                            Text(
                                text = "এক অ্যাপে সকল ডিজিটাল সার্ভিস!",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFFBBF24)
                            )
                        }
                    }

                    // Feature badges
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "কাজ করুন • আয় করুন",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Golden highlight bar
                Surface(
                    color = Color(0xFFFBBF24),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚡ এক অ্যাপ, অসীম সম্ভাবনা — শুরু করুন আজই",
                        color = Color(0xFF0A2540),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom feature icons row in banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    BannerFeatureIcon("নিরাপদ সেবা", Icons.Default.Shield)
                    BannerFeatureIcon("২৪/৭ সাপোর্ট", Icons.Default.HeadsetMic)
                    BannerFeatureIcon("দ্রুত পেমেন্ট", Icons.Default.Bolt)
                    BannerFeatureIcon("লক্ষ গ্রাহক", Icons.Default.Groups)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Carousel indicator dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White))
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.4f)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.4f)))
                }
            }
        }
    }
}

@Composable
private fun BannerFeatureIcon(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(title, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SocialCommunityCard(
    modifier: Modifier = Modifier,
    onSocialClick: (String) -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Facebook Follow
            SocialItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1877F2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("f", fontWeight = FontWeight.Black, color = Color.White, fontSize = 26.sp)
                    }
                },
                buttonText = "Follow",
                buttonBg = Color(0xFF1877F2),
                onClick = { onSocialClick("Facebook") }
            )

            // 2. Facebook Group Join
            SocialItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1877F2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                },
                buttonText = "Join Now",
                buttonBg = Color(0xFF1877F2),
                onClick = { onSocialClick("Community Group") }
            )

            // 3. WhatsApp Join
            SocialItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF25D366)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                },
                buttonText = "Join Now",
                buttonBg = Color(0xFF25D366),
                onClick = { onSocialClick("WhatsApp") }
            )

            // 4. Telegram Join
            SocialItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF26A5E4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                },
                buttonText = "Join Now",
                buttonBg = Color(0xFF26A5E4),
                onClick = { onSocialClick("Telegram") }
            )

            // 5. YouTube Subscribe
            SocialItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE50914)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                },
                buttonText = "Subscribe",
                buttonBg = Color(0xFFE50914),
                onClick = { onSocialClick("YouTube") }
            )
        }
    }
}

@Composable
private fun SocialItem(
    icon: @Composable () -> Unit,
    buttonText: String,
    buttonBg: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        icon()
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            color = buttonBg,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = buttonText,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
fun ScreenshotServiceCard(
    service: ServiceType,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(124.dp)
            .clickable { onClick() }
            .testTag("service_${service.name.lowercase()}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1672EC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ServiceIconRenderer(type = service)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = service.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    lineHeight = 14.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FloatingAgentButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(54.dp)
            .shadow(elevation = 6.dp, shape = CircleShape)
            .testTag("floating_support_agent"),
        shape = CircleShape,
        color = Color(0xFF0055D2)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SupportAgent,
                contentDescription = "Customer Care Agent",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
