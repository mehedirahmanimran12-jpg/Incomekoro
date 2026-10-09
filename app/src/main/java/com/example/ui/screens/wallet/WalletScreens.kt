package com.example.ui.screens.wallet

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentMethod
import com.example.data.model.Transaction
import com.example.data.model.User
import com.example.data.repository.AppRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun WalletScreen(
    repository: AppRepository,
    onNavigateToDeposit: () -> Unit,
    onNavigateToWithdraw: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onSelectTransaction: (String) -> Unit
) {
    val currentUser by repository.currentUser.collectAsState(initial = null)
    val transactions by repository.transactions.collectAsState(initial = emptyList())
    var showTransferDialog by remember { mutableStateOf(false) }

    val user = currentUser ?: User(balance = 0.0)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Digital Wallet",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(
                    onClick = onNavigateToTransactions,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("btn_history_top")
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = "History")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("screen_wallet"),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Balance Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF0F172A),
                                        Color(0xFF064E3B),
                                        Color(0xFF042F2E)
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column {
                            Text(
                                text = "Available Balance",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            AnimatedBalanceCounter(
                                targetBalance = user.balance,
                                prefix = "৳ "
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Pending In Review",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = "৳ ${String.format("%.2f", user.pendingBalance)}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFFBBF24)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Total Withdrawn",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = "৳ ${String.format("%.2f", user.totalWithdrawn)}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF38BDF8)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Actions
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionBigButton(
                        title = "Deposit",
                        icon = Icons.Default.AddCircle,
                        containerColor = Color(0xFF00E676),
                        contentColor = Color(0xFF00381B),
                        onClick = onNavigateToDeposit,
                        modifier = Modifier.weight(1f),
                        tag = "btn_wallet_deposit"
                    )
                    ActionBigButton(
                        title = "Withdraw",
                        icon = Icons.Default.ArrowOutward,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        onClick = onNavigateToWithdraw,
                        modifier = Modifier.weight(1f),
                        tag = "btn_wallet_withdraw"
                    )
                    ActionBigButton(
                        title = "Transfer",
                        icon = Icons.Default.SwapHoriz,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        onClick = { showTransferDialog = true },
                        modifier = Modifier.weight(1f),
                        tag = "btn_wallet_transfer"
                    )
                }
            }

            // Wallet Stats
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        WalletStatMini(label = "Total Earned", value = "৳ ${String.format("%.0f", user.totalEarned)}")
                        WalletStatMini(label = "Total Deposited", value = "৳ ${String.format("%.0f", user.totalDeposited)}")
                        WalletStatMini(label = "Completed Tasks", value = "${user.tasksCompleted}")
                    }
                }
            }

            // Transaction History Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transaction History",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onNavigateToTransactions() }
                    )
                }
            }

            if (transactions.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.AccountBalanceWallet,
                        title = "No Transactions",
                        description = "Start completing tasks or make a deposit to see your transactions."
                    )
                }
            } else {
                items(transactions.take(8)) { trx ->
                    TransactionRowItem(
                        transaction = trx,
                        onClick = { onSelectTransaction(trx.id) }
                    )
                }
            }
        }
    }

    if (showTransferDialog) {
        P2PTransferDialog(
            user = user,
            repository = repository,
            onDismiss = { showTransferDialog = false }
        )
    }
}

@Composable
fun DepositScreen(
    repository: AppRepository,
    onNavigateBack: () -> Unit,
    onDepositSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val settings by repository.appSettings.collectAsState(initial = com.example.data.model.AppSettings())
    var selectedMethod by remember { mutableStateOf(PaymentMethod.BKASH) }
    var amountText by remember { mutableStateOf("") }
    var senderNumber by remember { mutableStateOf("") }
    var transactionId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val adminNumber = when (selectedMethod) {
        PaymentMethod.BKASH -> settings.bkashNumber
        PaymentMethod.NAGAD -> settings.nagadNumber
        PaymentMethod.ROCKET -> settings.rocketNumber
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            IncomeKoroTopAppBar(
                title = "Deposit Funds",
                subtitle = "bKash / Nagad / Rocket",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            if (isSubmitted) {
                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Deposit Request Submitted",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF10B981)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Your deposit of ৳$amountText via ${selectedMethod.displayName} has been sent for verification. Balance will update once confirmed.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onDepositSuccess,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Back to Wallet")
                        }
                    }
                }
            } else {
                Text(
                    text = "1. Select Payment Method",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                BangladeshPaymentSelector(
                    selectedMethod = selectedMethod,
                    onMethodSelected = { selectedMethod = it }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Official Payment Instructions Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Official ${selectedMethod.displayName} Number:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(adminNumber))
                                    Toast.makeText(context, "Number copied!", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy")
                            }
                        }

                        Text(
                            text = adminNumber,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color(selectedMethod.hexColor)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "• Send Money / Cash-in to the number above.\n• Copy the Transaction ID (TrxID) after payment.\n• Fill in the form below and submit.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "2. Enter Deposit Details",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; errorMessage = null },
                    label = { Text("Deposit Amount (Min ৳${settings.minDeposit})") },
                    prefix = { Text("৳ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_deposit_amount")
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = senderNumber,
                    onValueChange = { senderNumber = it; errorMessage = null },
                    label = { Text("Sender Mobile Number (01xxxxxxxxx)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_deposit_sender")
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = transactionId,
                    onValueChange = { transactionId = it.uppercase(); errorMessage = null },
                    label = { Text("Transaction ID (TrxID)") },
                    leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_deposit_trxid")
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            val res = repository.submitDeposit(
                                amount = amount,
                                method = selectedMethod,
                                senderNumber = senderNumber,
                                transactionId = transactionId
                            )
                            isLoading = false
                            res.fold(
                                onSuccess = { isSubmitted = true },
                                onFailure = { errorMessage = it.message ?: "Deposit request failed." }
                            )
                        }
                    },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_confirm_deposit")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Confirm Deposit Request", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun WithdrawScreen(
    repository: AppRepository,
    onNavigateBack: () -> Unit,
    onWithdrawSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val currentUser by repository.currentUser.collectAsState(initial = null)
    val settings by repository.appSettings.collectAsState(initial = com.example.data.model.AppSettings())

    var selectedMethod by remember { mutableStateOf(PaymentMethod.BKASH) }
    var amountText by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var isSubmitted by remember { mutableStateOf(false) }

    val user = currentUser ?: User(balance = 0.0)
    val amount = amountText.toDoubleOrNull() ?: 0.0
    val fee = (amount * settings.withdrawFeePercent) / 100.0
    val netAmount = (amount - fee).coerceAtLeast(0.0)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            IncomeKoroTopAppBar(
                title = "Withdraw Funds",
                subtitle = "Available: ৳${String.format("%.2f", user.balance)}",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            if (isSubmitted) {
                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Withdrawal Submitted",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF10B981)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "৳${String.format("%.2f", netAmount)} will be sent to your ${selectedMethod.displayName} account ($accountNumber) within 1-12 hours.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onWithdrawSuccess,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Back to Wallet")
                        }
                    }
                }
            } else {
                Text(
                    text = "Select Payout Method",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                BangladeshPaymentSelector(
                    selectedMethod = selectedMethod,
                    onMethodSelected = { selectedMethod = it }
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; errorMessage = null },
                    label = { Text("Withdraw Amount (Min ৳${settings.minWithdrawal})") },
                    prefix = { Text("৳ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_withdraw_amount")
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it; errorMessage = null },
                    label = { Text("${selectedMethod.displayName} Personal Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_withdraw_account")
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Breakdown Calculation Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Withdraw Amount", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("৳ ${String.format("%.2f", amount)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Service Fee (${settings.withdrawFeePercent}%)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("- ৳ ${String.format("%.2f", fee)}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFEF4444))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("You will receive", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            Text("৳ ${String.format("%.2f", netAmount)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = Color(0xFF10B981))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = {
                        when {
                            amount < settings.minWithdrawal -> errorMessage = "Minimum withdrawal is ৳${settings.minWithdrawal}"
                            amount > user.balance -> errorMessage = "Insufficient available balance"
                            accountNumber.length < 11 -> errorMessage = "Enter valid 11-digit account number"
                            else -> showConfirmDialog = true
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_withdraw_proceed")
                ) {
                    Text("Proceed to Withdraw", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Withdrawal") },
            text = {
                Text("Are you sure you want to withdraw ৳$amount to your ${selectedMethod.displayName} account ($accountNumber)?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            val res = repository.submitWithdrawal(
                                amount = amount,
                                method = selectedMethod,
                                accountNumber = accountNumber
                            )
                            isLoading = false
                            res.fold(
                                onSuccess = { isSubmitted = true },
                                onFailure = { errorMessage = it.message ?: "Withdrawal failed." }
                            )
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TransactionsScreen(
    repository: AppRepository,
    onNavigateBack: () -> Unit,
    onSelectTransaction: (String) -> Unit
) {
    val transactions by repository.transactions.collectAsState(initial = emptyList())
    var filterType by remember { mutableStateOf<String>("All") }

    val filteredList = remember(transactions, filterType) {
        if (filterType == "All") transactions else transactions.filter { it.type.displayName.contains(filterType, ignoreCase = true) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            IncomeKoroTopAppBar(
                title = "All Transactions",
                subtitle = "${transactions.size} records",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Task", "Referral", "Deposit", "Withdrawal").forEach { filter ->
                    FilterChip(
                        selected = filterType == filter,
                        onClick = { filterType = filter },
                        label = { Text(filter) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            if (filteredList.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.ReceiptLong,
                    title = "No Transactions Found",
                    description = "No transaction records matching this filter."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filteredList, key = { it.id }) { trx ->
                        TransactionRowItem(
                            transaction = trx,
                            onClick = { onSelectTransaction(trx.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionDetailScreen(
    trxId: String,
    repository: AppRepository,
    onNavigateBack: () -> Unit
) {
    val transactions by repository.transactions.collectAsState(initial = emptyList())
    val trx = transactions.find { it.id == trxId }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            IncomeKoroTopAppBar(
                title = "Transaction Details",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        if (trx == null) {
            EmptyStateView(
                icon = Icons.Default.ErrorOutline,
                title = "Transaction Not Found",
                description = "Could not locate transaction record.",
                actionButtonText = "Back",
                onActionClick = onNavigateBack,
                modifier = Modifier.padding(padding)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        StatusBadge(status = trx.status.displayName)

                        Spacer(modifier = Modifier.height(14.dp))

                        val isPositive = trx.amount > 0
                        Text(
                            text = "${if (isPositive) "+" else ""}৳ ${String.format("%.2f", trx.amount)}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = trx.type.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(20.dp))

                        TrxDetailRow("Transaction ID", trx.id)
                        TrxDetailRow("Reference", trx.transactionRef ?: trx.id)
                        TrxDetailRow("Date & Time", formatTimestamp(trx.date))
                        TrxDetailRow("Method", trx.paymentMethod ?: "In-App Wallet")
                        TrxDetailRow("Description", trx.description)

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(trx.id))
                                Toast.makeText(context, "Transaction ID copied!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("btn_copy_trxid")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Copy Transaction ID")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrxDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ActionBigButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(tag),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = contentColor)
        }
    }
}

@Composable
private fun WalletStatMini(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun P2PTransferDialog(
    user: User,
    repository: AppRepository,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var targetPhone by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transfer Balance (P2P)") },
        text = {
            Column {
                Text(
                    text = "Transfer balance instantly to another registered user using their mobile number. Available: ৳${String.format("%.2f", user.balance)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                if (error != null) {
                    Text(error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = targetPhone,
                    onValueChange = { targetPhone = it },
                    label = { Text("Recipient Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_transfer_phone")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_transfer_amount")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (targetPhone.length < 11) {
                        error = "Enter valid recipient phone number"
                    } else if (amount <= 0 || amount > user.balance) {
                        error = "Enter valid amount within your balance"
                    } else {
                        isLoading = true
                        scope.launch {
                            val res = repository.transferBalance(targetPhone, amount)
                            isLoading = false
                            res.fold(
                                onSuccess = { onDismiss() },
                                onFailure = { error = it.message ?: "Transfer failed" }
                            )
                        }
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Send Balance")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
