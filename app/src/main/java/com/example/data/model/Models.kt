package com.example.data.model

import java.util.UUID

enum class InverterType(val displayName: String, val description: String) {
    ON_GRID(
        displayName = "On-Grid (Grid-Tied)",
        description = "Feeds power to grid with Net-Metering. Most cost-effective, no batteries needed."
    ),
    HYBRID(
        displayName = "Hybrid System",
        description = "Provides solar + battery backup during load shedding with grid net-metering export."
    ),
    OFF_GRID(
        displayName = "Off-Grid Standalone",
        description = "Complete independence from WAPDA grid with dedicated heavy battery storage."
    )
}

enum class MediaType {
    IMAGE,
    VIDEO
}

data class UserAccount(
    val id: String = UUID.randomUUID().toString(),
    val email: String,
    val fullName: String,
    val phone: String = "+92 333 6919922",
    val city: String = "Lahore, Pakistan",
    val isVerified: Boolean = true,
    val createdAtMillis: Long = System.currentTimeMillis()
)

data class MarketRate(
    val id: String,
    val brand: String,
    val modelName: String,
    val technology: String, // e.g. "N-Type TOPCon Bifacial"
    val wattage: Int,       // e.g. 585 W
    val pricePerWattPkr: Double, // e.g. 33.5 PKR/Watt
    val previousPricePkr: Double,
    val trendDelta: Double, // e.g. -0.5 PKR
    val efficiency: Double, // e.g. 22.8%
    val warrantyYears: Int = 25,
    val stockStatus: String = "In Stock"
)

data class AcLoad(
    val tonnage: Double, // 1.0, 1.5, 2.0
    val count: Int
)

data class SolarSystemInputs(
    val monthlyUnitsKwh: Int,
    val inverterType: InverterType,
    val acLoads: List<AcLoad>,
    val fansCount: Int,
    val tvsCount: Int,
    val refrigeratorsCount: Int
)

data class EquipmentItem(
    val category: String,
    val description: String,
    val quantity: String,
    val unitPricePkr: Double,
    val totalPricePkr: Double
)

data class SolarQuotation(
    val id: String = "US-" + UUID.randomUUID().toString().substring(0, 8).uppercase(),
    val customerEmail: String,
    val inputs: SolarSystemInputs,
    val recommendedSystemKw: Double,
    val panelCount: Int,
    val panelWattage: Int,
    val panelBrand: String,
    val inverterRatingKw: Double,
    val inverterType: InverterType,
    val batteryCapacityKwh: Double?,
    val batteryDescription: String?,
    val itemizedEquipment: List<EquipmentItem>,
    val totalCostPkr: Double,
    val estimatedMonthlyGenerationKwh: Double,
    val monthlySavingsPkr: Double,
    val annualSavingsPkr: Double,
    val paybackPeriodYears: Double,
    val twentyFiveYearSavingsPkr: Double,
    val createdAtMillis: Long = System.currentTimeMillis()
)

data class PostComment(
    val id: String = UUID.randomUUID().toString(),
    val postId: String,
    val authorName: String,
    val authorEmail: String,
    val text: String,
    val timestampMillis: Long = System.currentTimeMillis()
)

data class CommunityPost(
    val id: String = UUID.randomUUID().toString(),
    val authorName: String,
    val authorEmail: String,
    val authorCity: String,
    val caption: String,
    val mediaType: MediaType,
    val mediaUri: String? = null,
    val localDrawableRes: Int? = null,
    val systemTag: String, // e.g. "10 kW Hybrid - Lahore"
    var likesCount: Int = 0,
    var isLiked: Boolean = false,
    val comments: MutableList<PostComment> = mutableListOf(),
    val timestampMillis: Long = System.currentTimeMillis()
)
