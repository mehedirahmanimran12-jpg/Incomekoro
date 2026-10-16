package com.example.ui.screens.jobs

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.MicroJobItem
import com.example.data.repository.AppRepository
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicroJobsScreen(
    repository: AppRepository,
    onNavigateBack: () -> Unit
) {
    BackHandler(onBack = onNavigateBack)

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentUser by repository.currentUser.collectAsState(initial = null)
    val completedJobIds by repository.completedMicroJobIds.collectAsState(initial = emptySet())
    val microJobs = remember { repository.getMicroJobs() }
    val userBalance = currentUser?.balance ?: 1450.0

    // Dialog & Sheet States
    var showTutorialDialog by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }
    var selectedJobForProof by remember { mutableStateOf<MicroJobItem?>(null) }
    var proofText by remember { mutableStateOf("") }
    var isSubmittingProof by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF1F5F9),
        topBar = {
            Surface(
                color = Color(0xFF0055D2),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("micro_jobs_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Micro Jobs",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
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
        ) {
            // Top Action Buttons Row: টিউটোরিয়াল (Red) & নিয়ম (Blue) matching Screenshot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // টিউটোরিয়াল Button (Bright Red)
                Button(
                    onClick = { showTutorialDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE60000)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("micro_jobs_tutorial_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartDisplay,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "টিউটোরিয়াল",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // নিয়ম Button (Royal Blue)
                Button(
                    onClick = { showRulesDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0047BA)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("micro_jobs_rules_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Article,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "নিয়ম",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 2-Column Vertical Grid with Hero Banner at Top
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("micro_jobs_grid")
            ) {
                // Hero Banner spanning full width (2 columns)
                item(span = { GridItemSpan(2) }) {
                    MicroJobsHeroBanner()
                }

                // 2-Column Job Cards matching Screenshot exactly
                items(microJobs, key = { it.id }) { job ->
                    val isDone = completedJobIds.contains(job.id)
                    MicroJobGridCard(
                        job = job,
                        isCompleted = isDone,
                        onClick = {
                            selectedJobForProof = job
                            proofText = ""
                        }
                    )
                }
            }
        }
    }

    // --- Tutorial Dialog (টিউটোরিয়াল) ---
    if (showTutorialDialog) {
        AlertDialog(
            onDismissRequest = { showTutorialDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SmartDisplay,
                        contentDescription = null,
                        tint = Color(0xFFE60000),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "মাইক্রো জব টিউটোরিয়াল",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF0F172A)
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
                        text = "সহজ কাজের মাধ্যমে প্রতিদিন ইনকাম করুন:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0055D2)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "১. কাজ নির্বাচন:\nগ্রিড থেকে যেকোনো কার্ডে ক্লিক করে কাজের বিস্তারিত নির্দেশিকা পড়ে নিন।\n\n" +
                                "২. কাজ সম্পন্ন করুন:\n'লিংক ওপেন করুন' বাটনে ক্লিক করে সংশ্লিষ্ট ফেসবুক বা ইউটিউব পেজে গিয়ে লাইক, কমেন্ট বা সাবস্ক্রাইব করুন।\n\n" +
                                "৩. প্রমাণ সাবমিট করুন:\nআপনার প্রোফাইল লিংক বা কাজের সঠিক প্রমাণ লিখে 'প্রুফ জমা দিন' বাটনে চাপলেই আপনার ব্যালেন্সে রিওয়ার্ড যুক্ত হবে।",
                        fontSize = 13.sp,
                        color = Color(0xFF334155),
                        lineHeight = 19.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTutorialDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60000)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("বুঝেছি")
                }
            }
        )
    }

    // --- Rules Dialog (নিয়ম) ---
    if (showRulesDialog) {
        AlertDialog(
            onDismissRequest = { showRulesDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Article,
                        contentDescription = null,
                        tint = Color(0xFF0047BA),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "মাইক্রো জব নিয়মাবলী ও সতর্কতা",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF0F172A)
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
                        text = "সতর্কতা ও নিয়মাবলী:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0047BA)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "১. কাজ না করে মিথ্যা বা ভুল প্রমাণ সাবমিট করলে অ্যাকাউন্ট স্থায়ীভাবে ব্যান হতে পারে।\n\n" +
                                "২. ভিডিও টাস্কের ক্ষেত্রে নির্ধারিত সময় পর্যন্ত সম্পূর্ণ ভিডিও দেখা বাধ্যতামূলক।\n\n" +
                                "৩. একই কাজের প্রুফ বারবার সাবমিট করা নিষেধ।\n\n" +
                                "৪. রিওয়ার্ড তাৎক্ষণিকভাবে আপনার ওয়ালেটে ক্রেডিট করা হবে।",
                        fontSize = 13.sp,
                        color = Color(0xFF334155),
                        lineHeight = 19.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showRulesDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0047BA)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("সম্মতি দিচ্ছি")
                }
            }
        )
    }

    // --- Job Proof Submission Bottom Sheet ---
    if (selectedJobForProof != null) {
        val job = selectedJobForProof!!
        val isDone = completedJobIds.contains(job.id)

        ModalBottomSheet(
            onDismissRequest = {
                if (!isSubmittingProof) selectedJobForProof = null
            },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Job Serial Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFE60000),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${job.serialNumber}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "টাস্ক বিবরণ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0055D2)
                        )
                    }

                    IconButton(onClick = { selectedJobForProof = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = job.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = job.rewardDisplay,
                        color = Color(0xFF0284C7),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Instructions Box
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "কাজের নির্দেশনা:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF334155)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = job.instructions,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Open Link Action Button
                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(job.targetUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "লিংক ওপেন করা হচ্ছে...", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0055D2))
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = Color(0xFF0055D2),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "কাজের লিংক ওপেন করুন (${job.platform})",
                        color = Color(0xFF0055D2),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Proof Input Field
                Text(
                    text = "প্রমাণ / সাবমিশন তথ্য:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = proofText,
                    onValueChange = { proofText = it },
                    placeholder = {
                        Text(
                            "আপনার প্রোফাইল লিংক, ইউজারনেম বা কাজের প্রমাণ লিখুন...",
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .testTag("micro_job_proof_input"),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isDone && !isSubmittingProof
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Proof Button
                Button(
                    onClick = {
                        isSubmittingProof = true
                        coroutineScope.launch {
                            val result = repository.submitMicroJobProof(job, proofText)
                            isSubmittingProof = false
                            if (result.isSuccess) {
                                selectedJobForProof = null
                                Toast.makeText(
                                    context,
                                    "কাজ সফলভাবে সম্পন্ন হয়েছে! 0.5৳ আপনার ব্যালেন্সে যোগ হয়েছে 🎉",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    result.exceptionOrNull()?.message ?: "সাবমিট করতে ত্রুটি হয়েছে",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    },
                    enabled = !isDone && proofText.trim().length >= 3 && !isSubmittingProof,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDone) Color(0xFF10B981) else Color(0xFF0055D2)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("micro_job_submit_button")
                ) {
                    if (isSubmittingProof) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text(
                            text = if (isDone) "ইতিমধ্যে সম্পন্ন হয়েছে" else "প্রুফ সাবমিট করুন (0.5৳ রিওয়ার্ড)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// HERO BANNER MATCHING SCREENSHOT EXACTLY
// -------------------------------------------------------------
@Composable
fun MicroJobsHeroBanner() {
    Surface(
        color = Color(0xFF0B194B),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            // Background Generated Image if available
            Image(
                painter = painterResource(id = R.drawable.img_micro_jobs_hero),
                contentDescription = "Micro Jobs Hero Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp))
            )
        }
    }
}

// -------------------------------------------------------------
// 2-COLUMN GRID CARD MATCHING SCREENSHOT EXACTLY
// -------------------------------------------------------------
@Composable
fun MicroJobGridCard(
    job: MicroJobItem,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("micro_job_card_${job.serialNumber}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Image / Thumbnail area matching Screenshot with Red Serial Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(0xFFF8FAFC))
            ) {
                // Card-specific graphic preview based on Screenshot
                when (job.serialNumber) {
                    1 -> FacebookThumbnailMockup()
                    2 -> YouTubeThumbnailMockup1()
                    3 -> MobileEarningThumbnailMockup()
                    4 -> ProductPhotoMockup()
                    else -> FacebookThumbnailMockup()
                }

                // Top-Right Red Serial Badge (1, 2, 3, 4) matching Screenshot
                Surface(
                    color = Color(0xFFE60000),
                    shape = RoundedCornerShape(bottomStart = 8.dp, topEnd = 12.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 30.dp, height = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${job.serialNumber}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // If completed checkmark badge
                if (isCompleted) {
                    Surface(
                        color = Color(0xFF10B981),
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.padding(3.dp)
                        )
                    }
                }
            }

            // Card Text Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = job.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                    modifier = Modifier.height(34.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = job.rewardDisplay,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0284C7)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// THUMBNAIL MOCKUPS (MATCHING SCREENSHOT 1, 2, 3, 4)
// -------------------------------------------------------------

// Card 1: Facebook Dashboard Mockup
@Composable
fun FacebookThumbnailMockup() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(6.dp)
    ) {
        // Facebook Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .background(Color(0xFF1877F2), RoundedCornerShape(3.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(10.dp)
                    .background(Color.White, CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Professional Dashboard",
                fontSize = 7.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Lines representing profile details
        repeat(4) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFFCBD5E1), CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(5.dp)
                        .background(Color(0xFFE2E8F0), RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

// Card 2: YouTube Channel Videos Mockup
@Composable
fun YouTubeThumbnailMockup1() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF18181B))
            .padding(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(0xFFEF4444), RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Rootva Official",
                fontSize = 8.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF27272A), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Withdraw Tutorial",
                    fontSize = 8.sp,
                    color = Color.White
                )
            }
        }
    }
}

// Card 3: Mobile Earning Thumbnail Mockup
@Composable
fun MobileEarningThumbnailMockup() {
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
            modifier = Modifier.padding(6.dp)
        ) {
            Text(
                text = "মোবাইল দিয়ে ইনকাম",
                color = Color(0xFFFACC15),
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .background(Color(0xFF10B981), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "৳ ৭৪০.৭৫",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "ফুল ভিডিও দেখুন",
                color = Color.White,
                fontSize = 8.sp
            )
        }
    }
}

// Card 4: Product / Earring Photo Mockup
@Composable
fun ProductPhotoMockup() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3FF)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = null,
                tint = Color(0xFF7C3AED),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Product Review Task",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5B21B6)
            )
        }
    }
}
