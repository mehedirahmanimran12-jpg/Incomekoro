package com.example.ui.screens.shop

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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

enum class ShopSortOption(val title: String) {
    POPULAR("জনপ্রিয়"),
    HIGH_PROFIT("বেশি লাভ"),
    NEWEST("নতুন পণ্য"),
    PRICE_LOW("কম দাম")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResellingShopScreen(
    repository: AppRepository,
    onNavigateBack: () -> Unit,
    isBottomTab: Boolean = false
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentUser by repository.currentUser.collectAsState(initial = null)
    val resellingOrders by repository.resellingOrders.collectAsState(initial = emptyList())
    val cartItems by repository.cartItems.collectAsState(initial = emptyList())
    val favoriteIds by repository.favoriteProductIds.collectAsState(initial = emptySet())
    val allProducts = remember { repository.getAllResellingProducts() }

    val userBalance = currentUser?.balance ?: 1450.0

    // Filters and Search
    var selectedCategory by remember { mutableStateOf(ResellingCategory.ALL) }
    var selectedSort by remember { mutableStateOf(ShopSortOption.POPULAR) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    // Dialog & Sheet States
    var productForDetails by remember { mutableStateOf<ResellingProduct?>(null) }
    var productForOrder by remember { mutableStateOf<ResellingProduct?>(null) }
    var showCartSheet by remember { mutableStateOf(false) }
    var showOrdersSheet by remember { mutableStateOf(false) }
    var showFavoritesSheet by remember { mutableStateOf(false) }
    var successOrder by remember { mutableStateOf<ResellingOrder?>(null) }

    // Filter and sort products
    val filteredProducts = remember(selectedCategory, selectedSort, searchQuery, favoriteIds) {
        var list = allProducts.filter { product ->
            val matchCat = selectedCategory == ResellingCategory.ALL || product.category == selectedCategory
            val matchSearch = searchQuery.isBlank() ||
                    product.title.contains(searchQuery, ignoreCase = true) ||
                    product.productCode.contains(searchQuery, ignoreCase = true) ||
                    product.description.contains(searchQuery, ignoreCase = true)
            matchCat && matchSearch
        }

        when (selectedSort) {
            ShopSortOption.POPULAR -> list = list.sortedByDescending { it.rating }
            ShopSortOption.HIGH_PROFIT -> list = list.sortedByDescending { it.suggestedProfit }
            ShopSortOption.NEWEST -> list = list.reversed()
            ShopSortOption.PRICE_LOW -> list = list.sortedBy { it.wholesalePrice }
        }
        list
    }

    val totalCartCount = cartItems.sumOf { it.quantity }

    Scaffold(
        containerColor = Color(0xFFF1F5F9),
        topBar = {
            Surface(
                color = Color(0xFF0055D2),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("reselling_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = "রিসেলিং শপ",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )

                        // Search Toggle Icon
                        IconButton(
                            onClick = { isSearchExpanded = !isSearchExpanded },
                            modifier = Modifier.testTag("reselling_search_toggle")
                        ) {
                            Icon(
                                imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White
                            )
                        }

                        // Favorites Icon
                        IconButton(
                            onClick = { showFavoritesSheet = true },
                            modifier = Modifier.testTag("reselling_favorites_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (favoriteIds.isNotEmpty()) {
                                        Badge(
                                            containerColor = Color(0xFFEF4444),
                                            contentColor = Color.White
                                        ) {
                                            Text("${favoriteIds.size}", fontSize = 10.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorites",
                                    tint = Color.White
                                )
                            }
                        }

                        // Shopping Cart Icon
                        IconButton(
                            onClick = { showCartSheet = true },
                            modifier = Modifier.testTag("reselling_cart_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (totalCartCount > 0) {
                                        Badge(
                                            containerColor = Color(0xFF10B981),
                                            contentColor = Color.White
                                        ) {
                                            Text("$totalCartCount", fontSize = 10.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Cart",
                                    tint = Color.White
                                )
                            }
                        }

                        // Orders History Icon
                        IconButton(
                            onClick = { showOrdersSheet = true },
                            modifier = Modifier.testTag("reselling_orders_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (resellingOrders.isNotEmpty()) {
                                        Badge(
                                            containerColor = Color(0xFFF59E0B),
                                            contentColor = Color.White
                                        ) {
                                            Text("${resellingOrders.size}", fontSize = 10.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = "My Orders",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Expandable Search Bar
                    AnimatedVisibility(visible = isSearchExpanded) {
                        Surface(
                            color = Color(0xFF0047BA),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = {
                                        Text(
                                            "পণ্য বা কোড খুঁজুন (যেমন: পাঞ্জাবি, TWS)...",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 13.sp
                                        )
                                    },
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("reselling_search_field")
                                )
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
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
            // Category Filter Chips (Horizontal)
            Surface(
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ResellingCategory.values()) { category ->
                            val isSelected = selectedCategory == category
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) Color(0xFF0055D2) else Color(0xFFF1F5F9),
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .clickable { selectedCategory = category }
                                    .testTag("cat_chip_${category.name}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = category.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xFF334155)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Sort Chips & Counter
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "মোট ${filteredProducts.size} টি পণ্য",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ShopSortOption.values().forEach { sortOpt ->
                                val isSelected = selectedSort == sortOpt
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) Color(0xFFE0E7FF) else Color.Transparent)
                                        .clickable { selectedSort = sortOpt }
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = sortOpt.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF0055D2) else Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Products Grid
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "কোনো পণ্য খুঁজে পাওয়া যায়নি",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                        Text(
                            text = "অন্য কোনো ক্যাটাগরি বা নাম লিখে সার্চ করুন",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                selectedCategory = ResellingCategory.ALL
                                searchQuery = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("সকল পণ্য দেখুন")
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("reselling_products_grid")
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        val isFavorite = favoriteIds.contains(product.id)
                        ResellingProductCard(
                            product = product,
                            isFavorite = isFavorite,
                            onToggleFavorite = { repository.toggleFavoriteProduct(product.id) },
                            onViewDetails = { productForDetails = product },
                            onOrderNow = { productForOrder = product }
                        )
                    }
                }
            }
        }
    }

    // --- Product Details Bottom Sheet ---
    if (productForDetails != null) {
        val product = productForDetails!!
        ResellingProductDetailsSheet(
            product = product,
            isFavorite = favoriteIds.contains(product.id),
            onToggleFavorite = { repository.toggleFavoriteProduct(product.id) },
            onDismiss = { productForDetails = null },
            onAddToCart = { qty ->
                repository.addToCart(product, qty)
                Toast.makeText(context, "${product.title} কার্টে যোগ হয়েছে! 🛒", Toast.LENGTH_SHORT).show()
                productForDetails = null
            },
            onOrderNow = {
                val p = product
                productForDetails = null
                productForOrder = p
            }
        )
    }

    // --- Reselling Order Placement Sheet ---
    if (productForOrder != null) {
        val product = productForOrder!!
        ResellingOrderSheet(
            product = product,
            userBalance = userBalance,
            onDismiss = { productForOrder = null },
            onSubmitOrder = { qty, sellingPrice, name, phone, address, district, isDhaka, pin ->
                coroutineScope.launch {
                    val result = repository.submitResellingOrder(
                        product = product,
                        quantity = qty,
                        customerSellingPrice = sellingPrice,
                        customerName = name,
                        customerPhone = phone,
                        deliveryAddress = address,
                        district = district,
                        isInsideDhaka = isDhaka,
                        pin = pin
                    )
                    if (result.isSuccess) {
                        productForOrder = null
                        successOrder = result.getOrNull()
                    } else {
                        Toast.makeText(
                            context,
                            result.exceptionOrNull()?.message ?: "অর্ডার সম্পন্ন হতে ত্রুটি হয়েছে",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        )
    }

    // --- Order Success Dialog ---
    if (successOrder != null) {
        ResellingOrderSuccessDialog(
            order = successOrder!!,
            onDismiss = { successOrder = null },
            onViewOrders = {
                successOrder = null
                showOrdersSheet = true
            }
        )
    }

    // --- Shopping Cart Sheet ---
    if (showCartSheet) {
        ResellingCartSheet(
            cartItems = cartItems,
            onDismiss = { showCartSheet = false },
            onUpdateQty = { id, qty -> repository.updateCartQuantity(id, qty) },
            onRemove = { id -> repository.removeFromCart(id) },
            onCheckoutItem = { item ->
                showCartSheet = false
                productForOrder = item.product
            }
        )
    }

    // --- My Orders Sheet ---
    if (showOrdersSheet) {
        ResellingOrdersHistorySheet(
            orders = resellingOrders,
            onDismiss = { showOrdersSheet = false }
        )
    }

    // --- Favorites / Wishlist Sheet ---
    if (showFavoritesSheet) {
        val favoriteProducts = allProducts.filter { favoriteIds.contains(it.id) }
        ResellingFavoritesSheet(
            favoriteProducts = favoriteProducts,
            onDismiss = { showFavoritesSheet = false },
            onRemoveFavorite = { id -> repository.toggleFavoriteProduct(id) },
            onOrderProduct = { p ->
                showFavoritesSheet = false
                productForOrder = p
            }
        )
    }
}

// -------------------------------------------------------------
// PRODUCT CARD
// -------------------------------------------------------------
@Composable
fun ResellingProductCard(
    product: ResellingProduct,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onViewDetails: () -> Unit,
    onOrderNow: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() }
            .testTag("product_card_${product.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Image / Product Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                when (product.category) {
                                    ResellingCategory.MENS_FASHION -> Color(0xFF1E3A8A)
                                    ResellingCategory.WOMENS_FASHION -> Color(0xFFBE185D)
                                    ResellingCategory.ELECTRONICS -> Color(0xFF0F766E)
                                    ResellingCategory.WATCHES -> Color(0xFF4C1D95)
                                    ResellingCategory.BEAUTY -> Color(0xFFDB2777)
                                    ResellingCategory.HOME_LIVING -> Color(0xFFB45309)
                                    ResellingCategory.FOOTWEAR -> Color(0xFF334155)
                                    else -> Color(0xFF0055D2)
                                },
                                Color(0xFF0F172A)
                            )
                        )
                    )
            ) {
                // Central Icon / Graphic
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = when (product.category) {
                            ResellingCategory.MENS_FASHION -> Icons.Default.Checkroom
                            ResellingCategory.WOMENS_FASHION -> Icons.Default.DryCleaning
                            ResellingCategory.ELECTRONICS -> Icons.Default.Headphones
                            ResellingCategory.WATCHES -> Icons.Default.Watch
                            ResellingCategory.BEAUTY -> Icons.Default.Spa
                            ResellingCategory.HOME_LIVING -> Icons.Default.Blender
                            ResellingCategory.FOOTWEAR -> Icons.Default.ShoppingBag
                            else -> Icons.Default.Storefront
                        },
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(46.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = product.productCode,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Profit Badge (Top Left)
                Surface(
                    color = Color(0xFF10B981),
                    shape = RoundedCornerShape(bottomEnd = 10.dp, topStart = 12.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "লাভ ৳${product.suggestedProfit.toInt()}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Favorite Heart Button (Top Right)
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(32.dp)
                        .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFEF4444) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Tag / Stock badge (Bottom Left)
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = product.tag,
                        color = Color(0xFFFCD34D),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            // Product Details Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = product.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                    modifier = Modifier.height(34.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Price Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "পাইকারি মূল্য:",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "৳${product.wholesalePrice.toInt()}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0055D2)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "সর্বোচ্চ বিক্রয়:",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "৳${product.maxRetailPrice.toInt()}",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Profit Box
                Surface(
                    color = Color(0xFFECFDF5),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(0.6.dp, Color(0xFFA7F3D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "আপনার সম্ভাব্য লাভ:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF047857)
                        )
                        Text(
                            text = "+৳${product.suggestedProfit.toInt()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF059669)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedButton(
                        onClick = onViewDetails,
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Text(
                            text = "বিস্তারিত",
                            fontSize = 11.sp,
                            color = Color(0xFF334155),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = onOrderNow,
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .testTag("order_btn_${product.id}"),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2))
                    ) {
                        Text(
                            text = "অর্ডার",
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PRODUCT DETAILS SHEET
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResellingProductDetailsSheet(
    product: ResellingProduct,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onDismiss: () -> Unit,
    onAddToCart: (Int) -> Unit,
    onOrderNow: () -> Unit
) {
    val context = LocalContext.current
    var quantity by remember { mutableIntStateOf(1) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // Header Image Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0055D2), Color(0xFF0F172A))
                        )
                    )
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.productCode,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Profit Badge
                Surface(
                    color = Color(0xFF10B981),
                    shape = RoundedCornerShape(bottomEnd = 12.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "সম্ভাব্য লাভ ৳${product.suggestedProfit.toInt()}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Favorite
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFEF4444) else Color.White
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Title and Code
                Text(
                    text = product.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = product.category.displayName,
                            fontSize = 11.sp,
                            color = Color(0xFF1D4ED8),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "⭐ ${product.rating} (${product.reviewCount} রিভিউ)",
                        fontSize = 11.sp,
                        color = Color(0xFFF59E0B),
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "• স্টকে আছে (${product.stockQuantity} টি)",
                        fontSize = 11.sp,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Price Breakdown Card
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("পাইকারি মূল্য", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("৳${product.wholesalePrice.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0055D2))
                        }
                        VerticalDivider(modifier = Modifier.height(30.dp), color = Color(0xFFCBD5E1))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("সর্বোচ্চ বিক্রয়", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("৳${product.maxRetailPrice.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                        }
                        VerticalDivider(modifier = Modifier.height(30.dp), color = Color(0xFFCBD5E1))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("আপনার লাভ", fontSize = 11.sp, color = Color(0xFF047857), fontWeight = FontWeight.Bold)
                            Text("+৳${product.suggestedProfit.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Delivery Charge Info
                Surface(
                    color = Color(0xFFFFFBEB),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ডেলিভারি চার্জ: ঢাকা সিটিতে ৳${product.deliveryChargeInsideDhaka.toInt()} | সারা দেশে ৳${product.deliveryChargeOutsideDhaka.toInt()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF92400E)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Short Specs
                Text(
                    text = "পণ্যের বৈশিষ্ট্য:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                product.shortSpecs.forEach { spec ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF0055D2),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = spec, fontSize = 12.sp, color = Color(0xFF475569))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Description with Copy Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "পণ্যের বিবরণ:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Product Details", "${product.title}\n${product.description}\nমূল্য: ৳${product.maxRetailPrice.toInt()}\nঅর্ডার করতে ইনবক্স করুন।")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "ফেসবুক বা সোশ্যালে পোস্ট করার জন্য বিবরণ কপি হয়েছে! 📋", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("বিবরণ কপি করুন", fontSize = 11.sp)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quantity Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("অর্ডার পরিমাণ:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                        ) {
                            Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "$quantity",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        IconButton(
                            onClick = { if (quantity < product.stockQuantity) quantity++ },
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                        ) {
                            Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onAddToCart(quantity) },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF0055D2))
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = Color(0xFF0055D2), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("কার্টে রাখুন", color = Color(0xFF0055D2), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onOrderNow,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("order_sheet_order_now"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2))
                    ) {
                        Text("এখনই অর্ডার", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// RESELLING ORDER FORM SHEET
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResellingOrderSheet(
    product: ResellingProduct,
    userBalance: Double,
    onDismiss: () -> Unit,
    onSubmitOrder: (
        quantity: Int,
        sellingPrice: Double,
        name: String,
        phone: String,
        address: String,
        district: String,
        isInsideDhaka: Boolean,
        pin: String
    ) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    var customSellingPriceText by remember { mutableStateOf(product.maxRetailPrice.toInt().toString()) }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    var selectedDistrict by remember { mutableStateOf("ঢাকা") }
    var isInsideDhaka by remember { mutableStateOf(true) }
    var pinText by remember { mutableStateOf("") }

    val parsedSellingPrice = customSellingPriceText.toDoubleOrNull() ?: product.maxRetailPrice
    val deliveryFee = if (isInsideDhaka) product.deliveryChargeInsideDhaka else product.deliveryChargeOutsideDhaka
    val totalCustomerPayable = (parsedSellingPrice * quantity) + deliveryFee
    val resellerProfit = (parsedSellingPrice - product.wholesalePrice) * quantity

    val districts = listOf("ঢাকা", "চট্টগ্রাম", "রাজশাহী", "খুলনা", "বরিশাল", "সিলেট", "রংপুর", "ময়মনসিংহ", "কুমিল্লা", "গাজীপুর", "নারায়ণগঞ্জ")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "রিসেলিং অর্ডার ফর্ম",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0055D2)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Product Summary Card
            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFF0055D2), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "কোড: ${product.productCode} • পাইকারি: ৳${product.wholesalePrice.toInt()}",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pricing Configuration
            Text("১. বিক্রয় মূল্য ও লাভ নির্ধারণ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customSellingPriceText,
                    onValueChange = { customSellingPriceText = it },
                    label = { Text("গ্রাহকের বিক্রয় মূল্য (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reselling_custom_price_input")
                )

                // Quantity Picker
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("পরিমাণ", fontSize = 11.sp, color = Color(0xFF64748B))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Text("-", fontWeight = FontWeight.Bold)
                        }
                        Text("$quantity", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp))
                        IconButton(
                            onClick = { if (quantity < product.stockQuantity) quantity++ },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Text("+", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Realtime Profit Notice
            Surface(
                color = if (resellerProfit >= 0) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (resellerProfit >= 0) Color(0xFFA7F3D0) else Color(0xFFFECACA)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (resellerProfit >= 0) "আপনার নিট লাভ হবে:" else "বিক্রয় মূল্য পাইকারি মূল্যের চেয়ে কম!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (resellerProfit >= 0) Color(0xFF047857) else Color(0xFFB91C1C)
                    )
                    Text(
                        text = "৳${resellerProfit.toInt()}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (resellerProfit >= 0) Color(0xFF059669) else Color(0xFFDC2626)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Customer Details Form
            Text("২. গ্রাহকের ডেলিভারি তথ্য", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text("গ্রাহকের পূর্ণ নাম") },
                placeholder = { Text("যেমন: মোঃ রাকিবুল হাসান") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reselling_cust_name_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = customerPhone,
                onValueChange = { customerPhone = it },
                label = { Text("গ্রাহকের মোবাইল নম্বর (১১ ডিজিট)") },
                placeholder = { Text("017xxxxxxxx") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reselling_cust_phone_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = deliveryAddress,
                onValueChange = { deliveryAddress = it },
                label = { Text("সম্পূর্ণ ডেলিভারি ঠিকানা (বাসা/রোড/এলাকা)") },
                placeholder = { Text("বাড়ি ১২, রোড ৫, মিরপুর ১০, ঢাকা") },
                maxLines = 2,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reselling_cust_address_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // District Selection Chips
            Text("জেলা নির্বাচন:", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(districts) { dist ->
                    val isDistSelected = selectedDistrict == dist
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDistSelected) Color(0xFF0055D2) else Color(0xFFF1F5F9),
                        modifier = Modifier.clickable {
                            selectedDistrict = dist
                            isInsideDhaka = dist == "ঢাকা"
                        }
                    ) {
                        Text(
                            text = dist,
                            fontSize = 11.sp,
                            fontWeight = if (isDistSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isDistSelected) Color.White else Color(0xFF334155),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Delivery Option Radio
            Text("ডেলিভারি এলাকা:", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = if (isInsideDhaka) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isInsideDhaka) Color(0xFF0055D2) else Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isInsideDhaka = true }
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("ঢাকার ভেতরে", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isInsideDhaka) Color(0xFF0055D2) else Color(0xFF334155))
                        Text("চার্জ: ৳${product.deliveryChargeInsideDhaka.toInt()}", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                }

                Surface(
                    color = if (!isInsideDhaka) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (!isInsideDhaka) Color(0xFF0055D2) else Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isInsideDhaka = false }
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("ঢাকার বাইরে", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (!isInsideDhaka) Color(0xFF0055D2) else Color(0xFF334155))
                        Text("চার্জ: ৳${product.deliveryChargeOutsideDhaka.toInt()}", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cost Summary Card
            Surface(
                color = Color(0xFFF1F5F9),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("৩. হিসাবের সারসংক্ষেপ", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("পণ্যের বিক্রয় মূল্য ($quantity টি):", fontSize = 12.sp, color = Color(0xFF475569))
                        Text("৳${(parsedSellingPrice * quantity).toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("ডেলিভারি চার্জ:", fontSize = 12.sp, color = Color(0xFF475569))
                        Text("৳${deliveryFee.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFCBD5E1))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("গ্রাহক থেকে আদায় (COD):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0055D2))
                        Text("৳${totalCustomerPayable.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0055D2))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("অগ্রিম ডেলিভারি কর্তন (ওয়ালেট থেকে):", fontSize = 11.sp, color = Color(0xFFDC2626))
                        Text("-৳${deliveryFee.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("বর্তমান ওয়ালেট ব্যালেন্স:", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text("৳${userBalance.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF10B981))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // PIN Input
            OutlinedTextField(
                value = pinText,
                onValueChange = { if (it.length <= 4) pinText = it },
                label = { Text("৪ ডিজিটের ট্রানজ্যাকশন পিন") },
                placeholder = { Text("****") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reselling_pin_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            val canSubmit = customerName.isNotBlank() &&
                    customerPhone.length >= 11 &&
                    deliveryAddress.isNotBlank() &&
                    pinText.length == 4 &&
                    userBalance >= deliveryFee &&
                    resellerProfit >= 0

            Button(
                onClick = {
                    onSubmitOrder(
                        quantity,
                        parsedSellingPrice,
                        customerName,
                        customerPhone,
                        deliveryAddress,
                        selectedDistrict,
                        isInsideDhaka,
                        pinText
                    )
                },
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("reselling_confirm_order_button")
            ) {
                Text(
                    text = "অর্ডার নিশ্চিত করুন (৳${deliveryFee.toInt()} অগ্রিম)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// -------------------------------------------------------------
// ORDER SUCCESS DIALOG
// -------------------------------------------------------------
@Composable
fun ResellingOrderSuccessDialog(
    order: ResellingOrder,
    onDismiss: () -> Unit,
    onViewOrders: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color(0xFFECFDF5), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(38.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "অভিনন্দন!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "রিসেলিং অর্ডারটি সফলভাবে গ্রহণ করা হয়েছে",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("অর্ডার আইডি:", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(order.id, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0055D2))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("পণ্য:", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(order.productTitle, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("গ্রাহকের নাম:", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(order.customerName, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("মোবাইল:", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(order.customerPhone, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("কাস্টমার থেকে সংগ্রহ (COD):", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("৳${order.totalCustomerPayable.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("আপনার সম্ভাব্য লাভ:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                            Text("+৳${order.resellerProfit.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 গ্রাহক ডেলিভারি গ্রহণ করার সাথে সাথে আপনার কমিশন ব্যালেন্সে জমা হবে।",
                    fontSize = 11.sp,
                    color = Color(0xFF0284C7)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onViewOrders,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("অর্ডার তালিকা দেখুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ঠিক আছে")
            }
        }
    )
}

// -------------------------------------------------------------
// CART BOTTOM SHEET
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResellingCartSheet(
    cartItems: List<ResellingCartItem>,
    onDismiss: () -> Unit,
    onUpdateQty: (String, Int) -> Unit,
    onRemove: (String) -> Unit,
    onCheckoutItem: (ResellingCartItem) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "শপিং কার্ট (${cartItems.size} টি পণ্য)",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0055D2)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            if (cartItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.RemoveShoppingCart, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("আপনার কার্ট খালি", color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                ) {
                    items(cartItems, key = { it.product.id }) { item ->
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.product.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "পাইকারি: ৳${item.product.wholesalePrice.toInt()} | লাভ: ৳${item.product.suggestedProfit.toInt()}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF10B981),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    IconButton(
                                        onClick = { onRemove(item.product.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Qty
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { onUpdateQty(item.product.id, item.quantity - 1) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Text("-", fontWeight = FontWeight.Bold)
                                        }
                                        Text("${item.quantity}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                                        IconButton(
                                            onClick = { onUpdateQty(item.product.id, item.quantity + 1) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Text("+", fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Button(
                                        onClick = { onCheckoutItem(item) },
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2)),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("অর্ডার করুন", fontSize = 11.sp)
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
// ORDERS HISTORY SHEET
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResellingOrdersHistorySheet(
    orders: List<ResellingOrder>,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "আমার রিসেলিং অর্ডারসমূহ (${orders.size})",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0055D2)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            if (orders.isEmpty()) {
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
                        .heightIn(max = 420.dp)
                ) {
                    items(orders, key = { it.id }) { ord ->
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
                                        color = if (ord.status == "ডেলিভার্ড") Color(0xFFECFDF5) else Color(0xFFFEF3C7),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = ord.status,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (ord.status == "ডেলিভার্ড") Color(0xFF059669) else Color(0xFFD97706),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ord.productTitle,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "গ্রাহক: ${ord.customerName} (${ord.customerPhone})",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "আদায়: ৳${ord.totalCustomerPayable.toInt()} (${ord.deliveryArea})",
                                        fontSize = 11.sp,
                                        color = Color(0xFF334155)
                                    )
                                    Text(
                                        text = "লাভ: ৳${ord.resellerProfit.toInt()}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                }
                                Text(
                                    text = dateFormat.format(Date(ord.timestamp)),
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
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
// FAVORITES / WISHLIST SHEET
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResellingFavoritesSheet(
    favoriteProducts: List<ResellingProduct>,
    onDismiss: () -> Unit,
    onRemoveFavorite: (String) -> Unit,
    onOrderProduct: (ResellingProduct) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "পছন্দের তালিকা (${favoriteProducts.size})",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0055D2)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            if (favoriteProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("কোনো পছন্দের পণ্য যোগ করা হয়নি", color = Color(0xFF64748B))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                ) {
                    items(favoriteProducts, key = { it.id }) { product ->
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "পাইকারি: ৳${product.wholesalePrice.toInt()} • সম্ভাব্য লাভ: ৳${product.suggestedProfit.toInt()}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                IconButton(
                                    onClick = { onRemoveFavorite(product.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Favorite, contentDescription = "Remove", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                Button(
                                    onClick = { onOrderProduct(product) },
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055D2)),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("অর্ডার", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
