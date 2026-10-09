package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

enum class ServiceType(val title: String) {
    MOBILE_RECHARGE("Mobile Recharge"),
    SIM_OFFER("Sim Offer"),
    RESELLING_SHOP("Reselling Shop"),
    FREELANCING_COURSE("Freelancing\nCourse"),
    ONLINE_SERVICE("Online Service"),
    MICRO_JOB("Micro Job"),
    JOB_POST("Job post"),
    ADS_INCOME("Ads Income"),
    COURSE("Course"),
    SOCIAL_WORK("Social Work"),
    LEADERBOARD("Leaderboard"),
    DOLLAR_INCOME("Dollar Income"),
    REVIEW_JOB("Review Job"),
    MATH_GAME("Math Game"),
    LEADERSHIP("Leadership"),
    DAILY_TARGET_BONUS("Daily Target Bonus"),
    WEEKLY_BONUS("Weekly Bonus"),
    MONTHLY_SALARY("Monthly Salary"),
    BUY_AND_SALE("Buy & Sale"),
    VENDOR("Vendor"),
    INCOME_GUIDE("Income Guide")
}

@Composable
fun ServiceIconRenderer(type: ServiceType, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        when (type) {
            ServiceType.MOBILE_RECHARGE -> {
                Canvas(modifier = Modifier.size(34.dp)) {
                    val w = size.width
                    val h = size.height
                    // Smartphone body
                    drawRoundRect(
                        color = Color(0xFF007BFF),
                        topLeft = Offset(w * 0.22f, h * 0.1f),
                        size = Size(w * 0.56f, h * 0.8f),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                    // Screen
                    drawRoundRect(
                        color = Color(0xFFE8F4FD),
                        topLeft = Offset(w * 0.28f, h * 0.18f),
                        size = Size(w * 0.44f, h * 0.64f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    // Green Arrow
                    drawCircle(color = Color(0xFF10B981), radius = w * 0.12f, center = Offset(w * 0.5f, h * 0.42f))
                    // Upward arrow
                    val arrowPath = Path().apply {
                        moveTo(w * 0.5f, h * 0.34f)
                        lineTo(w * 0.44f, h * 0.44f)
                        lineTo(w * 0.56f, h * 0.44f)
                        close()
                    }
                    drawPath(arrowPath, color = Color.White)
                }
            }
            ServiceType.SIM_OFFER -> {
                Canvas(modifier = Modifier.size(34.dp)) {
                    val w = size.width
                    val h = size.height
                    // SIM card body with chamfer
                    val simPath = Path().apply {
                        moveTo(w * 0.25f, h * 0.32f)
                        lineTo(w * 0.45f, h * 0.12f)
                        lineTo(w * 0.75f, h * 0.12f)
                        lineTo(w * 0.75f, h * 0.88f)
                        lineTo(w * 0.25f, h * 0.88f)
                        close()
                    }
                    drawPath(simPath, color = Color(0xFF38BDF8))
                    // Chip outline
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(w * 0.38f, h * 0.5f),
                        size = Size(w * 0.24f, h * 0.26f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
            }
            ServiceType.RESELLING_SHOP -> {
                Canvas(modifier = Modifier.size(34.dp)) {
                    val w = size.width
                    val h = size.height
                    // Bag body
                    drawRoundRect(
                        color = Color(0xFF10B981),
                        topLeft = Offset(w * 0.22f, h * 0.32f),
                        size = Size(w * 0.56f, h * 0.58f),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                    // Top handle
                    drawArc(
                        color = Color(0xFF047857),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.34f, h * 0.16f),
                        size = Size(w * 0.32f, h * 0.32f),
                        style = Stroke(width = 6f)
                    )
                    // Smile fold
                    drawArc(
                        color = Color.White,
                        startAngle = 20f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(w * 0.35f, h * 0.5f),
                        size = Size(w * 0.3f, h * 0.22f),
                        style = Stroke(width = 5f)
                    )
                }
            }
            ServiceType.FREELANCING_COURSE -> {
                Icon(
                    imageVector = Icons.Default.LaptopMac,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(28.dp)
                )
            }
            ServiceType.ONLINE_SERVICE -> {
                Icon(
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(28.dp)
                )
            }
            ServiceType.MICRO_JOB -> {
                Canvas(modifier = Modifier.size(34.dp)) {
                    val w = size.width
                    val h = size.height
                    // Briefcase body
                    drawRoundRect(
                        color = Color(0xFFB45309),
                        topLeft = Offset(w * 0.16f, h * 0.3f),
                        size = Size(w * 0.68f, h * 0.58f),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                    // Handle
                    drawArc(
                        color = Color(0xFF78350F),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.35f, h * 0.16f),
                        size = Size(w * 0.3f, h * 0.24f),
                        style = Stroke(width = 6f)
                    )
                    // Golden buckle
                    drawRoundRect(
                        color = Color(0xFFFBBF24),
                        topLeft = Offset(w * 0.42f, h * 0.52f),
                        size = Size(w * 0.16f, h * 0.16f),
                        cornerRadius = CornerRadius(3f, 3f)
                    )
                }
            }
            ServiceType.JOB_POST -> {
                Icon(
                    imageVector = Icons.Default.Work,
                    contentDescription = null,
                    tint = Color(0xFF1E3A8A),
                    modifier = Modifier.size(28.dp)
                )
            }
            ServiceType.ADS_INCOME -> {
                Canvas(modifier = Modifier.size(34.dp)) {
                    val w = size.width
                    val h = size.height
                    drawRoundRect(
                        color = Color(0xFF0284C7),
                        topLeft = Offset(w * 0.25f, h * 0.14f),
                        size = Size(w * 0.5f, h * 0.72f),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                    drawRoundRect(
                        color = Color(0xFFFDE047),
                        topLeft = Offset(w * 0.32f, h * 0.28f),
                        size = Size(w * 0.36f, h * 0.18f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    drawRoundRect(
                        color = Color(0xFF38BDF8),
                        topLeft = Offset(w * 0.32f, h * 0.52f),
                        size = Size(w * 0.36f, h * 0.18f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
            }
            ServiceType.COURSE -> {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = Color(0xFF7C3AED),
                    modifier = Modifier.size(28.dp)
                )
            }
            ServiceType.SOCIAL_WORK -> {
                Canvas(modifier = Modifier.size(34.dp)) {
                    val w = size.width
                    val h = size.height
                    drawCircle(color = Color(0xFF1877F2), radius = w * 0.42f, center = Offset(w * 0.5f, h * 0.5f))
                    // Thumbs up / F shape
                    drawCircle(color = Color.White, radius = w * 0.24f, center = Offset(w * 0.5f, h * 0.5f))
                    drawCircle(color = Color(0xFF1877F2), radius = w * 0.16f, center = Offset(w * 0.5f, h * 0.5f))
                }
            }
            ServiceType.LEADERBOARD -> {
                Canvas(modifier = Modifier.size(34.dp)) {
                    val w = size.width
                    val h = size.height
                    // Podium
                    drawRoundRect(color = Color(0xFFEF4444), topLeft = Offset(w * 0.34f, h * 0.38f), size = Size(w * 0.32f, h * 0.5f), cornerRadius = CornerRadius(4f, 4f))
                    drawRoundRect(color = Color(0xFFF87171), topLeft = Offset(w * 0.14f, h * 0.54f), size = Size(w * 0.24f, h * 0.34f), cornerRadius = CornerRadius(4f, 4f))
                    drawRoundRect(color = Color(0xFFF87171), topLeft = Offset(w * 0.62f, h * 0.62f), size = Size(w * 0.24f, h * 0.26f), cornerRadius = CornerRadius(4f, 4f))
                    // Star on top
                    drawCircle(color = Color(0xFFFBBF24), radius = w * 0.1f, center = Offset(w * 0.5f, h * 0.26f))
                }
            }
            ServiceType.DOLLAR_INCOME -> {
                Canvas(modifier = Modifier.size(34.dp)) {
                    val w = size.width
                    val h = size.height
                    drawCircle(color = Color(0xFFF59E0B), radius = w * 0.42f, center = Offset(w * 0.5f, h * 0.5f))
                    drawCircle(color = Color(0xFFFBBF24), radius = w * 0.32f, center = Offset(w * 0.5f, h * 0.5f))
                    // Center coin dollar notch
                    drawCircle(color = Color(0xFFB45309), radius = w * 0.18f, center = Offset(w * 0.5f, h * 0.5f))
                }
            }
            ServiceType.REVIEW_JOB -> {
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(28.dp)
                )
            }
            ServiceType.MATH_GAME -> {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(28.dp)
                )
            }
            ServiceType.LEADERSHIP -> {
                Icon(
                    imageVector = Icons.Default.MilitaryTech,
                    contentDescription = null,
                    tint = Color(0xFFEA580C),
                    modifier = Modifier.size(28.dp)
                )
            }
            ServiceType.DAILY_TARGET_BONUS -> {
                Canvas(modifier = Modifier.size(34.dp)) {
                    val w = size.width
                    val h = size.height
                    drawCircle(color = Color(0xFFEF4444), radius = w * 0.42f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = 6f))
                    drawCircle(color = Color(0xFF3B82F6), radius = w * 0.26f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = 5f))
                    drawCircle(color = Color(0xFFFBBF24), radius = w * 0.12f, center = Offset(w * 0.5f, h * 0.5f))
                }
            }
            ServiceType.WEEKLY_BONUS -> {
                Canvas(modifier = Modifier.size(34.dp)) {
                    val w = size.width
                    val h = size.height
                    drawRoundRect(color = Color(0xFFF59E0B), topLeft = Offset(w * 0.22f, h * 0.34f), size = Size(w * 0.56f, h * 0.54f), cornerRadius = CornerRadius(8f, 8f))
                    drawRoundRect(color = Color(0xFFEF4444), topLeft = Offset(w * 0.18f, h * 0.26f), size = Size(w * 0.64f, h * 0.14f), cornerRadius = CornerRadius(4f, 4f))
                    drawRoundRect(color = Color(0xFFEF4444), topLeft = Offset(w * 0.45f, h * 0.34f), size = Size(w * 0.1f, h * 0.54f))
                }
            }
            ServiceType.MONTHLY_SALARY -> {
                Canvas(modifier = Modifier.size(34.dp)) {
                    val w = size.width
                    val h = size.height
                    drawRoundRect(color = Color(0xFF10B981), topLeft = Offset(w * 0.22f, h * 0.26f), size = Size(w * 0.56f, h * 0.38f), cornerRadius = CornerRadius(6f, 6f))
                    drawCircle(color = Color(0xFF047857), radius = w * 0.1f, center = Offset(w * 0.5f, h * 0.45f))
                    // Hand under cash
                    drawRoundRect(color = Color(0xFFFBBF24), topLeft = Offset(w * 0.2f, h * 0.64f), size = Size(w * 0.6f, h * 0.18f), cornerRadius = CornerRadius(6f, 6f))
                }
            }
            ServiceType.BUY_AND_SALE -> {
                Icon(
                    imageVector = Icons.Default.CurrencyExchange,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(28.dp)
                )
            }
            ServiceType.VENDOR -> {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(28.dp)
                )
            }
            ServiceType.INCOME_GUIDE -> {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
