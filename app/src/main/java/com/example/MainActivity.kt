package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import com.example.data.repository.AuthRepository
import com.example.data.repository.CommunityRepository
import com.example.data.repository.EmergencyDialer
import com.example.data.repository.MarketRatesRepository
import com.example.ui.components.UrbanSoltechTopBar
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CommunityScreen
import com.example.ui.screens.LiveRatesScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SolarCalculatorScreen
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.SolarYellow
import com.example.ui.theme.TextOnYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrbanSoltechTheme

import com.example.data.repository.RateNotificationManager

class MainActivity : ComponentActivity() {

    private val notificationManager by lazy { RateNotificationManager(applicationContext) }
    private lateinit var ratesRepository: MarketRatesRepository
    private val communityRepository by lazy { CommunityRepository() }
    private val authRepository by lazy { AuthRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ratesRepository = MarketRatesRepository(lifecycleScope, notificationManager)

        val initialTab = intent?.getIntExtra("EXTRA_TARGET_TAB", 0) ?: 0

        setContent {
            UrbanSoltechTheme(darkTheme = true) {
                UrbanSoltechApp(
                    ratesRepository = ratesRepository,
                    communityRepository = communityRepository,
                    authRepository = authRepository,
                    notificationManager = notificationManager,
                    initialTabIndex = initialTab
                )
            }
        }
    }
}

enum class ScreenTab(val title: String) {
    LIVE_RATES("Live Rates"),
    CALCULATOR("Calculator"),
    COMMUNITY("Community"),
    PROFILE("My Account")
}

@Composable
fun UrbanSoltechApp(
    ratesRepository: MarketRatesRepository,
    communityRepository: CommunityRepository,
    authRepository: AuthRepository,
    notificationManager: RateNotificationManager,
    initialTabIndex: Int = 0
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(initialTabIndex) }
    var showAuthScreen by remember { mutableStateOf(false) }

    val currentUser by authRepository.currentUser.collectAsState()

    // Handle back button when not on Home tab
    BackHandler(enabled = selectedTabIndex != 0 || showAuthScreen) {
        if (showAuthScreen) {
            showAuthScreen = false
        } else {
            selectedTabIndex = 0
        }
    }

    if (showAuthScreen) {
        AuthScreen(
            authRepository = authRepository,
            onAuthSuccess = { showAuthScreen = false }
        )
        return
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .statusBarsPadding(),
        topBar = {
            UrbanSoltechTopBar(
                onEmergencyCallClicked = {
                    EmergencyDialer.dialHelpline(context)
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar"),
                containerColor = SlateSurface,
                tonalElevation = 8.dp
            ) {
                // Tab 0: Live Rates
                NavigationBarItem(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTabIndex == 0) Icons.Filled.TrendingUp else Icons.Outlined.TrendingUp,
                            contentDescription = "Live Rates"
                        )
                    },
                    label = { Text("Live Rates", fontSize = 11.sp, fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TextOnYellow,
                        selectedTextColor = SolarYellow,
                        indicatorColor = SolarYellow,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_live_rates")
                )

                // Tab 1: Calculator
                NavigationBarItem(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTabIndex == 1) Icons.Filled.Calculate else Icons.Outlined.Calculate,
                            contentDescription = "Calculator"
                        )
                    },
                    label = { Text("Calculator", fontSize = 11.sp, fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TextOnYellow,
                        selectedTextColor = SolarYellow,
                        indicatorColor = SolarYellow,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_calculator")
                )

                // Tab 2: Community
                NavigationBarItem(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTabIndex == 2) Icons.Filled.Forum else Icons.Outlined.Forum,
                            contentDescription = "Community"
                        )
                    },
                    label = { Text("Community", fontSize = 11.sp, fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TextOnYellow,
                        selectedTextColor = SolarYellow,
                        indicatorColor = SolarYellow,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_community")
                )

                // Tab 3: My Account / Profile
                NavigationBarItem(
                    selected = selectedTabIndex == 3,
                    onClick = { selectedTabIndex = 3 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTabIndex == 3) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                            contentDescription = "Account"
                        )
                    },
                    label = { Text("My Account", fontSize = 11.sp, fontWeight = if (selectedTabIndex == 3) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TextOnYellow,
                        selectedTextColor = SolarYellow,
                        indicatorColor = SolarYellow,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_account")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTabIndex) {
                0 -> LiveRatesScreen(
                    ratesRepository = ratesRepository,
                    notificationManager = notificationManager,
                    onNavigateToCalculator = { selectedTabIndex = 1 }
                )
                1 -> SolarCalculatorScreen(
                    authRepository = authRepository
                )
                2 -> CommunityScreen(
                    communityRepository = communityRepository,
                    authRepository = authRepository
                )
                3 -> ProfileScreen(
                    authRepository = authRepository,
                    notificationManager = notificationManager,
                    onNavigateToAuth = { showAuthScreen = true }
                )
            }
        }
    }
}
