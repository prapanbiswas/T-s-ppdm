package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.OrderStatus
import com.example.data.model.TShirtOrder
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PdfReportType(val titleBengali: String, val subtitle: String, val filePrefix: String) {
    INDIVIDUAL_ORDERS(
        "১. গ্রাহক অর্ডার তালিকা শীট (Individual Orders)",
        "প্রতিটি অর্ডারের একক রো: আইডি, নাম, মোবাইল, প্রতি সাইজের সংখ্যা (1, 2, 3), পেমেন্ট ও মূল্য",
        "TShirt_Individual_Orders"
    ),
    MANUFACTURING_SUBTOTAL(
        "২. কারখানা উৎপাদন ও সাবটোটাল শীট (Manufacturing Subtotal)",
        "কারখানার জন্য সহজ হিসাব: সম্মিলিত অর্ডার সংখ্যা ও প্রতিটি সাইজের সর্বমোট পিস (L: 9 pcs, M: 6 pcs ইত্যাদি)",
        "TShirt_Manufacturing_Subtotal"
    ),
    COMBINED_REPORT(
        "৩. পূর্ণাঙ্গ রিপোর্ট (উভয় শীট এক সাথে - 2 in 1)",
        "গ্রাহক অর্ডার তালিকা এবং কারখানার সাইজ সাবটোটাল শীট উভয়ই এক ফাইলে",
        "TShirt_Complete_Production_Report"
    )
}

data class PdfGenerationResult(
    val file: File,
    val uri: Uri,
    val reportType: PdfReportType,
    val pageCount: Int,
    val totalOrders: Int,
    val totalPieces: Int,
    val grandTotalAmount: Double,
    val sizeBreakdown: Map<String, Int>,
    val paidOrdersCount: Int,
    val dueOrdersCount: Int,
    val paidPieces: Int,
    val duePieces: Int
)

/**
 * High-performance native PDF document generator for apparel production and distribution orders.
 * Generates two distinct, simplified reports:
 * 1. Individual Orders Sheet: A clean single-row multi-column spreadsheet with columns for Order ID,
 *    Name, Mobile Number, every size quantity in numbers (1, 2, 3...), total quantity, amount, and payment status.
 * 2. Manufacturing Subtotal Sheet: A simplified, crystal-clear factory cutting order sheet
 *    with total orders and exact piece counts per size (e.g. L size: 9 pcs, M size: 6 pcs) for effortless manufacturing.
 */
object PdfReportGenerator {

    private const val TAG = "PdfReportGenerator"

    // Standard A4 Points (72 DPI)
    private const val A4_PORTRAIT_WIDTH = 595f
    private const val A4_PORTRAIT_HEIGHT = 842f
    private const val A4_LANDSCAPE_WIDTH = 842f
    private const val A4_LANDSCAPE_HEIGHT = 595f

    // Theme Colors
    private val COLOR_PRIMARY = Color.rgb(148, 41, 17)       // Mandir Crimson / Terracotta Rust (#942911)
    private val COLOR_PRIMARY_DARK = Color.rgb(112, 29, 11)  // Deep Khoyer (#701D0B)
    private val COLOR_SECONDARY = Color.rgb(217, 119, 6)     // Mandir Gold / Amber (#D97706)
    private val COLOR_TEXT_DARK = Color.rgb(30, 20, 16)      // Deep Ink
    private val COLOR_TEXT_MUTED = Color.rgb(105, 85, 78)    // Earthy Muted
    private val COLOR_ROW_ALT = Color.rgb(249, 246, 240)     // Warm Ivory tint
    private val COLOR_BORDER = Color.rgb(222, 214, 202)      // Clean border
    private val COLOR_HEADER_BG = Color.rgb(243, 236, 226)   // Warm header tint
    private val COLOR_SUCCESS = Color.rgb(22, 101, 52)       // Emerald Green
    private val COLOR_SUCCESS_BG = Color.rgb(240, 253, 244)  // Light green tint
    private val COLOR_WARNING = Color.rgb(180, 83, 9)        // Amber
    private val COLOR_WARNING_BG = Color.rgb(254, 243, 199)  // Light amber tint
    private val COLOR_ERROR = Color.rgb(185, 28, 28)         // Crimson Red
    private val COLOR_HIGHLIGHT_BG = Color.rgb(254, 242, 242)

    /**
     * Dispatches report generation based on selected [PdfReportType].
     */
    fun createReportPdf(
        context: Context,
        orders: List<TShirtOrder>,
        storeName: String = "পোদ্দারপাড়া সর্বজনীন দুর্গা মন্দির",
        scopeLabel: String = "সকল অর্ডার (All Orders)",
        reportType: PdfReportType = PdfReportType.INDIVIDUAL_ORDERS
    ): PdfGenerationResult {
        return when (reportType) {
            PdfReportType.INDIVIDUAL_ORDERS -> createIndividualOrdersPdf(context, orders, storeName, scopeLabel)
            PdfReportType.MANUFACTURING_SUBTOTAL -> createManufacturingSubtotalPdf(context, orders, storeName, scopeLabel)
            PdfReportType.COMBINED_REPORT -> createCombinedReportPdf(context, orders, storeName, scopeLabel)
        }
    }

    /**
     * Backward-compatible alias for existing callers.
     */
    fun createProductionReportPdf(
        context: Context,
        orders: List<TShirtOrder>,
        storeName: String = "পোদ্দারপাড়া সর্বজনীন দুর্গা মন্দির",
        scopeLabel: String = "সকল অর্ডার (All Orders)"
    ): PdfGenerationResult {
        return createIndividualOrdersPdf(context, orders, storeName, scopeLabel)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // REPORT 1: INDIVIDUAL ORDERS SHEET (Single Row & Multiple Columns)
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Generates the Individual Orders Sheet in Landscape A4 (842 x 595).
     * Single row per order with columns:
     * Order ID | Customer Name | Mobile Number | Kid | S | M | L | XL | 2XL | 3XL | Total Qty | Amount | Paid Status | Signature
     */
    fun createIndividualOrdersPdf(
        context: Context,
        orders: List<TShirtOrder>,
        storeName: String = "পোদ্দারপাড়া সর্বজনীন দুর্গা মন্দির",
        scopeLabel: String = "সকল অর্ডার (All Orders)"
    ): PdfGenerationResult {
        val document = PdfDocument()

        val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_TEXT_DARK }
        val paintFill = Paint(Paint.ANTI_ALIAS_FLAG)
        val paintStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            color = COLOR_BORDER
        }

        val stats = calculateOrderStats(orders)

        // Landscape A4 Page layout
        val marginX = 24f
        val marginBottom = 28f
        val rowHeight = 22f
        val tableHeaderHeight = 24f

        // Estimate total pages for Individual Orders in Landscape
        val rowsFirstPage = ((A4_LANDSCAPE_HEIGHT - 95f - tableHeaderHeight - marginBottom) / rowHeight).toInt().coerceAtLeast(1)
        val rowsSubsequentPage = ((A4_LANDSCAPE_HEIGHT - 45f - tableHeaderHeight - marginBottom) / rowHeight).toInt().coerceAtLeast(1)

        val totalPages = if (orders.isEmpty()) 1 else {
            if (orders.size <= rowsFirstPage) 1
            else 1 + ((orders.size - rowsFirstPage + rowsSubsequentPage - 1) / rowsSubsequentPage)
        }

        var pageNumber = 1
        var currentPage = document.startPage(PdfDocument.PageInfo.Builder(A4_LANDSCAPE_WIDTH.toInt(), A4_LANDSCAPE_HEIGHT.toInt(), pageNumber).create())
        var canvas = currentPage.canvas

        // Draw First Page Header
        var currentY = drawIndividualOrdersHeader(canvas, paintText, paintFill, storeName, scopeLabel, orders.size, stats.totalPieces, marginX)
        currentY = drawIndividualOrdersTableHeader(canvas, paintText, paintFill, paintStroke, currentY, marginX)

        for ((index, order) in orders.withIndex()) {
            if (currentY + rowHeight > A4_LANDSCAPE_HEIGHT - marginBottom) {
                drawPageFooter(canvas, paintText, pageNumber, totalPages, A4_LANDSCAPE_WIDTH, A4_LANDSCAPE_HEIGHT, marginX)
                document.finishPage(currentPage)

                pageNumber++
                currentPage = document.startPage(PdfDocument.PageInfo.Builder(A4_LANDSCAPE_WIDTH.toInt(), A4_LANDSCAPE_HEIGHT.toInt(), pageNumber).create())
                canvas = currentPage.canvas

                // Sub-page header
                currentY = drawSubPageHeaderLandscape(canvas, paintText, storeName, pageNumber, totalPages, marginX)
                currentY = drawIndividualOrdersTableHeader(canvas, paintText, paintFill, paintStroke, currentY, marginX)
            }

            val isAlt = index % 2 == 1
            drawIndividualOrderRow(canvas, paintText, paintFill, paintStroke, order, currentY, rowHeight, isAlt, marginX)
            currentY += rowHeight
        }

        // Draw Summary Totals Row at the end of the table
        if (orders.isNotEmpty()) {
            if (currentY + rowHeight > A4_LANDSCAPE_HEIGHT - marginBottom) {
                drawPageFooter(canvas, paintText, pageNumber, totalPages, A4_LANDSCAPE_WIDTH, A4_LANDSCAPE_HEIGHT, marginX)
                document.finishPage(currentPage)

                pageNumber++
                currentPage = document.startPage(PdfDocument.PageInfo.Builder(A4_LANDSCAPE_WIDTH.toInt(), A4_LANDSCAPE_HEIGHT.toInt(), pageNumber).create())
                canvas = currentPage.canvas
                currentY = drawSubPageHeaderLandscape(canvas, paintText, storeName, pageNumber, totalPages, marginX)
            }
            drawIndividualOrdersSummaryRow(canvas, paintText, paintFill, paintStroke, stats, currentY, rowHeight, marginX)
        }

        drawPageFooter(canvas, paintText, pageNumber, totalPages, A4_LANDSCAPE_WIDTH, A4_LANDSCAPE_HEIGHT, marginX)
        document.finishPage(currentPage)

        val outputDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val dateStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val pdfFile = File(outputDir, "${PdfReportType.INDIVIDUAL_ORDERS.filePrefix}_$dateStamp.pdf")

        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        val fileUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        return PdfGenerationResult(
            file = pdfFile,
            uri = fileUri,
            reportType = PdfReportType.INDIVIDUAL_ORDERS,
            pageCount = pageNumber,
            totalOrders = orders.size,
            totalPieces = stats.totalPieces,
            grandTotalAmount = stats.grandTotalAmount,
            sizeBreakdown = stats.sizeMap,
            paidOrdersCount = stats.paidOrdersCount,
            dueOrdersCount = stats.dueOrdersCount,
            paidPieces = stats.paidPieces,
            duePieces = stats.duePieces
        )
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // REPORT 2: MANUFACTURING SUBTOTAL SHEET (All-Total Factory Order Sheet)
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Generates the All-Total Order / Manufacturing Subtotal Sheet in Portrait A4 (595 x 842).
     * Tailor/Factory oriented:
     * - Total Orders count (e.g. 10 orders)
     * - Clear subtotal piece count for each size (e.g. L size: 9 pcs, M size: 6 pcs...)
     * - Grand Total pieces to manufacture
     * - Cutting & stitching checkboxes and sign-off block.
     */
    fun createManufacturingSubtotalPdf(
        context: Context,
        orders: List<TShirtOrder>,
        storeName: String = "পোদ্দারপাড়া সর্বজনীন দুর্গা মন্দির",
        scopeLabel: String = "সকল অর্ডার (All Orders)"
    ): PdfGenerationResult {
        val document = PdfDocument()

        val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_TEXT_DARK }
        val paintFill = Paint(Paint.ANTI_ALIAS_FLAG)
        val paintStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            color = COLOR_BORDER
        }

        val stats = calculateOrderStats(orders)
        val marginX = 28f

        val currentPage = document.startPage(PdfDocument.PageInfo.Builder(A4_PORTRAIT_WIDTH.toInt(), A4_PORTRAIT_HEIGHT.toInt(), 1).create())
        val canvas = currentPage.canvas

        // 1. Header
        var currentY = drawManufacturingSubtotalHeader(canvas, paintText, paintFill, storeName, scopeLabel, marginX)

        // 2. High-visibility KPI Summary Cards
        currentY = drawManufacturingKpiCards(canvas, paintText, paintFill, paintStroke, stats, orders.size, currentY, marginX)

        // 3. Main Manufacturing Table
        currentY = drawManufacturingSubtotalTable(canvas, paintText, paintFill, paintStroke, stats, currentY, marginX)

        // 4. Factory Plain-Language Cutting Order Note
        currentY = drawFactoryCuttingSummaryNote(canvas, paintText, paintFill, paintStroke, stats, orders.size, currentY, marginX)

        // 5. Payment & Financial Status
        currentY = drawFinancialStatusSummary(canvas, paintText, paintFill, paintStroke, stats, currentY, marginX)

        // 6. Signature & Verification Boxes
        drawFactorySignOffSection(canvas, paintText, paintFill, paintStroke, currentY, marginX)

        // Page footer
        drawPageFooter(canvas, paintText, 1, 1, A4_PORTRAIT_WIDTH, A4_PORTRAIT_HEIGHT, marginX)

        document.finishPage(currentPage)

        val outputDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val dateStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val pdfFile = File(outputDir, "${PdfReportType.MANUFACTURING_SUBTOTAL.filePrefix}_$dateStamp.pdf")

        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        val fileUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        return PdfGenerationResult(
            file = pdfFile,
            uri = fileUri,
            reportType = PdfReportType.MANUFACTURING_SUBTOTAL,
            pageCount = 1,
            totalOrders = orders.size,
            totalPieces = stats.totalPieces,
            grandTotalAmount = stats.grandTotalAmount,
            sizeBreakdown = stats.sizeMap,
            paidOrdersCount = stats.paidOrdersCount,
            dueOrdersCount = stats.dueOrdersCount,
            paidPieces = stats.paidPieces,
            duePieces = stats.duePieces
        )
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // REPORT 3: COMBINED REPORT (Subtotal Sheet + Individual Orders Table)
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Generates a 2-in-1 complete document: Page 1 = Manufacturing Subtotal Sheet,
     * followed by the detailed Individual Orders Sheet.
     */
    fun createCombinedReportPdf(
        context: Context,
        orders: List<TShirtOrder>,
        storeName: String = "পোদ্দারপাড়া সর্বজনীন দুর্গা মন্দির",
        scopeLabel: String = "সকল অর্ডার (All Orders)"
    ): PdfGenerationResult {
        val document = PdfDocument()

        val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_TEXT_DARK }
        val paintFill = Paint(Paint.ANTI_ALIAS_FLAG)
        val paintStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            color = COLOR_BORDER
        }

        val stats = calculateOrderStats(orders)
        val marginX = 24f
        val marginBottom = 28f
        val rowHeight = 22f
        val tableHeaderHeight = 24f

        val rowsSubsequentPage = ((A4_LANDSCAPE_HEIGHT - 45f - tableHeaderHeight - marginBottom) / rowHeight).toInt().coerceAtLeast(1)
        val orderPages = if (orders.isEmpty()) 1 else ((orders.size + rowsSubsequentPage - 1) / rowsSubsequentPage)
        val totalPages = 1 + orderPages

        // Page 1: Manufacturing Subtotal Sheet in Landscape
        var pageNumber = 1
        var currentPage = document.startPage(PdfDocument.PageInfo.Builder(A4_LANDSCAPE_WIDTH.toInt(), A4_LANDSCAPE_HEIGHT.toInt(), pageNumber).create())
        var canvas = currentPage.canvas

        var currentY = drawManufacturingSubtotalHeader(canvas, paintText, paintFill, storeName, "$scopeLabel • সম্পূর্ণ প্যাকেজ", marginX)
        currentY = drawManufacturingKpiCardsLandscape(canvas, paintText, paintFill, paintStroke, stats, orders.size, currentY, marginX)
        currentY = drawManufacturingSubtotalTableLandscape(canvas, paintText, paintFill, paintStroke, stats, currentY, marginX)
        drawFactorySignOffSectionLandscape(canvas, paintText, paintFill, paintStroke, currentY, marginX)
        drawPageFooter(canvas, paintText, pageNumber, totalPages, A4_LANDSCAPE_WIDTH, A4_LANDSCAPE_HEIGHT, marginX)
        document.finishPage(currentPage)

        // Subsequent Pages: Individual Orders Table
        pageNumber++
        currentPage = document.startPage(PdfDocument.PageInfo.Builder(A4_LANDSCAPE_WIDTH.toInt(), A4_LANDSCAPE_HEIGHT.toInt(), pageNumber).create())
        canvas = currentPage.canvas

        currentY = drawSubPageHeaderLandscape(canvas, paintText, storeName, pageNumber, totalPages, marginX)
        currentY = drawIndividualOrdersTableHeader(canvas, paintText, paintFill, paintStroke, currentY, marginX)

        for ((index, order) in orders.withIndex()) {
            if (currentY + rowHeight > A4_LANDSCAPE_HEIGHT - marginBottom) {
                drawPageFooter(canvas, paintText, pageNumber, totalPages, A4_LANDSCAPE_WIDTH, A4_LANDSCAPE_HEIGHT, marginX)
                document.finishPage(currentPage)

                pageNumber++
                currentPage = document.startPage(PdfDocument.PageInfo.Builder(A4_LANDSCAPE_WIDTH.toInt(), A4_LANDSCAPE_HEIGHT.toInt(), pageNumber).create())
                canvas = currentPage.canvas

                currentY = drawSubPageHeaderLandscape(canvas, paintText, storeName, pageNumber, totalPages, marginX)
                currentY = drawIndividualOrdersTableHeader(canvas, paintText, paintFill, paintStroke, currentY, marginX)
            }

            val isAlt = index % 2 == 1
            drawIndividualOrderRow(canvas, paintText, paintFill, paintStroke, order, currentY, rowHeight, isAlt, marginX)
            currentY += rowHeight
        }

        if (orders.isNotEmpty()) {
            drawIndividualOrdersSummaryRow(canvas, paintText, paintFill, paintStroke, stats, currentY, rowHeight, marginX)
        }

        drawPageFooter(canvas, paintText, pageNumber, totalPages, A4_LANDSCAPE_WIDTH, A4_LANDSCAPE_HEIGHT, marginX)
        document.finishPage(currentPage)

        val outputDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val dateStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val pdfFile = File(outputDir, "${PdfReportType.COMBINED_REPORT.filePrefix}_$dateStamp.pdf")

        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        val fileUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        return PdfGenerationResult(
            file = pdfFile,
            uri = fileUri,
            reportType = PdfReportType.COMBINED_REPORT,
            pageCount = totalPages,
            totalOrders = orders.size,
            totalPieces = stats.totalPieces,
            grandTotalAmount = stats.grandTotalAmount,
            sizeBreakdown = stats.sizeMap,
            paidOrdersCount = stats.paidOrdersCount,
            dueOrdersCount = stats.dueOrdersCount,
            paidPieces = stats.paidPieces,
            duePieces = stats.duePieces
        )
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // DATA MODEL & AGGREGATION
    // ─────────────────────────────────────────────────────────────────────────────

    data class OrderStats(
        val totalChild12: Int,
        val totalS: Int,
        val totalM: Int,
        val totalL: Int,
        val totalXL: Int,
        val totalXXL: Int,
        val totalXXXL: Int,
        val totalPieces: Int,
        val grandTotalAmount: Double,
        val paidOrdersCount: Int,
        val dueOrdersCount: Int,
        val cancelledOrdersCount: Int,
        val paidPieces: Int,
        val duePieces: Int,
        val paidAmount: Double,
        val dueAmount: Double,
        val sizeMap: Map<String, Int>
    )

    private fun calculateOrderStats(orders: List<TShirtOrder>): OrderStats {
        var totalChild12 = 0
        var totalS = 0
        var totalM = 0
        var totalL = 0
        var totalXL = 0
        var totalXXL = 0
        var totalXXXL = 0
        var totalPieces = 0
        var grandTotalAmount = 0.0

        var paidOrders = 0
        var dueOrders = 0
        var cancelledOrders = 0
        var paidPieces = 0
        var duePieces = 0
        var paidAmount = 0.0
        var dueAmount = 0.0

        for (order in orders) {
            if (order.status != OrderStatus.CANCELLED) {
                totalChild12 += order.qtyChild12
                totalS += order.qtyS
                totalM += order.qtyM
                totalL += order.qtyL
                totalXL += order.qtyXL
                totalXXL += order.qtyXXL
                totalXXXL += order.qtyXXXL
                totalPieces += order.totalQuantity
                grandTotalAmount += order.totalAmount

                if (order.status == OrderStatus.PAID || order.status == OrderStatus.DELIVERED) {
                    paidOrders++
                    paidPieces += order.totalQuantity
                    paidAmount += order.totalAmount
                } else {
                    dueOrders++
                    duePieces += order.totalQuantity
                    dueAmount += order.totalAmount
                }
            } else {
                cancelledOrders++
            }
        }

        val sizeMap = mapOf(
            "L" to totalL,
            "M" to totalM,
            "XL" to totalXL,
            "S" to totalS,
            "2XL" to totalXXL,
            "3XL" to totalXXXL,
            "Child (1-2y)" to totalChild12
        )

        return OrderStats(
            totalChild12 = totalChild12,
            totalS = totalS,
            totalM = totalM,
            totalL = totalL,
            totalXL = totalXL,
            totalXXL = totalXXL,
            totalXXXL = totalXXXL,
            totalPieces = totalPieces,
            grandTotalAmount = grandTotalAmount,
            paidOrdersCount = paidOrders,
            dueOrdersCount = dueOrders,
            cancelledOrdersCount = cancelledOrders,
            paidPieces = paidPieces,
            duePieces = duePieces,
            paidAmount = paidAmount,
            dueAmount = dueAmount,
            sizeMap = sizeMap
        )
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // DRAWING: INDIVIDUAL ORDERS SHEET (LANDSCAPE A4)
    // ─────────────────────────────────────────────────────────────────────────────

    private fun drawIndividualOrdersHeader(
        canvas: Canvas,
        paintText: Paint,
        paintFill: Paint,
        storeName: String,
        scopeLabel: String,
        orderCount: Int,
        totalPieces: Int,
        marginX: Float
    ): Float {
        var y = 20f

        // Top decorative brand bar
        paintFill.color = COLOR_PRIMARY
        canvas.drawRect(marginX, y, A4_LANDSCAPE_WIDTH - marginX, y + 4f, paintFill)
        y += 18f

        // Title and date row
        paintText.textSize = 15f
        paintText.isFakeBoldText = true
        paintText.color = COLOR_PRIMARY
        canvas.drawText(storeName, marginX, y, paintText)

        paintText.textSize = 9f
        paintText.isFakeBoldText = false
        paintText.color = COLOR_TEXT_MUTED
        val dateText = "তারিখ: ${SimpleDateFormat("dd MMMM, yyyy  hh:mm a", Locale.US).format(Date())}"
        val dateWidth = paintText.measureText(dateText)
        canvas.drawText(dateText, A4_LANDSCAPE_WIDTH - marginX - dateWidth, y, paintText)
        y += 17f

        // Subtitle & Scope
        paintText.textSize = 12f
        paintText.isFakeBoldText = true
        paintText.color = COLOR_TEXT_DARK
        canvas.drawText("শারদীয় দুর্গোৎসব — গ্রাহক টি-শার্ট অর্ডার তালিকা ও বিতরণ শীট (Individual Orders Sheet)", marginX, y, paintText)

        paintText.textSize = 9f
        paintText.isFakeBoldText = true
        paintText.color = COLOR_SECONDARY
        val scopeText = "পরিসীমা: $scopeLabel"
        val scopeWidth = paintText.measureText(scopeText)
        canvas.drawText(scopeText, A4_LANDSCAPE_WIDTH - marginX - scopeWidth, y, paintText)
        y += 15f

        // Quick Stats strip
        paintFill.color = COLOR_ROW_ALT
        val stripRect = RectF(marginX, y, A4_LANDSCAPE_WIDTH - marginX, y + 20f)
        canvas.drawRoundRect(stripRect, 4f, 4f, paintFill)

        paintText.textSize = 8.5f
        paintText.isFakeBoldText = true
        paintText.color = COLOR_PRIMARY_DARK
        canvas.drawText("মোট গ্রাহক অর্ডার: $orderCount টি", marginX + 10f, y + 14f, paintText)

        paintText.color = COLOR_TEXT_DARK
        canvas.drawText("সর্বমোট উৎপাদন: $totalPieces টি টি-শার্ট", marginX + 160f, y + 14f, paintText)

        paintText.color = COLOR_TEXT_MUTED
        canvas.drawText("অনলাইন ট্র্যাকিং: podderpara.shop", A4_LANDSCAPE_WIDTH - marginX - 180f, y + 14f, paintText)

        y += 28f
        return y
    }

    private fun drawSubPageHeaderLandscape(
        canvas: Canvas,
        paintText: Paint,
        storeName: String,
        pageNumber: Int,
        totalPages: Int,
        marginX: Float
    ): Float {
        var y = 20f
        paintText.textSize = 10f
        paintText.isFakeBoldText = true
        paintText.color = COLOR_PRIMARY
        canvas.drawText("$storeName — গ্রাহক অর্ডার তালিকা (চলমান)", marginX, y, paintText)

        paintText.textSize = 8.5f
        paintText.isFakeBoldText = false
        paintText.color = COLOR_TEXT_MUTED
        val pageText = "পৃষ্ঠা $pageNumber / $totalPages"
        val width = paintText.measureText(pageText)
        canvas.drawText(pageText, A4_LANDSCAPE_WIDTH - marginX - width, y, paintText)

        y += 14f
        return y
    }

    // Individual orders columns layout:
    // Total available width on Landscape = 842 - 48 = 794 pt.
    // 1. # ID: 54 pt
    // 2. Name: 130 pt
    // 3. Mobile: 94 pt
    // 4. Kid: 32 pt
    // 5. S: 30 pt
    // 6. M: 30 pt
    // 7. L: 30 pt
    // 8. XL: 30 pt
    // 9. 2XL: 32 pt
    // 10. 3XL: 32 pt
    // 11. Total Qty: 44 pt
    // 12. Amount: 68 pt
    // 13. Paid/Status: 76 pt
    // 14. Signature: 112 pt
    // Sum = 54 + 130 + 94 + 32 + 30 + 30 + 30 + 30 + 32 + 32 + 44 + 68 + 76 + 112 = 794 pt.
    private val COL_OFFSETS_LANDSCAPE = floatArrayOf(
        0f,   // 0: Start of ID
        54f,  // 1: Name
        184f, // 2: Mobile
        278f, // 3: Kid
        310f, // 4: S
        340f, // 5: M
        370f, // 6: L
        400f, // 7: XL
        430f, // 8: 2XL
        462f, // 9: 3XL
        494f, // 10: Total
        538f, // 11: Amount
        606f, // 12: Paid
        682f, // 13: Signature
        794f  // 14: End of table
    )

    private fun drawIndividualOrdersTableHeader(
        canvas: Canvas,
        paintText: Paint,
        paintFill: Paint,
        paintStroke: Paint,
        y: Float,
        marginX: Float
    ): Float {
        val height = 22f
        val rect = RectF(marginX, y, marginX + 794f, y + height)

        paintFill.color = COLOR_HEADER_BG
        canvas.drawRoundRect(rect, 4f, 4f, paintFill)
        canvas.drawRoundRect(rect, 4f, 4f, paintStroke)

        paintText.textSize = 8.2f
        paintText.isFakeBoldText = true
        paintText.color = COLOR_TEXT_DARK

        val baseline = y + 15f
        canvas.drawText("# আইডি", marginX + COL_OFFSETS_LANDSCAPE[0] + 4f, baseline, paintText)
        canvas.drawText("গ্রাহকের নাম (Name)", marginX + COL_OFFSETS_LANDSCAPE[1] + 4f, baseline, paintText)
        canvas.drawText("মোবাইল (Mobile)", marginX + COL_OFFSETS_LANDSCAPE[2] + 4f, baseline, paintText)

        // Size headers (centered)
        drawCenteredText(canvas, paintText, "Kid", marginX + COL_OFFSETS_LANDSCAPE[3], 32f, baseline)
        drawCenteredText(canvas, paintText, "S", marginX + COL_OFFSETS_LANDSCAPE[4], 30f, baseline)
        drawCenteredText(canvas, paintText, "M", marginX + COL_OFFSETS_LANDSCAPE[5], 30f, baseline)
        drawCenteredText(canvas, paintText, "L", marginX + COL_OFFSETS_LANDSCAPE[6], 30f, baseline)
        drawCenteredText(canvas, paintText, "XL", marginX + COL_OFFSETS_LANDSCAPE[7], 30f, baseline)
        drawCenteredText(canvas, paintText, "2XL", marginX + COL_OFFSETS_LANDSCAPE[8], 32f, baseline)
        drawCenteredText(canvas, paintText, "3XL", marginX + COL_OFFSETS_LANDSCAPE[9], 32f, baseline)

        drawCenteredText(canvas, paintText, "মোট", marginX + COL_OFFSETS_LANDSCAPE[10], 44f, baseline)
        drawCenteredText(canvas, paintText, "মূল্য (BDT)", marginX + COL_OFFSETS_LANDSCAPE[11], 68f, baseline)
        drawCenteredText(canvas, paintText, "পেমেন্ট স্ট্যাটাস", marginX + COL_OFFSETS_LANDSCAPE[12], 76f, baseline)
        drawCenteredText(canvas, paintText, "গ্রাহক স্বাক্ষর / নোট", marginX + COL_OFFSETS_LANDSCAPE[13], 112f, baseline)

        return y + height + 2f
    }

    private fun drawIndividualOrderRow(
        canvas: Canvas,
        paintText: Paint,
        paintFill: Paint,
        paintStroke: Paint,
        order: TShirtOrder,
        y: Float,
        rowHeight: Float,
        isAlt: Boolean,
        marginX: Float
    ) {
        val rect = RectF(marginX, y, marginX + 794f, y + rowHeight)
        paintFill.color = if (isAlt) COLOR_ROW_ALT else Color.WHITE
        canvas.drawRect(rect, paintFill)

        paintStroke.color = COLOR_BORDER
        canvas.drawLine(marginX, y + rowHeight, marginX + 794f, y + rowHeight, paintStroke)

        val baseline = y + 15f

        // 1. Order ID
        paintText.textSize = 8.2f
        paintText.isFakeBoldText = true
        paintText.color = COLOR_PRIMARY
        canvas.drawText("#${order.orderId}", marginX + COL_OFFSETS_LANDSCAPE[0] + 4f, baseline, paintText)

        // 2. Customer Name
        paintText.isFakeBoldText = false
        paintText.color = COLOR_TEXT_DARK
        val truncatedName = if (order.customerName.length > 22) order.customerName.take(21) + "…" else order.customerName
        canvas.drawText(truncatedName, marginX + COL_OFFSETS_LANDSCAPE[1] + 4f, baseline, paintText)

        // 3. Mobile Number (unmasked)
        paintText.color = COLOR_TEXT_MUTED
        canvas.drawText(order.mobileNumber, marginX + COL_OFFSETS_LANDSCAPE[2] + 4f, baseline, paintText)

        // 4-10: Sizes in single numbers (1, 2, 3...)
        drawSizeQtyCell(canvas, paintText, order.qtyChild12, marginX + COL_OFFSETS_LANDSCAPE[3], 32f, baseline)
        drawSizeQtyCell(canvas, paintText, order.qtyS, marginX + COL_OFFSETS_LANDSCAPE[4], 30f, baseline)
        drawSizeQtyCell(canvas, paintText, order.qtyM, marginX + COL_OFFSETS_LANDSCAPE[5], 30f, baseline)
        drawSizeQtyCell(canvas, paintText, order.qtyL, marginX + COL_OFFSETS_LANDSCAPE[6], 30f, baseline)
        drawSizeQtyCell(canvas, paintText, order.qtyXL, marginX + COL_OFFSETS_LANDSCAPE[7], 30f, baseline)
        drawSizeQtyCell(canvas, paintText, order.qtyXXL, marginX + COL_OFFSETS_LANDSCAPE[8], 32f, baseline)
        drawSizeQtyCell(canvas, paintText, order.qtyXXXL, marginX + COL_OFFSETS_LANDSCAPE[9], 32f, baseline)

        // 11. Total Quantity
        paintText.isFakeBoldText = true
        paintText.textSize = 8.5f
        paintText.color = if (order.status == OrderStatus.CANCELLED) COLOR_ERROR else COLOR_PRIMARY_DARK
        drawCenteredText(canvas, paintText, "${order.totalQuantity}", marginX + COL_OFFSETS_LANDSCAPE[10], 44f, baseline)

        // 12. Total Amount
        paintText.isFakeBoldText = false
        paintText.color = COLOR_TEXT_DARK
        val amountStr = CurrencyUtils.formatTakaPdf(order.totalAmount)
        drawCenteredText(canvas, paintText, amountStr, marginX + COL_OFFSETS_LANDSCAPE[11], 68f, baseline)

        // 13. Payment / Status Badge
        paintText.isFakeBoldText = true
        paintText.textSize = 7.5f
        when (order.status) {
            OrderStatus.PAID -> {
                paintText.color = COLOR_SUCCESS
                drawCenteredText(canvas, paintText, "Paid (পরিশোধ)", marginX + COL_OFFSETS_LANDSCAPE[12], 76f, baseline)
            }
            OrderStatus.DELIVERED -> {
                paintText.color = COLOR_SUCCESS
                drawCenteredText(canvas, paintText, "Delivered", marginX + COL_OFFSETS_LANDSCAPE[12], 76f, baseline)
            }
            OrderStatus.UNDER_PROCESSING -> {
                paintText.color = COLOR_WARNING
                drawCenteredText(canvas, paintText, "Due (বাকি)", marginX + COL_OFFSETS_LANDSCAPE[12], 76f, baseline)
            }
            OrderStatus.CANCELLED -> {
                paintText.color = COLOR_ERROR
                drawCenteredText(canvas, paintText, "Cancelled", marginX + COL_OFFSETS_LANDSCAPE[12], 76f, baseline)
            }
        }

        // 14. Signature line / blank box
        paintStroke.color = COLOR_BORDER
        val sigX = marginX + COL_OFFSETS_LANDSCAPE[13] + 8f
        val sigWidth = 96f
        canvas.drawLine(sigX, y + rowHeight - 4f, sigX + sigWidth, y + rowHeight - 4f, paintStroke)
    }

    private fun drawSizeQtyCell(canvas: Canvas, paint: Paint, qty: Int, left: Float, width: Float, baseline: Float) {
        if (qty > 0) {
            paint.isFakeBoldText = true
            paint.textSize = 8.5f
            paint.color = COLOR_PRIMARY_DARK
            drawCenteredText(canvas, paint, "$qty", left, width, baseline)
        } else {
            paint.isFakeBoldText = false
            paint.textSize = 8.0f
            paint.color = COLOR_BORDER
            drawCenteredText(canvas, paint, "-", left, width, baseline)
        }
    }

    private fun drawIndividualOrdersSummaryRow(
        canvas: Canvas,
        paintText: Paint,
        paintFill: Paint,
        paintStroke: Paint,
        stats: OrderStats,
        y: Float,
        rowHeight: Float,
        marginX: Float
    ) {
        val rect = RectF(marginX, y, marginX + 794f, y + rowHeight)
        paintFill.color = COLOR_HEADER_BG
        canvas.drawRect(rect, paintFill)
        paintStroke.color = COLOR_PRIMARY
        canvas.drawRect(rect, paintStroke)

        val baseline = y + 15f

        paintText.textSize = 8.5f
        paintText.isFakeBoldText = true
        paintText.color = COLOR_PRIMARY_DARK
        canvas.drawText("সর্বমোট উৎপাদন (TOTAL PIECES):", marginX + 8f, baseline, paintText)

        drawCenteredText(canvas, paintText, "${stats.totalChild12}", marginX + COL_OFFSETS_LANDSCAPE[3], 32f, baseline)
        drawCenteredText(canvas, paintText, "${stats.totalS}", marginX + COL_OFFSETS_LANDSCAPE[4], 30f, baseline)
        drawCenteredText(canvas, paintText, "${stats.totalM}", marginX + COL_OFFSETS_LANDSCAPE[5], 30f, baseline)
        drawCenteredText(canvas, paintText, "${stats.totalL}", marginX + COL_OFFSETS_LANDSCAPE[6], 30f, baseline)
        drawCenteredText(canvas, paintText, "${stats.totalXL}", marginX + COL_OFFSETS_LANDSCAPE[7], 30f, baseline)
        drawCenteredText(canvas, paintText, "${stats.totalXXL}", marginX + COL_OFFSETS_LANDSCAPE[8], 32f, baseline)
        drawCenteredText(canvas, paintText, "${stats.totalXXXL}", marginX + COL_OFFSETS_LANDSCAPE[9], 32f, baseline)

        paintText.color = COLOR_PRIMARY
        drawCenteredText(canvas, paintText, "${stats.totalPieces}", marginX + COL_OFFSETS_LANDSCAPE[10], 44f, baseline)

        paintText.color = COLOR_TEXT_DARK
        drawCenteredText(canvas, paintText, CurrencyUtils.formatTakaPdf(stats.grandTotalAmount), marginX + COL_OFFSETS_LANDSCAPE[11], 68f, baseline)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // DRAWING: MANUFACTURING SUBTOTAL SHEET (PORTRAIT & LANDSCAPE)
    // ─────────────────────────────────────────────────────────────────────────────

    private fun drawManufacturingSubtotalHeader(
        canvas: Canvas,
        paintText: Paint,
        paintFill: Paint,
        storeName: String,
        scopeLabel: String,
        marginX: Float
    ): Float {
        var y = 24f

        paintFill.color = COLOR_PRIMARY
        canvas.drawRect(marginX, y, A4_PORTRAIT_WIDTH - marginX, y + 4.5f, paintFill)
        y += 22f

        paintText.textSize = 16f
        paintText.isFakeBoldText = true
        paintText.color = COLOR_PRIMARY
        canvas.drawText(storeName, marginX, y, paintText)

        paintText.textSize = 9f
        paintText.isFakeBoldText = false
        paintText.color = COLOR_TEXT_MUTED
        val dateText = "তারিখ: ${SimpleDateFormat("dd MMMM, yyyy  hh:mm a", Locale.US).format(Date())}"
        val dateWidth = paintText.measureText(dateText)
        canvas.drawText(dateText, A4_PORTRAIT_WIDTH - marginX - dateWidth, y, paintText)
        y += 18f

        paintText.textSize = 13f
        paintText.isFakeBoldText = true
        paintText.color = COLOR_TEXT_DARK
        canvas.drawText("টি-শার্ট কারখানা উৎপাদন ও সাইজ সাবটোটাল শীট (Manufacturing Subtotal Sheet)", marginX, y, paintText)

        paintText.textSize = 8.5f
        paintText.isFakeBoldText = true
        paintText.color = COLOR_SECONDARY
        val scopeText = "পরিসীমা: $scopeLabel"
        val scopeWidth = paintText.measureText(scopeText)
        canvas.drawText(scopeText, A4_PORTRAIT_WIDTH - marginX - scopeWidth, y, paintText)
        y += 24f

        return y
    }

    private fun drawManufacturingKpiCards(
        canvas: Canvas,
        paintText: Paint,
        paintFill: Paint,
        paintStroke: Paint,
        stats: OrderStats,
        orderCount: Int,
        y: Float,
        marginX: Float
    ): Float {
        val totalWidth = A4_PORTRAIT_WIDTH - (marginX * 2)
        val cardWidth = (totalWidth - 16f) / 3f
        val cardHeight = 56f

        // Card 1: Total Orders
        drawKpiCard(canvas, paintText, paintFill, paintStroke, marginX, y, cardWidth, cardHeight,
            label = "মোট সম্মিলিত অর্ডার",
            value = "$orderCount টি অর্ডার",
            subtext = "Customer Bookings",
            valueColor = COLOR_PRIMARY_DARK
        )

        // Card 2: Total Pieces to Manufacture
        drawKpiCard(canvas, paintText, paintFill, paintStroke, marginX + cardWidth + 8f, y, cardWidth, cardHeight,
            label = "তৈরি করতে হবে (TOTAL)",
            value = "${stats.totalPieces} টি টি-শার্ট",
            subtext = "Factory Cutting & Stitching",
            valueColor = COLOR_PRIMARY
        )

        // Card 3: Paid vs Due
        drawKpiCard(canvas, paintText, paintFill, paintStroke, marginX + (cardWidth + 8f) * 2, y, cardWidth, cardHeight,
            label = "পরিশোধিত বনাম বাকি",
            value = "${stats.paidOrdersCount} Paid | ${stats.dueOrdersCount} Due",
            subtext = CurrencyUtils.formatTakaPdf(stats.grandTotalAmount),
            valueColor = COLOR_SUCCESS
        )

        return y + cardHeight + 16f
    }

    private fun drawKpiCard(
        canvas: Canvas,
        paintText: Paint,
        paintFill: Paint,
        paintStroke: Paint,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        label: String,
        value: String,
        subtext: String,
        valueColor: Int
    ) {
        val rect = RectF(x, y, x + width, y + height)
        paintFill.color = COLOR_ROW_ALT
        canvas.drawRoundRect(rect, 6f, 6f, paintFill)
        paintStroke.color = COLOR_BORDER
        canvas.drawRoundRect(rect, 6f, 6f, paintStroke)

        paintText.textSize = 8.2f
        paintText.isFakeBoldText = false
        paintText.color = COLOR_TEXT_MUTED
        canvas.drawText(label, x + 8f, y + 14f, paintText)

        paintText.textSize = 12.5f
        paintText.isFakeBoldText = true
        paintText.color = valueColor
        canvas.drawText(value, x + 8f, y + 32f, paintText)

        paintText.textSize = 8.0f
        paintText.isFakeBoldText = false
        paintText.color = COLOR_TEXT_MUTED
        canvas.drawText(subtext, x + 8f, y + 46f, paintText)
    }

    private fun drawManufacturingSubtotalTable(
        canvas: Canvas,
        paintText: Paint,
        paintFill: Paint,
        paintStroke: Paint,
        stats: OrderStats,
        y: Float,
        marginX: Float
    ): Float {
        var currentY = y
        val totalWidth = A4_PORTRAIT_WIDTH - (marginX * 2)

        // Section Title
        paintText.textSize = 11f
        paintText.isFakeBoldText = true
        paintText.color = COLOR_PRIMARY
        canvas.drawText("কারখানা সাইজ সাবটোটাল ও কাটিং মাস্টার হিসাব (PRODUCTION SIZE BREAKDOWN)", marginX, currentY, paintText)
        currentY += 12f

        // Table Header
        val headerHeight = 22f
        val headerRect = RectF(marginX, currentY, marginX + totalWidth, currentY + headerHeight)
        paintFill.color = COLOR_PRIMARY
        canvas.drawRoundRect(headerRect, 4f, 4f, paintFill)

        paintText.textSize = 8.5f
        paintText.isFakeBoldText = true
        paintText.color = Color.WHITE

        val baseline = currentY + 15f
        canvas.drawText("ক্র:", marginX + 6f, baseline, paintText)
        canvas.drawText("সাইজ (Size Name)", marginX + 32f, baseline, paintText)
        canvas.drawText("মাপ ও বিবরণ", marginX + 130f, baseline, paintText)
        drawCenteredText(canvas, paintText, "তৈরি করতে হবে (PCS)", marginX + 230f, 110f, baseline)
        drawCenteredText(canvas, paintText, "শতাংশ (%)", marginX + 345f, 65f, baseline)
        drawCenteredText(canvas, paintText, "কাটিং ও সেলাই চেকলিস্ট", marginX + 415f, 120f, baseline)

        currentY += headerHeight

        // Rows Data: Ordered by general apparel distribution volume (L, M, XL, S, XXL, XXXL, Child)
        val sizeRows = listOf(
            Triple("L Size", "Large (বুকের মাপ ৪২\"), উৎসবের মূল সাইজ", stats.totalL),
            Triple("M Size", "Medium (বুকের মাপ ৪০\"), তরুণ ও যুবক", stats.totalM),
            Triple("XL Size", "Extra Large (বুকের মাপ ৪৪\"), নিয়মিত", stats.totalXL),
            Triple("S Size", "Small (বুকের মাপ ৩৮\"), কিশোর ও স্লিম", stats.totalS),
            Triple("2XL Size", "Double XL (বুকের মাপ ৪৬\"), প্লাস সাইজ", stats.totalXXL),
            Triple("3XL Size", "Triple XL (বুকের মাপ ৪৮\"), স্পেশাল সাইজ", stats.totalXXXL),
            Triple("Child (1-2y)", "বাচ্চাদের সাইজ (বয়স ১-২ বছর)", stats.totalChild12)
        )

        val rowHeight = 25f
        for ((idx, item) in sizeRows.withIndex()) {
            val (sizeName, desc, count) = item
            val isAlt = idx % 2 == 1
            val rowRect = RectF(marginX, currentY, marginX + totalWidth, currentY + rowHeight)

            paintFill.color = if (isAlt) COLOR_ROW_ALT else Color.WHITE
            canvas.drawRect(rowRect, paintFill)

            paintStroke.color = COLOR_BORDER
            canvas.drawLine(marginX, currentY + rowHeight, marginX + totalWidth, currentY + rowHeight, paintStroke)

            val rowBaseline = currentY + 17f

            // Index
            paintText.textSize = 9f
            paintText.isFakeBoldText = false
            paintText.color = COLOR_TEXT_MUTED
            canvas.drawText("${idx + 1}.", marginX + 6f, rowBaseline, paintText)

            // Size Name
            paintText.isFakeBoldText = true
            paintText.textSize = 10f
            paintText.color = COLOR_PRIMARY_DARK
            canvas.drawText(sizeName, marginX + 32f, rowBaseline, paintText)

            // Description
            paintText.isFakeBoldText = false
            paintText.textSize = 8.2f
            paintText.color = COLOR_TEXT_MUTED
            canvas.drawText(desc, marginX + 130f, rowBaseline, paintText)

            // Piece Count (Prominent & Clear)
            paintText.isFakeBoldText = true
            paintText.textSize = 11f
            paintText.color = if (count > 0) COLOR_PRIMARY else COLOR_TEXT_MUTED
            val pieceText = if (count > 0) "$count Pieces" else "0 pcs"
            drawCenteredText(canvas, paintText, pieceText, marginX + 230f, 110f, rowBaseline)

            // Share percentage
            paintText.isFakeBoldText = false
            paintText.textSize = 8.5f
            paintText.color = COLOR_TEXT_DARK
            val percentage = if (stats.totalPieces > 0) String.format(Locale.US, "%.1f%%", (count.toFloat() / stats.totalPieces * 100)) else "0%"
            drawCenteredText(canvas, paintText, percentage, marginX + 345f, 65f, rowBaseline)

            // Checkbox for Cutting Master
            paintStroke.color = COLOR_BORDER
            paintFill.color = Color.WHITE
            val box1 = RectF(marginX + 420f, currentY + 7f, marginX + 432f, currentY + 19f)
            canvas.drawRect(box1, paintFill)
            canvas.drawRect(box1, paintStroke)

            paintText.textSize = 7.5f
            paintText.color = COLOR_TEXT_MUTED
            canvas.drawText("কাটিং", marginX + 436f, currentY + 16f, paintText)

            val box2 = RectF(marginX + 472f, currentY + 7f, marginX + 484f, currentY + 19f)
            canvas.drawRect(box2, paintFill)
            canvas.drawRect(box2, paintStroke)
            canvas.drawText("সেলাই", marginX + 488f, currentY + 16f, paintText)

            currentY += rowHeight
        }

        // Grand Total Row
        val totalRowHeight = 28f
        val totalRect = RectF(marginX, currentY, marginX + totalWidth, currentY + totalRowHeight)
        paintFill.color = COLOR_HEADER_BG
        canvas.drawRect(totalRect, paintFill)
        paintStroke.color = COLOR_PRIMARY
        canvas.drawRect(totalRect, paintStroke)

        val totalBaseline = currentY + 19f
        paintText.textSize = 10.5f
        paintText.isFakeBoldText = true
        paintText.color = COLOR_PRIMARY_DARK
        canvas.drawText("সর্বমোট উৎপাদন সংখ্যা (GRAND TOTAL):", marginX + 32f, totalBaseline, paintText)

        paintText.textSize = 13f
        paintText.color = COLOR_PRIMARY
        val grandTotalText = "${stats.totalPieces} Pieces (পিস)"
        drawCenteredText(canvas, paintText, grandTotalText, marginX + 230f, 110f, totalBaseline)

        paintText.textSize = 9.5f
        paintText.color = COLOR_TEXT_DARK
        drawCenteredText(canvas, paintText, "১০০%", marginX + 345f, 65f, totalBaseline)

        currentY += totalRowHeight + 16f
        return currentY
    }

    private fun drawFactoryCuttingSummaryNote(
        canvas: Canvas,
        paintText: Paint,
        paintFill: Paint,
        paintStroke: Paint,
        stats: OrderStats,
        orderCount: Int,
        y: Float,
        marginX: Float
    ): Float {
        val totalWidth = A4_PORTRAIT_WIDTH - (marginX * 2)
        val height = 64f
        val rect = RectF(marginX, y, marginX + totalWidth, y + height)

        paintFill.color = COLOR_HIGHLIGHT_BG
        canvas.drawRoundRect(rect, 6f, 6f, paintFill)
        paintStroke.color = COLOR_PRIMARY.apply { }
        canvas.drawRoundRect(rect, 6f, 6f, paintStroke)

        paintText.textSize = 9.5f
        paintText.isFakeBoldText = true
        paintText.color = COLOR_PRIMARY
        canvas.drawText("📌 কারখানা কাটিং আদেশপত্র নির্দেশিকা (FACTORY CUTTING ORDER SUMMARY):", marginX + 12f, y + 17f, paintText)

        paintText.textSize = 9.0f
        paintText.isFakeBoldText = false
        paintText.color = COLOR_TEXT_DARK
        val summaryNote1 = "সম্মিলিত $orderCount টি অর্ডারের জন্য কারখানা থেকে সর্বমোট ${stats.totalPieces} টি টি-শার্ট তৈরি করতে হবে।"
        canvas.drawText(summaryNote1, marginX + 12f, y + 33f, paintText)

        val summaryNote2 = "• L: ${stats.totalL} pcs  • M: ${stats.totalM} pcs  • XL: ${stats.totalXL} pcs  • S: ${stats.totalS} pcs  • 2XL: ${stats.totalXXL} pcs  • 3XL: ${stats.totalXXXL} pcs  • Kid: ${stats.totalChild12} pcs"
        paintText.isFakeBoldText = true
        paintText.color = COLOR_PRIMARY_DARK
        canvas.drawText(summaryNote2, marginX + 12f, y + 49f, paintText)

        return y + height + 16f
    }

    private fun drawFinancialStatusSummary(
        canvas: Canvas,
        paintText: Paint,
        paintFill: Paint,
        paintStroke: Paint,
        stats: OrderStats,
        y: Float,
        marginX: Float
    ): Float {
        val totalWidth = A4_PORTRAIT_WIDTH - (marginX * 2)
        val height = 48f
        val rect = RectF(marginX, y, marginX + totalWidth, y + height)

        paintFill.color = COLOR_ROW_ALT
        canvas.drawRoundRect(rect, 6f, 6f, paintFill)
        paintStroke.color = COLOR_BORDER
        canvas.drawRoundRect(rect, 6f, 6f, paintStroke)

        paintText.textSize = 9f
        paintText.isFakeBoldText = true
        paintText.color = COLOR_TEXT_DARK
        canvas.drawText("আর্থিক ও পরিশোধ বিবরণী:", marginX + 12f, y + 16f, paintText)

        paintText.isFakeBoldText = false
        paintText.textSize = 8.5f
        paintText.color = COLOR_SUCCESS
        val paidText = "পরিশোধিত: ${stats.paidOrdersCount} অর্ডার (${stats.paidPieces} pcs) — ${CurrencyUtils.formatTakaPdf(stats.paidAmount)}"
        canvas.drawText(paidText, marginX + 12f, y + 33f, paintText)

        paintText.color = COLOR_WARNING
        val dueText = "বাকি/প্রক্রিয়াধীন: ${stats.dueOrdersCount} অর্ডার (${stats.duePieces} pcs) — ${CurrencyUtils.formatTakaPdf(stats.dueAmount)}"
        canvas.drawText(dueText, marginX + 260f, y + 33f, paintText)

        return y + height + 20f
    }

    private fun drawFactorySignOffSection(
        canvas: Canvas,
        paintText: Paint,
        paintFill: Paint,
        paintStroke: Paint,
        y: Float,
        marginX: Float
    ) {
        val totalWidth = A4_PORTRAIT_WIDTH - (marginX * 2)
        val boxWidth = (totalWidth - 16f) / 3f
        val boxHeight = 54f

        drawSignBox(canvas, paintText, paintStroke, marginX, y, boxWidth, boxHeight, "১. কাটিং মাস্টার", "কাপড় কাটা ও পরিমাণ নিশ্চিতকরণ")
        drawSignBox(canvas, paintText, paintStroke, marginX + boxWidth + 8f, y, boxWidth, boxHeight, "২. সেলাই ও ফিনিশিং", "কোয়ালিটি চেক ও আয়রন")
        drawSignBox(canvas, paintText, paintStroke, marginX + (boxWidth + 8f) * 2, y, boxWidth, boxHeight, "৩. মন্দির বিতরণ ইনচার্জ", "চূড়ান্ত গ্রহণ ও বিতরণ")
    }

    private fun drawSignBox(
        canvas: Canvas,
        paintText: Paint,
        paintStroke: Paint,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        title: String,
        subtitle: String
    ) {
        val rect = RectF(x, y, x + width, y + height)
        paintStroke.color = COLOR_BORDER
        canvas.drawRoundRect(rect, 4f, 4f, paintStroke)

        paintText.textSize = 8.2f
        paintText.isFakeBoldText = true
        paintText.color = COLOR_TEXT_DARK
        canvas.drawText(title, x + 8f, y + 14f, paintText)

        paintText.textSize = 7.2f
        paintText.isFakeBoldText = false
        paintText.color = COLOR_TEXT_MUTED
        canvas.drawText(subtitle, x + 8f, y + 25f, paintText)

        // Sign line
        paintStroke.color = COLOR_BORDER
        canvas.drawLine(x + 8f, y + height - 8f, x + width - 8f, y + height - 8f, paintStroke)

        paintText.textSize = 7.0f
        paintText.color = COLOR_TEXT_MUTED
        canvas.drawText("স্বাক্ষর ও তারিখ", x + 8f, y + height - 10f, paintText)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // DRAWING: MANUFACTURING SUBTOTAL IN LANDSCAPE (FOR COMBINED REPORT PAGE 1)
    // ─────────────────────────────────────────────────────────────────────────────

    private fun drawManufacturingKpiCardsLandscape(
        canvas: Canvas,
        paintText: Paint,
        paintFill: Paint,
        paintStroke: Paint,
        stats: OrderStats,
        orderCount: Int,
        y: Float,
        marginX: Float
    ): Float {
        val totalWidth = A4_LANDSCAPE_WIDTH - (marginX * 2)
        val cardWidth = (totalWidth - 24f) / 4f
        val cardHeight = 52f

        drawKpiCard(canvas, paintText, paintFill, paintStroke, marginX, y, cardWidth, cardHeight,
            "মোট গ্রাহক অর্ডার", "$orderCount টি অর্ডার", "Customer Bookings", COLOR_PRIMARY_DARK)

        drawKpiCard(canvas, paintText, paintFill, paintStroke, marginX + (cardWidth + 8f), y, cardWidth, cardHeight,
            "কারখানা উৎপাদন (TOTAL)", "${stats.totalPieces} টি টি-শার্ট", "Garments to Make", COLOR_PRIMARY)

        drawKpiCard(canvas, paintText, paintFill, paintStroke, marginX + (cardWidth + 8f) * 2, y, cardWidth, cardHeight,
            "পরিশোধিত অর্ডার", "${stats.paidOrdersCount} Paid (${stats.paidPieces} pcs)", CurrencyUtils.formatTakaPdf(stats.paidAmount), COLOR_SUCCESS)

        drawKpiCard(canvas, paintText, paintFill, paintStroke, marginX + (cardWidth + 8f) * 3, y, cardWidth, cardHeight,
            "বাকি / প্রক্রিয়াধীন", "${stats.dueOrdersCount} Due (${stats.duePieces} pcs)", CurrencyUtils.formatTakaPdf(stats.dueAmount), COLOR_WARNING)

        return y + cardHeight + 14f
    }

    private fun drawManufacturingSubtotalTableLandscape(
        canvas: Canvas,
        paintText: Paint,
        paintFill: Paint,
        paintStroke: Paint,
        stats: OrderStats,
        y: Float,
        marginX: Float
    ): Float {
        var currentY = y
        val totalWidth = A4_LANDSCAPE_WIDTH - (marginX * 2)

        val headerHeight = 22f
        val headerRect = RectF(marginX, currentY, marginX + totalWidth, currentY + headerHeight)
        paintFill.color = COLOR_PRIMARY
        canvas.drawRoundRect(headerRect, 4f, 4f, paintFill)

        paintText.textSize = 8.5f
        paintText.isFakeBoldText = true
        paintText.color = Color.WHITE

        val baseline = currentY + 15f
        canvas.drawText("ক্র:", marginX + 10f, baseline, paintText)
        canvas.drawText("সাইজ (Size Name)", marginX + 40f, baseline, paintText)
        canvas.drawText("মাপ ও বিবরণ (Fit/Description)", marginX + 160f, baseline, paintText)
        drawCenteredText(canvas, paintText, "তৈরি করতে হবে (PIECES TO MAKE)", marginX + 370f, 160f, baseline)
        drawCenteredText(canvas, paintText, "শতাংশ (%)", marginX + 540f, 80f, baseline)
        drawCenteredText(canvas, paintText, "কাটিং ও সেলাই চেকলিস্ট", marginX + 630f, 150f, baseline)

        currentY += headerHeight

        val sizeRows = listOf(
            Triple("L Size", "Large (বুকের মাপ ৪২\"), উৎসবের প্রধান সাইজ", stats.totalL),
            Triple("M Size", "Medium (বুকের মাপ ৪০\"), তরুণ ও যুবক", stats.totalM),
            Triple("XL Size", "Extra Large (বুকের মাপ ৪৪\"), নিয়মিত", stats.totalXL),
            Triple("S Size", "Small (বুকের মাপ ৩৮\"), কিশোর ও স্লিম", stats.totalS),
            Triple("2XL Size", "Double XL (বুকের মাপ ৪৬\"), প্লাস সাইজ", stats.totalXXL),
            Triple("3XL Size", "Triple XL (বুকের মাপ ৪৮\"), স্পেশাল সাইজ", stats.totalXXXL),
            Triple("Child (1-2y)", "বাচ্চাদের সাইজ (বয়স ১-২ বছর)", stats.totalChild12)
        )

        val rowHeight = 22f
        for ((idx, item) in sizeRows.withIndex()) {
            val (sizeName, desc, count) = item
            val isAlt = idx % 2 == 1
            val rowRect = RectF(marginX, currentY, marginX + totalWidth, currentY + rowHeight)

            paintFill.color = if (isAlt) COLOR_ROW_ALT else Color.WHITE
            canvas.drawRect(rowRect, paintFill)

            paintStroke.color = COLOR_BORDER
            canvas.drawLine(marginX, currentY + rowHeight, marginX + totalWidth, currentY + rowHeight, paintStroke)

            val rowBaseline = currentY + 15f
            paintText.textSize = 8.5f
            paintText.isFakeBoldText = false
            paintText.color = COLOR_TEXT_MUTED
            canvas.drawText("${idx + 1}.", marginX + 10f, rowBaseline, paintText)

            paintText.isFakeBoldText = true
            paintText.color = COLOR_PRIMARY_DARK
            canvas.drawText(sizeName, marginX + 40f, rowBaseline, paintText)

            paintText.isFakeBoldText = false
            paintText.color = COLOR_TEXT_MUTED
            canvas.drawText(desc, marginX + 160f, rowBaseline, paintText)

            paintText.isFakeBoldText = true
            paintText.textSize = 10f
            paintText.color = if (count > 0) COLOR_PRIMARY else COLOR_TEXT_MUTED
            val pieceText = if (count > 0) "$count Pieces" else "0 pcs"
            drawCenteredText(canvas, paintText, pieceText, marginX + 370f, 160f, rowBaseline)

            paintText.isFakeBoldText = false
            paintText.textSize = 8.5f
            paintText.color = COLOR_TEXT_DARK
            val percentage = if (stats.totalPieces > 0) String.format(Locale.US, "%.1f%%", (count.toFloat() / stats.totalPieces * 100)) else "0%"
            drawCenteredText(canvas, paintText, percentage, marginX + 540f, 80f, rowBaseline)

            // Checkboxes
            paintStroke.color = COLOR_BORDER
            paintFill.color = Color.WHITE
            val box1 = RectF(marginX + 640f, currentY + 5f, marginX + 652f, currentY + 17f)
            canvas.drawRect(box1, paintFill)
            canvas.drawRect(box1, paintStroke)

            paintText.textSize = 7.5f
            paintText.color = COLOR_TEXT_MUTED
            canvas.drawText("কাটিং সম্পন্ন", marginX + 656f, currentY + 15f, paintText)

            val box2 = RectF(marginX + 710f, currentY + 5f, marginX + 722f, currentY + 17f)
            canvas.drawRect(box2, paintFill)
            canvas.drawRect(box2, paintStroke)
            canvas.drawText("সেলাই সম্পন্ন", marginX + 726f, currentY + 15f, paintText)

            currentY += rowHeight
        }

        // Total Row
        val totalRowHeight = 24f
        val totalRect = RectF(marginX, currentY, marginX + totalWidth, currentY + totalRowHeight)
        paintFill.color = COLOR_HEADER_BG
        canvas.drawRect(totalRect, paintFill)
        paintStroke.color = COLOR_PRIMARY
        canvas.drawRect(totalRect, paintStroke)

        val totalBaseline = currentY + 16f
        paintText.textSize = 9.5f
        paintText.isFakeBoldText = true
        paintText.color = COLOR_PRIMARY_DARK
        canvas.drawText("সর্বমোট কারখানা উৎপাদন সংখ্যা (GRAND TOTAL):", marginX + 40f, totalBaseline, paintText)

        paintText.textSize = 11.5f
        paintText.color = COLOR_PRIMARY
        drawCenteredText(canvas, paintText, "${stats.totalPieces} Pieces (পিস)", marginX + 370f, 160f, totalBaseline)

        paintText.textSize = 9.5f
        paintText.color = COLOR_TEXT_DARK
        drawCenteredText(canvas, paintText, "১০০%", marginX + 540f, 80f, totalBaseline)

        currentY += totalRowHeight + 14f
        return currentY
    }

    private fun drawFactorySignOffSectionLandscape(
        canvas: Canvas,
        paintText: Paint,
        paintFill: Paint,
        paintStroke: Paint,
        y: Float,
        marginX: Float
    ) {
        val totalWidth = A4_LANDSCAPE_WIDTH - (marginX * 2)
        val boxWidth = (totalWidth - 16f) / 3f
        val boxHeight = 44f

        drawSignBox(canvas, paintText, paintStroke, marginX, y, boxWidth, boxHeight, "১. কাটিং মাস্টার", "কাপড় কাটা ও পরিমাণ নিশ্চিতকরণ")
        drawSignBox(canvas, paintText, paintStroke, marginX + boxWidth + 8f, y, boxWidth, boxHeight, "২. সেলাই ও ফিনিশিং", "কোয়ালিটি চেক ও আয়রন")
        drawSignBox(canvas, paintText, paintStroke, marginX + (boxWidth + 8f) * 2, y, boxWidth, boxHeight, "৩. মন্দির বিতরণ ইনচার্জ", "চূড়ান্ত গ্রহণ ও বিতরণ")
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // FOOTER & TEXT UTILITIES
    // ─────────────────────────────────────────────────────────────────────────────

    private fun drawPageFooter(
        canvas: Canvas,
        paintText: Paint,
        pageNumber: Int,
        totalPages: Int,
        pageWidth: Float,
        pageHeight: Float,
        marginX: Float
    ) {
        val y = pageHeight - 16f
        paintText.textSize = 7.5f
        paintText.isFakeBoldText = false
        paintText.color = COLOR_TEXT_MUTED

        val footerLeft = "পোদ্দারপাড়া সর্বজনীন দুর্গা মন্দির • শারদীয় দুর্গোৎসব টি-শার্ট বিতরণ ব্যবস্থাপনা"
        canvas.drawText(footerLeft, marginX, y, paintText)

        val pageText = "পৃষ্ঠা $pageNumber / $totalPages"
        val width = paintText.measureText(pageText)
        canvas.drawText(pageText, pageWidth - marginX - width, y, paintText)
    }

    private fun drawCenteredText(
        canvas: Canvas,
        paint: Paint,
        text: String,
        cellLeft: Float,
        cellWidth: Float,
        baseline: Float
    ) {
        val textWidth = paint.measureText(text)
        val x = cellLeft + ((cellWidth - textWidth) / 2f).coerceAtLeast(0f)
        canvas.drawText(text, x, baseline, paint)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // FILE EXPORT & SHARING HELPERS
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Saves the generated PDF file directly into the public Downloads directory.
     */
    fun savePdfToDownloads(context: Context, pdfFile: File, targetFileName: String): Uri? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, targetFileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        pdfFile.inputStream().use { input ->
                            input.copyTo(out)
                        }
                    }
                    Log.d(TAG, "Successfully exported PDF via MediaStore to: $uri")
                    uri
                } else null
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val destFile = File(downloadsDir, targetFileName)
                pdfFile.copyTo(destFile, overwrite = true)
                Log.d(TAG, "Successfully exported PDF to public path: ${destFile.absolutePath}")
                Uri.fromFile(destFile)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save PDF to downloads directory", e)
            null
        }
    }

    /**
     * Creates an Intent to share the PDF via Android Share Sheet.
     */
    fun createShareIntent(context: Context, pdfFile: File, subjectTitle: String): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subjectTitle)
            putExtra(Intent.EXTRA_TEXT, "$subjectTitle — পোদ্দারপাড়া সর্বজনীন দুর্গা মন্দির")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Creates an Intent to open and view the PDF in an external viewer.
     */
    fun createOpenIntent(context: Context, pdfFile: File): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
