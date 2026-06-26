package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.MotorcycleViewModel

@Composable
fun MainAppContainer(
    viewModel: MotorcycleViewModel,
    modifier: Modifier = Modifier
) {
    val isLoaded by viewModel.isLoaded.collectAsState()
    val profile by viewModel.profile.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var showResetConfirm by remember { mutableStateOf(false) }

    // Dialog state controllers for global odometer update via bottom navbar
    var showOdometerDialog by remember { mutableStateOf(false) }
    var odoInputText by remember { mutableStateOf("") }

    // If profile hasn't loaded from DB yet, show a loading spinner
    if (!isLoaded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Run Onboarding Flow if Setup isn't complete yet or profile is empty
    if (profile == null || !profile!!.isSetupComplete) {
        OnboardingScreen(
            viewModel = viewModel,
            modifier = modifier
        )
    } else {
        val currentProfile = profile!!
        // Main App Layout with M3 Scaffold, Top Header, and Bottom Tab Bar
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                OptInTopAppBar(
                    title = currentProfile.name,
                    onResetRequest = { showResetConfirm = true }
                )
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .testTag("main_bottom_nav"),
                    windowInsets = WindowInsets.navigationBars
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Beranda Status") },
                        label = { Text("Kesehatan") },
                        modifier = Modifier.testTag("nav_tab_health")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.NotificationsActive, contentDescription = "Semua Pengingat") },
                        label = { Text("Pengingat") },
                        modifier = Modifier.testTag("nav_tab_reminders")
                    )

                    // Special center navigation button "+" to directly trigger Odometer Update Dialog
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            odoInputText = currentProfile.currentOdometer.toString()
                            showOdometerDialog = true
                        },
                        icon = {
                            Surface(
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Ubah Odometer",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        label = { Text("Odo") },
                        modifier = Modifier.testTag("nav_tab_add_odometer")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.History, contentDescription = "Riwayat Servis") },
                        label = { Text("Riwayat") },
                        modifier = Modifier.testTag("nav_tab_history")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profil Pengguna") },
                        label = { Text("Profil") },
                        modifier = Modifier.testTag("nav_tab_profile")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToReminders = { selectedTab = 1 }
                    )
                    1 -> RemindersScreen(
                        viewModel = viewModel
                    )
                    2 -> HistoryScreen(
                        viewModel = viewModel
                    )
                    3 -> ProfileScreen(
                        viewModel = viewModel
                    )
                }
            }
        }

        // Global Odometer Update Dialog Modal
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

        // Reset Profile / Re-onboard Dialog
        if (showResetConfirm) {
            AlertDialog(
                onDismissRequest = { showResetConfirm = false },
                title = { Text("Mulai Ulang Aplikasi?", fontWeight = FontWeight.Bold) },
                text = {
                    Text("Langkah ini akan menghapus semua profil motor saat ini beserta seluruh riwayat servis & jadwal suku cadang dalam SQLite database kustom.")
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("dialog_reset_confirm"),
                        onClick = {
                            viewModel.resetApp()
                            selectedTab = 0
                            showResetConfirm = false
                        }
                    ) {
                        Text("Reset & Hapus")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetConfirm = false }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptInTopAppBar(
    title: String,
    onResetRequest: () -> Unit
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        ),
        title = {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TwoWheeler,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                )
            }
        },
        actions = {
            IconButton(
                onClick = onResetRequest,
                modifier = Modifier.testTag("reset_app_button")
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "Reset Setup",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }
    )
}
