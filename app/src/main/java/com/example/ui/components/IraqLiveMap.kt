package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProviderEntity
import kotlin.math.sqrt

@Composable
fun IraqLiveMap(
    selectedCity: String,
    selectedService: String,
    activeProviders: List<ProviderEntity>,
    onRouteCalculated: (distanceKm: Double, priceIqd: Double, etaMinutes: Int, startLabel: String, endLabel: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    
    // Live animation of the vehicles
    val infiniteTransition = rememberInfiniteTransition(label = "map_anim")
    val riverFlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "river_flow"
    )

    // Selection of Start/End Points
    var startPoint by remember { mutableStateOf<Offset?>(Offset(200f, 600f)) }
    var endPoint by remember { mutableStateOf<Offset?>(Offset(450f, 300f)) }

    // Navigation and Routing (Waze-like feature)
    var isNavigationActive by remember { mutableStateOf(false) }
    var selectedRouteIndex by remember { mutableStateOf(0) } // 0 = Smart Route, 1 = Shortest, 2 = Highway
    
    // Auto-cycling navigation instructions for turn-by-turn simulation on Waze mode
    val instructionStep = (riverFlow / 5f).toInt() % 4

    // Route labels
    val startLabel = "Bab Al-Sharqi, Baghdad"
    val endLabel = "Mansour Mall, Baghdad"

    // Alternative routes definitions
    val routes = listOf(
        RouteInfo(
            nameAr = "طريق سوا الذكي (مفتاح ويز الأسرع)",
            nameEn = "Sawa Smart (Waze Fastest)",
            distanceBonusKm = 0.0,
            etaFactor = 2.2,
            type = "SMART",
            color = Color(0xFFD4AF37) // Gold
        ),
        RouteInfo(
            nameAr = "الطريق التراثي الأقصر (عبر باب المعظم)",
            nameEn = "Shortest Heritage (via Bab Al-Muadham)",
            distanceBonusKm = -1.2,
            etaFactor = 2.9, // Shorter has higher traffic factor sometimes
            type = "SHORTEST",
            color = Color(0xFF3A86C8) // Blue
        ),
        RouteInfo(
            nameAr = "طريق القناة السريع (سفر حر ومريح)",
            nameEn = "Al-Qanal Highway (Smooth Cruise)",
            distanceBonusKm = 2.4,
            etaFactor = 1.8, // Faster speed limits
            type = "HIGHWAY",
            color = Color(0xFF00A86B) // Emerald Green
        )
    )

    val currentRoute = routes[selectedRouteIndex]

    // Recalculate distance and fares whenever points or selected route changes
    LaunchedEffect(startPoint, endPoint, selectedService, selectedRouteIndex) {
        val sp = startPoint
        val ep = endPoint
        if (sp != null && ep != null) {
            val pixelDistance = sqrt((sp.x - ep.x) * (sp.x - ep.x) + (sp.y - ep.y) * (sp.y - ep.y))
            val baseKm = (pixelDistance / 40.0) // Scale pixels to real KM
            val distanceKm = (baseKm + currentRoute.distanceBonusKm).coerceAtLeast(1.5)
            val roundedKm = String.format("%.1f", distanceKm).toDouble()

            val basePrice = when (selectedService) {
                "TAXI" -> 3000.0
                "TOKTOK" -> 1500.0
                "TOWING" -> 25000.0
                "GAS" -> 4000.0
                "WATER" -> 2000.0
                else -> 5000.0
            }
            val perKmPrice = when (selectedService) {
                "TAXI" -> 750.0
                "TOKTOK" -> 400.0
                "TOWING" -> 1500.0
                "GAS" -> 0.0
                "WATER" -> 0.0
                else -> 800.0
            }

            val totalIqd = basePrice + (roundedKm * perKmPrice)
            val eta = (roundedKm * currentRoute.etaFactor).toInt().coerceAtLeast(3)

            onRouteCalculated(roundedKm, totalIqd, eta, startLabel, endLabel)
        }
    }

    Box(
        modifier = modifier
            .testTag("iraq_live_map")
            .background(
                if (isDark) Color(0xFF141917) else Color(0xFFFAF7F2),
                RoundedCornerShape(20.dp)
            )
    ) {
        // Render Iraq Map canvas with Mesopotamia rivers
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // Double-tap or normal tap to toggle end points
                        if (startPoint == null) {
                            startPoint = offset
                        } else if (endPoint == null) {
                            endPoint = offset
                        } else {
                            // Cycle points
                            startPoint = offset
                            endPoint = null
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. Draw sand/mud plains
            val landBrush = Brush.radialGradient(
                colors = if (isDark) {
                    listOf(Color(0xFF1B2320), Color(0xFF111614))
                } else {
                    listOf(Color(0xFFFAF2E4), Color(0xFFEEE3D0))
                },
                center = Offset(width / 2, height / 2)
            )
            drawRect(brush = landBrush)

            // 2. Draw Tigris River (دجلة) entering from North-West to South-East
            val tigrisPath = Path().apply {
                moveTo(width * 0.1f, height * 0.05f)
                cubicTo(
                    width * 0.4f, height * 0.3f,
                    width * 0.3f, height * 0.6f,
                    width * 0.7f, height * 0.95f
                )
            }
            drawPath(
                path = tigrisPath,
                color = if (isDark) Color(0xFF1E3D4A) else Color(0xFFD0E6FC),
                style = Stroke(
                    width = 12f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), riverFlow)
                )
            )

            // 3. Draw Euphrates River (الفرات) running parallel to Tigris
            val euphratesPath = Path().apply {
                moveTo(width * 0.05f, height * 0.2f)
                cubicTo(
                    width * 0.35f, height * 0.45f,
                    width * 0.4f, height * 0.75f,
                    width * 0.85f, height * 0.98f
                )
            }
            drawPath(
                path = euphratesPath,
                color = if (isDark) Color(0xFF1B4958) else Color(0xFFE1EFFD),
                style = Stroke(
                    width = 10f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), riverFlow)
                )
            )

            // 4. Draw Iraqi City landmarks highlights
            val cityCenters = listOf(
                Offset(width * 0.45f, height * 0.45f) to "بغداد (Baghdad)",
                Offset(width * 0.75f, height * 0.85f) to "البصرة (Basra)",
                Offset(width * 0.35f, height * 0.15f) to "أربيل (Erbil)",
                Offset(width * 0.43f, height * 0.58f) to "كربلاء (Karbala)"
            )

            cityCenters.forEach { (offset, city) ->
                drawCircle(
                    color = if (isDark) Color(0xFF00966C).copy(alpha = 0.1f) else Color(0xFF02C39A).copy(alpha = 0.15f),
                    radius = 45f,
                    center = offset
                )
                drawCircle(
                    color = if (isDark) Color(0xFF00966C).copy(alpha = 0.4f) else Color(0xFF02C39A).copy(alpha = 0.5f),
                    radius = 8f,
                    center = offset
                )
            }

            // 5. Draw route paths
            val spOffset = startPoint
            val epOffset = endPoint
            if (spOffset != null && epOffset != null) {
                // If navigation is active, we showcase the selected alternate route as active
                // and show other secondary routes in faint dotted colors (just like Waze!)
                routes.forEachIndexed { index, route ->
                    val isSelected = (index == selectedRouteIndex)
                    
                    val pathColor = if (isSelected) route.color else route.color.copy(alpha = 0.33f)
                    val pathWidth = if (isSelected) 8f else 4f
                    
                    // Simple simulated curvy Bezier route between start and end to make it realistic
                    val routePath = Path().apply {
                        moveTo(spOffset.x, spOffset.y)
                        // Control points depend on route type
                        val ctrlX = when (route.type) {
                            "SMART" -> (spOffset.x + epOffset.x) / 2
                            "SHORTEST" -> (spOffset.x + epOffset.x) / 2 - 100f
                            else -> (spOffset.x + epOffset.x) / 2 + 120f
                        }
                        val ctrlY = when (route.type) {
                            "SMART" -> (spOffset.y + epOffset.y) / 2 - 50f
                            "SHORTEST" -> (spOffset.y + epOffset.y) / 2
                            else -> (spOffset.y + epOffset.y) / 2 + 100f
                        }
                        quadraticTo(ctrlX, ctrlY, epOffset.x, epOffset.y)
                    }

                    drawPath(
                        path = routePath,
                        color = pathColor,
                        style = Stroke(
                            width = pathWidth,
                            pathEffect = if (!isSelected) PathEffect.dashPathEffect(floatArrayOf(10f, 10f)) else null
                        )
                    )

                    // Draw vehicle moving on the active route
                    if (isSelected) {
                        val routeProgress = (riverFlow % 20f) / 20f
                        
                        // Approximate vehicle coordinates on quadratic bezier curve
                        val u = routeProgress
                        val ctrlX = when (route.type) {
                            "SMART" -> (spOffset.x + epOffset.x) / 2
                            "SHORTEST" -> (spOffset.x + epOffset.x) / 2 - 100f
                            else -> (spOffset.x + epOffset.x) / 2 + 120f
                        }
                        val ctrlY = when (route.type) {
                            "SMART" -> (spOffset.y + epOffset.y) / 2 - 50f
                            "SHORTEST" -> (spOffset.y + epOffset.y) / 2
                            else -> (spOffset.y + epOffset.y) / 2 + 100f
                        }
                        
                        val vehicleX = (1 - u) * (1 - u) * spOffset.x + 2 * (1 - u) * u * ctrlX + u * u * epOffset.x
                        val vehicleY = (1 - u) * (1 - u) * spOffset.y + 2 * (1 - u) * u * ctrlY + u * u * epOffset.y

                        // Halo circle around transit vehicle with custom styling
                        drawCircle(
                            color = route.color.copy(alpha = 0.35f),
                            radius = if (isNavigationActive) 35f else 28f,
                            center = Offset(vehicleX, vehicleY)
                        )
                        drawCircle(
                            color = if (isNavigationActive) Color(0xFFE63946) else Color(0xFF00966C),
                            radius = 10f,
                            center = Offset(vehicleX, vehicleY)
                        )
                    }
                }
            }

            // 6. Draw active nearby providers (simulated positions)
            activeProviders.forEach { provider ->
                val seedOffsetLat = provider.currentLatOffset * width * 5
                val seedOffsetLng = provider.currentLngOffset * height * 5
                val markerX = (width * 0.48f + seedOffsetLat).coerceIn(10f, width - 10f)
                val markerY = (height * 0.48f + seedOffsetLng).coerceIn(10f, height - 10f)

                drawCircle(
                    color = when (provider.serviceType) {
                        "TAXI" -> Color(0xFFE9C46A)
                        "TOKTOK" -> Color(0xFFF4A261)
                        "TOWING" -> Color(0xFFE76F51)
                        else -> Color(0xFF2A9D8F)
                    }.copy(alpha = 0.3f),
                    radius = 24f,
                    center = Offset(markerX, markerY)
                )
                drawCircle(
                    color = when (provider.serviceType) {
                        "TAXI" -> Color(0xFFE9C46A)
                        "TOKTOK" -> Color(0xFFF4A261)
                        "TOWING" -> Color(0xFFE76F51)
                        else -> Color(0xFF2A9D8F)
                    },
                    radius = 8f,
                    center = Offset(markerX, markerY)
                )
            }

            // 7. Draw Start and End Markers
            if (spOffset != null) {
                drawCircle(color = Color(0xFF00966C), radius = 12f, center = spOffset)
                drawCircle(color = Color.White, radius = 5f, center = spOffset)
            }
            if (epOffset != null) {
                drawCircle(color = Color(0xFFE63946), radius = 12f, center = epOffset)
                drawCircle(color = Color.White, radius = 5f, center = epOffset)
            }
        }

        // --- WAZE NAVIGATION GUI PANEL ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Smart Navigation Bar Toggle / Quick Switcher
            AnimatedVisibility(
                visible = isNavigationActive,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF15221B) else Color(0xFFEDFBF4),
                        contentColor = if (isDark) Color(0xFFD0F8E4) else Color(0xFF0B6338)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "navigating waze",
                                    tint = Color(0xFFFF9800),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "خرائط وملاحة سوا ويز 🛰️ (نشط)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Speed indicator with simulated variance
                            val currentSpeed = 50 + (instructionStep * 4) + (riverFlow.toInt() % 3)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "$currentSpeed كم/س",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (currentSpeed > 60) Color.Red else Color.Unspecified
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "حد: 60",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Turn-by-Turn GPS Instruction Simulation
                        val gpsPromptAr = when (instructionStep) {
                            0 -> "بعد ٣٠٠ متر، انعطف يميناً باتجاه شارع الحارثية"
                            1 -> "استمر على طول شارع الرشيد لتفادي اختناق جسر السنك"
                            2 -> "انتبه: ازدحام خفيف مسجل بمدخل المنصور، تم تعديل الطريق تلقائياً"
                            else -> "وصلت تقريباً! وجهتك تقع على بعد ١٠٠ متر على اليمين"
                        }
                        val gpsPromptEn = when (instructionStep) {
                            0 -> "In 300m, turn right onto Harthiya Street"
                            1 -> "Continue on Al-Rashid St to bypass Al-Sinak traffic"
                            2 -> "Notice: Moderate jam near Mansour. Path recalculated"
                            else -> "Almost there! Your destination is 100m on your right"
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = "gps help", tint = Color(0xFF00966C), modifier = Modifier.size(16.dp))
                            Column {
                                Text(text = gpsPromptAr, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = gpsPromptEn, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }

            // Normal Overlay & Route Selector Action Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (isDark) Color(0xFF1B201F).copy(alpha = 0.92f) else Color.White.copy(alpha = 0.96f),
                shape = RoundedCornerShape(14.dp),
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Header switch and Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Waze route choice",
                                tint = currentRoute.color,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "ملاحة ويز: اختر أفضل وأقرب مسار",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Tap on paths to change best route",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }

                        // Navigation ON-OFF Switch!
                        Button(
                            onClick = { isNavigationActive = !isNavigationActive },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isNavigationActive) Color(0xFF00966C) else MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = if (isNavigationActive) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isNavigationActive) "إيقاف الملاحة" else "تشغيل التنقل 🛰️",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // horizontal alternate route chooser row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        routes.forEachIndexed { idx, r ->
                            val isSelected = (idx == selectedRouteIndex)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) r.color.copy(alpha = 0.15f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) r.color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedRouteIndex = idx }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (idx == 0) "الأسرع (ويز)" else if (idx == 1) "الأقصر" else "طريق مريح",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) r.color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "${if (idx == 0) "-0%" else if (idx == 1) "-1.2 كم" else "+2.4 كم"}",
                                        fontSize = 8.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Center map button
        FloatingActionButton(
            onClick = {
                startPoint = Offset(200f, 600f)
                endPoint = Offset(450f, 300f)
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .size(36.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Center path",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

data class RouteInfo(
    val nameAr: String,
    val nameEn: String,
    val distanceBonusKm: Double,
    val etaFactor: Double,
    val type: String,
    val color: Color
)
