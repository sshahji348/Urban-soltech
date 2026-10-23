package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.ModeFanOff
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.WindPower
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AcLoad
import com.example.data.model.EquipmentItem
import com.example.data.model.InverterType
import com.example.data.model.SolarQuotation
import com.example.data.model.SolarSystemInputs
import com.example.data.repository.AuthRepository
import com.example.data.repository.PdfQuotationGenerator
import com.example.data.repository.SolarCalculatorEngine
import com.example.ui.components.EmergencyBannerCard
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GreenLive
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.SolarYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextOnYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SolarCalculatorScreen(
    authRepository: AuthRepository
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Input States
    var monthlyUnitsText by remember { mutableStateOf("950") }
    var selectedInverterType by remember { mutableStateOf(InverterType.HYBRID) }

    // AC Loads
    var acCount1Ton by remember { mutableIntStateOf(0) }
    var acCount1_5Ton by remember { mutableIntStateOf(2) }
    var acCount2Ton by remember { mutableIntStateOf(1) }

    // Standard Appliances
    var fansCount by remember { mutableIntStateOf(8) }
    var tvsCount by remember { mutableIntStateOf(2) }
    var fridgesCount by remember { mutableIntStateOf(2) }

    // UI Steps: 0 = Input, 1 = Calculating Simulation, 2 = Quotation Result
    var currentStep by remember { mutableIntStateOf(0) }
    var quotationResult by remember { mutableStateOf<SolarQuotation?>(null) }

    // Calculation simulation state
    var calculationProgress by remember { mutableFloatStateOf(0f) }
    var simulationStageText by remember { mutableStateOf("Initializing engineering simulation...") }
    var isSimulatingFast by remember { mutableStateOf(false) }

    // Simulation effect
    LaunchedEffect(currentStep) {
        if (currentStep == 1) {
            val stages = listOf(
                0.15f to "Analyzing regional solar irradiance & PSH factor (5.1 PSH)...",
                0.35f to "Calculating connected AC inductive loads & surge capacity...",
                0.55f to "Sizing ${selectedInverterType.displayName} & battery bank...",
                0.75f to "Computing Tier-1 bifacial panels & Galvanized L2/L3 structure...",
                0.90f to "Optimizing DC cabling, circuit protection & DISCO net-metering...",
                1.00f to "Quotation ready! Formatted engineering report generated."
            )

            for ((targetProgress, message) in stages) {
                simulationStageText = message
                val stepCount = 10
                val progressIncrement = (targetProgress - calculationProgress) / stepCount
                val delayTime = if (isSimulatingFast) 80L else 300L
                for (i in 0 until stepCount) {
                    calculationProgress += progressIncrement
                    delay(delayTime)
                }
            }

            // Calculation completed, generate quotation
            val units = monthlyUnitsText.toIntOrNull() ?: 800
            val acLoads = listOf(
                AcLoad(1.0, acCount1Ton),
                AcLoad(1.5, acCount1_5Ton),
                AcLoad(2.0, acCount2Ton)
            )
            val inputs = SolarSystemInputs(
                monthlyUnitsKwh = units,
                inverterType = selectedInverterType,
                acLoads = acLoads,
                fansCount = fansCount,
                tvsCount = tvsCount,
                refrigeratorsCount = fridgesCount
            )
            val quote = SolarCalculatorEngine.calculateQuotation(
                inputs = inputs,
                userEmail = authRepository.currentUser.value?.email ?: "sshahji348@gmail.com"
            )
            quotationResult = quote
            authRepository.saveQuotation(quote)
            currentStep = 2
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .testTag("solar_calculator_screen")
    ) {
        when (currentStep) {
            0 -> {
                // Input Form Screen
                CalculatorInputForm(
                    monthlyUnitsText = monthlyUnitsText,
                    onMonthlyUnitsChange = { monthlyUnitsText = it },
                    selectedInverterType = selectedInverterType,
                    onInverterTypeChange = { selectedInverterType = it },
                    acCount1Ton = acCount1Ton,
                    onAc1TonChange = { acCount1Ton = it.coerceAtLeast(0) },
                    acCount1_5Ton = acCount1_5Ton,
                    onAc1_5TonChange = { acCount1_5Ton = it.coerceAtLeast(0) },
                    acCount2Ton = acCount2Ton,
                    onAc2TonChange = { acCount2Ton = it.coerceAtLeast(0) },
                    fansCount = fansCount,
                    onFansChange = { fansCount = it.coerceAtLeast(0) },
                    tvsCount = tvsCount,
                    onTvsChange = { tvsCount = it.coerceAtLeast(0) },
                    fridgesCount = fridgesCount,
                    onFridgesChange = { fridgesCount = it.coerceAtLeast(0) },
                    onStartCalculation = {
                        calculationProgress = 0f
                        currentStep = 1
                    }
                )
            }
            1 -> {
                // Animated Engineering Sizing Simulation Screen
                CalculationSimulationView(
                    progress = calculationProgress,
                    statusText = simulationStageText,
                    isFast = isSimulatingFast,
                    onToggleFast = { isSimulatingFast = !isSimulatingFast },
                    onSkip = {
                        calculationProgress = 1f
                        val units = monthlyUnitsText.toIntOrNull() ?: 800
                        val quote = SolarCalculatorEngine.calculateQuotation(
                            inputs = SolarSystemInputs(
                                monthlyUnitsKwh = units,
                                inverterType = selectedInverterType,
                                acLoads = listOf(
                                    AcLoad(1.0, acCount1Ton),
                                    AcLoad(1.5, acCount1_5Ton),
                                    AcLoad(2.0, acCount2Ton)
                                ),
                                fansCount = fansCount,
                                tvsCount = tvsCount,
                                refrigeratorsCount = fridgesCount
                            ),
                            userEmail = authRepository.currentUser.value?.email ?: "sshahji348@gmail.com"
                        )
                        quotationResult = quote
                        authRepository.saveQuotation(quote)
                        currentStep = 2
                    }
                )
            }
            2 -> {
                // Quotation Result & PDF Generator Screen
                quotationResult?.let { quote ->
                    QuotationResultView(
                        quotation = quote,
                        onRecalculate = { currentStep = 0 },
                        onExportPdf = {
                            val pdfFile = PdfQuotationGenerator.generatePdfQuotation(context, quote)
                            PdfQuotationGenerator.viewPdf(context, pdfFile)
                            Toast.makeText(context, "Quotation PDF generated successfully!", Toast.LENGTH_SHORT).show()
                        },
                        onSharePdf = {
                            val pdfFile = PdfQuotationGenerator.generatePdfQuotation(context, quote)
                            PdfQuotationGenerator.sharePdf(context, pdfFile)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorInputForm(
    monthlyUnitsText: String,
    onMonthlyUnitsChange: (String) -> Unit,
    selectedInverterType: InverterType,
    onInverterTypeChange: (InverterType) -> Unit,
    acCount1Ton: Int,
    onAc1TonChange: (Int) -> Unit,
    acCount1_5Ton: Int,
    onAc1_5TonChange: (Int) -> Unit,
    acCount2Ton: Int,
    onAc2TonChange: (Int) -> Unit,
    fansCount: Int,
    onFansChange: (Int) -> Unit,
    tvsCount: Int,
    onTvsChange: (Int) -> Unit,
    fridgesCount: Int,
    onFridgesChange: (Int) -> Unit,
    onStartCalculation: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SolarYellow),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Solar Sizing Calculator",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Provide your monthly units and appliances for an exact kW recommendation, equipment breakdown, and payback calculation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Input 1: Monthly Units (kWh)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                border = CardDefaults.outlinedCardBorder().copy(width = 0.8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Monthly WAPDA Electricity Units",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SolarYellow
                    )
                    Text(
                        text = "Check your recent summer electricity bill for total units consumed (kWh)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = monthlyUnitsText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 5) {
                                onMonthlyUnitsChange(input)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("monthly_units_input"),
                        label = { Text("Units / Month (kWh)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SolarYellow,
                            unfocusedBorderColor = SlateBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = SolarYellow
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val unitsVal = monthlyUnitsText.toIntOrNull() ?: 800
                    Slider(
                        value = unitsVal.toFloat().coerceIn(100f, 4000f),
                        onValueChange = { onMonthlyUnitsChange(it.toInt().toString()) },
                        valueRange = 100f..4000f,
                        colors = SliderDefaults.colors(
                            thumbColor = SolarYellow,
                            activeTrackColor = SolarYellow,
                            inactiveTrackColor = SlateSurfaceVariant
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("100 Units (Small)", fontSize = 11.sp, color = TextMuted)
                        Text("1,500 Units (Standard)", fontSize = 11.sp, color = TextMuted)
                        Text("4,000+ Units (Heavy)", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        }

        // Input 2: Inverter Type
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                border = CardDefaults.outlinedCardBorder().copy(width = 0.8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. Select Inverter & System Architecture",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SolarYellow
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    InverterType.values().forEach { type ->
                        val isSelected = selectedInverterType == type
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) SolarYellow else SlateBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onInverterTypeChange(type) }
                                .testTag("inverter_option_${type.name}"),
                            color = if (isSelected) SlateSurfaceElevated else SlateSurfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, if (isSelected) SolarYellow else SlateBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(SolarYellow)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = type.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) SolarYellow else Color.White
                                    )
                                    Text(
                                        text = type.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Input 3: Air Conditioners (1, 1.5, 2.0 ton)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                border = CardDefaults.outlinedCardBorder().copy(width = 0.8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AcUnit,
                            contentDescription = "AC",
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "3. Air Conditioners (ACs)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SolarYellow
                        )
                    }
                    Text(
                        text = "Specify how many units of each tonnage you plan to run on solar",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ApplianceCounterRow(
                        title = "1.0 Ton Inverter AC",
                        subtitle = "~1.2 kW running power",
                        count = acCount1Ton,
                        onCountChange = onAc1TonChange,
                        testTagPrefix = "ac_1_ton"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ApplianceCounterRow(
                        title = "1.5 Ton Inverter AC",
                        subtitle = "~1.8 kW running power (Most common)",
                        count = acCount1_5Ton,
                        onCountChange = onAc1_5TonChange,
                        testTagPrefix = "ac_1_5_ton"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ApplianceCounterRow(
                        title = "2.0 Ton Inverter AC",
                        subtitle = "~2.4 kW running power",
                        count = acCount2Ton,
                        onCountChange = onAc2TonChange,
                        testTagPrefix = "ac_2_ton"
                    )
                }
            }
        }

        // Input 4: Standard Appliances (Fans, TVs, Refrigerators)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                border = CardDefaults.outlinedCardBorder().copy(width = 0.8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "4. Other Heavy & Daily Appliances",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SolarYellow
                    )
                    Text(
                        text = "Count of ceiling fans, televisions, and deep freezers/refrigerators",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ApplianceCounterRow(
                        title = "Ceiling & Exhaust Fans",
                        subtitle = "75W per fan",
                        count = fansCount,
                        onCountChange = onFansChange,
                        icon = Icons.Default.WindPower,
                        testTagPrefix = "fans"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ApplianceCounterRow(
                        title = "Televisions / LEDs",
                        subtitle = "100W per TV",
                        count = tvsCount,
                        onCountChange = onTvsChange,
                        icon = Icons.Default.Tv,
                        testTagPrefix = "tvs"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ApplianceCounterRow(
                        title = "Refrigerators & Freezers",
                        subtitle = "350W per compressor",
                        count = fridgesCount,
                        onCountChange = onFridgesChange,
                        icon = Icons.Default.Kitchen,
                        testTagPrefix = "fridges"
                    )
                }
            }
        }

        // Primary Calculate Action Button
        item {
            Button(
                onClick = onStartCalculation,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("start_sizing_calculation_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SolarYellow,
                    contentColor = TextOnYellow
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ElectricBolt,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generate Official Quotation & System Sizing",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        item {
            EmergencyBannerCard()
        }
    }
}

@Composable
fun ApplianceCounterRow(
    title: String,
    subtitle: String,
    count: Int,
    onCountChange: (Int) -> Unit,
    icon: ImageVector? = null,
    testTagPrefix: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = SlateSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = SolarAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onCountChange(count - 1) },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(SlateSurfaceElevated)
                        .testTag("${testTagPrefix}_decrement")
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "$count",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SolarYellow,
                    modifier = Modifier
                        .padding(horizontal = 14.dp)
                        .testTag("${testTagPrefix}_count")
                )

                IconButton(
                    onClick = { onCountChange(count + 1) },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(SolarYellow)
                        .testTag("${testTagPrefix}_increment")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CalculationSimulationView(
    progress: Float,
    statusText: String,
    isFast: Boolean,
    onToggleFast: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("calculation_simulation_view"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(SlateSurface)
                .border(2.dp, SolarYellow, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = "Calculating",
                tint = SolarYellow,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Urban Soltech Engineering Engine",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SolarYellow
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = SolarYellow,
            trackColor = SlateSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${(progress * 100).toInt()}% Complete",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Simulating load curves...",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onToggleFast,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SolarAmber),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isFast) "Standard Speed" else "Turbo (Fast Simulation)")
            }

            Button(
                onClick = onSkip,
                colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceElevated),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Instant Result", color = Color.White)
            }
        }
    }
}

@Composable
fun QuotationResultView(
    quotation: SolarQuotation,
    onRecalculate: () -> Unit,
    onExportPdf: () -> Unit,
    onSharePdf: () -> Unit
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 0
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("quotation_result_view"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quotation Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quotation_hero_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(SolarYellow, SolarAmber))
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = GreenLive.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "SYSTEM SIZED & VERIFIED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = GreenLive,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = quotation.id,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "RECOMMENDED SYSTEM:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = "${quotation.recommendedSystemKw} kW ${quotation.inverterType.displayName}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = SolarYellow
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4-box stat grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuotationMetricBox(
                            label = "Solar Panels",
                            value = "${quotation.panelCount} Plates",
                            subtext = "585W Bifacial",
                            modifier = Modifier.weight(1f)
                        )
                        QuotationMetricBox(
                            label = "Inverter",
                            value = "${quotation.inverterRatingKw.toInt()} kW",
                            subtext = quotation.inverterType.name,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuotationMetricBox(
                            label = "Monthly Gen",
                            value = "${quotation.estimatedMonthlyGenerationKwh.toInt()} kWh",
                            subtext = "Units/Month",
                            modifier = Modifier.weight(1f)
                        )
                        QuotationMetricBox(
                            label = "ROI Payback",
                            value = "${quotation.paybackPeriodYears} Yrs",
                            subtext = "Return Period",
                            highlightColor = GreenLive,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (quotation.batteryDescription != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SlateSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BatteryChargingFull,
                                    contentDescription = "Battery",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Dedicated Battery Bank Included",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = quotation.batteryDescription,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons: PDF Export & Share
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onExportPdf,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("export_pdf_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SolarYellow,
                        contentColor = TextOnYellow
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View / Download PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = onSharePdf,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("share_pdf_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SlateSurfaceElevated,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Quotation", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Financial & Bill Savings Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                border = CardDefaults.outlinedCardBorder().copy(width = 0.8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FINANCIAL SAVINGS & RETURN ON INVESTMENT",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = GreenLive
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    FinancialRow(
                        label = "Total System Cost (Turnkey)",
                        value = "PKR ${numberFormat.format(quotation.totalCostPkr)}",
                        isBold = true,
                        valueColor = SolarYellow
                    )
                    FinancialRow(
                        label = "Estimated Monthly Bill Savings",
                        value = "~PKR ${numberFormat.format(quotation.monthlySavingsPkr)} / month",
                        valueColor = GreenLive
                    )
                    FinancialRow(
                        label = "Annual Electricity Savings",
                        value = "~PKR ${numberFormat.format(quotation.annualSavingsPkr)} / year",
                        valueColor = GreenLive
                    )
                    FinancialRow(
                        label = "Capital Investment Payback Period",
                        value = "${quotation.paybackPeriodYears} Years",
                        isBold = true,
                        valueColor = Color.White
                    )
                    FinancialRow(
                        label = "25-Year Net Electricity Earnings",
                        value = "PKR ${numberFormat.format(quotation.twentyFiveYearSavingsPkr)}",
                        isBold = true,
                        valueColor = CyanAccent
                    )
                }
            }
        }

        // Itemized Equipment Table Section Header
        item {
            Text(
                text = "ITEMIZED EQUIPMENT & TURNKEY COST",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Equipment items list
        items(quotation.itemizedEquipment) { item ->
            EquipmentCardRow(item = item)
        }

        // Recalculate Button
        item {
            OutlinedButton(
                onClick = onRecalculate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("recalculate_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SolarYellow),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Modify Appliances / Recalculate", fontWeight = FontWeight.Bold)
            }
        }

        item {
            EmergencyBannerCard()
        }
    }
}

@Composable
fun QuotationMetricBox(
    label: String,
    value: String,
    subtext: String,
    highlightColor: Color = SolarYellow,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = SlateSurfaceVariant,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = highlightColor)
            Text(text = subtext, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
fun FinancialRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    valueColor: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = valueColor
        )
    }
}

@Composable
fun EquipmentCardRow(item: EquipmentItem) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 0
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        border = CardDefaults.outlinedCardBorder().copy(width = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = SolarYellow,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
                Text(
                    text = "Qty: ${item.quantity}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "PKR ${numberFormat.format(item.totalPricePkr)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
