package com.example.ui.screens.income

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.AppRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdsIncomeScreen(
    repository: AppRepository,
    onNavigateBack: () -> Unit,
    title: String = "Ads Income"
) {
    BackHandler(onBack = onNavigateBack)

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser by repository.currentUser.collectAsState(initial = null)

    // Dialog & Simulation States
    var showTutorialDialog by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }
    var showAdTaskSheet by remember { mutableStateOf(false) }
    var showSuccessRewardDialog by remember { mutableStateOf(false) }
    var lastRewardAmount by remember { mutableStateOf(5.0) }

    // Ad Watch Simulation State
    var isWatchingAd by remember { mutableStateOf(false) }
    var adSecondsLeft by remember { mutableStateOf(15) }
    var adStep by remember { mutableStateOf(1) } // 1: Initial, 2: Watch Ad clicked, 3: Continue clicked (Timer running), 4: Finished

    // Timer effect for 15-second requirement
    LaunchedEffect(isWatchingAd) {
        if (isWatchingAd) {
            adSecondsLeft = 15
            while (adSecondsLeft > 0) {
                delay(1000L)
                adSecondsLeft -= 1
            }
            adStep = 4 // Timer finished
        }
    }

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
                        modifier = Modifier.testTag("ads_income_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = title,
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
                .verticalScroll(rememberScrollState())
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Two Top Action Buttons: টিউটোরিয়াল (Red) & নিয়ম (Blue)
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
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("dollar_income_tutorial_button")
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
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("dollar_income_rules_button")
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

            // 2. Hero Image: Businessman looking at ascending 3D stair chart with growth arrow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .background(Color(0xFF0F172A))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_dollar_income_hero),
                    contentDescription = "Dollar Income Growth",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Blue Capsule Banner with Orange Border: "bot এ আয় করার নিয়ম"
            Surface(
                color = Color(0xFF0029D8),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(2.5.dp, Color(0xFFFF5500)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "bot এ আয় করার নিয়ম",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Black Strip with Orange Top & Bottom Borders
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .border(
                        BorderStroke(1.5.dp, Color(0xFFFF5500))
                    )
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "নিচের JOIN বলে  বাটন ক্লিক করে কাজ শুরু করুন",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 5. Yellow "JOIN NOW >" Stadium Button matching Screenshot
            Surface(
                color = Color(0xFFFFE600),
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(3.5.dp, Color(0xFFD4C000)),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .widthIn(min = 250.dp)
                    .height(56.dp)
                    .clickable {
                        // Launch the Interactive Ad/Bot Earning Experience
                        adStep = 1
                        isWatchingAd = false
                        adSecondsLeft = 15
                        showAdTaskSheet = true
                    }
                    .testTag("dollar_income_join_now_button")
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "JOIN NOW",
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // White circle with chevron arrow
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Join Now",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 6. Solid Royal Blue Instructional Container matching Screenshot
            Surface(
                color = Color(0xFF0022D2),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Watch Ad & Earn 🔔 এ চাপ দিন।",
                        color = Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "এরপর নিচে থাকা Continue 🔘 তে চাপ দিন।",
                        color = Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "নতুন পেইজ আসলে ⏳ অন্তত ১৫ সেকেন্ড সেখানে থাকুন।",
                        color = Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "সময় শেষ হলে উপরে থাকা ❌ এ চাপ দিয়ে বের হয়ে আসুন।",
                        color = Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "এই একই নিয়মে কাজ করতে থাকুন 🤑",
                        color = Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "✨ নিয়মিত কাজ করলে আয় বাড়তেই থাকবে 💵",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "🔥 আজই শুরু করুন — যত দেখবেন ততই ইনকাম হবে",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
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
                        text = "এড ইনকাম (Ads Income) টিউটোরিয়াল",
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
                        text = "বট ও এড দেখে আয়ের নিয়মাবলী:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0055D2)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "১. নিচের 'JOIN NOW' বাটনে চাপ দিন।\n\n" +
                                "২. সেখানে 'Watch Ad & Earn 🔔' বাটনে চাপ দিন।\n\n" +
                                "৩. এরপর নিচে প্রদর্শিত 'Continue 🔘' বাটনে চাপ দিন।\n\n" +
                                "৪. নতুন পেইজ ওপেন হলে অবশ্যই অন্তত ১৫ সেকেন্ড অপেক্ষা করুন।\n\n" +
                                "৫. ১৫ সেকেন্ড শেষ হলে উপরের ডান কোণায় ❌ এ চাপ দিয়ে ফিরে আসুন।\n\n" +
                                "৬. সাথে সাথেই আপনার ওয়ালেটে ডলার/টাকা রিওয়ার্ড যুক্ত হয়ে যাবে।",
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
                        text = "কাজের শর্ত ও নিয়মাবলী",
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
                        text = "সতর্কতা ও নীতিমালা:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0047BA)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "১. ১৫ সেকেন্ডের আগে পেইজ থেকে বের হয়ে আসলে কোনো রিওয়ার্ড জমা হবে না।\n\n" +
                                "২. অটোমেটিক ক্লিকার বা অনৈতিক কোনো পদ্ধতি ব্যবহার করা সম্পূর্ণ নিষিদ্ধ।\n\n" +
                                "৩. প্রতিদিন আনলিমিটেড এড দেখে যত খুশি তত ইনকাম করতে পারবেন।\n\n" +
                                "৪. অর্জিত ব্যালেন্স বিকাশ, নগদ অথবা রকেটের মাধ্যমে যেকোনো সময় উইথড্র করতে পারবেন।",
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

    // -------------------------------------------------------------
    // INTERACTIVE AD & BOT TASK MODAL (WATCH AD & EARN)
    // -------------------------------------------------------------
    if (showAdTaskSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                if (!isWatchingAd) showAdTaskSheet = false
            },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF0055D2),
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Ads Income Bot & Ad Task",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A)
                        )
                    }

                    if (!isWatchingAd) {
                        IconButton(onClick = { showAdTaskSheet = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Task Steps matching the exact screenshot instructions
                when (adStep) {
                    1 -> {
                        // Step 1: "Watch Ad & Earn 🔔 এ চাপ দিন।"
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "ধাপ ১: বিজ্ঞাপন শুরু করুন",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E40AF)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "নিচের 'Watch Ad & Earn 🔔' বাটনে চাপ দিন:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF334155),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = { adStep = 2 },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF0055D2)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("step_watch_ad_button")
                                ) {
                                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Watch Ad & Earn 🔔",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Option to open real Telegram bot as well
                        OutlinedButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/incomekoro_bot"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "টেলিগ্রাম লিংক ওপেন করা হচ্ছে...", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Telegram Bot ওপেন করুন 🤖", color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                        }
                    }

                    2 -> {
                        // Step 2: "এরপর নিচে থাকা Continue 🔘 তে চাপ দিন।"
                        Surface(
                            color = Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "ধাপ ২: বিজ্ঞাপন লোড নিশ্চিত করুন",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "বিজ্ঞাপন লোড হয়েছে! নিচে থাকা 'Continue 🔘' বাটনে চাপ দিন:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF334155),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        adStep = 3
                                        isWatchingAd = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF10B981)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("step_continue_button")
                                ) {
                                    Icon(Icons.Default.RadioButtonChecked, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Continue 🔘",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    3 -> {
                        // Step 3: "নতুন পেইজ আসলে ⏳ অন্তত ১৫ সেকেন্ড সেখানে থাকুন।"
                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = Color(0xFFEF4444),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "ADVERTISEMENT",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    // Disabled ❌ button until 15s finishes
                                    Surface(
                                        color = Color.White.copy(alpha = 0.2f),
                                        shape = CircleShape,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${adSecondsLeft}s",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Visual Ad Content Simulation
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
                                            ),
                                            RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.MonetizationOn,
                                            contentDescription = null,
                                            tint = Color(0xFFFFE600),
                                            modifier = Modifier.size(44.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "স্মার্টফোন দিয়ে ঘরে বসে আয় করুন",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "⏳ অন্তত ১৫ সেকেন্ড এখানে থাকুন...",
                                    color = Color(0xFFFBBF24),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                LinearProgressIndicator(
                                    progress = { (15 - adSecondsLeft) / 15f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF10B981),
                                    trackColor = Color.White.copy(alpha = 0.2f),
                                )
                            }
                        }
                    }

                    4 -> {
                        // Step 4: "সময় শেষ হলে উপরে থাকা ❌ এ চাপ দিয়ে বের হয়ে আসুন।"
                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = Color(0xFF10B981),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "সময় সম্পন্ন ✅",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    // Active ❌ Button with glow effect
                                    IconButton(
                                        onClick = {
                                            isWatchingAd = false
                                            showAdTaskSheet = false
                                            coroutineScope.launch {
                                                val result = repository.completeAdsIncomeTask(5.0)
                                                if (result.isSuccess) {
                                                    lastRewardAmount = result.getOrNull() ?: 5.0
                                                    showSuccessRewardDialog = true
                                                } else {
                                                    Toast.makeText(context, "রিওয়ার্ড যোগ করতে ত্রুটি হয়েছে", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(Color(0xFFEF4444), CircleShape)
                                            .testTag("ad_close_reward_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close Ad & Claim Reward",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(52.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "সময় শেষ! ❌ এ চাপ দিয়ে বের হয়ে আসুন",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "রিওয়ার্ড সংগ্রহ করতে উপরে বা নিচে থাকা ❌ বাটনে চাপ দিন:",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        isWatchingAd = false
                                        showAdTaskSheet = false
                                        coroutineScope.launch {
                                            val result = repository.completeAdsIncomeTask(5.0)
                                            if (result.isSuccess) {
                                                lastRewardAmount = result.getOrNull() ?: 5.0
                                                showSuccessRewardDialog = true
                                            } else {
                                                Toast.makeText(context, "রিওয়ার্ড যোগ করতে ত্রুটি হয়েছে", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF10B981)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(46.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "বের হন ও ৳৫.০০ রিওয়ার্ড নিন 🤑",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------
    // SUCCESS REWARD CELEBRATION DIALOG
    // -------------------------------------------------------------
    if (showSuccessRewardDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessRewardDialog = false },
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
                        text = "আপনি সফলভাবে Watch Ad & Earn কাজটি সম্পন্ন করেছেন!",
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
                                text = "অর্জিত রিওয়ার্ড:",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color(0xFF1E40AF)
                            )
                            Text(
                                text = "+৳${String.format(java.util.Locale.US, "%.2f", lastRewardAmount)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "আপনার বর্তমান ব্যালেন্স: ৳${String.format(java.util.Locale.US, "%.2f", currentUser?.balance ?: 1450.0)}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessRewardDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ধন্যবাদ 🤑")
                }
            }
        )
    }
}
