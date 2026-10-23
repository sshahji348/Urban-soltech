package com.example.data.repository

import com.example.data.model.MarketRate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class MarketRatesRepository(
    private val coroutineScope: CoroutineScope,
    private val notificationManager: RateNotificationManager? = null
) {

    private val baseRates = listOf(
        MarketRate(
            id = "longi_himo_x6",
            brand = "Longi Solar",
            modelName = "Hi-MO X6 Explorer (HPBC N-Type)",
            technology = "HPBC Anti-Dust Bifacial",
            wattage = 585,
            pricePerWattPkr = 33.50,
            previousPricePkr = 34.00,
            trendDelta = -0.50,
            efficiency = 22.8,
            warrantyYears = 25,
            stockStatus = "Available Ready Stock"
        ),
        MarketRate(
            id = "jinko_tiger_neo",
            brand = "Jinko Solar",
            modelName = "Tiger Neo 72HL4-BDV (TOPCon)",
            technology = "N-Type TOPCon Dual-Glass",
            wattage = 585,
            pricePerWattPkr = 33.20,
            previousPricePkr = 33.50,
            trendDelta = -0.30,
            efficiency = 22.6,
            warrantyYears = 30,
            stockStatus = "Available Ready Stock"
        ),
        MarketRate(
            id = "canadian_topbihiku",
            brand = "Canadian Solar",
            modelName = "TOPBiHiKu7 Bi-Facial",
            technology = "N-Type Bifacial Multi-Busbar",
            wattage = 590,
            pricePerWattPkr = 34.00,
            previousPricePkr = 33.80,
            trendDelta = +0.20,
            efficiency = 22.9,
            warrantyYears = 25,
            stockStatus = "Limited Quantity"
        ),
        MarketRate(
            id = "ja_deepblue",
            brand = "JA Solar",
            modelName = "DeepBlue 4.0 Pro JAM72D40",
            technology = "Bycium+ Cell Technology",
            wattage = 580,
            pricePerWattPkr = 32.80,
            previousPricePkr = 33.00,
            trendDelta = -0.20,
            efficiency = 22.4,
            warrantyYears = 25,
            stockStatus = "Available Ready Stock"
        ),
        MarketRate(
            id = "trina_vertex_n",
            brand = "Trina Solar",
            modelName = "Vertex N NEG19RC.20",
            technology = "210mm N-Type i-TOPCon",
            wattage = 605,
            pricePerWattPkr = 34.50,
            previousPricePkr = 34.50,
            trendDelta = 0.00,
            efficiency = 23.1,
            warrantyYears = 30,
            stockStatus = "Available Ready Stock"
        )
    )

    private val _ratesState = MutableStateFlow(baseRates)
    val ratesState: StateFlow<List<MarketRate>> = _ratesState.asStateFlow()

    private val _lastRefreshedTime = MutableStateFlow(System.currentTimeMillis())
    val lastRefreshedTime: StateFlow<Long> = _lastRefreshedTime.asStateFlow()

    private val _secondsToNextRefresh = MutableStateFlow(3600) // 1 hour countdown
    val secondsToNextRefresh: StateFlow<Int> = _secondsToNextRefresh.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        startHourlyAutoRefreshScheduler()
    }

    private fun startHourlyAutoRefreshScheduler() {
        coroutineScope.launch(Dispatchers.Default) {
            while (true) {
                delay(1000L)
                val currentRemaining = _secondsToNextRefresh.value
                if (currentRemaining <= 1) {
                    refreshRatesNow()
                    _secondsToNextRefresh.value = 3600
                } else {
                    _secondsToNextRefresh.value = currentRemaining - 1
                }
            }
        }
    }

    fun refreshRatesNow() {
        coroutineScope.launch(Dispatchers.Default) {
            _isRefreshing.value = true
            delay(600L) // smooth refresh transition
            val updated = _ratesState.value.map { item ->
                // Micro-fluctuation between -0.3 and +0.3 PKR/Watt
                val delta = (Random.nextDouble(-0.35, 0.30) * 100).toInt() / 100.0
                val newPrice = (item.pricePerWattPkr + delta).coerceIn(31.50, 37.00)
                val roundedNewPrice = (newPrice * 100.0).toInt() / 100.0
                val roundedDelta = ((roundedNewPrice - item.pricePerWattPkr) * 100.0).toInt() / 100.0
                item.copy(
                    previousPricePkr = item.pricePerWattPkr,
                    pricePerWattPkr = roundedNewPrice,
                    trendDelta = roundedDelta
                )
            }
            _ratesState.value = updated
            _lastRefreshedTime.value = System.currentTimeMillis()
            _secondsToNextRefresh.value = 3600
            _isRefreshing.value = false

            // Notify opted-in users about the new hourly rates
            val newBenchmark = getMarketBenchmarkRate()
            val averageDelta = (updated.map { it.trendDelta }.average() * 100).toInt() / 100.0
            val topPanel = updated.firstOrNull()
            notificationManager?.sendRateUpdateNotification(
                benchmarkRate = newBenchmark,
                trendDelta = averageDelta,
                topBrandName = topPanel?.brand ?: "Tier-1 Solar",
                topBrandRate = topPanel?.pricePerWattPkr ?: newBenchmark
            )
        }
    }

    fun getFormattedLastUpdated(): String {
        val sdf = SimpleDateFormat("hh:mm:ss a, dd MMM", Locale.US)
        return sdf.format(Date(_lastRefreshedTime.value))
    }

    fun getMarketBenchmarkRate(): Double {
        val rates = _ratesState.value
        return if (rates.isNotEmpty()) {
            val avg = rates.map { it.pricePerWattPkr }.average()
            (avg * 10.0).toInt() / 10.0
        } else {
            33.5
        }
    }
}
