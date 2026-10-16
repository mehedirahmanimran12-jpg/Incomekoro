package com.example.ui.screens.course

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.LearningCourseVideo
import com.example.data.repository.AppRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreelancingCourseScreen(
    repository: AppRepository,
    onNavigateBack: () -> Unit
) {
    BackHandler(onBack = onNavigateBack)

    val context = LocalContext.current
    val initialVideos = remember { repository.getLearningCourseVideos() }
    var videosList by remember { mutableStateOf(initialVideos) }
    var searchQuery by remember { mutableStateOf("") }
    var activeVideo by remember { mutableStateOf(initialVideos.firstOrNull() ?: LearningCourseVideo(
        id = "default",
        title = "🎉 Rootva 1 Year Successful Program",
        thumbnailType = 1
    )) }
    var selectedVideoForDialog by remember { mutableStateOf<LearningCourseVideo?>(null) }

    // Filtered video list based on real-time search query
    val filteredVideos = remember(videosList, searchQuery) {
        if (searchQuery.isBlank()) {
            videosList
        } else {
            videosList.filter {
                it.title.contains(searchQuery.trim(), ignoreCase = true)
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
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
                        modifier = Modifier.testTag("course_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Learning Course",
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
                .background(Color(0xFFF8FAFC)),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            // 1. YouTube Video Player Hero Area (Matching Screenshot Exactly)
            item {
                YouTubePlayerHeroBanner(
                    video = activeVideo,
                    onPlayClick = {
                        selectedVideoForDialog = activeVideo
                    },
                    onOpenYouTube = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(activeVideo.videoUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "ভিডিও লোড হচ্ছে...", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onShareClick = {
                        try {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Rootva Learning Course: ${activeVideo.title}\n${activeVideo.videoUrl}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "শেয়ার করুন"))
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
                )
            }

            // 2. Search Input Field (Matching Screenshot: "ভিডিও টাইটেল সার্চ করুন")
            item {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "ভিডিও টাইটেল সার্চ করুন",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                textStyle = TextStyle(
                                    color = Color(0xFF1E293B),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(Color(0xFF0055D2)),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("course_search_input")
                            )
                        }

                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. List of Course Video Cards (Matching Screenshot Exactly)
            items(filteredVideos, key = { it.id }) { video ->
                Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    LearningCourseVideoCard(
                        video = video,
                        isSelected = video.id == activeVideo.id,
                        onClick = {
                            activeVideo = video
                            selectedVideoForDialog = video
                        }
                    )
                }
            }
        }
    }

    // -------------------------------------------------------------
    // VIDEO PLAYER & LESSON DETAILS DIALOG
    // -------------------------------------------------------------
    if (selectedVideoForDialog != null) {
        val current = selectedVideoForDialog!!
        AlertDialog(
            onDismissRequest = { selectedVideoForDialog = null },
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = Color(0xFFE60000),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "কোর্স ভিডিও টিউটোরিয়াল",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF0F172A)
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = current.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "চ্যানেল: ${current.channelName} • ${current.duration}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Status Bar
                    Surface(
                        color = if (current.isCompleted) Color(0xFFF0FDF4) else Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (current.isCompleted) Color(0xFFBBF7D0) else Color(0xFFBFDBFE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (current.isCompleted) "অবস্থা: সম্পন্ন (১০০%)" else "অবস্থা: চলমান (০%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (current.isCompleted) Color(0xFF166534) else Color(0xFF1E40AF)
                            )
                            Icon(
                                imageVector = if (current.isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (current.isCompleted) Color(0xFF166534) else Color(0xFF1E40AF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Watch on YouTube direct button
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(current.videoUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "ইউটিউব ওপেন করা হচ্ছে...", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60000)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Icon(Icons.Default.SmartDisplay, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("YouTube এ ভিডিও দেখুন", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Toggle Complete Button
                    OutlinedButton(
                        onClick = {
                            val newStatus = !current.isCompleted
                            val newPercent = if (newStatus) 100 else 0
                            val updated = current.copy(isCompleted = newStatus, progressPercent = newPercent)
                            videosList = videosList.map { if (it.id == current.id) updated else it }
                            if (activeVideo.id == current.id) {
                                activeVideo = updated
                            }
                            selectedVideoForDialog = null
                            Toast.makeText(
                                context,
                                if (newStatus) "টিউটোরিয়াল সম্পন্ন হিসেবে চিহ্নিত হয়েছে! 🎉" else "চলমান হিসেবে রাখা হয়েছে",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text(
                            text = if (current.isCompleted) "অসম্পূর্ণ চিহ্নিত করুন" else "সম্পূর্ণ চিহ্নিত করুন (১০০%)",
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0055D2)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedVideoForDialog = null }) {
                    Text("বন্ধ করুন", color = Color(0xFF64748B))
                }
            }
        )
    }
}

// -------------------------------------------------------------
// YOUTUBE VIDEO PLAYER HERO BANNER MATCHING SCREENSHOT EXACTLY
// -------------------------------------------------------------
@Composable
fun YouTubePlayerHeroBanner(
    video: LearningCourseVideo,
    onPlayClick: () -> Unit,
    onOpenYouTube: () -> Unit,
    onShareClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black)
            .testTag("course_hero_player")
    ) {
        // Thumbnail Image
        Image(
            painter = painterResource(id = R.drawable.img_learning_course_hero),
            contentDescription = "Active Course Video",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark gradient overlays for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.65f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.75f)
                        )
                    )
                )
        )

        // Top Overlay: Channel Avatar & Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rootva Monogram Avatar Logo
            Surface(
                color = Color(0xFF0284C7),
                shape = CircleShape,
                border = BorderStroke(1.5.dp, Color(0xFFFDE047)),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "R",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = video.channelName,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            }
        }

        // Center: Red YouTube Play Button
        Surface(
            color = Color(0xFFFF0000),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 64.dp, height = 42.dp)
                .clickable { onPlayClick() }
                .testTag("course_hero_play_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Video",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Bottom Overlay: Share & "Watch on YouTube"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Share button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onShareClick() }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Share",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // "Watch on YouTube" button
            Surface(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.clickable { onOpenYouTube() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Watch on",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.SmartDisplay,
                        contentDescription = "YouTube",
                        tint = Color(0xFFFF0000),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "YouTube",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// COURSE VIDEO LIST CARD MATCHING SCREENSHOT EXACTLY
// -------------------------------------------------------------
@Composable
fun LearningCourseVideoCard(
    video: LearningCourseVideo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.2.dp,
            color = if (isSelected) Color(0xFF2563EB) else Color(0xFF93C5FD)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("course_card_${video.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Custom Thumbnail Mockup
            Box(
                modifier = Modifier
                    .size(width = 90.dp, height = 62.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                when (video.thumbnailType) {
                    1 -> CourseThumbnailOneYear()
                    2 -> CourseThumbnailAccountOpen()
                    3 -> CourseThumbnailSecretIncome()
                    4 -> CourseThumbnailDetails()
                    else -> CourseThumbnailOneYear()
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Middle Content: Title, Duration, Progress Bar
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 6.dp)
            ) {
                Text(
                    text = video.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = video.duration,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(5.dp))

                // Progress Bar Row with Percentage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { video.progressPercent / 100f },
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (video.progressPercent > 0) Color(0xFF22C55E) else Color(0xFFCBD5E1),
                        trackColor = Color(0xFFF1F5F9)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${video.progressPercent}%",
                        color = Color(0xFF0055D2),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            // Right Status Badge: Green Checkmark (100%) or Red Play (0%)
            if (video.progressPercent >= 100) {
                Surface(
                    color = Color(0xFF22C55E),
                    shape = CircleShape,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            } else {
                Surface(
                    color = Color(0xFFEF4444),
                    shape = CircleShape,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// COURSE THUMBNAIL GRAPHIC PREVIEWS MATCHING SCREENSHOT
// -------------------------------------------------------------

// Thumbnail 1: 1 Year Successful Program
@Composable
fun CourseThumbnailOneYear() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_learning_course_hero),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

// Thumbnail 2: মোবাইল দিয়ে ২ মিনিটে Rootva একাউন্ট খুলুন
@Composable
fun CourseThumbnailAccountOpen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF0284C7), Color(0xFF1E3A8A))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .width(26.dp)
                    .height(44.dp)
            ) {
                Column(
                    modifier = Modifier.padding(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.size(6.dp).background(Color(0xFF0284C7), CircleShape))
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(Color(0xFFE2E8F0)))
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(Color(0xFFE2E8F0)))
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            Column {
                Text(
                    text = "কীভাবে একাউন্ট",
                    color = Color(0xFFFDE047),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "করবেন ফ্রিতে",
                    color = Color.White,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = Color(0xFFEF4444),
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "২ মিনিটে",
                        color = Color.White,
                        fontSize = 6.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

// Thumbnail 3: নতুনদের জন্য রোডভা সিক্রেট ($450 DOLLAR INCOME)
@Composable
fun CourseThumbnailSecretIncome() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(3.dp)
        ) {
            Text(
                text = "৫০ হাজার পেমেন্ট",
                color = Color(0xFFFACC15),
                fontSize = 7.5.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Surface(
                color = Color(0xFF10B981),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text(
                    text = "$450 DOLLAR",
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
            Text(
                text = "লাইভ উত্তোলন",
                color = Color.White,
                fontSize = 6.5.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Thumbnail 4: Rootva কাজ সম্পর্কে A to Z বিস্তারিত
@Composable
fun CourseThumbnailDetails() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF312E81), Color(0xFF1E3A8A))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(3.dp)
        ) {
            Text(
                text = "Rootva কাজ",
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "A to Z বিস্তারিত",
                color = Color(0xFFFACC15),
                fontSize = 7.5.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Icon(
                imageVector = Icons.Default.PlayCircle,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(16.dp).padding(top = 1.dp)
            )
        }
    }
}
