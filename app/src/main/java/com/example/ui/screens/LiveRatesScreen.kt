package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.platform.LocalContext
import com.example.data.repository.RateNotificationManager
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketRate
import com.example.data.repository.MarketRatesRepository
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
import com.example.ui.theme.SolarYellowBright
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextOnYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun LiveRatesScreen(
    ratesRepository: MarketRatesRepository,
    notificationManager: RateNotificationManager,
    onNavigateToCalculator: () -> Unit
) {
    val context = LocalContext.current
    val rates by ratesRepository.ratesState.collectAsState()
    val secondsLeft by ratesRepository.secondsToNextRefresh.collectAsState()
    val isRefreshing by ratesRepository.isRefreshing.collectAsState()
    val isOptedIn by notificationManager.isOptedIn.collectAsState()
    val benchmarkRate = ratesRepository.getMarketBenchmarkRate()
    val lastUpdated = ratesRepository.getFormattedLastUpdated()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            notificationManager.setOptedIn(true)
            Toast.makeText(context, "Hourly rate alerts enabled!", Toast.LENGTH_SHORT).show()
        } else {
            notificationManager.setOptedIn(false)
            Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    val countdownText = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .testTag("live_rates_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Market Benchmark Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("market_benchmark_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(SolarYellow, SolarAmber))
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(GreenLive)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE MARKET INDEX",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = GreenLive
                            )
                        }

                        // Hourly countdown pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = SlateSurfaceVariant,
                            border = CardDefaults.outlinedCardBorder().copy(width = 0.5.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Countdown",
                                    tint = SolarYellowBright,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Auto-refreshes in $countdownText",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Tier-1 Average Benchmark",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "PKR $benchmarkRate",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = SolarYellow
                                )
                                Text(
                                    text = " / Watt",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { ratesRepository.refreshRatesNow() },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SlateSurfaceElevated)
                                .testTag("refresh_rates_button")
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = SolarYellow,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Rates",
                                    tint = SolarYellow
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Refreshed hourly from Pakistan wholesale solar markets (Karachi & Lahore). Rates subject to exchange parity.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Last updated: $lastUpdated",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanAccent
                    )
                }
            }
        }

        // Push Notification Opt-in Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rate_notification_optin_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceElevated),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = if (isOptedIn) Brush.horizontalGradient(listOf(SolarYellow, GreenLive)) else Brush.horizontalGradient(listOf(SlateBorder, SlateBorder))
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isOptedIn) SolarYellow else SlateSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isOptedIn) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                    contentDescription = "Notifications",
                                    tint = if (isOptedIn) Color.Black else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Hourly Rate Alerts",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    if (isOptedIn) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = GreenLive.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = GreenLive,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = if (isOptedIn) "Push alerts arrive hourly on rate refresh" else "Turn on to get notified when rates update",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = isOptedIn,
                            onCheckedChange = { shouldEnable ->
                                if (shouldEnable) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationManager.hasNotificationPermission()) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        notificationManager.setOptedIn(true)
                                        Toast.makeText(context, "Hourly rate alerts enabled!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    notificationManager.setOptedIn(false)
                                    Toast.makeText(context, "Hourly rate alerts muted", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = SolarYellow,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = SlateSurfaceVariant
                            ),
                            modifier = Modifier.testTag("rate_notification_switch")
                        )
                    }

                    if (isOptedIn) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Alerts include new rate & direct link to app",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Button(
                                onClick = {
                                    val topPanel = rates.firstOrNull()
                                    notificationManager.sendRateUpdateNotification(
                                        benchmarkRate = benchmarkRate,
                                        trendDelta = -0.30,
                                        topBrandName = topPanel?.brand ?: "Tier-1 Solar",
                                        topBrandRate = topPanel?.pricePerWattPkr ?: benchmarkRate,
                                        isTest = true
                                    )
                                    Toast.makeText(context, "Sample notification dispatched!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceVariant),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("send_test_notification_btn")
                            ) {
                                Text("Send Test Alert", fontSize = 11.sp, color = SolarYellow, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Section Title: Brand Rates
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TIER-1 BRAND RATES",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${rates.size} Verified Brands",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }

        // Tier-1 Panel Cards
        items(rates, key = { it.id }) { rate ->
            SolarRateItemCard(rate = rate)
        }

        // Quick Call to Action: Calculate System with Live Rates
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calculator_cta_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = "Calculator",
                            tint = SolarYellow,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Size Your System with Today's Rates",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Enter your monthly WAPDA units and AC count to generate an official itemized quotation and return on investment report.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onNavigateToCalculator,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SolarYellow,
                            contentColor = TextOnYellow
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("goto_calculator_btn")
                    ) {
                        Text("Open Solar System Calculator", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Emergency Assistance Helpline Card
        item {
            EmergencyBannerCard()
        }
    }
}

@Composable
fun SolarRateItemCard(rate: MarketRate) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 0
    }
    val singlePlateCost = rate.wattage * rate.pricePerWattPkr

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rate_card_${rate.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        border = CardDefaults.outlinedCardBorder().copy(width = 0.8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Brand & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = rate.brand,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Tier 1",
                        tint = CyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SlateSurfaceVariant
                ) {
                    Text(
                        text = rate.stockStatus,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (rate.stockStatus.contains("Ready")) GreenLive else SolarAmber,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${rate.modelName} • ${rate.technology}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Price Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Price per Watt",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PKR ${rate.pricePerWattPkr}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SolarYellow
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        // Trend indicator
                        if (rate.trendDelta != 0.0) {
                            val isDrop = rate.trendDelta < 0
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isDrop) GreenLive.copy(alpha = 0.2f) else SolarAmber.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = if (isDrop) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                                    contentDescription = "Trend",
                                    tint = if (isDrop) GreenLive else SolarAmber,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${if (rate.trendDelta > 0) "+" else ""}${rate.trendDelta}",
                                    fontSize = 10.sp,
                                    color = if (isDrop) GreenLive else SolarAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Approx. per Panel (${rate.wattage}W)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = "PKR ${numberFormat.format(singlePlateCost)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Specs Pill Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpecBadge(label = "Rating", value = "${rate.wattage} W")
                SpecBadge(label = "Efficiency", value = "${rate.efficiency}%")
                SpecBadge(label = "Warranty", value = "${rate.warrantyYears} Yrs")
            }
        }
    }
}

@Composable
fun SpecBadge(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = SlateSurfaceElevated
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
        }
    }
}
