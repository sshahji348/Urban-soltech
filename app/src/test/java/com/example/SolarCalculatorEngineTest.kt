package com.example

import com.example.data.model.AcLoad
import com.example.data.model.InverterType
import com.example.data.model.SolarSystemInputs
import com.example.data.repository.SolarCalculatorEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SolarCalculatorEngineTest {

    @Test
    fun testOnGridCalculationHasNoBattery() {
        val inputs = SolarSystemInputs(
            monthlyUnitsKwh = 1000,
            inverterType = InverterType.ON_GRID,
            acLoads = listOf(AcLoad(1.5, 2)),
            fansCount = 6,
            tvsCount = 2,
            refrigeratorsCount = 1
        )
        val quotation = SolarCalculatorEngine.calculateQuotation(inputs, "test@gmail.com")

        assertTrue("Recommended system must be at least 5kW", quotation.recommendedSystemKw >= 5.0)
        assertTrue("Panel count must be positive", quotation.panelCount >= 8)
        assertNull("On-Grid system should not have batteries", quotation.batteryCapacityKwh)
        assertTrue("Total cost must be calculated", quotation.totalCostPkr > 0)
        assertTrue("Payback must be positive", quotation.paybackPeriodYears > 0.0)
    }

    @Test
    fun testHybridCalculationIncludesBatteryBank() {
        val inputs = SolarSystemInputs(
            monthlyUnitsKwh = 1200,
            inverterType = InverterType.HYBRID,
            acLoads = listOf(AcLoad(1.5, 2), AcLoad(2.0, 1)),
            fansCount = 8,
            tvsCount = 3,
            refrigeratorsCount = 2
        )
        val quotation = SolarCalculatorEngine.calculateQuotation(inputs, "test@gmail.com")

        assertNotNull("Hybrid system must have battery capacity", quotation.batteryCapacityKwh)
        assertTrue("Battery capacity should be at least 5 kWh", quotation.batteryCapacityKwh!! >= 5.0)
        assertTrue("Itemized equipment must contain energy storage", quotation.itemizedEquipment.any { it.category == "Energy Storage" })
        assertTrue("Payback period should be reasonable (typically 2 - 5 years)", quotation.paybackPeriodYears in 1.5..7.0)
    }
}
