package com.example.ui.screens.recharge

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MobileRechargeRecord
import com.example.data.model.SimType
import com.example.data.model.TelecomOperator
import com.example.data.repository.AppRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileRechargeScreen(
    repository: AppRepository,
    onNavigateBack: () -> Unit,
    onRechargeSuccess: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val currentUser by repository.currentUser.collectAsState(initial = null)
    val userBalance = currentUser?.balance ?: 1450.0

    // Form inputs
    var phoneNumber by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedOperator by remember { mutableStateOf(TelecomOperator.GRAMEENPHONE) }
    var selectedSimType by remember { mutableStateOf(SimType.PREPAID) }

    // Confirmation step dialog
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pinText by remember { mutableStateOf("") }
    var pinVisible by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successRecord by remember { mutableStateOf<MobileRechargeRecord?>(null) }

    // Auto-detect operator prefix
    LaunchedEffect(phoneNumber) {
        val clean = phoneNumber.replace("-", "").replace(" ", "").trim()
        if (clean.length >= 3) {
            when (clean.take(3)) {
                "017", "013" -> if (selectedOperator != TelecomOperator.SKITTO) selectedOperator = TelecomOperator.GRAMEENPHONE
                "018" -> selectedOperator = TelecomOperator.ROBI
                "019", "014" -> selectedOperator = TelecomOperator.BANGLALINK
                "016" -> selectedOperator = TelecomOperator.AIRTEL
                "015" -> selectedOperator = TelecomOperator.TELETALK
            }
        }
    }

    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0

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
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("btn_recharge_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "মোবাইল রিচার্জ",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Form Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_recharge_form"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // 1. মোবাইল নম্বর (Mobile Number)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "মোবাইল নম্বর",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { input ->
                                if (input.length <= 11 && input.all { it.isDigit() }) {
                                    phoneNumber = input
                                    errorMessage = null
                                }
                            },
                            placeholder = {
                                Text("মোবাইল নম্বর লিখুন (০১XXXXXXXXX)", color = Color(0xFF94A3B8), fontSize = 14.5.sp)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = Color(0xFF0055D2)
                                )
                            },
                            trailingIcon = {
                                if (phoneNumber.isNotEmpty()) {
                                    IconButton(onClick = { phoneNumber = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "মুছুন", tint = Color(0xFF94A3B8))
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_recharge_phone"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0055D2),
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            )
                        )
                    }

                    // 2. পরিমাণ (Amount)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "পরিমাণ",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { input ->
                                if (input.length <= 6 && input.all { it.isDigit() }) {
                                    amountText = input
                                    errorMessage = null
                                }
                            },
                            placeholder = {
                                Text("টাকার পরিমাণ লিখুন", color = Color(0xFF94A3B8), fontSize = 14.5.sp)
                            },
                            leadingIcon = {
                                Text(
                                    text = "৳",
                                    color = Color(0xFF0055D2),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                                )
                            },
                            trailingIcon = {
                                if (amountText.isNotEmpty()) {
                                    IconButton(onClick = { amountText = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "মুছুন", tint = Color(0xFF94A3B8))
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_recharge_amount"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0055D2),
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            )
                        )
                    }

                    // 3. অপারেটর নির্বাচন (Operator Selection)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "অপারেটর নির্বাচন",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )

                        val operators = TelecomOperator.values()
                        // 2 rows of 3 operators
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                operators.take(3).forEach { op ->
                                    OperatorItemView(
                                        operator = op,
                                        isSelected = selectedOperator == op,
                                        modifier = Modifier.weight(1f),
                                        onClick = { selectedOperator = op }
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                operators.drop(3).forEach { op ->
                                    OperatorItemView(
                                        operator = op,
                                        isSelected = selectedOperator == op,
                                        modifier = Modifier.weight(1f),
                                        onClick = { selectedOperator = op }
                                    )
                                }
                            }
                        }
                    }

                    // 4. সংযোগের ধরন (Connection Type)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "সংযোগের ধরন",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SimType.values().forEach { simType ->
                                val isSelected = selectedSimType == simType
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) Color(0xFF0055D2) else Color(0xFFF1F5F9),
                                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedSimType = simType }
                                        .testTag("sim_type_${simType.name.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedSimType = simType },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = Color.White,
                                                unselectedColor = Color(0xFF64748B)
                                            ),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = simType.bengaliName,
                                            color = if (isSelected) Color.White else Color(0xFF1E293B),
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Error Message
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFDC2626),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 5. পরবর্তী ধাপ বাটন (Next Step Button)
                    Button(
                        onClick = {
                            val cleanNumber = phoneNumber.replace("-", "").replace(" ", "").trim()
                            when {
                                cleanNumber.length != 11 || !cleanNumber.startsWith("01") -> {
                                    errorMessage = "সঠিক ১১ ডিজিটের মোবাইল নম্বর দিন (01XXXXXXXXX)"
                                }
                                parsedAmount < 10.0 -> {
                                    errorMessage = "সর্বনিম্ন রিচার্জের পরিমাণ ৳১০"
                                }
                                parsedAmount > 10000.0 -> {
                                    errorMessage = "সর্বোচ্চ রিচার্জের পরিমাণ ৳১০,০০০"
                                }
                                parsedAmount > userBalance -> {
                                    errorMessage = "পর্যাপ্ত ব্যালেন্স নেই! বর্তমান ব্যালেন্স ৳${String.format("%.2f", userBalance)}"
                                }
                                else -> {
                                    errorMessage = null
                                    pinText = ""
                                    showConfirmDialog = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_recharge_next"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2))
                    ) {
                        Text(
                            text = "পরবর্তী ধাপ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // Confirmation & PIN Dialog (Step 2)
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isProcessing) showConfirmDialog = false
            },
            title = {
                Text(
                    text = "রিচার্জ নিশ্চিতকরণ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1E293B)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Summary Details
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DetailRow("নম্বর:", phoneNumber)
                            DetailRow("অপারেটর:", selectedOperator.bengaliName)
                            DetailRow("ধরন:", selectedSimType.bengaliName)
                            DetailRow("পরিমাণ:", "৳${parsedAmount.toInt()}")
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "পিন নম্বর দিন",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )

                    OutlinedTextField(
                        value = pinText,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinText = it },
                        placeholder = { Text("৪ ডিজিট পিন লিখুন", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF0055D2)) },
                        trailingIcon = {
                            IconButton(onClick = { pinVisible = !pinVisible }) {
                                Icon(
                                    imageVector = if (pinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        },
                        visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinText.length < 4) {
                            Toast.makeText(context, "সঠিক ৪ ডিজিট পিন কোড দিন", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isProcessing = true
                        coroutineScope.launch {
                            val result = repository.submitMobileRecharge(
                                phoneNumber = phoneNumber,
                                operator = selectedOperator,
                                simType = selectedSimType,
                                amount = parsedAmount,
                                pin = pinText
                            )
                            isProcessing = false
                            if (result.isSuccess) {
                                showConfirmDialog = false
                                successRecord = result.getOrNull()
                                onRechargeSuccess()
                            } else {
                                Toast.makeText(
                                    context,
                                    result.exceptionOrNull()?.message ?: "রিচার্জ সম্পন্ন করা যায়নি",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    },
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2))
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("প্রসেসিং...", color = Color.White)
                    } else {
                        Text("রিচার্জ নিশ্চিত করুন", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = false },
                    enabled = !isProcessing
                ) {
                    Text("বাতিল", color = Color(0xFF64748B))
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    // Success Modal
    if (successRecord != null) {
        AlertDialog(
            onDismissRequest = {
                successRecord = null
                phoneNumber = ""
                amountText = ""
            },
            confirmButton = {
                Button(
                    onClick = {
                        successRecord = null
                        phoneNumber = ""
                        amountText = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2))
                ) {
                    Text("ঠিক আছে", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            title = {
                Text(
                    text = "রিচার্জ সফল হয়েছে!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF059669),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                val rec = successRecord!!
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(54.dp)
                    )
                    Text(
                        text = "আপনার ৳${rec.amount.toInt()} রিচার্জ সফলভাবে সম্পন্ন হয়েছে।",
                        fontSize = 14.sp,
                        color = Color(0xFF334155),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "TrxID: ${rec.transactionRef}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}

@Composable
private fun OperatorItemView(
    operator: TelecomOperator,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) Color(operator.brandColor).copy(alpha = 0.12f) else Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(operator.brandColor) else Color(0xFFCBD5E1)
        ),
        modifier = modifier
            .height(68.dp)
            .clickable { onClick() }
            .testTag("operator_${operator.name.lowercase()}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(operator.brandColor)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = operator.displayName.take(1),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = operator.bengaliName,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(operator.brandColor) else Color(0xFF1E293B)
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = Color(0xFF64748B))
        Text(text = value, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
    }
}
