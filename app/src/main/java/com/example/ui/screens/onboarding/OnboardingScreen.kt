package com.example.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UserPreferences
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val illustrationType: Int
)

@Composable
fun OnboardingScreen(
    preferences: UserPreferences,
    onFinishOnboarding: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val pages = listOf(
        OnboardingPageData(
            title = "Earn From Your Skills",
            subtitle = "Monetize your everyday digital skills, daily social tasks, and review activities easily from your smartphone.",
            illustrationType = 1
        ),
        OnboardingPageData(
            title = "Complete Tasks & Grow",
            subtitle = "Access an expansive verified marketplace of tasks with guaranteed rewards and instant tracking.",
            illustrationType = 2
        ),
        OnboardingPageData(
            title = "Build Your Income",
            subtitle = "Withdraw your earnings directly to bKash, Nagad, and Rocket safely with fast processing.",
            illustrationType = 3
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })

    val finishFlow = {
        scope.launch {
            preferences.setOnboardingCompleted(true)
            onFinishOnboarding()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (pagerState.currentPage < pages.size - 1) {
                    TextButton(
                        onClick = { finishFlow() },
                        modifier = Modifier.testTag("btn_skip")
                    ) {
                        Text(
                            text = "Skip",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pager Indicators
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(if (isSelected) 24.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (pagerState.currentPage > 0) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_prev")
                        ) {
                            Text("Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = {
                            if (pagerState.currentPage < pages.size - 1) {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                finishFlow()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .height(50.dp)
                            .testTag(if (pagerState.currentPage == pages.size - 1) "btn_get_started" else "btn_next")
                    ) {
                        Text(
                            text = if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("onboarding_pager")
        ) { pageIndex ->
            val page = pages[pageIndex]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Vector Illustrated graphic
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    OriginalIllustration(type = page.illustrationType)
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = page.subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
fun OriginalIllustration(type: Int) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        when (type) {
            1 -> {
                // Digital Skills & Growth Chart Illustration
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF00E676).copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = w * 0.45f
                    )
                )
                // Draw growth bars
                drawRoundRect(
                    color = Color(0xFF10B981),
                    topLeft = Offset(w * 0.2f, h * 0.6f),
                    size = androidx.compose.ui.geometry.Size(w * 0.12f, h * 0.25f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                )
                drawRoundRect(
                    color = Color(0xFF38BDF8),
                    topLeft = Offset(w * 0.44f, h * 0.4f),
                    size = androidx.compose.ui.geometry.Size(w * 0.12f, h * 0.45f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                )
                drawRoundRect(
                    color = Color(0xFFFBBF24),
                    topLeft = Offset(w * 0.68f, h * 0.2f),
                    size = androidx.compose.ui.geometry.Size(w * 0.12f, h * 0.65f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                )
                // Trend Line
                val trendPath = Path().apply {
                    moveTo(w * 0.26f, h * 0.55f)
                    lineTo(w * 0.5f, h * 0.35f)
                    lineTo(w * 0.74f, h * 0.16f)
                }
                drawPath(
                    path = trendPath,
                    color = Color.White,
                    style = Stroke(width = 6f)
                )
                drawCircle(color = Color.White, radius = 9f, center = Offset(w * 0.74f, h * 0.16f))
            }
            2 -> {
                // Task Checkmark & Badges
                drawCircle(
                    color = Color(0xFF0F172A),
                    radius = w * 0.38f,
                    center = Offset(w * 0.5f, h * 0.5f)
                )
                drawCircle(
                    color = Color(0xFF00E676),
                    radius = w * 0.32f,
                    center = Offset(w * 0.5f, h * 0.5f),
                    style = Stroke(width = 12f)
                )
                val checkPath = Path().apply {
                    moveTo(w * 0.35f, h * 0.5f)
                    lineTo(w * 0.46f, h * 0.62f)
                    lineTo(w * 0.66f, h * 0.38f)
                }
                drawPath(
                    path = checkPath,
                    color = Color(0xFF00E676),
                    style = Stroke(width = 14f)
                )
            }
            3 -> {
                // Wallet & Currency Emblem
                drawRoundRect(
                    color = Color(0xFF1E293B),
                    topLeft = Offset(w * 0.15f, h * 0.28f),
                    size = androidx.compose.ui.geometry.Size(w * 0.7f, h * 0.46f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
                )
                drawCircle(
                    color = Color(0xFFFBBF24),
                    radius = w * 0.15f,
                    center = Offset(w * 0.5f, h * 0.51f)
                )
                drawCircle(
                    color = Color(0xFFD97706),
                    radius = w * 0.11f,
                    center = Offset(w * 0.5f, h * 0.51f),
                    style = Stroke(width = 6f)
                )
            }
        }
    }
}
