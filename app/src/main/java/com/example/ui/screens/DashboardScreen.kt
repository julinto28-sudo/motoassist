package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ComponentStatus
import com.example.data.MotorcycleProfile
import com.example.data.ServiceSchedule
import com.example.ui.MotorcycleViewModel
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.OkGreen
import com.example.ui.theme.WarningAmber
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: MotorcycleViewModel,
    onNavigateToReminders: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.profile.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val healthScore by viewModel.healthScore.collectAsState()
    val activeReminders by viewModel.activeReminders.collectAsState()

    // Dialog state controllers
    var showOdometerDialog by remember { mutableStateOf(false) }
    var odoInputText by remember { mutableStateOf("") }

    var showAverageKmDialog by remember { mutableStateOf(false) }
    var averageKmInputText by remember { mutableStateOf("") }

    var showServiceDialog by remember { mutableStateOf<ServiceSchedule?>(null) }
    var serviceCostText by remember { mutableStateOf("") }
    var serviceNotesText by remember { mutableStateOf("") }
    var customOdoText by remember { mutableStateOf("") }

    var showAddCustomDialog by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("") }
    var customIntKm by remember { mutableStateOf("") }
    var customIntMonth by remember { mutableStateOf("") }
    var customLastKm by remember { mutableStateOf("") }

    val indonesiaLocale = Locale("id", "ID")
    val currencyFormatter = NumberFormat.getCurrencyInstance(indonesiaLocale).apply {
        maximumFractionDigits = 0
    }

    if (profile == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentProfile = profile!!
    val lastUpdate = currentProfile.lastOdometerUpdateDate
    val daysSinceUpdate = if (lastUpdate > 0L) {
        val diffMs = System.currentTimeMillis() - lastUpdate
        diffMs.toFloat() / (1000f * 60f * 60f * 24f)
    } else {
        0f
    }
    val estimatedAdd = (daysSinceUpdate * currentProfile.averageDailyKm).toInt()
    val estimatedOdo = currentProfile.lastOdometerValue + estimatedAdd
    val hasEstimate = estimatedAdd >= 5

    Box(modifier = modifier.fillMaxSize()) {
        val isDark = isSystemInDarkTheme()
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val greetingText = when (hour) {
            in 4..10 -> "Selamat pagi,"
            in 11..14 -> "Selamat siang,"
            in 15..18 -> "Selamat sore,"
            else -> "Selamat malam,"
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            // Bento Header / User Greeting Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = greetingText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = if (isDark) com.example.ui.theme.TextSecondaryDark else com.example.ui.theme.TextSecondaryLight
                        )
                        Text(
                            text = currentProfile.name,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = if (isDark) com.example.ui.theme.TextPrimaryDark else Color(0xFF001E2F)
                        )
                    }

                    // Bento Notification Icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E2022) else Color.White)
                            .clickable { onNavigateToReminders() }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifikasi",
                            tint = if (isDark) com.example.ui.theme.PrimaryTeal else Color(0xFF006782),
                            modifier = Modifier.size(24.dp)
                        )
                        if (activeReminders.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 1. Overall Health Score (Status Kesehatan) Bento Card
            item {
                val healthCardBg = if (isDark) Color(0xFF001D36) else com.example.ui.theme.BentoBlueSkyBg
                val healthCardText = if (isDark) Color(0xFFD1E4FF) else com.example.ui.theme.BentoBlueSkyText

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("health_card"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = healthCardBg),
                    border = if (isDark) null else androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BentoBorderColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "Status Kesehatan",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = healthCardText
                                )
                                Text(
                                    text = "${currentProfile.brand} ${currentProfile.model} • ${currentProfile.plateNumber}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = healthCardText.copy(alpha = 0.7f)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(healthCardText.copy(alpha = 0.15f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                val healthBadgeText = when {
                                    healthScore >= 80 -> "PRIMA"
                                    healthScore >= 50 -> "SAYANGI"
                                    else -> "KRITIS"
                                }
                                Text(
                                    text = healthBadgeText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = healthCardText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "$healthScore%",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black,
                                color = healthCardText,
                                lineHeight = 48.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            val healthDescLabel = when {
                                healthScore >= 80 -> "Performa Optimal"
                                healthScore >= 50 -> "Butuh Pengecekan"
                                else -> "Penanganan Segera"
                            }
                            Text(
                                text = healthDescLabel,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = healthCardText.copy(alpha = 0.85f),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LinearProgressIndicator(
                            progress = { healthScore.toFloat() / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = if (isDark) com.example.ui.theme.SecondaryTeal else com.example.ui.theme.SecondaryTeal,
                            trackColor = healthCardText.copy(alpha = 0.15f)
                        )
                    }
                }
            }

            // 2. Alert Notification Banner inside Bento Grid flow
            if (activeReminders.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToReminders() }
                            .testTag("critical_notification_banner"),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Peringatan",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "PEMBERITAHUAN PENGINGAT",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "Ada ${activeReminders.size} komponen yang memerlukan tindakan perbaikan atau servis segera!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }            // 3. Motorcycle Odometer Bento block
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White
                    ),
                    border = if (isDark) null else androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BentoBorderColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        // Row 1: Odometer
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isDark) Color(0xFF2A2D35) else Color(0xFFF1F0F4)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Speed,
                                        contentDescription = null,
                                        tint = if (isDark) com.example.ui.theme.PrimaryTeal else Color(0xFF44474E),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "ODOMETER KENDARAAN",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = if (isDark) com.example.ui.theme.TextSecondaryDark else Color(0xFF006782)
                                    )
                                    Text(
                                        text = NumberFormat.getNumberInstance(indonesiaLocale).format(currentProfile.currentOdometer) + " KM",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (currentProfile.odometerUpdateStreak > 0) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isDark) Color(0xFF3E1F16) else Color(0xFFFFEBE6))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Whatshot,
                                                contentDescription = "Streak",
                                                tint = if (isDark) Color(0xFFFF8A65) else Color(0xFFFF5722),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${currentProfile.odometerUpdateStreak} Hari Beruntun",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                color = if (isDark) Color(0xFFFF8A65) else Color(0xFFFF5722)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = if (isDark) Color(0xFF32363D) else Color(0xFFE1E2EC)
                        )

                        if (hasEstimate && estimatedOdo > currentProfile.currentOdometer) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isDark) Color(0xFF1B2A32) else Color(0xFFE0F2F1))
                                    .padding(20.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isDark) Color(0xFF004D40) else Color(0xFFB2DFDB)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = if (isDark) Color(0xFF4DB6AC) else Color(0xFF00796B),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Estimasi Pintar Odometer",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = if (isDark) Color(0xFFB2DFDB) else Color(0xFF004D40)
                                        )
                                        Text(
                                            text = "Berdasarkan rata-rata harian, odometer Anda diperkirakan bertambah +$estimatedAdd KM ke $estimatedOdo KM.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isDark) Color(0xFFE0F2F1) else Color(0xFF00695C)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.updateOdometer(estimatedOdo)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isDark) Color(0xFF00796B) else Color(0xFF00695C)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f).height(36.dp).testTag("predictive_confirm_button"),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Ya, Perbarui ke $estimatedOdo KM", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    }
                                    
                                    OutlinedButton(
                                        onClick = {
                                            odoInputText = currentProfile.currentOdometer.toString()
                                            showOdometerDialog = true
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = if (isDark) Color(0xFFB2DFDB) else Color(0xFF004D40)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.height(36.dp).testTag("predictive_adjust_button"),
                                        contentPadding = PaddingValues(horizontal = 12.dp)
                                    ) {
                                        Text("Sesuaikan", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    }
                                }
                            }
                            HorizontalDivider(
                                color = if (isDark) Color(0xFF32363D) else Color(0xFFE1E2EC)
                            )
                        }

                        // Row 2: Rata-rata Km Harian
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isDark) Color(0xFF2A2D35) else Color(0xFFF1F0F4)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = if (isDark) com.example.ui.theme.SecondaryTeal else Color(0xFF006784),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "RATA-RATA JARAK HARIAN",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = if (isDark) com.example.ui.theme.TextSecondaryDark else Color(0xFF006782)
                                    )
                                    Text(
                                        text = "${currentProfile.averageDailyKm} KM / hari",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            FilledTonalButton(
                                onClick = {
                                    averageKmInputText = currentProfile.averageDailyKm.toString()
                                    showAverageKmDialog = true
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isDark) Color(0xFF2A2D35) else com.example.ui.theme.BentoBlueSlateBg,
                                    contentColor = if (isDark) Color.White else com.example.ui.theme.BentoBlueSlateText
                                ),
                                modifier = Modifier.testTag("update_average_km_button")
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Atur", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }

            // 4. Component Title with Add Custom Button
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pemantauan Pelumas Utama (Oli)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 5. Oli Components Section
            val oilSchedules = schedules.filter { it.componentName.contains("Oli", ignoreCase = true) }
            val otherSchedules = schedules.filter { !it.componentName.contains("Oli", ignoreCase = true) }

            if (oilSchedules.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E2022) else Color(0xFFF1F0F4))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Tidak ada pemantauan oli.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Layout the oil schedules side-by-side
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        oilSchedules.forEach { sched ->
                            val usedKm = (currentProfile.currentOdometer - sched.lastServicedKm).coerceAtLeast(0)
                            val kmRemaining = sched.intervalKm - usedKm
                            val percentage = (usedKm.toFloat() / sched.intervalKm.toFloat()).coerceIn(0f, 1.2f)

                            val status = when {
                                percentage >= 1.0f -> ComponentStatus.CRITICAL
                                percentage >= 0.8f -> ComponentStatus.WARNING
                                else -> ComponentStatus.OK
                            }

                            val colorIndicator = when (status) {
                                ComponentStatus.CRITICAL -> CriticalRed
                                ComponentStatus.WARNING -> WarningAmber
                                ComponentStatus.OK -> OkGreen
                            }

                            val isWarm = status == ComponentStatus.WARNING
                            val isCrit = status == ComponentStatus.CRITICAL

                            val (cardBg, cardText, iconBg) = when {
                                isCrit -> Triple(
                                    if (isDark) Color(0xFF5A1A1A) else Color(0xFFFFDAD7),
                                    if (isDark) Color(0xFFFFDAD7) else Color(0xFF410002),
                                    if (isDark) Color(0xFFBA1A1A) else Color(0xFFBA1A1A)
                                )
                                isWarm -> Triple(
                                    if (isDark) Color(0xFF433E2B) else com.example.ui.theme.BentoYellowBg,
                                    if (isDark) Color(0xFFECE1C4) else com.example.ui.theme.BentoYellowText,
                                    if (isDark) Color(0xFF705D00) else com.example.ui.theme.BentoYellowIconText
                                )
                                else -> Triple(
                                    if (isDark) Color(0xFF1E2436) else com.example.ui.theme.BentoBlueSlateBg,
                                    if (isDark) Color(0xFFE2E7FF) else com.example.ui.theme.BentoBlueSlateText,
                                    if (isDark) Color(0xFF2E3D75) else com.example.ui.theme.BentoBlueSlateIconBg
                                )
                            }

                            // Calculate estimation based on averageDailyKm
                            val averageKm = currentProfile.averageDailyKm.coerceAtLeast(1)
                            val daysRemaining = if (kmRemaining > 0) {
                                (kmRemaining + averageKm - 1) / averageKm
                            } else {
                                0
                            }
                            val estimatedTimeInMillis = System.currentTimeMillis() + daysRemaining.toLong() * 24 * 60 * 60 * 1000
                            val sdf = java.text.SimpleDateFormat("d MMM yyyy", indonesiaLocale)
                            val dateStr = sdf.format(java.util.Date(estimatedTimeInMillis))

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 6.dp)
                                    .testTag("component_card_${sched.componentName}"),
                                shape = RoundedCornerShape(26.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = if (isDark || isCrit || isWarm) null else androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BentoBorderColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(iconBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.OilBarrel,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(colorIndicator)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = sched.componentName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = cardText,
                                        maxLines = 1,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = if (kmRemaining >= 0) {
                                            "${NumberFormat.getNumberInstance(indonesiaLocale).format(kmRemaining)} KM sisa"
                                        } else {
                                            "Lewat ${NumberFormat.getNumberInstance(indonesiaLocale).format(-kmRemaining)} KM!"
                                        },
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = if (kmRemaining >= 0) cardText else com.example.ui.theme.CriticalRed,
                                        maxLines = 1,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    LinearProgressIndicator(
                                        progress = { percentage.coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(CircleShape),
                                        color = colorIndicator,
                                        trackColor = cardText.copy(alpha = 0.15f)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Display beautiful prediction based on average daily mileage!
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(cardText.copy(alpha = 0.08f))
                                            .padding(8.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = if (kmRemaining > 0) "PRAKIRAAN JATUH TEMPO" else "STATUS PELUMAS",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 8.sp,
                                                    letterSpacing = 0.5.sp
                                                ),
                                                color = cardText.copy(alpha = 0.6f)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (kmRemaining > 0) {
                                                    "$dateStr\n(~$daysRemaining hari lagi)"
                                                } else {
                                                    "Segera Ganti Oli!\nUsia pakai habis"
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    lineHeight = 12.sp
                                                ),
                                                color = if (kmRemaining > 0) cardText else com.example.ui.theme.CriticalRed
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            serviceCostText = ""
                                            serviceNotesText = ""
                                            customOdoText = currentProfile.currentOdometer.toString()
                                            showServiceDialog = sched
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isCrit || isWarm || isDark) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary,
                                            contentColor = if (isCrit || isWarm || isDark) Color.Black else Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(32.dp)
                                            .testTag("service_done_button_${sched.componentName}")
                                    ) {
                                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Servis", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Header for Non-Oli components / other parts
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Suku Cadang & Komponen Lain",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    TextButton(
                        onClick = { showAddCustomDialog = true },
                        modifier = Modifier.testTag("add_custom_component_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (otherSchedules.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada komponen lain yang terpantau.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val pairs = otherSchedules.chunked(2)
                pairs.forEach { pair ->
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            pair.forEach { sched ->
                                val usedKm = (currentProfile.currentOdometer - sched.lastServicedKm).coerceAtLeast(0)
                                val kmRemaining = sched.intervalKm - usedKm
                                val percentage = (usedKm.toFloat() / sched.intervalKm.toFloat()).coerceIn(0f, 1.2f)

                                val status = when {
                                    percentage >= 1.0f -> ComponentStatus.CRITICAL
                                    percentage >= 0.8f -> ComponentStatus.WARNING
                                    else -> ComponentStatus.OK
                                }

                                val colorIndicator = when (status) {
                                    ComponentStatus.CRITICAL -> CriticalRed
                                    ComponentStatus.WARNING -> WarningAmber
                                    ComponentStatus.OK -> OkGreen
                                }

                                val isWarm = status == ComponentStatus.WARNING
                                val isCrit = status == ComponentStatus.CRITICAL

                                val (cardBg, cardText, iconBg) = when {
                                    isCrit -> Triple(
                                        if (isDark) Color(0xFF5A1A1A) else Color(0xFFFFDAD7),
                                        if (isDark) Color(0xFFFFDAD7) else Color(0xFF410002),
                                        if (isDark) Color(0xFFBA1A1A) else Color(0xFFBA1A1A)
                                    )
                                    isWarm -> Triple(
                                        if (isDark) Color(0xFF433E2B) else com.example.ui.theme.BentoYellowBg,
                                        if (isDark) Color(0xFFECE1C4) else com.example.ui.theme.BentoYellowText,
                                        if (isDark) Color(0xFF705D00) else com.example.ui.theme.BentoYellowIconText
                                    )
                                    sched.componentName.contains("Aki", ignoreCase = true) || sched.componentName.contains("Busi", ignoreCase = true) -> Triple(
                                        if (isDark) Color(0xFF1B2620) else com.example.ui.theme.BentoGreenBg,
                                        if (isDark) Color(0xFFD7E8DE) else com.example.ui.theme.BentoGreenText,
                                        if (isDark) Color(0xFF3D664D) else com.example.ui.theme.BentoGreenIconBg
                                    )
                                    else -> Triple(
                                        if (isDark) MaterialTheme.colorScheme.surface else Color.White,
                                        MaterialTheme.colorScheme.onSurface,
                                        if (isDark) Color(0xFF2A2D35) else Color(0xFFF1F0F4)
                                    )
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 6.dp)
                                        .testTag("component_card_${sched.componentName}"),
                                    shape = RoundedCornerShape(26.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardBg),
                                    border = if (isDark || isCrit || isWarm) null else androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BentoBorderColor),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(iconBg),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                val vectorIcon = when {
                                                    sched.componentName.contains("Aki", ignoreCase = true) -> Icons.Default.FlashOn
                                                    sched.componentName.contains("Busi", ignoreCase = true) -> Icons.Default.Bolt
                                                    sched.componentName.contains("Filter", ignoreCase = true) || sched.componentName.contains("Saringan", ignoreCase = true) -> Icons.Default.FilterAlt
                                                    else -> Icons.Default.Settings
                                                }
                                                Icon(
                                                    imageVector = vectorIcon,
                                                    contentDescription = null,
                                                    tint = if (isDark || isCrit || isWarm) Color.White else MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(colorIndicator)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Text(
                                            text = sched.componentName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = cardText,
                                            maxLines = 1,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = if (kmRemaining >= 0) {
                                                "${NumberFormat.getNumberInstance(indonesiaLocale).format(kmRemaining)} KM sisa"
                                            } else {
                                                "Lewat ${NumberFormat.getNumberInstance(indonesiaLocale).format(-kmRemaining)} KM!"
                                            },
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                            color = if (kmRemaining >= 0) cardText else com.example.ui.theme.CriticalRed,
                                            maxLines = 1,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        LinearProgressIndicator(
                                            progress = { percentage.coerceIn(0f, 1f) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .clip(CircleShape),
                                            color = colorIndicator,
                                            trackColor = cardText.copy(alpha = 0.15f)
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Button(
                                            onClick = {
                                                serviceCostText = ""
                                                serviceNotesText = ""
                                                customOdoText = currentProfile.currentOdometer.toString()
                                                showServiceDialog = sched
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isCrit || isWarm || isDark) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary,
                                                contentColor = if (isCrit || isWarm || isDark) Color.Black else Color.White
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(32.dp)
                                                .testTag("service_done_button_${sched.componentName}")
                                        ) {
                                            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Servis", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                        }
                                    }
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION FOR DIALOG MODALS ---

        // A. Edit Odometer Dialog
        if (showOdometerDialog) {
            AlertDialog(
                onDismissRequest = { showOdometerDialog = false },
                title = { Text("Perbarui Odometer Motor", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            "Masukkan kilometer odometer terbaru saat ini untuk menyesuaikan sisa pakai saringan udara, oli, & busi.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        OutlinedTextField(
                            value = odoInputText,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) odoInputText = it
                            },
                            label = { Text("Odometer Terbaru (KM)") },
                            suffix = { Text("KM") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("dialog_odometer_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val newOdo = odoInputText.toIntOrNull()
                            if (newOdo != null && newOdo >= currentProfile.currentOdometer) {
                                viewModel.updateOdometer(newOdo)
                                showOdometerDialog = false
                            }
                        },
                        modifier = Modifier.testTag("dialog_odometer_confirm")
                    ) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showOdometerDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }

        // A.2 Edit Average daily KM Dialog
        if (showAverageKmDialog) {
            AlertDialog(
                onDismissRequest = { showAverageKmDialog = false },
                title = { Text("Atur Jarak Tempuh Harian", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            "Masukkan estimasi jarak perjalanan harian Anda dalam kilometer. Data ini membantu menghitung prakiraan tanggal jatuh tempo perawatan secara akurat.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        OutlinedTextField(
                            value = averageKmInputText,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) averageKmInputText = it
                            },
                            label = { Text("Rata-rata Jarak (KM / Hari)") },
                            suffix = { Text("KM / Hari") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("dialog_average_km_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val newAvg = averageKmInputText.toIntOrNull()
                            if (newAvg != null && newAvg > 0) {
                                viewModel.updateAverageDailyKm(newAvg)
                                showAverageKmDialog = false
                            }
                        },
                        modifier = Modifier.testTag("dialog_average_km_confirm")
                    ) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAverageKmDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }

        // B. Log Completed Service Dialog
        if (showServiceDialog != null) {
            val sched = showServiceDialog!!
            var inputError by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showServiceDialog = null },
                title = { Text("Catat Servis: ${sched.componentName}", fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            "Mencatat penggantian baru akan menyetel ulang riwayat sisa pemakaian ke nol berdasarkan kilometer pencatatan.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = customOdoText,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) customOdoText = it
                            },
                            label = { Text("Kilometer Saat Servis (KM)") },
                            suffix = { Text("KM") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("dialog_service_odo_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = serviceCostText,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) serviceCostText = it
                            },
                            label = { Text("Biaya Spare part / Servis (Suku Cadang)") },
                            prefix = { Text("Rp ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("dialog_service_cost_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = serviceNotesText,
                            onValueChange = { serviceNotesText = it },
                            label = { Text("Catatan Mekanik / Merek Suku Cadang") },
                            placeholder = { Text("Contoh: Oli Yamalube Sport 10w-40, busi iridium.") },
                            modifier = Modifier.fillMaxWidth().testTag("dialog_service_notes_input")
                        )

                        if (inputError.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(inputError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        modifier = Modifier.testTag("dialog_service_confirm"),
                        onClick = {
                            val odoVal = customOdoText.toIntOrNull()
                            val costVal = serviceCostText.toDoubleOrNull() ?: 0.0
                            if (odoVal == null || odoVal < sched.lastServicedKm) {
                                inputError = "Kilometer servis tidak boleh kosong atau lebih kecil dari servis sebelumnya (${sched.lastServicedKm} KM)."
                            } else {
                                viewModel.logServiceCompleted(sched.id, costVal, serviceNotesText, odoVal)
                                showServiceDialog = null
                            }
                        }
                    ) {
                        Text("Simpan Riwayat")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showServiceDialog = null }) {
                        Text("Batal")
                    }
                }
            )
        }

        // C. Add Custom Component Schedule Dialog
        if (showAddCustomDialog) {
            var inputError by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAddCustomDialog = false },
                title = { Text("Tambah Komponen Baru", fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            "Tambahkan suku cadang kustom untuk dilacak kesehatannya secara visual.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            label = { Text("Nama Komponen / Suku Cadang") },
                            placeholder = { Text("Contoh: Aki, Ban Belakang, Kampas Ganda") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("dialog_custom_name")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = customIntKm,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) customIntKm = it
                            },
                            label = { Text("Interval Servis Berkala (KM)") },
                            suffix = { Text("KM") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("dialog_custom_interval_km")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = customIntMonth,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) customIntMonth = it
                            },
                            label = { Text("Interval Servis Berkala (Bulan)") },
                            suffix = { Text("Bulan") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("dialog_custom_interval_months")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = customLastKm,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) customLastKm = it
                            },
                            label = { Text("Terakhir Diganti Pada Kilometer") },
                            suffix = { Text("KM") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("dialog_custom_last_km")
                        )

                        if (inputError.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(inputError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        modifier = Modifier.testTag("dialog_custom_confirm"),
                        onClick = {
                            val kmInt = customIntKm.toIntOrNull() ?: 0
                            val monthInt = customIntMonth.toIntOrNull() ?: 1
                            val lastKm = customLastKm.toIntOrNull() ?: currentProfile.currentOdometer

                            if (customName.isBlank()) {
                                inputError = "Nama komponen tidak boleh kosong."
                            } else if (kmInt <= 0) {
                                inputError = "Interval KM harus berupa angka positif."
                            } else if (lastKm > currentProfile.currentOdometer) {
                                inputError = "Kilometer penggantian terakhir tidak boleh melebihi odometer saat ini (${currentProfile.currentOdometer} KM)."
                            } else {
                                viewModel.addCustomSchedule(customName, kmInt, monthInt, lastKm)
                                // Clean files
                                customName = ""
                                customIntKm = ""
                                customIntMonth = ""
                                customLastKm = ""
                                inputError = ""
                                showAddCustomDialog = false
                            }
                        }
                    ) {
                        Text("Tambah")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        customName = ""
                        customIntKm = ""
                        customIntMonth = ""
                        customLastKm = ""
                        inputError = ""
                        showAddCustomDialog = false
                    }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}
