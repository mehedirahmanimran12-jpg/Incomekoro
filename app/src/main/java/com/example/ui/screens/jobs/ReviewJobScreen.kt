package com.example.ui.screens.jobs

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import com.example.data.model.ReviewJobItem
import com.example.data.repository.AppRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewJobScreen(
    repository: AppRepository,
    onNavigateBack: () -> Unit
) {
    BackHandler(onBack = onNavigateBack)

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentUser by repository.currentUser.collectAsState(initial = null)
    val completedReviewJobIds by repository.completedReviewJobIds.collectAsState(initial = emptySet())
    val reviewJobs = remember { repository.getReviewJobs() }

    // Dialog & Sheet States
    var showTutorialDialog by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }
    var selectedJobForProof by remember { mutableStateOf<ReviewJobItem?>(null) }
    var proofText by remember { mutableStateOf("") }
    var isSubmittingProof by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var lastRewardedAmount by remember { mutableStateOf(8.0) }

    Scaffold(
        containerColor = Color.White,
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
                        modifier = Modifier.testTag("review_works_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Review Works",
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Two Top Action Buttons: টিউটোরিয়াল (Red) & নিয়ম (Blue)
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // টিউটোরিয়াল Button (Bright Red)
                    Button(
                        onClick = { showTutorialDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60000)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("review_job_tutorial_button")
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0047BA)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("review_job_rules_button")
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
            }

            // 2. Hero Image Banner: Review Job income 100-200 Taka
            item(span = { GridItemSpan(2) }) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_review_job_hero),
                        contentDescription = "Review Job Income 100-200 Taka",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // 3. Section Title: "রিভিউ জব" (Royal Blue bold header)
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "রিভিউ জব",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = Color(0xFF0055D2),
                    modifier = Modifier.padding(top = 10.dp, bottom = 2.dp, start = 2.dp)
                )
            }

            // 4. 2-Column Grid of Review Job Cards
            items(reviewJobs, key = { it.id }) { job ->
                val isCompleted = job.id in completedReviewJobIds
                ReviewJobCardItem(
                    job = job,
                    isCompleted = isCompleted,
                    onClick = {
                        selectedJobForProof = job
                        proofText = ""
                    }
                )
            }
        }
    }

    // -------------------------------------------------------------
    // REVIEW PROOF SUBMISSION BOTTOM SHEET
    // -------------------------------------------------------------
    if (selectedJobForProof != null) {
        val currentJob = selectedJobForProof!!
        val isAlreadyDone = currentJob.id in completedReviewJobIds

        ModalBottomSheet(
            onDismissRequest = {
                if (!isSubmittingProof) selectedJobForProof = null
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .padding(bottom = 28.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFE60000),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${currentJob.serialNumber}",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = currentJob.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "প্ল্যাটফর্ম: ${currentJob.platform}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFFE0F2FE),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = currentJob.rewardDisplay,
                            color = Color(0xFF0284C7),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Instruction Box
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF0055D2),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "কাজের নিয়মাবলী:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = Color(0xFF0055D2)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentJob.instructions,
                            fontSize = 13.sp,
                            color = Color(0xFF334155),
                            lineHeight = 19.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action 1: Open Target Link Button
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentJob.targetUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "লিংক ওপেন করা যাচ্ছে না", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("open_review_link_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "রিভিউ পেজ ওপেন করুন (${currentJob.platform})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action 2: Proof Input
                if (!isAlreadyDone) {
                    Text(
                        text = "আপনার রিভিউ প্রমাণ জমা দিন:",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = proofText,
                        onValueChange = { proofText = it },
                        placeholder = {
                            Text(
                                "আপনার গুগল রিভিউ নাম / অ্যাকাউন্টের নাম লিখুন...",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp)
                            .testTag("review_proof_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0055D2),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            if (proofText.trim().isBlank()) {
                                Toast.makeText(context, "দয়া করে আপনার রিভিউ নাম লিখুন", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isSubmittingProof = true
                            coroutineScope.launch {
                                val result = repository.submitReviewJobProof(currentJob, proofText)
                                isSubmittingProof = false
                                if (result.isSuccess) {
                                    lastRewardedAmount = currentJob.reward
                                    selectedJobForProof = null
                                    showSuccessDialog = true
                                } else {
                                    val err = result.exceptionOrNull()?.message ?: "জমা দিতে সমস্যা হয়েছে"
                                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = !isSubmittingProof && proofText.trim().isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_review_proof_button")
                    ) {
                        if (isSubmittingProof) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "প্রুফ জমা দিন (${currentJob.rewardDisplay} রিওয়ার্ড)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Surface(
                        color = Color(0xFFECFDF5),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "আপনি এই কাজটি সম্পন্ন করে ${currentJob.rewardDisplay} রিওয়ার্ড সংগ্রহ করেছেন!",
                                color = Color(0xFF065F46),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------
    // SUCCESS REWARD DIALOG
    // -------------------------------------------------------------
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
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
                        text = "অভিনন্দন! 🎉",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A)
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "আপনার রিভিউ সফলভাবে অনুমোদিত হয়েছে এবং ওয়ালেটে রিওয়ার্ড জমা হয়েছে।",
                        fontSize = 13.sp,
                        color = Color(0xFF334155)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
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
                            Text(
                                text = "প্রাপ্ত রিওয়ার্ড:",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color(0xFF1E40AF)
                            )
                            Text(
                                text = "+৳${String.format(java.util.Locale.US, "%.2f", lastRewardedAmount)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "বর্তমান ব্যালেন্স: ৳${String.format(java.util.Locale.US, "%.2f", currentUser?.balance ?: 1450.0)}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ধন্যবাদ 🌟")
                }
            }
        )
    }

    // -------------------------------------------------------------
    // TUTORIAL DIALOG (টিউটোরিয়াল)
    // -------------------------------------------------------------
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
                        text = "রিভিউ জব টিউটোরিয়াল",
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
                        text = "সহজে রিভিউ দিয়ে প্রতিদিন ১০০-২০০ টাকা আয়ের নিয়ম:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0055D2)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "১. যেকোনো রিভিউ কার্ডে ক্লিক করুন।\n\n" +
                                "২. 'রিভিউ পেজ ওপেন করুন' বাটনে চাপ দিয়ে নির্দিষ্ট গুগল ম্যাপ বা সাইট ওপেন করুন।\n\n" +
                                "৩. সেখানে ৫ স্টার (⭐⭐⭐⭐⭐) দিন এবং প্রতিষ্ঠান সম্পর্কে ১-২ লাইনের ভালো মন্তব্য লিখুন।\n\n" +
                                "৪. রিভিউ পোস্ট করে আপনার যে জিমেইল বা আইডির নাম দিয়ে রিভিউ দিয়েছেন সেই নাম নিচে লিখুন।\n\n" +
                                "৫. 'প্রুফ জমা দিন' এ চাপ দিলেই সাথে সাথে ৳৮.০ রিওয়ার্ড আপনার অ্যাকাউন্টে যুক্ত হবে।",
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

    // -------------------------------------------------------------
    // RULES DIALOG (নিয়ম)
    // -------------------------------------------------------------
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
                        text = "রিভিউ জবের নীতিমালা ও শর্ত",
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
                        text = "সতর্কতা ও নিয়মাবলী:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0047BA)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "১. অবশ্যই আপনার নিজস্ব গুগল অ্যাকাউন্ট ব্যবহার করে ৫ স্টার রেটিং দিতে হবে।\n\n" +
                                "২. ইতিবাচক ও সুন্দর মন্তব্য লিখতে হবে (যেমন: Excellent service, highly recommended)।\n\n" +
                                "৩. রিভিউ না দিয়ে মিথ্যা নাম বা প্রুফ জমা দিলে আইডি সাময়িকভাবে বাতিল হতে পারে।\n\n" +
                                "৪. একজন ব্যবহারকারী একটি কাজে একবারই রিওয়ার্ড সংগ্রহ করতে পারবেন।\n\n" +
                                "৫. প্রতিদিন নতুন নতুন রিভিউ জব যুক্ত হয়, যত খুশি তত কাজ করতে পারবেন।",
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
                    Text("সম্মত আছি")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// REVIEW JOB CARD ITEM MATCHING THE SCREENSHOT EXACTLY
// -------------------------------------------------------------
@Composable
fun ReviewJobCardItem(
    job: ReviewJobItem,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("review_job_card_${job.serialNumber}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Artwork Area matching Screenshot
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(138.dp)
                    .background(Color(0xFFFFB300))
            ) {
                // Organic background details
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Bottom-left dark curved silhouette
                    val path1 = Path().apply {
                        moveTo(0f, h * 0.55f)
                        cubicTo(w * 0.15f, h * 0.6f, w * 0.2f, h * 0.9f, 0f, h)
                        close()
                    }
                    drawPath(path1, color = Color(0xFF1E293B))

                    // Bottom-right dark curved silhouette
                    val path2 = Path().apply {
                        moveTo(w, h * 0.65f)
                        cubicTo(w * 0.85f, h * 0.7f, w * 0.82f, h * 0.95f, w, h)
                        close()
                    }
                    drawPath(path2, color = Color(0xFF1E293B))
                }

                // Center White Cloud / Scalloped Badge
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(26.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Golden Stars arranged around
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Top Stars
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, start = 16.dp, end = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB800),
                                    modifier = Modifier.size(16.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB800),
                                    modifier = Modifier.size(17.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB800),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Middle Text: review.jobs
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "review",
                                    color = Color(0xFF0F2B48),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    letterSpacing = (-0.5).sp
                                )
                                Text(
                                    text = ".jobs",
                                    color = Color(0xFFF59E0B),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    letterSpacing = (-0.5).sp
                                )
                            }

                            // Bottom Stars
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp, start = 20.dp, end = 20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB800),
                                    modifier = Modifier.size(15.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB800),
                                    modifier = Modifier.size(16.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB800),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }

                // Top-Right Bright Red Serial Number Badge matching screenshot
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 0.dp, end = 0.dp)
                ) {
                    Surface(
                        color = Color(0xFFE60000),
                        shape = RoundedCornerShape(bottomStart = 8.dp),
                        modifier = Modifier
                            .width(26.dp)
                            .height(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${job.serialNumber}",
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Completed Indicator
                if (isCompleted) {
                    Surface(
                        color = Color(0xDD10B981),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(Color.White, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "সম্পন্ন",
                                    color = Color(0xFF10B981),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Info Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                Text(
                    text = job.title,
                    color = Color(0xFF0F172A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = job.rewardDisplay,
                    color = Color(0xFF0284C7),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
