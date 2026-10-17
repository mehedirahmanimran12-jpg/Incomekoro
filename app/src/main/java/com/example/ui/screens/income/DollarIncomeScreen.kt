package com.example.ui.screens.income

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DollarIncomeScreen(
    repository: AppRepository,
    onNavigateBack: () -> Unit,
    onNavigateToShop: () -> Unit = {},
    onNavigateToReferral: () -> Unit = {}
) {
    BackHandler(onBack = onNavigateBack)

    val context = LocalContext.current
    val currentUser by repository.currentUser.collectAsState(initial = null)
    val resellingOrders by repository.resellingOrders.collectAsState(initial = emptyList())
    val teamMembers by repository.teamMembers.collectAsState(initial = emptyList())

    // Dynamic stats based on user data
    val currentLeadersCount = teamMembers.size.coerceAtLeast(currentUser?.teamCount ?: 0)
    val currentOrdersCount = resellingOrders.size

    // Dialog states
    var showTutorialDialog by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showInfoNoticeDialog by remember { mutableStateOf(false) }

    val isEligible = currentLeadersCount >= 10 && currentOrdersCount >= 1

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
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("dollar_income_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Dollar Income",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Top-Right Speech Bubble with Info "i" icon matching Screenshot
                    IconButton(
                        onClick = { showInfoNoticeDialog = true },
                        modifier = Modifier.testTag("dollar_income_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "Dollar Income Info",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .background(Color.White)
        ) {
            // 1. Two Top Action Buttons: টিউটোরিয়াল (Red) & নিয়ম (Blue)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0047BA)),
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

            // 2. Hero Image Banner: DOLLAR INCOME with cash and coins on starry background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(155.dp)
                    .background(Color(0xFF091638))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_dollar_income_banner),
                    contentDescription = "Dollar Income Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Maroon/Burgundy Card Matching Screenshot Exactly
            Surface(
                color = Color(0xFF83003A),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    // Header: ⭐ ডলার ইনকাম সুযোগ ⭐
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⭐ ডলার ইনকাম সুযোগ ⭐",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Condition Title: 💥 ওয়েবসাইট পাওয়ার শর্ত 💥
                    Text(
                        text = "💥 ওয়েবসাইট পাওয়ার শর্ত 💥",
                        color = Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Condition 1: ✅ ১০ জন সরাসরি লিডার
                    Text(
                        text = "✅ ১০ জন সরাসরি লিডার",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Condition 2: ✅ ১টি রিসিলিং সম্পন্ন করতে হবে
                    Text(
                        text = "✅ ১টি রিসিলিং সম্পন্ন করতে হবে",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Outcome Title: এরপরই আপনি পাবেন—
                    Text(
                        text = "এরপরই আপনি পাবেন—",
                        color = Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Outcome 1: 🌎 অফিসিয়াল ওয়েবসাইট
                    Text(
                        text = "🌎 অফিসিয়াল ওয়েবসাইট",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Outcome 2: 💲 ডলার ইনকাম শুরু করার সুযোগ
                    Text(
                        text = "💲 ডলার ইনকাম শুরু করার সুযোগ",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. 2x2 Grid of Progress Stat Cards with Yellow Borders
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Row 1: আপনার অর্জন (0) | আপনার লাগবে (10)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Card 1: আপনার অর্জন
                    StatCard(
                        number = "$currentLeadersCount",
                        numberColor = Color(0xFFDC2626), // Red
                        label = "আপনার অর্জন",
                        modifier = Modifier.weight(1f)
                    )

                    // Card 2: আপনার লাগবে (10)
                    StatCard(
                        number = "10",
                        numberColor = Color(0xFF7C3AED), // Purple
                        label = "আপনার লাগবে",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: আপনার অর্ডার (0) | আপনার লাগবে (1)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Card 3: আপনার অর্ডার
                    StatCard(
                        number = "$currentOrdersCount",
                        numberColor = Color(0xFFDC2626), // Red
                        label = "আপনার অর্ডার",
                        modifier = Modifier.weight(1f)
                    )

                    // Card 4: আপনার লাগবে (1)
                    StatCard(
                        number = "1",
                        numberColor = Color(0xFF7C3AED), // Purple
                        label = "আপনার লাগবে",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Divider
            HorizontalDivider(
                color = Color(0xFFE2E8F0),
                thickness = 2.dp,
                modifier = Modifier.padding(horizontal = 14.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 6. Primary Action Button: বিস্তারিত দেখুন
            Button(
                onClick = { showDetailsDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2)),
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 14.dp)
                    .testTag("dollar_income_details_button")
            ) {
                Text(
                    text = "বিস্তারিত দেখুন",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // -------------------------------------------------------------
    // STAT CARD COMPONENT
    // -------------------------------------------------------------
    // (Used inside the 2x2 grid)

    // -------------------------------------------------------------
    // DETAILS DIALOG (বিস্তারিত দেখুন)
    // -------------------------------------------------------------
    if (showDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ডলার ইনকাম যোগ্যতা বিবরণ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF0F172A)
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "অফিসিয়াল ডলার ইনকাম ওয়েবসাইট আনলক করার শর্তাবলী:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0055D2)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Leaders requirement status
                    Surface(
                        color = if (currentLeadersCount >= 10) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (currentLeadersCount >= 10) Color(0xFFBBF7D0) else Color(0xFFFECACA)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "সরাসরি লিডার সংখ্যা:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "বর্তমান: $currentLeadersCount জন / প্রয়োজন: ১০ জন",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Icon(
                                imageVector = if (currentLeadersCount >= 10) Icons.Default.CheckCircle else Icons.Default.Pending,
                                contentDescription = null,
                                tint = if (currentLeadersCount >= 10) Color(0xFF166534) else Color(0xFFDC2626)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Reselling requirement status
                    Surface(
                        color = if (currentOrdersCount >= 1) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (currentOrdersCount >= 1) Color(0xFFBBF7D0) else Color(0xFFFECACA)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "রিসেলিং অর্ডার:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "বর্তমান: $currentOrdersCount টি / প্রয়োজন: ১টি",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Icon(
                                imageVector = if (currentOrdersCount >= 1) Icons.Default.CheckCircle else Icons.Default.Pending,
                                contentDescription = null,
                                tint = if (currentOrdersCount >= 1) Color(0xFF166534) else Color(0xFFDC2626)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isEligible) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://incomekoro.com/dollar-income"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "ওয়েবসাইট ওপেন করা হচ্ছে...", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("অফিসিয়াল ওয়েবসাইটে যান 🌎", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Quick Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    showDetailsDialog = false
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val code = currentUser?.referralCode ?: "250829"
                                    val clip = ClipData.newPlainText("Referral", "https://incomekoro.com/ref/$code")
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "রেফারেল লিংক কপি হয়েছে!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Text("টিম বাড়ান", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    showDetailsDialog = false
                                    onNavigateToShop()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Text("রিসেলিং করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetailsDialog = false }) {
                    Text("বুঝেছি", color = Color(0xFF0055D2))
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
                        text = "ডলার ইনকাম টিউটোরিয়াল",
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
                        text = "কিভাবে অফিসিয়াল ডলার ইনকাম শুরু করবেন:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0055D2)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "১. প্রথমে আপনার রেফারেল লিংকের মাধ্যমে অন্তত ১০ জন সরাসরি একটিভ লিডার তৈরি করুন।\n\n" +
                                "২. রিসেলিং শপ থেকে যেকোনো ১টি প্রডাক্ট সফলভাবে অর্ডার বা ডেলিভারি সম্পন্ন করুন।\n\n" +
                                "৩. উভয় শর্ত পূরণ হওয়ার সাথে সাথে আপনাকে কোম্পানির অফিসিয়াল ডলার ইনকাম ওয়েবসাইট ও আলাদা ড্যাশবোর্ড এক্সেস প্রদান করা হবে।\n\n" +
                                "৪. সেখানে প্রতিদিন মাইক্রো সার্ভিস ও ইন্টারন্যাশনাল ক্যাম্পেইন থেকে আনলিমিটেড ডলার ইনকাম করতে পারবেন।",
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
                    Text("ঠিক আছে")
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
                        text = "শর্তাবলী ও নীতিমালা",
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
                        text = "নীতিমালা ও নির্দেশিকা:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0047BA)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "১. লিডারদের অবশ্যই জেনুইন এবং একাউন্ট সক্রিয় থাকতে হবে। ফেক একাউন্ট খুললে রিওয়ার্ড বাতিল করা হবে।\n\n" +
                                "২. রিসেলিং অর্ডার অবশ্যই সফলভাবে সম্পন্ন হতে হবে (ক্যানসেল বা রিটার্ন গ্রহণযোগ্য নয়)।\n\n" +
                                "৩. শর্ত পূরণ করার পর সর্বোচ্চ ২৪ ঘণ্টার মধ্যে ডলার ইনকাম পোর্টাল আনলক হবে।\n\n" +
                                "৪. অর্জিত ডলার বিকাশ, নগদ বা ক্রিপ্টোকারেন্সির মাধ্যমে সরাসরি উইথড্র করা যাবে।",
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
    // INFO SPEECH BUBBLE NOTICE DIALOG
    // -------------------------------------------------------------
    if (showInfoNoticeDialog) {
        AlertDialog(
            onDismissRequest = { showInfoNoticeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF0055D2),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "হেল্প ও নোটিশ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF0F172A)
                    )
                }
            },
            text = {
                Text(
                    text = "ডলার ইনকাম ওয়েবসাইট সংক্রান্ত যেকোনো জিজ্ঞাসা বা ভেরিফিকেশনের জন্য আমাদের সাপোর্ট সেন্টারে যোগাযোগ করুন। শর্ত পূরণ হলে আপনার একাউন্টে স্বয়ংক্রিয়ভাবে নোটিফিকেশন পৌঁছে যাবে।",
                    fontSize = 13.sp,
                    color = Color(0xFF334155),
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showInfoNoticeDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ঠিক আছে")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// STAT CARD (2x2 GRID ITEM)
// -------------------------------------------------------------
@Composable
private fun StatCard(
    number: String,
    numberColor: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(2.dp, Color(0xFFF59E0B)), // Warm yellow/gold border
        modifier = modifier.height(82.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = number,
                color = numberColor,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                color = Color.Black,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
