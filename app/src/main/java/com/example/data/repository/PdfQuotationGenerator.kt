package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.SolarQuotation
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfQuotationGenerator {

    private val currencyFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 0
    }

    fun generatePdfQuotation(context: Context, quotation: SolarQuotation): File {
        val pdfDocument = PdfDocument()
        // A4 page size in points (72 points per inch): 595 x 842
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Header Background (Deep Industrial Slate)
        paint.color = Color.rgb(11, 15, 25) // #0B0F19
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 130f, paint)

        // Gold Accent Bar under header
        paint.color = Color.rgb(255, 215, 0) // #FFD700
        canvas.drawRect(0f, 130f, pageWidth.toFloat(), 135f, paint)

        // 2. Logo Lightning Bolt (Vector Drawing)
        paint.color = Color.rgb(255, 215, 0)
        paint.style = Paint.Style.FILL
        val path = android.graphics.Path().apply {
            moveTo(45f, 30f)
            lineTo(30f, 65f)
            lineTo(42f, 65f)
            lineTo(26f, 105f)
            lineTo(55f, 58f)
            lineTo(42f, 58f)
            close()
        }
        canvas.drawPath(path, paint)

        // 3. Header Text
        paint.color = Color.rgb(255, 215, 0)
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("URBAN SOLTECH", 70f, 55f, paint)

        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 11f
        paint.isFakeBoldText = false
        canvas.drawText("ENGINEERING SUSTAINABLE ENERGY SOLUTIONS", 70f, 75f, paint)
        canvas.drawText("Helpline: +92 333 6919922  |  Islamabad - Lahore - Karachi", 70f, 95f, paint)

        // Right side header info (Date & Quotation #)
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val dateStr = dateFormat.format(Date(quotation.createdAtMillis))
        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.WHITE
        paint.textSize = 10f
        canvas.drawText("Quotation Ref: ${quotation.id}", pageWidth - 30f, 50f, paint)
        canvas.drawText("Date: $dateStr", pageWidth - 30f, 70f, paint)
        canvas.drawText("Client: ${quotation.customerEmail}", pageWidth - 30f, 90f, paint)

        // Reset alignment
        paint.textAlign = Paint.Align.LEFT

        // 4. Executive Summary Card (Recommended System & ROI)
        var currentY = 160f
        paint.color = Color.rgb(241, 245, 249) // Slate 100
        val summaryRect = RectF(30f, currentY, pageWidth - 30f, currentY + 95f)
        canvas.drawRoundRect(summaryRect, 8f, 8f, paint)

        // Border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        paint.color = Color.rgb(203, 213, 225)
        canvas.drawRoundRect(summaryRect, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        // Metric 1: System kW
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("${quotation.recommendedSystemKw} kW", 45f, currentY + 35f, paint)
        paint.textSize = 9f
        paint.isFakeBoldText = false
        paint.color = Color.rgb(71, 85, 105)
        canvas.drawText("RECOMMENDED SYSTEM", 45f, currentY + 52f, paint)
        canvas.drawText("${quotation.panelCount}x 585W Bifacial", 45f, currentY + 68f, paint)

        // Metric 2: Monthly Generation
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("${quotation.estimatedMonthlyGenerationKwh.toInt()} Units", 185f, currentY + 35f, paint)
        paint.textSize = 9f
        paint.isFakeBoldText = false
        paint.color = Color.rgb(71, 85, 105)
        canvas.drawText("EST. MONTHLY UNITS", 185f, currentY + 52f, paint)
        canvas.drawText("~${(quotation.estimatedMonthlyGenerationKwh * 12).toInt()} Units / year", 185f, currentY + 68f, paint)

        // Metric 3: Total Investment
        paint.color = Color.rgb(180, 83, 9)
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("PKR ${currencyFormat.format(quotation.totalCostPkr)}", 335f, currentY + 35f, paint)
        paint.textSize = 9f
        paint.isFakeBoldText = false
        paint.color = Color.rgb(71, 85, 105)
        canvas.drawText("TOTAL ESTIMATED COST", 335f, currentY + 52f, paint)
        canvas.drawText(quotation.inverterType.displayName, 335f, currentY + 68f, paint)

        // Metric 4: Payback Period
        paint.color = Color.rgb(21, 128, 61)
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("${quotation.paybackPeriodYears} Years", 480f, currentY + 35f, paint)
        paint.textSize = 9f
        paint.isFakeBoldText = false
        paint.color = Color.rgb(71, 85, 105)
        canvas.drawText("ESTIMATED PAYBACK", 480f, currentY + 52f, paint)
        canvas.drawText("ROI via Unit Savings", 480f, currentY + 68f, paint)

        // 5. Itemized Equipment Table Header
        currentY = 280f
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("ITEMIZED SYSTEM BREAKDOWN & EQUIPMENT QUOTATION", 30f, currentY, paint)

        currentY += 15f
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRect(30f, currentY, pageWidth - 30f, currentY + 24f, paint)

        paint.color = Color.rgb(30, 41, 59)
        paint.textSize = 9.5f
        paint.isFakeBoldText = true
        canvas.drawText("CATEGORY & ITEM DESCRIPTION", 40f, currentY + 16f, paint)
        canvas.drawText("QTY", 370f, currentY + 16f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("AMOUNT (PKR)", pageWidth - 40f, currentY + 16f, paint)
        paint.textAlign = Paint.Align.LEFT

        // Draw items
        currentY += 24f
        var isOdd = false
        for (item in quotation.itemizedEquipment) {
            val rowHeight = 32f
            if (isOdd) {
                paint.color = Color.rgb(248, 250, 252)
                canvas.drawRect(30f, currentY, pageWidth - 30f, currentY + rowHeight, paint)
            }
            isOdd = !isOdd

            // Separator line
            paint.color = Color.rgb(226, 232, 240)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.5f
            canvas.drawLine(30f, currentY + rowHeight, pageWidth - 30f, currentY + rowHeight, paint)
            paint.style = Paint.Style.FILL

            // Item Name & Category
            paint.color = Color.rgb(15, 23, 42)
            paint.textSize = 10f
            paint.isFakeBoldText = true
            canvas.drawText(item.category, 40f, currentY + 13f, paint)

            paint.color = Color.rgb(100, 116, 139)
            paint.textSize = 8.5f
            paint.isFakeBoldText = false
            val cleanDesc = if (item.description.length > 55) item.description.substring(0, 52) + "..." else item.description
            canvas.drawText(cleanDesc, 40f, currentY + 25f, paint)

            // Quantity
            paint.color = Color.rgb(51, 65, 85)
            paint.textSize = 9f
            canvas.drawText(item.quantity, 370f, currentY + 18f, paint)

            // Price
            paint.textAlign = Paint.Align.RIGHT
            paint.color = Color.rgb(15, 23, 42)
            paint.textSize = 10f
            paint.isFakeBoldText = true
            canvas.drawText(currencyFormat.format(item.totalPricePkr), pageWidth - 40f, currentY + 18f, paint)
            paint.textAlign = Paint.Align.LEFT

            currentY += rowHeight
        }

        // 6. Total Summary Row
        currentY += 10f
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawRoundRect(RectF(30f, currentY, pageWidth - 30f, currentY + 36f), 4f, 4f, paint)

        paint.color = Color.rgb(255, 215, 0)
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("TOTAL ESTIMATED INVESTMENT (PKR):", 45f, currentY + 23f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("PKR ${currencyFormat.format(quotation.totalCostPkr)}", pageWidth - 45f, currentY + 23f, paint)
        paint.textAlign = Paint.Align.LEFT

        // 7. Financial & Load Breakdown Box
        currentY += 55f
        paint.color = Color.rgb(241, 245, 249)
        val finRect = RectF(30f, currentY, pageWidth - 30f, currentY + 80f)
        canvas.drawRoundRect(finRect, 6f, 6f, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("FINANCIAL SAVINGS & PAYBACK PROJECTIONS", 45f, currentY + 22f, paint)

        paint.textSize = 9f
        paint.isFakeBoldText = false
        paint.color = Color.rgb(51, 65, 85)
        canvas.drawText("• Monthly Bill Savings: ~PKR ${currencyFormat.format(quotation.monthlySavingsPkr)} / month", 45f, currentY + 42f, paint)
        canvas.drawText("• Annual Bill Savings: ~PKR ${currencyFormat.format(quotation.annualSavingsPkr)} / year", 45f, currentY + 58f, paint)
        canvas.drawText("• 25-Year Cumulative Net Profit: ~PKR ${currencyFormat.format(quotation.twentyFiveYearSavingsPkr)}", 45f, currentY + 74f, paint)

        canvas.drawText("• Monthly Load Basis: ${quotation.inputs.monthlyUnitsKwh} kWh/Units", 340f, currentY + 42f, paint)
        canvas.drawText("• ACs Handled: ${quotation.inputs.acLoads.sumOf { it.count }} Units", 340f, currentY + 58f, paint)
        canvas.drawText("• Warranties: 25 Yrs Panels, 5 Yrs Inverter", 340f, currentY + 74f, paint)

        // 8. Footer (Emergency helpline + terms)
        currentY = pageHeight - 65f
        paint.color = Color.rgb(203, 213, 225)
        canvas.drawLine(30f, currentY, pageWidth - 30f, currentY, paint)

        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 8.5f
        paint.isFakeBoldText = false
        canvas.drawText("Urban Soltech (Pvt) Ltd.  |  24/7 Technical Helpline & Emergency Support: +92 333 6919922", 30f, currentY + 18f, paint)
        canvas.drawText("Quotation is valid for 7 days subject to international currency fluctuations. Complete EPC turnkey solution.", 30f, currentY + 32f, paint)

        pdfDocument.finishPage(page)

        // Save PDF to cache directory
        val quotationsDir = File(context.cacheDir, "quotations")
        if (!quotationsDir.exists()) {
            quotationsDir.mkdirs()
        }
        val file = File(quotationsDir, "Urban_Soltech_Quotation_${quotation.id}.pdf")
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return file
    }

    fun sharePdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Urban Soltech Solar System Quotation")
            putExtra(Intent.EXTRA_TEXT, "Here is your official Urban Soltech solar system sizing and quotation report. Contact us at +92 333 6919922 for site survey and booking.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share Solar Quotation PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun viewPdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            sharePdf(context, file)
        }
    }
}
