package com.example.ui.screens.course

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.data.model.CourseOrder
import com.example.data.model.FreelancingCourse
import com.example.data.repository.AppRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreelancingCourseScreen(
    repository: AppRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentUser by repository.currentUser.collectAsState(initial = null)
    val purchasedIds by repository.purchasedCourseIds.collectAsState(initial = emptySet())
    val courses = remember { repository.getFreelancingCourses() }
    val userBalance = currentUser?.balance ?: 1450.0

    // Dialog & Sheet States
    var showTutorialDialog by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }
    var courseToOrder by remember { mutableStateOf<FreelancingCourse?>(null) }
    var courseToWatch by remember { mutableStateOf<FreelancingCourse?>(null) }
    var successOrder by remember { mutableStateOf<CourseOrder?>(null) }
    var isProcessingOrder by remember { mutableStateOf(false) }

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
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("freelancing_course_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Rootva Freelancing Course",
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
            // Top Action Buttons Row: টিউটোরিয়াল (Red) & নিয়ম (Blue)
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
                        .testTag("course_tutorial_button")
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
                        .testTag("course_rules_button")
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

            // Courses Vertical List matching Screenshot exactly
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(courses, key = { it.id }) { course ->
                    val isPurchased = purchasedIds.contains(course.id)
                    FreelancingCourseCard(
                        course = course,
                        isPurchased = isPurchased,
                        onOrderClick = {
                            if (isPurchased) {
                                courseToWatch = course
                            } else {
                                courseToOrder = course
                            }
                        }
                    )
                }
            }
        }
    }

    // --- Order Confirmation Dialog ---
    if (courseToOrder != null) {
        val course = courseToOrder!!
        AlertDialog(
            onDismissRequest = {
                if (!isProcessingOrder) courseToOrder = null
            },
            title = {
                Text(
                    text = "কোর্স অর্ডার নিশ্চিতকরণ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF0055D2)
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = course.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("কোর্স ফি:", fontSize = 13.sp, color = Color(0xFF64748B))
                                Text(course.priceBengali, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0055D2))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("আপনার ওয়ালেট ব্যালেন্স:", fontSize = 13.sp, color = Color(0xFF64748B))
                                Text("৳${userBalance.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF10B981))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• অর্ডার সম্পন্ন করার সাথে সাথে লাইফটাইম কোর্স ভিডিও আনলক হয়ে যাবে।",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isProcessingOrder = true
                        coroutineScope.launch {
                            val result = repository.purchaseCourse(course)
                            isProcessingOrder = false
                            if (result.isSuccess) {
                                courseToOrder = null
                                successOrder = result.getOrNull()
                            } else {
                                Toast.makeText(
                                    context,
                                    result.exceptionOrNull()?.message ?: "অর্ডার সম্পন্ন করা সম্ভব হয়নি",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    },
                    enabled = !isProcessingOrder && userBalance >= course.price,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isProcessingOrder) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("অর্ডার কনফার্ম করুন")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { courseToOrder = null },
                    enabled = !isProcessingOrder
                ) {
                    Text("বাতিল")
                }
            }
        )
    }

    // --- Order Placed Success Dialog ---
    if (successOrder != null) {
        val order = successOrder!!
        val purchasedCourse = courses.find { it.id == order.courseId }
        AlertDialog(
            onDismissRequest = { successOrder = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color(0xFFECFDF5), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(36.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "অভিনন্দন!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "আপনার কোর্স অর্ডারটি সফল হয়েছে!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = order.courseTitle,
                        fontSize = 13.sp,
                        color = Color(0xFF0055D2),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "অর্ডার রেফারেন্স: #${order.id}\nএখনই কোর্সের সকল ভিডিও দেখতে পারেন।",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        successOrder = null
                        if (purchasedCourse != null) {
                            courseToWatch = purchasedCourse
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ভিডিও দেখুন 🎬")
                }
            },
            dismissButton = {
                TextButton(onClick = { successOrder = null }) {
                    Text("ঠিক আছে")
                }
            }
        )
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
                        text = "টিউটোরিয়াল ও ব্যবহার নির্দেশিকা",
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
                        text = "Rootva Freelancing Course-এ আপনাকে স্বাগতম!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0055D2)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "১. কিভাবে কোর্স কিনবেন:\nপছন্দের কোর্সের নিচে 'অর্ডার করুন' বাটনে ক্লিক করে ওয়ালেট ব্যালেন্স থেকে ৫০ টাকায় কনফার্ম করুন।\n\n" +
                                "২. কিভাবে ক্লাস করবেন:\nকোর্স এনরোল হওয়ার পর সরাসরি 'ভিডিও দেখুন' বাটনে চাপ দিয়ে প্রতিটি লেকচার এইচডি কোয়ালিটিতে দেখতে পারবেন।\n\n" +
                                "৩. সাপোর্ট ও প্র্যাকটিস ফাইল:\nপ্রতিটি কোর্সের ডেসক্রিপশনে প্র্যাকটিস মেটেরিয়াল এবং ক্যাপকাট টেমপ্লেট ডাউনলোড লিংক সংযুক্ত থাকে।",
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
                        text = "কোর্সের নিয়মাবলী ও নীতিমালা",
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
                        text = "কোর্স ব্যবহারের শর্তসমূহ:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0047BA)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "১. এককালীন ৫০ টাকা ফি পরিশোধে লাইফটাইম অ্যাক্সেস পাওয়া যাবে।\n\n" +
                                "২. কোর্সের কোনো ভিডিও বা টিউটোরিয়াল কপি, ডাউনলোড করে অন্যত্র বিক্রি বা শেয়ার করা আইনত দণ্ডনীয়।\n\n" +
                                "৩. কোর্স সংক্রান্ত যেকোনো প্রশ্ন বা সমস্যায় আমাদের হেল্প সেন্টার ২৪/৭ সক্রিয় থাকবে।\n\n" +
                                "৪. প্রতিটি লেকচার মনোযোগ সহকারে সম্পন্ন করে হোমওয়ার্ক প্র্যাকটিস করুন।",
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

    // --- Course Video Player Sheet ---
    if (courseToWatch != null) {
        val course = courseToWatch!!
        ModalBottomSheet(
            onDismissRequest = { courseToWatch = null },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = course.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0055D2),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { courseToWatch = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Video Player Mockup Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "লেকচার ১: কোর্সের মূল পরিচিতি ও সেটআপ",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "কোর্স সিলেবাস ও লেকচার তালিকা:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(8.dp))

                val lessons = listOf(
                    "লেকচার ১: ইন্টারফেস ও টুলস পরিচিতি (১৫ মিনিট)",
                    "লেকচার ২: ভিডিও কাটিং, স্প্লিট ও টাইমিং (২০ মিনিট)",
                    "লেকচার ৩: সাউন্ড এফেক্ট ও ব্যাকগ্রাউন্ড মিউজিক (১৮ মিনিট)",
                    "লেকচার ৪: প্রফেশনাল টেক্সট ও অ্যানিমেশন (২২ মিনিট)",
                    "লেকচার ৫: সিনেমাটিক কালার গ্রেডিং (২৫ মিনিট)",
                    "লেকচার ৬: ফুল এইচডি রেন্ডার ও আর্নিং স্ট্র্যাটেজি (৩০ মিনিট)"
                )

                lessons.forEachIndexed { index, lesson ->
                    Surface(
                        color = if (index == 0) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (index == 0) Color(0xFF93C5FD) else Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                Toast.makeText(context, "$lesson শুরু হচ্ছে...", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (index == 0) Icons.Default.PlayCircle else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = if (index == 0) Color(0xFF0055D2) else Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = lesson,
                                fontSize = 12.sp,
                                fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (index == 0) Color(0xFF0055D2) else Color(0xFF334155)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// COURSE CARD MATCHING SCREENSHOT EXACTLY
// -------------------------------------------------------------
@Composable
fun FreelancingCourseCard(
    course: FreelancingCourse,
    isPurchased: Boolean,
    onOrderClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0xFF0055D2),
                shape = RoundedCornerShape(8.dp)
            )
            .testTag("course_card_${course.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Course Banner Image
            val imageResId = when (course.drawableResName) {
                "img_capcut_course" -> R.drawable.img_capcut_course
                "img_adsterra_course" -> R.drawable.img_adsterra_course
                else -> null
            }

            if (imageResId != null) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = course.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                )
            } else {
                // High-fidelity fallback banner
                CourseBannerFallback(course = course)
            }

            // Title and Price Block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = course.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = course.priceBengali,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            // Full-Width Action Button (অর্ডার করুন)
            Button(
                onClick = onOrderClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPurchased) Color(0xFF10B981) else Color(0xFF0055D2)
                ),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("course_order_button_${course.id}")
            ) {
                Text(
                    text = if (isPurchased) "ভিডিও দেখুন" else "অর্ডার করুন",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// -------------------------------------------------------------
// COURSE BANNER FALLBACK (If drawable is loading)
// -------------------------------------------------------------
@Composable
fun CourseBannerFallback(course: FreelancingCourse) {
    val isCapcut = course.id == "course_capcut"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(
                Brush.horizontalGradient(
                    if (isCapcut) listOf(Color(0xFF0B0F19), Color(0xFF1E1B4B))
                    else listOf(Color(0xFF065F46), Color(0xFF0F172A))
                )
            )
            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = if (isCapcut) Icons.Default.VideoCameraBack else Icons.Default.MonetizationOn,
                contentDescription = null,
                tint = if (isCapcut) Color(0xFF38BDF8) else Color(0xFF34D399),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = course.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "ZERO TO HERO",
                color = Color(0xFFFCD34D),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
