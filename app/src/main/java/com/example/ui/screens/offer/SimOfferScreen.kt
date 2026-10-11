package com.example.ui.screens.offer

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimOfferScreen(
    repository: AppRepository,
    onNavigateBack: () -> Unit
) {
    val currentUser by repository.currentUser.collectAsState(initial = null)
    val driveOrders by repository.driveOrders.collectAsState(initial = emptyList())
    val allOffers = remember { repository.getAllDriveOffers() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val userBalance = currentUser?.balance ?: 1450.0

    // Filters
    var selectedOperator by remember { mutableStateOf<TelecomOperator?>(null) } // null = All
    var selectedDivision by remember { mutableStateOf("সকল বিভাগ") }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog & Sheet States
    var selectedOfferForPurchase by remember { mutableStateOf<DriveOfferItem?>(null) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var successOrder by remember { mutableStateOf<DriveOfferOrder?>(null) }

    // Division options
    val divisions = listOf(
        "সকল বিভাগ",
        "ঢাকা",
        "চট্টগ্রাম",
        "রাজশাহী",
        "খুলনা",
        "বরিশাল",
        "সিলেট",
        "রংপুর",
        "ময়মনসিংহ"
    )

    // Filtered offers list
    val filteredOffers = remember(selectedOperator, selectedDivision, searchQuery) {
        allOffers.filter { offer ->
            val matchOp = selectedOperator == null || offer.operator == selectedOperator
            val matchDiv = selectedDivision == "সকল বিভাগ" || offer.division == "অল বিডি" || offer.division.contains(selectedDivision)
            val matchSearch = searchQuery.isBlank() ||
                    offer.title.contains(searchQuery, ignoreCase = true) ||
                    offer.operator.displayName.contains(searchQuery, ignoreCase = true) ||
                    offer.operator.bengaliName.contains(searchQuery)
            matchOp && matchDiv && matchSearch
        }
    }

    Scaffold(
        containerColor = Color(0xFFF4F6F9),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "সিম অফার (ড্রাইভ প্যাক)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("sim_offer_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // History Icon
                    IconButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier.testTag("sim_offer_history_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (driveOrders.isNotEmpty()) {
                                    Badge(
                                        containerColor = Color(0xFF10B981),
                                        contentColor = Color.White
                                    ) {
                                        Text("${driveOrders.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = "ড্রাইভ হিস্ট্রি",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0055D2)
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. Balance & Notice Header Banner
            item {
                Surface(
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ওয়ালেট ব্যালেন্স",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "৳ ${String.format(Locale.US, "%.2f", userBalance)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0055D2)
                            )
                        }

                        Surface(
                            color = Color(0xFFECFDF5),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "ইনস্ট্যান্ট ক্যাশব্যাক",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Operator Filter Horizontal Row
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(top = 8.dp, bottom = 12.dp)
                ) {
                    Text(
                        text = "অপারেটর নির্বাচন করুন",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // "সকল" Tab
                        item {
                            OperatorFilterChip(
                                title = "সকল",
                                isSelected = selectedOperator == null,
                                brandColor = Color(0xFF0055D2),
                                onClick = { selectedOperator = null }
                            )
                        }

                        // Specific Operators
                        items(TelecomOperator.values().filter { it != TelecomOperator.SKITTO }) { op ->
                            OperatorFilterChip(
                                title = op.bengaliName,
                                isSelected = selectedOperator == op,
                                brandColor = Color(op.brandColor),
                                onClick = { selectedOperator = op }
                            )
                        }
                    }
                }
            }

            // 3. Division & Search Filter
            item {
                Surface(
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        // Division Dropdown Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "বিভাগ ফিল্টার:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF475569)
                                )
                            }

                            // Division chips scroll or selection
                            var expandedDivisionMenu by remember { mutableStateOf(false) }
                            Box {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF1F5F9),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                    modifier = Modifier.clickable { expandedDivisionMenu = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = selectedDivision,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = expandedDivisionMenu,
                                    onDismissRequest = { expandedDivisionMenu = false }
                                ) {
                                    divisions.forEach { div ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = div,
                                                    fontWeight = if (div == selectedDivision) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (div == selectedDivision) Color(0xFF0055D2) else Color.Black
                                                )
                                            },
                                            onClick = {
                                                selectedDivision = div
                                                expandedDivisionMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Search Input
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = "অফার বা ডাটা সার্চ করুন (যেমন: 40 GB, মিনিট)...",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("sim_offer_search_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0055D2),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color(0xFFFAFAFA),
                                unfocusedContainerColor = Color(0xFFFAFAFA)
                            )
                        )
                    }
                }
            }

            // 4. Section Count Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "মোট অফার পাওয়া গেছে: ${filteredOffers.size} টি",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )

                    if (selectedOperator != null || selectedDivision != "সকল বিভাগ" || searchQuery.isNotEmpty()) {
                        Text(
                            text = "রিসেট ফিল্টার",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFEF4444),
                            modifier = Modifier.clickable {
                                selectedOperator = null
                                selectedDivision = "সকল বিভাগ"
                                searchQuery = ""
                            }
                        )
                    }
                }
            }

            // 5. Offer Cards List (Matching Screenshot 1)
            if (filteredOffers.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "এই ফিল্টারে কোনো অফার পাওয়া যায়নি",
                                fontSize = 14.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            } else {
                items(filteredOffers, key = { it.id }) { offer ->
                    DriveOfferCard(
                        offer = offer,
                        onBuyClick = { selectedOfferForPurchase = offer }
                    )
                }
            }
        }
    }

    // Purchase Bottom Sheet (Matching Screenshot 2)
    if (selectedOfferForPurchase != null) {
        DriveOfferPurchaseBottomSheet(
            offer = selectedOfferForPurchase!!,
            userBalance = userBalance,
            onDismiss = { selectedOfferForPurchase = null },
            onConfirmOrder = { phone, simType, division, pin ->
                val offer = selectedOfferForPurchase!!
                coroutineScope.launch {
                    val result = repository.submitDriveOfferOrder(
                        offerTitle = offer.title,
                        operator = offer.operator,
                        phoneNumber = phone,
                        simType = simType,
                        division = division,
                        regularPrice = offer.regularPrice,
                        offerPrice = offer.offerPrice,
                        cashback = offer.cashback,
                        pin = pin
                    )
                    result.onSuccess { order ->
                        selectedOfferForPurchase = null
                        successOrder = order
                    }.onFailure { err ->
                        Toast.makeText(context, err.message ?: "আবেদন ব্যর্থ হয়েছে", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    // Success Confirmation Dialog
    if (successOrder != null) {
        val order = successOrder!!
        AlertDialog(
            onDismissRequest = { successOrder = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp),
            confirmButton = {
                Button(
                    onClick = { successOrder = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ঠিক আছে", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCFCE7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(38.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "ড্রাইভ অফার আবেদন সফল!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A),
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "আপনার আবেদনটি সফলভাবে গৃহীত হয়েছে। ৫-১৫ মিনিটের মধ্যে সিমটিতে অফারটি সক্রিয় হয়ে যাবে।",
                        fontSize = 13.sp,
                        color = Color(0xFF475569),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("প্যাকেজ:", fontSize = 12.sp, color = Color(0xFF64748B))
                                Text(order.offerTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("মোবাইল নম্বর:", fontSize = 12.sp, color = Color(0xFF64748B))
                                Text(order.phoneNumber, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("অপারেটর:", fontSize = 12.sp, color = Color(0xFF64748B))
                                Text("${order.operator.bengaliName} (${order.simType.bengaliName})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("ক্যাশব্যাক:", fontSize = 12.sp, color = Color(0xFF64748B))
                                Text("৳${String.format(Locale.US, "%.0f", order.cashback)} জমা হয়েছে", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("স্ট্যাটাস:", fontSize = 12.sp, color = Color(0xFF64748B))
                                Text(order.status, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                            }
                        }
                    }
                }
            }
        )
    }

    // Order History Bottom Sheet
    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "আমার ড্রাইভ অফার হিস্ট্রি",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    IconButton(onClick = { showHistorySheet = false }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                if (driveOrders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "এখনো কোনো ড্রাইভ অফার নেওয়া হয়নি",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(driveOrders) { order ->
                            DriveOrderHistoryCard(order = order)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OperatorFilterChip(
    title: String,
    isSelected: Boolean,
    brandColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) brandColor else Color(0xFFF1F5F9),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
        modifier = Modifier.testTag("filter_op_$title")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(brandColor)
                )
            }
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFF334155)
            )
        }
    }
}

@Composable
private fun DriveOfferCard(
    offer: DriveOfferItem,
    onBuyClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 5.dp)
            .testTag("offer_card_${offer.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Operator Badge, Division Tag, Validity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Operator Badge
                    Surface(
                        color = Color(offer.operator.brandColor),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = offer.operator.bengaliName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    // Division badge
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = offer.division,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                // Validity Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "মেয়াদ: ${offer.validity}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Offer Title
            Text(
                text = offer.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = offer.description,
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

            Spacer(modifier = Modifier.height(10.dp))

            // Price, Cashback & Action Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Pricing Section
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "রেগুলার: ৳${String.format(Locale.US, "%.0f", offer.regularPrice)}",
                            fontSize = 11.5.sp,
                            color = Color(0xFF94A3B8),
                            textDecoration = TextDecoration.LineThrough
                        )
                        Text(
                            text = "৳${String.format(Locale.US, "%.0f", offer.offerPrice)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0055D2)
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Green Cashback Badge
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "৳${String.format(Locale.US, "%.0f", offer.cashback)} ক্যাশব্যাক",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }

                // Buy Button
                Button(
                    onClick = onBuyClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("buy_button_${offer.id}")
                ) {
                    Text(
                        text = "কিনুন",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriveOfferPurchaseBottomSheet(
    offer: DriveOfferItem,
    userBalance: Double,
    onDismiss: () -> Unit,
    onConfirmOrder: (phoneNumber: String, simType: SimType, division: String, pin: String) -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var selectedSimType by remember { mutableStateOf(SimType.PREPAID) }
    var selectedDivision by remember { mutableStateOf(offer.division.ifBlank { "অল বিডি" }) }
    var pin by remember { mutableStateOf("") }

    val divisions = listOf(
        "সকল বিভাগ",
        "ঢাকা",
        "চট্টগ্রাম",
        "রাজশাহী",
        "খুলনা",
        "বরিশাল",
        "সিলেট",
        "রংপুর",
        "ময়মনসিংহ"
    )

    val netCost = offer.offerPrice - offer.cashback
    val isBalanceSufficient = userBalance >= offer.offerPrice

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 28.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ড্রাইভ অফার নিশ্চিতকরণ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF64748B)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

            Spacer(modifier = Modifier.height(12.dp))

            // Selected Offer Summary Card (Screenshot 2 style)
            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(offer.operator.brandColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = offer.operator.displayName.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = offer.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${offer.operator.bengaliName} • ${offer.validity}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "ক্যাশব্যাক ৳${String.format(Locale.US, "%.0f", offer.cashback)}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A)
                            )
                        }
                    }

                    Text(
                        text = "৳${String.format(Locale.US, "%.0f", offer.offerPrice)}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0055D2)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Mobile Number Input
            Text(
                text = "গ্রাহকের মোবাইল নম্বর",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { if (it.length <= 11) phoneNumber = it },
                placeholder = { Text("01XXXXXXXXX", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.PhoneIphone,
                        contentDescription = null,
                        tint = Color(0xFF0055D2),
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("purchase_phone_input"),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0055D2),
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. SIM Type (Prepaid / Postpaid)
            Text(
                text = "সংযোগের ধরন",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(SimType.PREPAID, SimType.POSTPAID).forEach { type ->
                    val isSelected = selectedSimType == type
                    Surface(
                        onClick = { selectedSimType = type },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFF0055D2) else Color(0xFFF1F5F9),
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = type.bengaliName,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF334155)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Division Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "বিভাগ",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155)
                )

                var expandedDiv by remember { mutableStateOf(false) }
                Box {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier.clickable { expandedDiv = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(selectedDivision, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                        }
                    }

                    DropdownMenu(expanded = expandedDiv, onDismissRequest = { expandedDiv = false }) {
                        divisions.forEach { div ->
                            DropdownMenuItem(
                                text = { Text(div, fontSize = 12.sp) },
                                onClick = {
                                    selectedDivision = div
                                    expandedDiv = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Secret PIN Input
            Text(
                text = "আপনার গোপন পিন",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 6) pin = it },
                placeholder = { Text("৪ ডিজিটের পিন নম্বর লিখুন", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF0055D2),
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("purchase_pin_input"),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0055D2),
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Wallet Deduct Summary
            Surface(
                color = Color(0xFFEFF6FF),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("বর্তমান ব্যালেন্স:", fontSize = 12.sp, color = Color(0xFF475569))
                        Text("৳${String.format(Locale.US, "%.2f", userBalance)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("অফার মূল্য কর্তন:", fontSize = 12.sp, color = Color(0xFF475569))
                        Text("৳${String.format(Locale.US, "%.0f", offer.offerPrice)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ক্যাশব্যাক জমা:", fontSize = 12.sp, color = Color(0xFF475569))
                        Text("+৳${String.format(Locale.US, "%.0f", offer.cashback)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            val isFormValid = phoneNumber.length == 11 && phoneNumber.startsWith("01") && pin.length >= 4 && isBalanceSufficient

            Button(
                onClick = { onConfirmOrder(phoneNumber, selectedSimType, selectedDivision, pin) },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0055D2),
                    disabledContainerColor = Color(0xFF94A3B8)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("confirm_drive_offer_button")
            ) {
                Text(
                    text = if (!isBalanceSufficient) "অপর্যাপ্ত ব্যালেন্স" else "অফারটি নিশ্চিত করুন",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun DriveOrderHistoryCard(order: DriveOfferOrder) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.US) }
    val formattedDate = remember(order.timestamp) { dateFormat.format(Date(order.timestamp)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = Color(order.operator.brandColor),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = order.operator.bengaliName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = order.phoneNumber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                Surface(
                    color = if (order.status == "সফল") Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = order.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (order.status == "সফল") Color(0xFF16A34A) else Color(0xFFD97706),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = order.offerTitle,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )

                Text(
                    text = "৳${String.format(Locale.US, "%.0f", order.offerPrice)} (ক্যাশব্যাক: ৳${String.format(Locale.US, "%.0f", order.cashback)})",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0055D2)
                )
            }
        }
    }
}
