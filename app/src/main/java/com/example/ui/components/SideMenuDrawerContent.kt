package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User

@Composable
fun SideMenuDrawerContent(
    user: User,
    onNavigateToHome: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToPassiveIncome: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToMeeting: () -> Unit,
    onNavigateToSocial: (String) -> Unit,
    onNavigateToFaq: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onRateApp: () -> Unit,
    onLogout: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val displayName = if (user.name.isNotBlank()) user.name else "MEHEDI"
    val affiliateId = if (user.referralCode.isNotBlank()) user.referralCode else "250829"

    ModalDrawerSheet(
        drawerContainerColor = Color(0xFFF4F6F9),
        drawerContentColor = Color(0xFF1E293B),
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight()
            .testTag("side_menu_drawer")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Royal Blue Header matching Screenshot_20261009-151723.jpg
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0055D2))
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Profile Avatar with green checkmark
                    Box(modifier = Modifier.size(90.dp)) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(color = Color(0xFFE2E8F0), radius = size.width * 0.48f)
                                drawCircle(color = Color(0xFFF8FAFC), radius = size.width * 0.42f)
                                // Soft cat/avatar silhouette
                                drawCircle(color = Color(0xFFE2E8F0), radius = size.width * 0.28f, center = Offset(size.width * 0.5f, size.height * 0.45f))
                            }
                            Icon(
                                imageVector = Icons.Default.Pets,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(46.dp)
                            )
                        }

                        // Green Verified Badge
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                                .align(Alignment.BottomEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Verified",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "অ্যাফিলিয়েট পার্টনার",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // BUSINESS ACCOUNT Pill Badge
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BUSINESS ACCOUNT",
                                color = Color(0xFFE11D48),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Affiliate ID Card with yellow copy button
                    Surface(
                        color = Color(0xFF003F9E),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "অ্যাফিলিয়েট আইডি",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = Color(0xFF38BDF8)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = affiliateId,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 22.sp
                                    ),
                                    color = Color.White
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF064E3B))
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(affiliateId))
                                        Toast.makeText(context, "অ্যাফিলিয়েট আইডি কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Affiliate ID",
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Menu Items matching Screenshot
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Section: প্রধান মেনু
                Text(
                    text = "প্রধান মেনু",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(start = 6.dp, top = 4.dp, bottom = 4.dp)
                )

                DrawerMenuItem(
                    title = "হোম",
                    icon = Icons.Default.Home,
                    iconColor = Color(0xFFE11D48),
                    onClick = {
                        onCloseDrawer()
                        onNavigateToHome()
                    }
                )

                DrawerMenuItem(
                    title = "About Us",
                    icon = Icons.Default.Groups,
                    iconColor = Color(0xFF2563EB),
                    onClick = {
                        onCloseDrawer()
                        onNavigateToAbout()
                    }
                )

                DrawerMenuItem(
                    title = "আমাদের সম্পর্কে",
                    icon = Icons.Default.Business,
                    iconColor = Color(0xFF0284C7),
                    onClick = {
                        onCloseDrawer()
                        onNavigateToAbout()
                    }
                )

                DrawerMenuItem(
                    title = "প্যাসিভ ইনকাম",
                    icon = Icons.Default.Stars,
                    iconColor = Color(0xFFEA580C),
                    onClick = {
                        onCloseDrawer()
                        onNavigateToPassiveIncome()
                    }
                )

                HorizontalDivider(
                    color = Color(0xFFCBD5E1),
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                // Section: সাপোর্ট ও কমিউনিটি
                Text(
                    text = "সাপোর্ট ও কমিউনিটি",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(start = 6.dp, top = 4.dp, bottom = 4.dp)
                )

                DrawerMenuItem(
                    title = "হেল্প সেন্টার",
                    icon = Icons.Default.SupportAgent,
                    iconColor = Color(0xFF2563EB),
                    onClick = {
                        onCloseDrawer()
                        onNavigateToSupport()
                    }
                )

                DrawerMenuItem(
                    title = "জয়েন মিটিং শিডিউল",
                    icon = Icons.Default.VideoCameraFront,
                    iconColor = Color(0xFF059669),
                    onClick = {
                        onCloseDrawer()
                        onNavigateToMeeting()
                    }
                )

                DrawerMenuItem(
                    title = "কাস্টমার গ্রুপ",
                    icon = Icons.Default.Chat,
                    iconColor = Color(0xFF25D366),
                    onClick = {
                        onCloseDrawer()
                        onNavigateToSocial("WhatsApp কাস্টমার গ্রুপ")
                    }
                )

                DrawerMenuItem(
                    title = "বিজনেস গ্রুপ",
                    icon = Icons.Default.Store,
                    iconColor = Color(0xFF16A34A),
                    onClick = {
                        onCloseDrawer()
                        onNavigateToSocial("WhatsApp বিজনেস গ্রুপ")
                    }
                )

                DrawerMenuItem(
                    title = "টেলিগ্রাম গ্রুপ",
                    icon = Icons.Default.Send,
                    iconColor = Color(0xFF26A5E4),
                    onClick = {
                        onCloseDrawer()
                        onNavigateToSocial("টেলিগ্রাম চ্যানেল ও গ্রুপ")
                    }
                )

                DrawerMenuItem(
                    title = "ফেসবুক গ্রুপ",
                    icon = Icons.Default.ThumbUp,
                    iconColor = Color(0xFF1877F2),
                    onClick = {
                        onCloseDrawer()
                        onNavigateToSocial("অফিসিয়াল ফেসবুক গ্রুপ")
                    }
                )

                DrawerMenuItem(
                    title = "ইউটিউব",
                    icon = Icons.Default.PlayCircle,
                    iconColor = Color(0xFFE50914),
                    onClick = {
                        onCloseDrawer()
                        onNavigateToSocial("অফিসিয়াল ইউটিউব চ্যানেল")
                    }
                )

                DrawerMenuItem(
                    title = "সাধারণ জিজ্ঞাসা",
                    icon = Icons.Default.Help,
                    iconColor = Color(0xFF0284C7),
                    onClick = {
                        onCloseDrawer()
                        onNavigateToFaq()
                    }
                )

                DrawerMenuItem(
                    title = "প্রাইভেসি পলিসি",
                    icon = Icons.Default.Security,
                    iconColor = Color(0xFF3B82F6),
                    onClick = {
                        onCloseDrawer()
                        onNavigateToPrivacy()
                    }
                )

                DrawerMenuItem(
                    title = "অ্যাপটি রেট দিন ⭐",
                    icon = Icons.Default.Star,
                    iconColor = Color(0xFFF59E0B),
                    onClick = {
                        onCloseDrawer()
                        onRateApp()
                    }
                )

                DrawerMenuItem(
                    title = "লগ আউট",
                    icon = Icons.Default.Logout,
                    iconColor = Color(0xFFDC2626),
                    onClick = {
                        onCloseDrawer()
                        onLogout()
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DrawerMenuItem(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
