package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.MotorcycleViewModel

@Composable
fun OnboardingScreen(
    viewModel: MotorcycleViewModel,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(1) }
    val scrollState = rememberScrollState()

    // Form flow state collectors
    val name by viewModel.onboardingName.collectAsState()
    val brand by viewModel.onboardingBrand.collectAsState()
    val model by viewModel.onboardingModel.collectAsState()
    val plate by viewModel.onboardingPlate.collectAsState()
    val odometer by viewModel.onboardingOdometer.collectAsState()
    val type by viewModel.onboardingType.collectAsState()
    val averageKm by viewModel.onboardingAverageKm.collectAsState()

    var validationError by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Logo & Stepper Indicator
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.TwoWheeler,
                    contentDescription = "Logo MotoCare",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MotoCare",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Professional Progress Stepper Dots
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..3) {
                    val isSelected = step >= i
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (step == i) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Display slide dependent on step
            when (step) {
                1 -> {
                    // Slide 1: Welcome & Value Prop
                    Text(
                        text = "Rawat Motor Anda\nTanpa Lupa Lagi",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 36.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Selamat datang di MotoCare. Solusi digital terbaik untuk mencatat jadwal servis rutin, melacak penggantian suku cadang, dan memantau kesehatan motor kesayangan Anda agar tetap prima.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            lineHeight = 22.sp
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.6f)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Render our generated illustration
                            Image(
                                painter = painterResource(id = R.drawable.img_onboarding_banner_1781806186188),
                                contentDescription = "Onboarding banner",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(24.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.4f)
                                            )
                                        )
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { step = 2 },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("onboarding_start_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = "Mulai Sekarang",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
                2 -> {
                    // Slide 2: Main Profile Fields
                    Text(
                        text = "Profil Motor Anda",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "Isi detail motor Anda di bawah ini untuk memulai pemantauan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { viewModel.updateOnboardingFields(name = it) },
                        label = { Text("Nama Pemilik") },
                        placeholder = { Text("Contoh: Kak Julinto, Budi") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_motor_name"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = brand,
                        onValueChange = { viewModel.updateOnboardingFields(brand = it) },
                        label = { Text("Merek Motor") },
                        placeholder = { Text("Contoh: Honda, Yamaha, Suzuki, Vespa") },
                        leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_motor_brand"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = model,
                        onValueChange = { viewModel.updateOnboardingFields(model = it) },
                        label = { Text("Tipe / Model") },
                        placeholder = { Text("Contoh: Vario 160, NMax, Supra X") },
                        leadingIcon = { Icon(Icons.Default.Motorcycle, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_motor_model"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = plate,
                        onValueChange = { viewModel.updateOnboardingFields(plate = it) },
                        label = { Text("Nomor Polisi (Opsional)") },
                        placeholder = { Text("Contoh: B 1234 CD") },
                        leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_motor_plate"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = odometer,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() }) {
                                viewModel.updateOnboardingFields(odometer = it)
                            }
                        },
                        label = { Text("Kilometer Saat Ini (Odometer)") },
                        placeholder = { Text("Mulai dari Km berapa?") },
                        leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null) },
                        suffix = { Text("KM") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_motor_odometer"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = averageKm,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() }) {
                                viewModel.updateOnboardingFields(averageKm = it)
                            }
                        },
                        label = { Text("Rata-rata KM Harian") },
                        placeholder = { Text("Contoh: 25 KM harian") },
                        leadingIcon = { Icon(Icons.Default.Timeline, contentDescription = null) },
                        suffix = { Text("KM/Hari") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_motor_average_km"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    if (validationError.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = validationError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { step = 1 },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .padding(end = 8.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Kembali")
                        }

                        Button(
                            onClick = {
                                if (name.isBlank() || brand.isBlank() || model.isBlank()) {
                                    validationError = "Mohon lengkapi Nama, Merek, dan Model motor Anda."
                                } else if (odometer.toIntOrNull() == null) {
                                    validationError = "Harap masukkan angka odometer valid."
                                } else {
                                    validationError = ""
                                    step = 3
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .padding(start = 8.dp)
                                .testTag("onboarding_next_step2"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Lanjut")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                3 -> {
                    // Slide 3: Select Motorcycle Transmission Type
                    Text(
                        text = "Jenis Transmisi Motor",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "Sistem akan menyesuaikan setelan perawatan (seperti oli gardan dan rantai) berdasarkan tipe transmisi.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Type Options Row
                    val options = listOf("Matic", "Bebek", "Sport")
                    options.forEach { opt ->
                        val isSelected = type == opt
                        Card(
                            onClick = { viewModel.updateOnboardingFields(type = opt) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            border = CardDefaults.outlinedCardBorder(enabled = isSelected),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .testTag("type_card_$opt")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (opt) {
                                        "Matic" -> Icons.Default.ElectricScooter
                                        "Bebek" -> Icons.Default.ElectricBike
                                        else -> Icons.Default.SportsMotorsports
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(36.dp)
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = opt,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = when (opt) {
                                            "Matic" -> "Dilengkapi jadual oli transmisi/gardan & CVT belt."
                                            "Bebek" -> "Sistem rantai manual. Dilengkapi setelan rantai berkala."
                                            else -> "Motor kopling/Sport. Setelan rantai & pelumasan intensif."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }

                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.updateOnboardingFields(type = opt) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Notice Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Aplikasi ini menggunakan standar perawatan regional Indonesia (Km & Bulan) demi kecocokan suspensi dan komponen.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { step = 2 },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .padding(end = 8.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Kembali")
                        }

                        Button(
                            onClick = { viewModel.completeOnboarding() },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .padding(start = 8.dp)
                                .testTag("onboarding_finish_button"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Selesai")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
