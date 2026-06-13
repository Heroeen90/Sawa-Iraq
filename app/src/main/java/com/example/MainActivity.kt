package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BookingEntity
import com.example.data.ProviderEntity
import com.example.ui.components.IraqLiveMap
import com.example.ui.theme.SawaTheme
import com.example.viewmodel.SuperAppViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SuperAppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val role by viewModel.currentRole.collectAsState()
            val language by viewModel.currentLanguage.collectAsState()

            // Handle Arabization Layout Direction based on selected language
            val layoutDirection = if (language == "AR") LayoutDirection.Rtl else LayoutDirection.Ltr

            SawaTheme {
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("sawa_scaffold")
                    ) { innerPadding ->
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // 1. System header (RoleSwitcher & localization toggle)
                                SawaAppHeader(
                                    currentRole = role,
                                    currentLanguage = language,
                                    onRoleChange = { viewModel.setRole(it) },
                                    onLanguageToggle = { viewModel.toggleLanguage() }
                                )

                                // 2. Display different Dashboard views depending on selected Role
                                Box(modifier = Modifier.weight(1f)) {
                                    AnimatedContent(
                                        targetState = role,
                                        transitionSpec = {
                                            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                                        },
                                        label = "role_navigation"
                                    ) { targetRole ->
                                        when (targetRole) {
                                            "CUSTOMER" -> CustomerDashboard(viewModel)
                                            "PROVIDER" -> ProviderDashboard(viewModel)
                                            "ADMIN" -> AdminDashboard(viewModel)
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

}

// ----------------------------------------------------------------------------
// COMPOSABLES: HEADER COMPONENT
// ----------------------------------------------------------------------------
@Composable
fun SawaAppHeader(
    currentRole: String,
    currentLanguage: String,
    onRoleChange: (String) -> Unit,
    onLanguageToggle: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(Color(0xFF0F1E19), Color(0xFF111413))
                    } else {
                        listOf(Color(0xFFD4EAE0), Color(0xFFFAF7F2))
                    }
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (currentLanguage == "AR") "سوا العراق" else "Sawa Iraq",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (currentLanguage == "AR") "المنصة الخدمية الموحدة" else "SaaS All-in-One Super App",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            // Quick controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Language selector
                Button(
                    onClick = onLanguageToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = if (currentLanguage == "AR") "English" else "العربية",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Role Quick Switcher Pills (Customer, Provider, Admin)
        ScrollableTabRow(
            selectedTabIndex = when (currentRole) {
                "CUSTOMER" -> 0
                "PROVIDER" -> 1
                else -> 2
            },
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            divider = {},
            indicator = {}
        ) {
            Tab(
                selected = currentRole == "CUSTOMER",
                onClick = { onRoleChange("CUSTOMER") },
                text = {
                    Text(
                        text = if (currentLanguage == "AR") "👤 العميل" else "👤 Customer",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                unselectedContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        if (currentRole == "CUSTOMER") MaterialTheme.colorScheme.primary else Color.Transparent
                    )
            )
            Tab(
                selected = currentRole == "PROVIDER",
                onClick = { onRoleChange("PROVIDER") },
                text = {
                    Text(
                        text = if (currentLanguage == "AR") "🚖 مزود الخدمة" else "🚖 Provider / Driver",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                unselectedContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        if (currentRole == "PROVIDER") MaterialTheme.colorScheme.primary else Color.Transparent
                    )
            )
            Tab(
                selected = currentRole == "ADMIN",
                onClick = { onRoleChange("ADMIN") },
                text = {
                    Text(
                        text = if (currentLanguage == "AR") "📊 لوحة التحكم SaaS" else "📊 SaaS Dashboard",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                unselectedContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        if (currentRole == "ADMIN") MaterialTheme.colorScheme.primary else Color.Transparent
                    )
            )
        }
    }
}

// ----------------------------------------------------------------------------
// COMPOSABLES: CUSTOMER DASHBOARD (SUPER APP MULTI-SERVICES)
// ----------------------------------------------------------------------------
@Composable
fun CustomerDashboard(viewModel: SuperAppViewModel) {
    val language by viewModel.currentLanguage.collectAsState()
    val activeService by viewModel.selectedServiceType.collectAsState()
    val activeProviders by viewModel.activeProvidersByService.collectAsState()

    val mapDistance by viewModel.mapDistanceKm.collectAsState()
    val mapPrice by viewModel.mapPriceIqd.collectAsState()
    val mapEta by viewModel.mapEtaMinutes.collectAsState()
    val mapStartLabel by viewModel.mapStartLabel.collectAsState()
    val mapEndLabel by viewModel.mapEndLabel.collectAsState()

    val bookings by viewModel.bookings.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    val context = LocalContext.current

    // List of Iraq Super services
    val services = listOf(
        ServiceItem("TAXI", "تاكسي", "Taxi", Icons.Default.LocationOn, "سرعة وأمان"),
        ServiceItem("TOKTOK", "توكتوك", "Toktok", Icons.Default.Info, "تجاوز الازدحام"),
        ServiceItem("TOWING", "كرين سحب", "Towing", Icons.Default.Build, "إنقاذ سيارات"),
        ServiceItem("GAS", "غاز", "Gas Cylinder", Icons.Default.Warning, "توصيل للمنزل"),
        ServiceItem("WATER", "مياه RO", "RO Water", Icons.Default.Refresh, "صهاريج وموزعين"),
        ServiceItem("VENDOR", "متجولون", "Street Vendor", Icons.Default.ShoppingCart, "بقال وفواكه"),
        ServiceItem("GENERAL", "عامة", "General Services", Icons.Default.Add, "صيانة وتوصيل أثاث")
    )

    var currentPaymentMethod by remember { mutableStateOf("CASH") }
    var expandedPaymentOption by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header & Balance Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (language == "AR") "مرحباً بك، عميلنا الراقي" else "Welcome, Valued Guest",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (language == "AR") "رصيدك في محفظة سوا" else "Your Sawa Wallet Balance",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        val balance by viewModel.customerBalanceIqd.collectAsState()
                        Text(
                            text = "${String.format("%,.0f", balance)} د.ع",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Horizontal Category Row
        item {
            Column {
                Text(
                    text = if (language == "AR") "اختر من الخدمات الشاملة" else "Choose our Services",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(services) { service ->
                        val isSelected = service.id == activeService
                        ElevatedCard(
                            onClick = { viewModel.setSelectedServiceType(service.id) },
                            modifier = Modifier
                                .width(120.dp)
                                .height(100.dp)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = service.icon,
                                    contentDescription = service.titleEn,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (language == "AR") service.titleAr else service.titleEn,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = if (language == "AR") service.subAr else service.titleEn,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Iraq Map Area (Height 280dp)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                IraqLiveMap(
                    selectedCity = "Baghdad",
                    selectedService = activeService,
                    activeProviders = activeProviders,
                    onRouteCalculated = { distance, price, eta, start, end ->
                        viewModel.setRouteCalculations(distance, price, eta, start, end)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Routing & Actions details
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == "AR") "تفاصيل وتأكيد الرحلة" else "Trip Specifications",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (language == "AR") "المسافة الكلية" else "Total Distance",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "$mapDistance كم (Km)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (language == "AR") "الوقت المتوقع" else "ETA",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "$mapEta دقيقة (Min)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(
                                text = if (language == "AR") "التكلفة التقديرية" else "Fare price",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "${String.format("%,.0f", mapPrice)} د.ع",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    // Gateway electronic payment Iraqi selectors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (language == "AR") "طريقة الدفع" else "Payment gateway",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Box {
                            TextButton(onClick = { expandedPaymentOption = true }) {
                                Text(
                                    text = when (currentPaymentMethod) {
                                        "CASH" -> if (language == "AR") "💵 نقداً" else "💵 Cash"
                                        "ZAIN_CASH" -> if (language == "AR") "📱 زين كاش" else "📱 Zain Cash"
                                        else -> if (language == "AR") "📱 آسيا باي" else "📱 AsiaPay"
                                    }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "dropdown")
                            }

                            DropdownMenu(
                                expanded = expandedPaymentOption,
                                onDismissRequest = { expandedPaymentOption = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("💵 نقداً / Cash") },
                                    onClick = { currentPaymentMethod = "CASH"; expandedPaymentOption = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("📱 زين كاش / Zain Cash") },
                                    onClick = { currentPaymentMethod = "ZAIN_CASH"; expandedPaymentOption = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("📱 آسيا باي / AsiaPay") },
                                    onClick = { currentPaymentMethod = "ASIA_PAY"; expandedPaymentOption = false }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.requestService(currentPaymentMethod)
                            Toast.makeText(context, "تم إرسال طلبك بنجاح وجاري إيجاد السائق!", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_request_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "taxi icon")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == "AR") "اطلب الخدمة الآن" else "Book Super App now",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Active Booking simulator
        val activeBookings = bookings.filter { it.status == "ACCEPTED" }
        if (activeBookings.isNotEmpty()) {
            item {
                Text(
                    text = if (language == "AR") "رحلاتك النشطة الحالية" else "Your active bookings",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(activeBookings) { booking ->
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = "taxi type", tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = booking.providerName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = booking.providerPhone,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(Color(0xFFFF9800))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (language == "AR") "في الطريق" else "Active",
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "${if (language == "AR") "من:" else "From:"} ${booking.startLocation} ➔ ${if (language == "AR") "إلى:" else "To:"} ${booking.endLocation}",
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    Toast.makeText(context, "${if (language == "AR") "اتصال آمن بالسائق" else "Phoning driver..."} ${booking.providerPhone}", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (language == "AR") "اتصال" else "Call", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    Toast.makeText(context, if (language == "AR") "فتح دردشة داخل التطبيق" else "Opening Sawa secure chat", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                            ) {
                                Icon(Icons.Default.Email, contentDescription = "chat", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (language == "AR") "دردشة" else "Chat", fontSize = 12.sp)
                            }

                            // Simulation action to complete booking (allows demonstration of whole cycle)
                            Button(
                                onClick = { viewModel.completeBooking(booking) },
                                modifier = Modifier.weight(1.2f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "complete", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (language == "AR") "إتمام" else "End Ride", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Notification List
        if (notifications.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == "AR") "التنبيهات والإشعارات" else "Notifications & Alerts",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { viewModel.clearNotifications() }) {
                        Text(if (language == "AR") "تنظيف الكل" else "Clear")
                    }
                }
            }

            items(notifications) { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "notification bell", tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text(
                                text = if (language == "AR") msg.titleAr else msg.titleEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (language == "AR") msg.messageAr else msg.messageEn,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// COMPOSABLES: PROVIDER / DRIVER DASHBOARD
// ----------------------------------------------------------------------------
@Composable
fun ProviderDashboard(viewModel: SuperAppViewModel) {
    val language by viewModel.currentLanguage.collectAsState()
    val bookings by viewModel.bookings.collectAsState()
    val providers by viewModel.providers.collectAsState()
    val context = LocalContext.current

    // Simulating login switching for different providers so that taxi/vendor/etc. can toggle their active online/offline status!
    var simulatedProviderPhone by remember { mutableStateOf("07701234567") }

    val driverProfileRef = providers.firstOrNull { it.phone == simulatedProviderPhone } ?: ProviderEntity(
        id = 11,
        name = "Mustafa Al-Saeedi",
        phone = "07701234567",
        serviceType = "TAXI",
        vehiclePlate = "Baghdad 12345-A"
    )

    var payoutAmountInput by remember { mutableStateOf("15000") }
    var payoutGatewaySelection by remember { mutableStateOf("ZAIN_CASH") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Switch between simulated profile types (Taxi, Toktok, Towing, Gas)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (language == "AR") "محاكاة الدخول كمقدم خدمة (اختر للتجربة):" else "Simulate Service Provider Mode (Tap to test):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            Triple("07701234567", "🚖 تكسي", "🚖 Taxi"),
                            Triple("07501234567", "🛺 توكتوك", "🛺 Toktok"),
                            Triple("07812345678", "🏗️ كرين", "🏗️ Towing"),
                            Triple("07711223344", "🔥 غاز", "🔥 Gas")
                        ).forEach { (phone, textAr, textEn) ->
                            val isSelected = (simulatedProviderPhone == phone)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { simulatedProviderPhone = phone }
                                    .padding(vertical = 8.dp, horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (language == "AR") textAr else textEn,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
        // Driver Identity card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = driverProfileRef.name,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "محافظة: ${driverProfileRef.city} | ${driverProfileRef.vehiclePlate}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        // Availability switcher
                        Button(
                            onClick = { viewModel.toggleDriverAvailability(driverProfileRef) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (driverProfileRef.isAvailable) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        ) {
                            Text(
                                text = if (driverProfileRef.isAvailable) {
                                    if (language == "AR") "● متصل" else "● Online"
                                } else {
                                    if (language == "AR") "○ مغلق" else "○ Offline"
                                }
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    // Earning Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (language == "AR") "محفظة مستحقات السائق" else "Sawa Earnings Wallet",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            val balance by viewModel.providerBalanceIqd.collectAsState()
                            Text(
                                text = "${String.format("%,.0f", balance)} د.ع",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (language == "AR") "تقييمك الحالي" else "Your Rating",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "⭐ ${driverProfileRef.rating} / 5.0",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Payout to Electronic Wallets
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == "AR") "سحب فوري مستحقات السائق لدعم المعيشة" else "Withdraw Sawa Earnings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = payoutAmountInput,
                            onValueChange = { payoutAmountInput = it },
                            label = { Text(if (language == "AR") "المبلغ بالدينار د.ع" else "Amount (IQD)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .clickable {
                                        payoutGatewaySelection = if (payoutGatewaySelection == "ZAIN_CASH") "ASIA_PAY" else "ZAIN_CASH"
                                    }
                                    .padding(horizontal = 12.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = if (payoutGatewaySelection == "ZAIN_CASH") "Zain" else "Asia"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val amount = payoutAmountInput.toDoubleOrNull() ?: 0.0
                            val balance = viewModel.providerBalanceIqd.value
                            if (amount > balance) {
                                Toast.makeText(context, if (language == "AR") "الرصيد غير كافي للسحب!" else "Balance insufficient!", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.executeProviderPayout(driverProfileRef.id, amount, payoutGatewaySelection)
                                Toast.makeText(context, if (language == "AR") "تم إرسال الحوالة بنجاح إلى محفظتك الإلكترونية!" else "Payout successfully wired!", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "wire transfer")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (language == "AR") "تحويل فوري إلى المحفظة" else "Cashout instantly")
                    }
                }
            }
        }

        // List of past / pending reservations
        val driverBookings = bookings.filter { it.providerPhone == driverProfileRef.phone }
        item {
            Text(
                text = if (language == "AR") "سجل رحلاتك وعمولات النظام" else "System Bookings & SaaS Commissions",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (driverBookings.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Text(
                        text = if (language == "AR") "لا توجد طلبات سابقة مسجلة." else "No bookings recorded yet.",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            items(driverBookings) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "طلب #${order.id} | ${order.serviceType}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )

                            Text(
                                text = order.status,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (order.status == "COMPLETED") Color(0xFF00966C) else Color(0xFFFF9800)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "الزبون: ${order.customerName} (${order.customerPhone})",
                            fontSize = 11.sp
                        )

                        Text(
                            text = "${order.startLocation} ➔ ${order.endLocation}",
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (language == "AR") "الأجرة الكلية" else "Total Fare",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "${String.format("%,.0f", order.price)} د.ع",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                val comm = if (order.serviceType == "TAXI") 0.10 else 0.05
                                Text(
                                    text = if (language == "AR") "عمولة المنصة SaaS (${(comm*100).toInt()}%)" else "SaaS Fee (${(comm*100).toInt()}%)",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "${String.format("%,.0f", order.price * comm)} د.ع",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE53935)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// COMPOSABLES: WEB ADMIN SaaS PANEL DASHBOARD
// ----------------------------------------------------------------------------
@Composable
fun AdminDashboard(viewModel: SuperAppViewModel) {
    val language by viewModel.currentLanguage.collectAsState()
    val bookings by viewModel.bookings.collectAsState()
    val providers by viewModel.providers.collectAsState()
    val saasConfig by viewModel.saasConfig.collectAsState()

    val context = LocalContext.current

    // SaaS Configuration states
    var taxiCommInput by remember { mutableStateOf("10.0") }
    var toktokCommInput by remember { mutableStateOf("5.0") }
    val monthlySubFeeInput = "15000"

    // SaaS totals calculations
    val totalVol = bookings.size
    val grossRevenue = bookings.sumOf { it.price }
    val calculatedCommissionValue = bookings.sumOf {
        val multiplier = if (it.serviceType == "TAXI") 0.10 else 0.05
        it.price * multiplier
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // SaaS Title & State
        item {
            Column {
                Text(
                    text = if (language == "AR") "مركز السيطرة والتحكم لشركة Sawa SaaS" else "Sawa SaaS Administrative Engine",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (language == "AR") "قاعدة بيانات العراق والعمولات المجمعة" else "Iraqi Fleet Management & System Logs",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        // Metrics Grid (Vol, Gross, Collectibles)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == "AR") "إحصائيات المنصة الفورية" else "Realtime Platform Stats",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = if (language == "AR") "إجمالي الطلبات" else "Volume",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "$totalVol",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1.3f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = if (language == "AR") "مبيعات د.ع" else "Gross Vol",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "${String.format("%,.0f", grossRevenue)} د.ع",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        Column(
                            modifier = Modifier
                                        .weight(1.3f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = if (language == "AR") "أرباح العمولات د.ع" else "Earned Fees",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${String.format("%,.0f", calculatedCommissionValue)} د.ع",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Adjust Policies
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == "AR") "تعديل سياسات العمولات والعقود" else "Configure SaaS Policy",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = taxiCommInput,
                            onValueChange = { taxiCommInput = it },
                            label = { Text(if (language == "AR") "عمولة التاكسي (%)" else "Taxi Fee %") },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = toktokCommInput,
                            onValueChange = { toktokCommInput = it },
                            label = { Text(if (language == "AR") "عمولة التوكتوك (%)" else "Toktok Fee %") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val taxiVal = taxiCommInput.toDoubleOrNull() ?: 10.0
                            val tokVal = toktokCommInput.toDoubleOrNull() ?: 5.0
                            val flatVal = monthlySubFeeInput.toDoubleOrNull() ?: 15000.0

                            viewModel.updateSaaSAdminConfig(taxiVal, tokVal, flatVal)
                            Toast.makeText(context, if (language == "AR") "تم تحديث سياسات التسعير بنجاح!" else "Corporate pricing policies updated!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Done, contentDescription = "save config")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (language == "AR") "حفظ السياسات الجديدة" else "Apply New System Policies")
                    }
                }
            }
        }

        // Iraqi Governorate registries control listing
        item {
            Text(
                text = if (language == "AR") "إدارة أسطول المحافظات العراقية" else "Governing Iraqi Districts",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        val provinces = listOf(
            ProvinceRef("بغداد - العاصمة", "Baghdad (Capital)", 42, "نشط / Active"),
            ProvinceRef("البصرة - بوابة الجنوب", "Basra", 18, "نشط / Active"),
            ProvinceRef("أربيل - كوردستان", "Erbil", 15, "نشط / Active"),
            ProvinceRef("الموصل - نينوى", "Mosul", 9, "نشط / Active"),
            ProvinceRef("كربلاء المقدسة", "Karbala", 12, "نشط / Active"),
            ProvinceRef("النجف الأشرف", "Najaf", 7, "نشط / Active")
        )

        items(provinces) { province ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (language == "AR") province.nameAr else province.nameEn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "إجمالي السائقين النشطين: ${province.activeFleet}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    Text(
                        text = province.status,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// DATA CLASS STRUCTS FOR UI RENDER
// ----------------------------------------------------------------------------
data class ServiceItem(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val icon: ImageVector,
    val subAr: String
)

data class ProvinceRef(
    val nameAr: String,
    val nameEn: String,
    val activeFleet: Int,
    val status: String
)
