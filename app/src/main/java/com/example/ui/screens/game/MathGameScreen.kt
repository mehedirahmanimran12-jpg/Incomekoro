package com.example.ui.screens.game

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.MathProblem
import com.example.data.repository.AppRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathGameScreen(
    repository: AppRepository,
    onNavigateBack: () -> Unit
) {
    BackHandler(onBack = onNavigateBack)

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val currentUser by repository.currentUser.collectAsState(initial = null)
    val totalIncome by repository.mathGameTotalIncome.collectAsState(initial = 0.0)
    val availableCount by repository.mathGameAvailableCount.collectAsState(initial = 0)

    // Dialog States
    var showTutorialDialog by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showWinDialog by remember { mutableStateOf(false) }
    var lastWinReward by remember { mutableStateOf(2.0) }

    // Math Problem Generator
    fun generateProblem(): MathProblem {
        val opType = (1..3).random()
        return when (opType) {
            1 -> {
                val n1 = (5..30).random()
                val n2 = (3..25).random()
                MathProblem(num1 = n1, num2 = n2, operator = "+", correctAnswer = n1 + n2)
            }
            2 -> {
                val n1 = (15..40).random()
                val n2 = (3..14).random()
                MathProblem(num1 = n1, num2 = n2, operator = "-", correctAnswer = n1 - n2)
            }
            else -> {
                val n1 = (2..9).random()
                val n2 = (2..9).random()
                MathProblem(num1 = n1, num2 = n2, operator = "×", correctAnswer = n1 * n2)
            }
        }
    }

    var currentProblem by remember { mutableStateOf(generateProblem()) }
    var answerInput by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

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
                        modifier = Modifier.testTag("math_game_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Math Game",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Top-right Speech bubble with info icon matching screenshot
                    IconButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier.testTag("math_game_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "Math Game Info",
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
                .padding(bottom = 24.dp),
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60000)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("math_game_tutorial_button")
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
                        .testTag("math_game_rules_button")
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

            // 2. Hero Promotional Banner: "ম্যাথ গেম করে ইনকাম খেলুন আর আয় করুন প্রতিদিন!"
            Surface(
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .height(190.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_math_game_hero),
                    contentDescription = "Math Game Hero Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Two Stats Cards (Total Income & Available) matching Screenshot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Income Card (Emerald Green)
                Surface(
                    color = Color(0xFF009E60),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(82.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Total Income",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${totalIncome.toInt()}",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Available Card (Purple/Indigo)
                Surface(
                    color = Color(0xFF5E489E),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(82.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Available",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$availableCount",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Main Math Game Container matching Screenshot Exactly
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, Color(0xFF67B7E8)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Pill Badge: "No Games Left" or "Game X of 10"
                    Surface(
                        color = Color(0xFFADC4DE),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = if (availableCount <= 0) "No Games Left" else "Game ${11 - availableCount} of 10",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    // Question Box: "গেম শেষ" or "12 + 15 = ?"
                    Surface(
                        color = Color(0xFFF1F6FB),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.5.dp, Color(0xFF90C2E7)),
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(115.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (availableCount <= 0) "গেম শেষ" else currentProblem.displayQuestion,
                                color = if (availableCount <= 0) Color(0xFF5B7898) else Color(0xFF1E3A8A),
                                fontSize = if (availableCount <= 0) 34.sp else 32.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Small center light blue pill line
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(3.dp)
                            .background(Color(0xFF8DC2EB), RoundedCornerShape(2.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Answer Input Box: "Your Answer"
                    OutlinedTextField(
                        value = answerInput,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() || it == '-' } && input.length <= 6) {
                                answerInput = input
                            }
                        },
                        placeholder = {
                            Text(
                                text = "Your Answer",
                                color = Color(0xFF94A3B8),
                                fontSize = 16.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                            }
                        ),
                        enabled = availableCount > 0,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF67B7E8),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            disabledBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color(0xFFF8FAFC)
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(54.dp)
                            .testTag("math_game_answer_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Submit Answer Button matching Screenshot
            Button(
                onClick = {
                    if (availableCount <= 0) {
                        // When game over, prompt option to reload demo games
                        Toast.makeText(context, "আজকের গেম শেষ! আপনি আবার খেলতে পারেন।", Toast.LENGTH_SHORT).show()
                        repository.resetDailyMathGame(10)
                        currentProblem = generateProblem()
                        answerInput = ""
                        return@Button
                    }

                    val userIntAnswer = answerInput.trim().toIntOrNull()
                    if (userIntAnswer == null) {
                        Toast.makeText(context, "দয়া করে সঠিক সংখ্যা উত্তর লিখুন", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSubmitting = true
                    focusManager.clearFocus()
                    coroutineScope.launch {
                        val result = repository.submitMathGameAnswer(currentProblem, userIntAnswer, 2.0)
                        isSubmitting = false
                        if (result.isSuccess) {
                            lastWinReward = 2.0
                            showWinDialog = true
                            answerInput = ""
                            currentProblem = generateProblem()
                        } else {
                            val err = result.exceptionOrNull()?.message ?: "ভুল উত্তর! আবার চেষ্টা করুন"
                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (availableCount <= 0) Color(0xFF7B9DC9) else Color(0xFF0055D2)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .height(52.dp)
                    .testTag("math_game_submit_button")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "SUBMIT ANSWER",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Optional helper to reload 10 games when testing
            if (availableCount <= 0) {
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(
                    onClick = {
                        repository.resetDailyMathGame(10)
                        currentProblem = generateProblem()
                        answerInput = ""
                        Toast.makeText(context, "১০টি নতুন ম্যাথ গেম লোড করা হয়েছে!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color(0xFF0055D2),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "নতুন ১০টি গেম খেলুন (Play 10 Games)",
                        color = Color(0xFF0055D2),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    // -------------------------------------------------------------
    // WIN REWARD CELEBRATION MODAL
    // -------------------------------------------------------------
    if (showWinDialog) {
        AlertDialog(
            onDismissRequest = { showWinDialog = false },
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
                        text = "সঠিক উত্তর! অভিনন্দন 🎉",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A)
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "আপনি গণিত সমস্যার সঠিক সমাধান করেছেন এবং রিওয়ার্ড জিতেছেন!",
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
                                text = "+৳${String.format(java.util.Locale.US, "%.2f", lastWinReward)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "মোট ইনকাম: ৳${totalIncome.toInt()} | বাকি গেম: $availableCount টি",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showWinDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("পরবর্তী গেম 🎯")
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
                        text = "ম্যাথ গেম টিউটোরিয়াল",
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
                        text = "সহজ গণিত সমাধান করে প্রতিদিন আয়ের নিয়ম:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0055D2)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "১. স্ক্রিনে প্রদর্শিত সহজ যোগ, বিয়োগ বা গুণ হিসাবটি লক্ষ্য করুন।\n\n" +
                                "২. 'Your Answer' বক্সে আপনার সঠিক হিসাব করা সংখ্যাটি লিখুন।\n\n" +
                                "৩. এরপর 'SUBMIT ANSWER' বাটনে চাপ দিন।\n\n" +
                                "৪. উত্তর সঠিক হলে সাথে সাথে আপনার অ্যাকাউন্টে রিওয়ার্ড যুক্ত হবে।\n\n" +
                                "৫. প্রতিদিন নির্ধারিত সংখ্যক গেম খেলে নিশ্চিত ইনকাম করতে পারবেন।",
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
                        text = "ম্যাথ গেমের নীতিমালা ও নিয়ম",
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
                        text = "শর্তাবলী ও নিয়মাবলী:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0047BA)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "১. প্রতিদিন নির্দিষ্ট সংখ্যক ম্যাথ গেম খেলার সুযোগ পাবেন।\n\n" +
                                "২. ভুল উত্তর দিলে রিওয়ার্ড পাবেন না, তবে সঠিক উত্তর দিলে সাথে সাথেই ব্যালেন্সে টাকা যুক্ত হবে।\n\n" +
                                "৩. কোনো অটোমেশন স্ক্রিপ্ট বা অনৈতিক ক্লিকার ব্যবহার করা সম্পূর্ণ নিষিদ্ধ।\n\n" +
                                "৪. রাত ১২টার পর প্রতিদিনের গেম লিমিট স্বয়ংক্রিয়ভাবে রিনিউ হয়ে যাবে।",
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
    // INFO NOTICE DIALOG (TOP RIGHT SPEECH BUBBLE)
    // -------------------------------------------------------------
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
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
                        text = "ম্যাথ গেম তথ্য",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF0F172A)
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "ম্যাথ গেম হলো আপনার গণিত দক্ষতা কাজে লাগিয়ে প্রতিদিন সহজ উপায়ে নিশ্চিত আয়ের একটি চমৎকার ফিচার।",
                        fontSize = 13.sp,
                        color = Color(0xFF334155),
                        lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• প্রতি সঠিক সমাধানে: ৳২.০০\n• দৈনিক গেম লিমিট: ১০টি\n• রিসেট টাইম: প্রতিদিন রাত ১২:০০ টা",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E40AF),
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ঠিক আছে")
                }
            }
        )
    }
}
