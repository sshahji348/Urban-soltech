package com.example.data.repository

import com.example.data.model.AcLoad
import com.example.data.model.EquipmentItem
import com.example.data.model.InverterType
import com.example.data.model.SolarQuotation
import com.example.data.model.SolarSystemInputs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

object SolarCalculatorEngine {

    // Current standard Tier-1 bifacial solar panel wattage in Pakistan
    const val DEFAULT_PANEL_WATTAGE = 585
    const val DEFAULT_PANEL_BRAND = "Longi Hi-MO X6 / Jinko N-Type 585W"
    // Average market price per watt in PKR for tier-1 panels
    const val CURRENT_PANEL_PRICE_PER_WATT_PKR = 33.5
    // Average unit electricity tariff in Pakistan (composite peak/off-peak + fuel adjustment)
    const val GRID_TARIFF_PER_UNIT_PKR = 62.0
    // Net metering credit / export buyback tariff
    const val NET_METERING_EXPORT_TARIFF_PKR = 27.5
    // Average peak sun hours in Pakistan (annual weighted average ~ 4.8 - 5.2 hours/day)
    // 1 kW of solar generates approx 125 - 130 kWh (units) per month in Pakistan
    const val UNITS_GENERATED_PER_KW_PER_MONTH = 125.0

    fun calculateQuotation(
        inputs: SolarSystemInputs,
        userEmail: String
    ): SolarQuotation {
        // 1. Calculate required capacity based on monthly units
        val capacityFromUnits = inputs.monthlyUnitsKwh / UNITS_GENERATED_PER_KW_PER_MONTH

        // 2. Calculate peak load from appliances to size inverter properly
        // AC power requirements:
        // 1.0 Ton Inverter: ~1.2 kW running (2.0 kW surge)
        // 1.5 Ton Inverter: ~1.8 kW running (2.8 kW surge)
        // 2.0 Ton Inverter: ~2.4 kW running (3.6 kW surge)
        var totalAcRunningKw = 0.0
        for (ac in inputs.acLoads) {
            val kwPerUnit = when (ac.tonnage) {
                1.0 -> 1.2
                1.5 -> 1.8
                2.0 -> 2.4
                else -> 1.8
            }
            totalAcRunningKw += kwPerUnit * ac.count
        }

        val fansKw = inputs.fansCount * 0.075 // 75W per fan
        val tvsKw = inputs.tvsCount * 0.100   // 100W per TV
        val fridgesKw = inputs.refrigeratorsCount * 0.350 // 350W per fridge

        val totalConnectedLoadKw = totalAcRunningKw + fansKw + tvsKw + fridgesKw
        // Diversity factor: not all appliances run at 100% simultaneously
        val estimatedPeakRunningDemandKw = max(3.0, (totalAcRunningKw * 0.75) + fansKw + tvsKw + (fridgesKw * 0.5))

        // System capacity recommendation:
        // Must satisfy both monthly unit generation and peak demand
        val rawRecommendedKw = max(capacityFromUnits, estimatedPeakRunningDemandKw * 1.15)

        // Standard inverter sizes in Pakistan: 3.2kW, 5kW, 6kW, 8kW, 10kW, 12kW, 15kW, 20kW, 25kW, 30kW
        val inverterRatingKw = when {
            rawRecommendedKw <= 3.5 -> 3.5
            rawRecommendedKw <= 5.5 -> 5.0
            rawRecommendedKw <= 6.5 -> 6.0
            rawRecommendedKw <= 8.5 -> 8.0
            rawRecommendedKw <= 10.5 -> 10.0
            rawRecommendedKw <= 12.5 -> 12.0
            rawRecommendedKw <= 15.5 -> 15.0
            rawRecommendedKw <= 20.5 -> 20.0
            rawRecommendedKw <= 25.5 -> 25.0
            else -> ceil(rawRecommendedKw / 5.0) * 5.0
        }

        // Exact panel count based on required solar capacity:
        val requiredWatts = rawRecommendedKw * 1000.0
        val panelCount = max(6, ceil(requiredWatts / DEFAULT_PANEL_WATTAGE).toInt())
        val actualSystemKw = (panelCount * DEFAULT_PANEL_WATTAGE) / 1000.0

        // 3. Battery Calculation (Only for Hybrid and Off-Grid)
        val batteryCapacityKwh: Double?
        val batteryDescription: String?
        val batteryCostPkr: Double

        if (inputs.inverterType == InverterType.ON_GRID) {
            batteryCapacityKwh = null
            batteryDescription = null
            batteryCostPkr = 0.0
        } else {
            // Sizing nighttime essential backup:
            // 4 hours of night load for critical circuits + 1 AC
            val essentialNightKw = (totalAcRunningKw.coerceAtMost(1.8) * 0.6) + (fansKw * 0.7) + (fridgesKw * 0.3)
            val backupHours = if (inputs.inverterType == InverterType.OFF_GRID) 10.0 else 4.5
            val requiredStorageKwh = max(5.12, (essentialNightKw * backupHours) / 0.85) // 85% depth of discharge

            if (inputs.inverterType == InverterType.HYBRID) {
                // LiFePO4 Lithium Battery Bank (e.g., 51.2V 100Ah / 200Ah modules)
                val roundedStorageKwh = (ceil(requiredStorageKwh / 5.12) * 5.12)
                batteryCapacityKwh = roundedStorageKwh
                batteryDescription = "LiFePO4 Lithium Iron Phosphate 51.2V (${(roundedStorageKwh).roundToInt()} kWh Bank, 6000 Cycles)"
                // Lithium price in Pakistan: ~PKR 85,000 per kWh
                batteryCostPkr = roundedStorageKwh * 85000.0
            } else {
                // Off-Grid heavy duty storage
                val roundedStorageKwh = (ceil(requiredStorageKwh / 4.8) * 4.8)
                batteryCapacityKwh = roundedStorageKwh
                batteryDescription = "Heavy-Duty Deep Cycle Solar Tubular Battery Bank (${(roundedStorageKwh).roundToInt()} kWh capacity)"
                batteryCostPkr = roundedStorageKwh * 65000.0
            }
        }

        // 4. Equipment Itemized Costs
        val itemizedList = mutableListOf<EquipmentItem>()

        // A. Solar Panels
        val totalPanelWatts = panelCount * DEFAULT_PANEL_WATTAGE
        val panelsTotalPrice = totalPanelWatts * CURRENT_PANEL_PRICE_PER_WATT_PKR
        itemizedList.add(
            EquipmentItem(
                category = "Solar Panels",
                description = "$DEFAULT_PANEL_BRAND Tier-1 Bifacial Dual Glass",
                quantity = "$panelCount Panels (${actualSystemKw} kWp)",
                unitPricePkr = DEFAULT_PANEL_WATTAGE * CURRENT_PANEL_PRICE_PER_WATT_PKR,
                totalPricePkr = panelsTotalPrice
            )
        )

        // B. Inverter
        val inverterPricePkr = when (inputs.inverterType) {
            InverterType.ON_GRID -> when {
                inverterRatingKw <= 5.0 -> 210000.0
                inverterRatingKw <= 10.0 -> 330000.0
                inverterRatingKw <= 15.0 -> 440000.0
                inverterRatingKw <= 20.0 -> 560000.0
                else -> inverterRatingKw * 28000.0
            }
            InverterType.HYBRID -> when {
                inverterRatingKw <= 5.0 -> 340000.0
                inverterRatingKw <= 8.0 -> 480000.0
                inverterRatingKw <= 10.0 -> 590000.0
                inverterRatingKw <= 12.0 -> 680000.0
                else -> inverterRatingKw * 58000.0
            }
            InverterType.OFF_GRID -> when {
                inverterRatingKw <= 5.0 -> 260000.0
                inverterRatingKw <= 8.0 -> 390000.0
                inverterRatingKw <= 10.0 -> 480000.0
                else -> inverterRatingKw * 48000.0
            }
        }
        val inverterModelDesc = when (inputs.inverterType) {
            InverterType.ON_GRID -> "Huawei / Sungrow ${inverterRatingKw.roundToInt()}kW 3-Phase On-Grid Inverter"
            InverterType.HYBRID -> "Inverex / Growatt / Deye ${inverterRatingKw.roundToInt()}kW Smart Hybrid Inverter"
            InverterType.OFF_GRID -> "Off-Grid Heavy Duty Pure Sine Wave ${inverterRatingKw.roundToInt()}kW Inverter"
        }
        itemizedList.add(
            EquipmentItem(
                category = "Inverter",
                description = inverterModelDesc,
                quantity = "1 Unit",
                unitPricePkr = inverterPricePkr,
                totalPricePkr = inverterPricePkr
            )
        )

        // C. Battery Bank (if applicable)
        if (batteryDescription != null && batteryCostPkr > 0) {
            itemizedList.add(
                EquipmentItem(
                    category = "Energy Storage",
                    description = batteryDescription,
                    quantity = "${batteryCapacityKwh?.roundToInt()} kWh",
                    unitPricePkr = batteryCostPkr,
                    totalPricePkr = batteryCostPkr
                )
            )
        }

        // D. Mounting Structure (L2 / L3 Galvanized Iron Elevated Structure)
        // Rate: ~PKR 8.5 per Watt
        val structureCost = totalPanelWatts * 8.5
        itemizedList.add(
            EquipmentItem(
                category = "Mounting Structure",
                description = "Galvanized Iron (GI) Elevated L2/L3 Custom Heavy Gauge Structure",
                quantity = "$panelCount Stands",
                unitPricePkr = structureCost / panelCount,
                totalPricePkr = structureCost
            )
        )

        // E. Cables & Electrical Balance of System (BoS)
        // 4mm / 6mm German Standard Solar DC Cables, AC Armored Cables, PVC Piping, Trunking
        val cablingCost = max(75000.0, actualSystemKw * 13500.0)
        itemizedList.add(
            EquipmentItem(
                category = "Solar Cables & Conduits",
                description = "4mm/6mm XLPO UV Resistant Pure Copper DC Cables & AC Armored Cables",
                quantity = "Complete Set",
                unitPricePkr = cablingCost,
                totalPricePkr = cablingCost
            )
        )

        // F. Switchgear, Protection & Earthing
        // DC SPDs, AC SPDs, Schneider/ABB Breakers, Copper Earthing Bore & Lightning Arrestor
        val protectionCost = max(85000.0, actualSystemKw * 12000.0)
        itemizedList.add(
            EquipmentItem(
                category = "Protection & Safety",
                description = "AC/DC Distribution Boxes with SPDs, Breakers, Chemical Earthing Bore & Lightning Arrestor",
                quantity = "1 Complete System",
                unitPricePkr = protectionCost,
                totalPricePkr = protectionCost
            )
        )

        // G. Net Metering Preparation (if On-grid or Hybrid)
        if (inputs.inverterType != InverterType.OFF_GRID) {
            val netMeteringFee = 110000.0
            itemizedList.add(
                EquipmentItem(
                    category = "Net-Metering",
                    description = "Three-Phase Green Meter Licensing, DISCO Approval, Testing & Commissioning",
                    quantity = "1 DISCO License",
                    unitPricePkr = netMeteringFee,
                    totalPricePkr = netMeteringFee
                )
            )
        }

        // H. Professional Installation, Engineering & Testing Labor
        val installationLabor = max(65000.0, actualSystemKw * 9500.0)
        itemizedList.add(
            EquipmentItem(
                category = "Installation & Testing",
                description = "Civil works, structure fastening, electrical commissioning, and mobile monitoring setup",
                quantity = "Turnkey Service",
                unitPricePkr = installationLabor,
                totalPricePkr = installationLabor
            )
        )

        // Total Cost Calculation
        val totalCostPkr = itemizedList.sumOf { it.totalPricePkr }

        // 5. Financial Generation & Payback Analysis
        val estimatedMonthlyGenerationKwh = actualSystemKw * UNITS_GENERATED_PER_KW_PER_MONTH
        val selfConsumedUnits = estimatedMonthlyGenerationKwh.coerceAtMost(inputs.monthlyUnitsKwh.toDouble())
        val exportedUnits = max(0.0, estimatedMonthlyGenerationKwh - inputs.monthlyUnitsKwh)

        // Savings in PKR
        val monthlySelfConsumptionSavings = selfConsumedUnits * GRID_TARIFF_PER_UNIT_PKR
        val monthlyExportCredit = exportedUnits * NET_METERING_EXPORT_TARIFF_PKR
        val totalMonthlySavingsPkr = monthlySelfConsumptionSavings + monthlyExportCredit
        val annualSavingsPkr = totalMonthlySavingsPkr * 12.0

        // Payback period in years
        val rawPaybackYears = if (annualSavingsPkr > 0) totalCostPkr / annualSavingsPkr else 3.5
        val paybackPeriodYears = (rawPaybackYears * 10.0).roundToInt() / 10.0
        val twentyFiveYearSavingsPkr = (annualSavingsPkr * 25.0) - totalCostPkr

        return SolarQuotation(
            customerEmail = userEmail,
            inputs = inputs,
            recommendedSystemKw = (actualSystemKw * 10.0).roundToInt() / 10.0,
            panelCount = panelCount,
            panelWattage = DEFAULT_PANEL_WATTAGE,
            panelBrand = DEFAULT_PANEL_BRAND,
            inverterRatingKw = inverterRatingKw,
            inverterType = inputs.inverterType,
            batteryCapacityKwh = batteryCapacityKwh,
            batteryDescription = batteryDescription,
            itemizedEquipment = itemizedList,
            totalCostPkr = totalCostPkr,
            estimatedMonthlyGenerationKwh = estimatedMonthlyGenerationKwh,
            monthlySavingsPkr = totalMonthlySavingsPkr,
            annualSavingsPkr = annualSavingsPkr,
            paybackPeriodYears = paybackPeriodYears,
            twentyFiveYearSavingsPkr = twentyFiveYearSavingsPkr
        )
    }
}
