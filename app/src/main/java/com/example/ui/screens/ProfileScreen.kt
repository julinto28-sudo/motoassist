package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MotorcycleViewModel
import com.example.ui.theme.OkGreen
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MotorcycleViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.profile.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val history by viewModel.history.collectAsState()
    val healthScore by viewModel.healthScore.collectAsState()

    val indonesiaLocale = Locale("id", "ID")
    val formatter = NumberFormat.getNumberInstance(indonesiaLocale)

    var showEditDialog by remember { mutableStateOf(false) }

    // Dialog state holders
    var editName by remember { mutableStateOf("") }
    var editBrand by remember { mutableStateOf("") }
    var editModel by remember { mutableStateOf("") }
    var editPlate by remember { mutableStateOf("") }
    var editType by remember { mutableStateOf("Matic") }
    var editAverageKm by remember { mutableStateOf("") }

    val currentProfile = profile ?: return
    val isDark = isSystemInDarkTheme()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // Title & Header Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Profil Pengguna",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Detail pemilik dan spesifikasi motor terpantau.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        editName = currentProfile.name
                        editBrand = currentProfile.brand
                        editModel = currentProfile.model
                        editPlate = currentProfile.plateNumber
                        editType = currentProfile.type
                        editAverageKm = currentProfile.averageDailyKm.toString()
                        showEditDialog = true
                    },
                    modifier = Modifier
                        .testTag("edit_profile_fab_button")
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profil",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // 1. Avatar Card with owner name
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("profile_avatar_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = currentProfile.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = "Pemilik Kendaraan",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Stats Section: Health & History Count
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Stat 1: Health Score
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1E2022) else Color(0xFFF1F0F4)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "KESEHATAN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$healthScore%",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (healthScore >= 80) OkGreen else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Stat 2: Total Service Actions
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1E2022) else Color(0xFFF1F0F4)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TOTAL SERVIS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${history.size} Kali",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 2. Motorcycle Specifications list
        item {
            Text(
                text = "Informasi Kendaraan",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1E2022) else Color.White
                ),
                border = if (isDark) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE1E2EC)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ProfileItem(
                        icon = Icons.Default.DirectionsCar,
                        label = "Merek Motor",
                        value = currentProfile.brand
                    )
                    DividerHorizontal()

                    ProfileItem(
                        icon = Icons.Default.Motorcycle,
                        label = "Tipe / Model",
                        value = currentProfile.model
                    )
                    DividerHorizontal()

                    ProfileItem(
                        icon = Icons.Default.Pin,
                        label = "Nomor Polisi",
                        value = currentProfile.plateNumber.ifBlank { "-" }
                    )
                    DividerHorizontal()

                    ProfileItem(
                        icon = Icons.Default.Category,
                        label = "Jenis Transmisi",
                        value = currentProfile.type
                    )
                    DividerHorizontal()

                    ProfileItem(
                        icon = Icons.Default.Speed,
                        label = "Odometer",
                        value = "${formatter.format(currentProfile.currentOdometer)} KM"
                    )
                    DividerHorizontal()

                    ProfileItem(
                        icon = Icons.Default.Timeline,
                        label = "Jarak Harian",
                        value = "${currentProfile.averageDailyKm} KM / hari"
                    )
                }
            }
        }

        // 3. App Version & What's New Section
        item {
            Text(
                text = "Tentang Aplikasi",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
            )
        }

        item {
            var showChangelogDialog by remember { mutableStateOf(false) }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1E2022) else Color.White
                ),
                border = if (isDark) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE1E2EC)),
                modifier = Modifier.fillMaxWidth().testTag("about_app_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Versi Aplikasi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "v2.1.0-Premium",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    DividerHorizontal()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Apa Yang Baru?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Lihat pembaruan & fitur terbaru",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = { showChangelogDialog = true },
                            modifier = Modifier.testTag("whats_new_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "Lihat Fitur Baru",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (showChangelogDialog) {
                        AlertDialog(
                            onDismissRequest = { showChangelogDialog = false },
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text("Fitur Baru & Pembaruan", fontWeight = FontWeight.Bold)
                                }
                            },
                            text = {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "v2.1.0 (Pembaruan Terkini)",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.padding(start = 8.dp)
                                        ) {
                                            BulletItem(text = "🔥 Fitur Streak Hari Beruntun: Dapatkan motivasi ekstra dengan terus memperbarui odometer secara rutin tepat waktu.")
                                            BulletItem(text = "🧠 Estimasi Pintar Odometer: Aplikasi dapat secara cerdas menghitung perkiraan kilometer odometer terkini berdasarkan rata-rata harian jika Anda lupa memperbaruinya.")
                                            BulletItem(text = "⚡ Shortcut Cepat Odometer: Perbarui odometer kapan saja langsung dari tombol navigasi tengah di mana pun Anda berada.")
                                            BulletItem(text = "🎨 Peningkatan UI/UX Premium: Tampilan menu profil dan statistik kesehatan kendaraan kini jauh lebih intuitif dan bersih.")
                                        }
                                    }
                                    
                                    Column {
                                        Text(
                                            text = "v2.0.0 (Versi Utama)",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.padding(start = 8.dp)
                                        ) {
                                            BulletItem(text = "🛠️ Sistem Pemantauan Saringan Udara, Busi, dan Oli secara mandiri.")
                                            BulletItem(text = "📊 Riwayat Servis & Penggantian Suku Cadang yang mendetail.")
                                            BulletItem(text = "💡 Notifikasi Kesehatan Suku Cadang dengan kalkulator visual sisa pakai KM.")
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = { showChangelogDialog = false },
                                    modifier = Modifier.testTag("whats_new_close_button")
                                ) {
                                    Text("Mengerti")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Edit Profile Modal Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profil & Kendaraan", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Nama Pemilik") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_name")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = editBrand,
                            onValueChange = { editBrand = it },
                            label = { Text("Merek Motor") },
                            leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_brand")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = editModel,
                            onValueChange = { editModel = it },
                            label = { Text("Tipe / Model") },
                            leadingIcon = { Icon(Icons.Default.Motorcycle, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_model")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = editPlate,
                            onValueChange = { editPlate = it },
                            label = { Text("Nomor Polisi") },
                            leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_plate")
                        )
                    }

                    item {
                        Column {
                            Text(
                                "Jenis Transmisi",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Matic", "Bebek", "Sport").forEach { itemType ->
                                    val isSelected = editType == itemType
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { editType = itemType },
                                        label = { Text(itemType) },
                                        modifier = Modifier.weight(1f).testTag("edit_profile_chip_$itemType")
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = editAverageKm,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) editAverageKm = it
                            },
                            label = { Text("Rata-rata KM Harian") },
                            leadingIcon = { Icon(Icons.Default.Timeline, contentDescription = null) },
                            suffix = { Text("KM/Hari") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_avg_km")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val avgKm = editAverageKm.toIntOrNull() ?: currentProfile.averageDailyKm
                        viewModel.updateFullProfile(
                            name = editName.ifBlank { currentProfile.name },
                            brand = editBrand.ifBlank { currentProfile.brand },
                            model = editModel.ifBlank { currentProfile.model },
                            plateNumber = editPlate,
                            type = editType,
                            averageDailyKm = avgKm
                        )
                        showEditDialog = false
                    },
                    modifier = Modifier.testTag("edit_profile_confirm")
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun ProfileItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DividerHorizontal() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        thickness = 1.dp
    )
}

@Composable
fun BulletItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
