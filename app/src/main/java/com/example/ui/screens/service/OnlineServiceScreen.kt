package com.example.ui.screens.service

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.data.model.OnlineServiceItem
import com.example.data.model.OnlineServiceOrder
import com.example.data.repository.AppRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineServiceScreen(
    repository: AppRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentUser by repository.currentUser.collectAsState(initial = null)
    val serviceOrders by repository.onlineServiceOrders.collectAsState(initial = emptyList())
    val services = remember { repository.getOnlineServices() }
    val userBalance = currentUser?.balance ?: 1450.0

    // Dialog & Sheet States
    var showTutorialDialog by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var serviceToOrder by remember { mutableStateOf<OnlineServiceItem?>(null) }
    var successOrder by remember { mutableStateOf<OnlineServiceOrder?>(null) }
    var isProcessingOrder by remember { mutableStateOf(false) }

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
                        modifier = Modifier.testTag("online_service_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Online Service",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )

                    // Right History Icon matching Screenshot
                    IconButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier.testTag("online_service_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Order History",
                            tint = Color.White
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
        ) {
            // Top Action Buttons Row: টিউটোরিয়াল (Red) & নিয়ম (Blue)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // টিউটোরিয়াল Button (Red)
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
                        .testTag("service_tutorial_button")
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
                        .testTag("service_rules_button")
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

            // Scrollable Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Hero Banner matching Screenshot exactly
                item {
                    OnlineServiceHeroBanner()
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Digital Products List matching Screenshot
                items(services, key = { it.id }) { serviceItem ->
                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                        OnlineServiceCard(
                            service = serviceItem,
                            onOrderClick = { serviceToOrder = serviceItem }
                        )
                    }
                }
            }
        }
    }

    // --- Order Confirmation Dialog ---
    if (serviceToOrder != null) {
        val item = serviceToOrder!!
        val potentialProfit = item.resellingPrice - item.price

        AlertDialog(
            onDismissRequest = {
                if (!isProcessingOrder) serviceToOrder = null
            },
            title = {
                Text(
                    text = "ডিজিটাল সার্ভিস অর্ডার",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF0055D2)
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = item.title,
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
                                Text("সার্ভিস ক্রয় মূল্য:", fontSize = 13.sp, color = Color(0xFF64748B))
                                Text("৳${item.price.toInt()}.00", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0055D2))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("প্রস্তাবিত রিসেল মূল্য:", fontSize = 12.sp, color = Color(0xFF64748B))
                                Text("৳${item.resellingPrice.toInt()}.00", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF059669))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("সম্ভাব্য রিসেল লাভ:", fontSize = 12.sp, color = Color(0xFF047857), fontWeight = FontWeight.Bold)
                                Text("+৳${potentialProfit.toInt()}.00", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("বর্তমান ওয়ালেট ব্যালেন্স:", fontSize = 12.sp, color = Color(0xFF64748B))
                                Text("৳${userBalance.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• অর্ডার কনফার্ম করার সাথে সাথে ডাউনলোড লিংক ও সেটআপ নির্দেশনা পেয়ে যাবেন।",
                        fontSize = 11.sp,
                        color = Color(0xFF475569)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isProcessingOrder = true
                        coroutineScope.launch {
                            val result = repository.purchaseOnlineService(item)
                            isProcessingOrder = false
                            if (result.isSuccess) {
                                serviceToOrder = null
                                successOrder = result.getOrNull()
                            } else {
                                Toast.makeText(
                                    context,
                                    result.exceptionOrNull()?.message ?: "অর্ডার সম্পন্ন হতে ত্রুটি হয়েছে",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    },
                    enabled = !isProcessingOrder && userBalance >= item.price,
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
                    onClick = { serviceToOrder = null },
                    enabled = !isProcessingOrder
                ) {
                    Text("বাতিল")
                }
            }
        )
    }

    // --- Order Success Dialog ---
    if (successOrder != null) {
        val order = successOrder!!
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
                        text = "ডিজিটাল প্রোডাক্টটি সফলভাবে ক্রয় সম্পন্ন হয়েছে!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = order.serviceTitle,
                        fontSize = 13.sp,
                        color = Color(0xFF0055D2),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("অর্ডার আইডি: #${order.id}", fontSize = 11.sp, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ডাউনলোড লিংক:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = order.downloadUrl,
                                fontSize = 11.sp,
                                color = Color(0xFF0284C7),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Download Link", order.downloadUrl)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "ডাউনলোড লিংক কপি করা হয়েছে! 📋", Toast.LENGTH_SHORT).show()
                        successOrder = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("লিংক কপি করুন")
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
                        text = "অনলাইন সার্ভিস টিউটোরিয়াল",
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
                        text = "রোডভা ডিজিটাল সার্ভিস কীভাবে ব্যবহার করবেন:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0055D2)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "১. ডিজিটাল প্রোডাক্ট ক্রয়:\nপছন্দের আনলকড প্রিমিয়াম অ্যাপস বা টুলের নিচে 'অর্ডার করুন' বাটনে ক্লিক করে ওয়ালেট ব্যালেন্স থেকে ৩০ টাকায় সংগ্রহ করুন।\n\n" +
                                "২. রিসেলিং সুবিধা:\nপ্রতিটি প্রোডাক্ট নিজের ব্যবহারের পাশাপাশি ফেসবুক ও হোয়াটসঅ্যাপে ৫০ টাকায় রিসেল করে প্রতি অর্ডারে ২০ টাকা নিট লাভ করতে পারেন।\n\n" +
                                "৩. ইনস্টল ও সেটআপ:\nঅর্ডার সম্পন্ন হলে তৎক্ষণাৎ ইনস্টলেশন ফাইল ও লাইসেন্স কি ডাউনলোডের লিংক পেয়ে যাবেন।",
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
                        text = "অনলাইন সার্ভিস নিয়মাবলী",
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
                        text = "সার্ভিসের শর্ত ও নীতিমালা:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0047BA)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "১. প্রতিটি ডিজিটাল প্রোডাক্ট ১০০% টেস্টেড এবং ম্যালওয়্যারমুক্ত আনলকড ভার্সন।\n\n" +
                                "২. অ্যাপ আপডেটের নোটিফিকেশন আমাদের অফিশিয়াল চ্যানেলে সরবরাহ করা হবে।\n\n" +
                                "৩. যেকোনো টেকনিক্যাল সমস্যায় ২৪ ঘণ্টার মধ্যে রিপ্লেসমেন্ট সাপোর্ট প্রদান করা হয়।\n\n" +
                                "৪. রিসেলিং করার সময় নির্ধারিত সর্বোচ্চ মূল্যের বেশিতে বিক্রি না করার অনুরোধ রইল।",
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

    // --- History Bottom Sheet (আমার অর্ডারসমূহ) ---
    if (showHistorySheet) {
        val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "অনলাইন সার্ভিস অর্ডার হিস্ট্রি (${serviceOrders.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0055D2)
                    )
                    IconButton(onClick = { showHistorySheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (serviceOrders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("কোনো অর্ডার রেকর্ড নেই", color = Color(0xFF64748B))
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                    ) {
                        items(serviceOrders, key = { it.id }) { ord ->
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "#${ord.id}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0055D2)
                                        )
                                        Surface(
                                            color = Color(0xFFECFDF5),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = ord.status,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF059669),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ord.serviceTitle,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "মূল্য: ৳${ord.price.toInt()}.00",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF475569)
                                        )
                                        Text(
                                            text = dateFormat.format(Date(ord.timestamp)),
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Download Link", ord.downloadUrl)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "ডাউনলোড লিংক কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(32.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ডাউনলোড লিংক কপি করুন", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
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
fun OnlineServiceHeroBanner() {
    Surface(
        color = Color(0xFF0038FF),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Devices Graphic / Hero Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background image if available
                Image(
                    painter = painterResource(id = R.drawable.img_online_service_hero),
                    contentDescription = "Online Service Hero Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                )

                // ROOTVA Circle Logo in Top-Right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0055D2))
                        .border(1.5.dp, Color(0xFFFACC15), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "R",
                        color = Color(0xFFFACC15),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navy Floating Tag Bar matching Screenshot
            Surface(
                color = Color(0xFF0A1128),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
            ) {
                Text(
                    text = "ডিজিটাল প্রোডাক্ট রিসেল করুন ও নিজের কাজে ব্যবহার করুন – রোডভা অ্যাপে",
                    color = Color(0xFFFCD34D),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// SERVICE CARD MATCHING SCREENSHOT EXACTLY
// -------------------------------------------------------------
@Composable
fun OnlineServiceCard(
    service: OnlineServiceItem,
    onOrderClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("service_card_${service.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Image Area with Loading Circle Arc and "New" badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                // Circular Cyan Blue Arc and "LOADING" Text matching Screenshot
                Canvas(modifier = Modifier.size(130.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val arcSize = size.width - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    // Background soft track
                    drawArc(
                        color = Color(0xFFF1F5F9),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth)
                    )

                    // Cyan Blue Progress Arc matching Screenshot
                    drawArc(
                        color = Color(0xFF38BDF8),
                        startAngle = -90f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Center Text: "LOADING"
                Text(
                    text = "LOADING",
                    color = Color(0xFF0284C7),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                // "New" Red Badge (Top-Right) matching Screenshot
                if (service.isNew) {
                    Surface(
                        color = Color(0xFFE60000),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "New",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Bottom Info Container (Light gray-blue tinted)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEEF2F8))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Title
                Text(
                    text = service.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Price and Reselling Price Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = service.priceDisplay,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = service.resellingPriceDisplay,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Full-width Action Button: অর্ডার করুন
                Button(
                    onClick = onOrderClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0055D2)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("service_order_btn_${service.id}")
                ) {
                    Text(
                        text = "অর্ডার করুন",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
