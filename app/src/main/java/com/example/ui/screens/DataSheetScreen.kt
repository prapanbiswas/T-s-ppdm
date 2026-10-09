package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.OrderDataSheetSummary
import com.example.ui.OrderViewModel
import com.example.ui.components.PdfPreviewDialog
import com.example.ui.components.SleekButton
import com.example.ui.components.SleekCard
import com.example.ui.components.SleekOutlinedButton
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AppBorder
import com.example.ui.theme.AppSurface
import com.example.ui.theme.AppSurfaceSubtle
import com.example.ui.theme.AppTextMuted
import com.example.ui.theme.AppTextPrimary
import com.example.ui.theme.AppTextSecondary
import com.example.ui.theme.BrandDark
import com.example.ui.theme.BrandError
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSuccess
import com.example.ui.theme.BrandWarning
import com.example.ui.theme.MandirCrimsonDark
import com.example.ui.theme.MandirGold
import com.example.ui.theme.MandirGoldLight
import com.example.util.CurrencyUtils

@Composable
fun DataSheetScreen(
    viewModel: OrderViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val summary by viewModel.dataSheetSummary.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrdersForSummary.collectAsStateWithLifecycle()
    val filteredOrders by viewModel.orders.collectAsStateWithLifecycle()
    val storeName by viewModel.storeName.collectAsStateWithLifecycle()

    var showPdfDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .testTag("data_sheet_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Analytics Card
            item {
                Surface(
                    color = MandirCrimsonDark,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth().testTag("hero_totals_card")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "পোদ্দারপাড়া সর্বজনীন দুর্গা মন্দির",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp,
                                    color = MandirGoldLight
                                )
                                Text(
                                    text = "উৎসবের টি-শার্ট হিসাব ও সারাংশ",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = MandirGold,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        HorizontalDivider(thickness = 0.8.dp, color = Color.White.copy(alpha = 0.15f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Total Pieces
                            Column {
                                Text(
                                    text = "মোট টি-শার্ট সংখ্যা",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = Color(0xFFFECDD3)
                                )
                                Text(
                                    text = "${summary.totalTShirts} টি",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "${summary.totalOrders} টি অর্ডারের মাধ্যমে",
                                    fontSize = 12.sp,
                                    color = Color(0xFFFECDD3)
                                )
                            }

                            // Total Revenue
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "সর্বমোট মূল্য (টাকা)",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = Color(0xFFFECDD3)
                                )
                                Text(
                                    text = CurrencyUtils.formatTaka(summary.grandTotalAmount),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp,
                                    color = MandirGoldLight
                                )
                                Text(
                                    text = "বুকিংকৃত মোট পরিমাণ",
                                    fontSize = 12.sp,
                                    color = Color(0xFFFECDD3)
                                )
                            }
                        }
                    }
                }
            }

            // SMS Gateway Metrics Card
            item {
                SleekCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Sms,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SMS GATEWAY DISPATCH STATUS",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp,
                                    color = AppTextPrimary
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricBlock(
                                title = "Sent",
                                count = summary.smsSentCount,
                                color = BrandSuccess,
                                modifier = Modifier.weight(1f)
                            )
                            MetricBlock(
                                title = "Delivered",
                                count = summary.smsDeliveredCount,
                                color = Color(0xFF0D9488),
                                modifier = Modifier.weight(1f)
                            )
                            MetricBlock(
                                title = "Pending",
                                count = summary.smsPendingCount,
                                color = BrandWarning,
                                modifier = Modifier.weight(1f)
                            )
                            MetricBlock(
                                title = "Failed",
                                count = summary.smsFailedCount,
                                color = BrandError,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Cloud Backup Status Card
            item {
                SleekCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "CLOUD BACKUP & DATABASE SYNC",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp,
                                    color = AppTextPrimary
                                )
                            }

                            Text(
                                text = "Single User • Offline First",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppTextSecondary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricBlock(
                                title = "Cloud Backed Up",
                                count = summary.cloudSyncedCount,
                                color = BrandSuccess,
                                modifier = Modifier.weight(1f)
                            )
                            MetricBlock(
                                title = "Pending Cloud Sync",
                                count = summary.cloudPendingCount,
                                color = if (summary.cloudPendingCount > 0) BrandWarning else AppTextSecondary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Size Breakdown Header
            item {
                Text(
                    text = "PRODUCTION SIZE BREAKDOWN",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.8.sp,
                    color = AppTextSecondary
                )
            }

            // Size Breakdown Card
            item {
                SleekCard(
                    modifier = Modifier.fillMaxWidth().testTag("size_breakdown_card")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        val total = if (summary.totalTShirts > 0) summary.totalTShirts.toFloat() else 1f

                        SizeCountingBar("Child 1-2y", "Toddler Fit", summary.totalChild12, summary.totalChild12 / total, BrandIndigo)
                        HorizontalDivider(thickness = 0.8.dp, color = AppBorder)

                        SizeCountingBar("S", "Adult Small (36\")", summary.totalS, summary.totalS / total, BrandPrimary)
                        HorizontalDivider(thickness = 0.8.dp, color = AppBorder)

                        SizeCountingBar("M", "Adult Medium (38-40\")", summary.totalM, summary.totalM / total, BrandSuccess)
                        HorizontalDivider(thickness = 0.8.dp, color = AppBorder)

                        SizeCountingBar("L", "Adult Large (42-44\")", summary.totalL, summary.totalL / total, BrandWarning)
                        HorizontalDivider(thickness = 0.8.dp, color = AppBorder)

                        SizeCountingBar("XL", "Adult Extra Large (46\")", summary.totalXL, summary.totalXL / total, BrandIndigo)
                        HorizontalDivider(thickness = 0.8.dp, color = AppBorder)

                        SizeCountingBar("2XL", "Adult 2X Large (48-50\")", summary.totalXXL, summary.totalXXL / total, BrandPrimary)
                        HorizontalDivider(thickness = 0.8.dp, color = AppBorder)

                        SizeCountingBar("3XL", "Adult 3X Large (52-54\")", summary.totalXXXL, summary.totalXXXL / total, BrandDark)
                    }
                }
            }

            // Financial Summary
            item {
                Text(
                    text = "ORDER STATUS & REVENUE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.8.sp,
                    color = AppTextSecondary
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    RevenueStatusRow("Under Processing", summary.underProcessingCount, summary.underProcessingAmount, Icons.Default.PendingActions, BrandWarning)
                    RevenueStatusRow("Paid Orders", summary.paidCount, summary.paidAmount, Icons.Default.Payments, BrandSuccess)
                    RevenueStatusRow("Delivered Orders", summary.deliveredCount, summary.deliveredAmount, Icons.Default.LocalShipping, BrandIndigo)
                    if (summary.cancelledCount > 0) {
                        RevenueStatusRow("Cancelled Orders", summary.cancelledCount, summary.cancelledAmount, Icons.Default.Assessment, BrandError)
                    }
                }
            }

            // Export & Report Actions
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SleekButton(
                        text = "পিডিএফ রিপোর্ট তৈরি করুন (Generate PDF)",
                        onClick = { showPdfDialog = true },
                        icon = Icons.Default.PictureAsPdf,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "generate_pdf_report_btn"
                    )

                    SleekOutlinedButton(
                        text = "টেক্সট সারাংশ শেয়ার (Share Text Summary)",
                        onClick = { shareDataSheetText(context, summary) },
                        icon = Icons.Default.Share,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // PDF Preview & Export Dialog
        if (showPdfDialog) {
            PdfPreviewDialog(
                allOrders = allOrders,
                filteredOrders = filteredOrders,
                storeName = storeName,
                onDismiss = { showPdfDialog = false }
            )
        }
    }
}

@Composable
private fun MetricBlock(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
            Text(
                text = "$count",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun SizeCountingBar(
    sizeLabel: String,
    sizeName: String,
    count: Int,
    fraction: Float,
    barColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = AppSurfaceSubtle,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, AppBorder),
                    modifier = Modifier.size(width = 46.dp, height = 26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = sizeLabel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = AppTextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = sizeName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTextPrimary
                )
            }

            Text(
                text = "$count PCS",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = BrandPrimary
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(AppSurfaceSubtle, shape = RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .background(barColor, shape = RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
private fun RevenueStatusRow(
    title: String,
    count: Int,
    amount: Double,
    icon: ImageVector,
    color: Color
) {
    SleekCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AppTextPrimary
                    )
                    Text(
                        text = "$count Orders",
                        fontSize = 11.sp,
                        color = AppTextSecondary
                    )
                }
            }

            Text(
                text = CurrencyUtils.formatTaka(amount),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = AppTextPrimary
            )
        }
    }
}

private fun shareDataSheetText(context: Context, summary: OrderDataSheetSummary) {
    val shareBody = buildString {
        appendLine("📊 APPAREL PRODUCTION & DISPATCH SUMMARY")
        appendLine("==========================================")
        appendLine("Total Orders: ${summary.totalOrders}")
        appendLine("Total T-Shirts: ${summary.totalTShirts} pcs")
        appendLine("Grand Total Revenue: ${CurrencyUtils.formatTaka(summary.grandTotalAmount)}")
        appendLine("SMS Dispatched: ${summary.smsSentCount} | Failed: ${summary.smsFailedCount}")
        appendLine()
        appendLine("SIZE BREAKDOWN:")
        appendLine("• Child (1-2y): ${summary.totalChild12} pcs")
        appendLine("• Adult S: ${summary.totalS} pcs")
        appendLine("• Adult M: ${summary.totalM} pcs")
        appendLine("• Adult L: ${summary.totalL} pcs")
        appendLine("• Adult XL: ${summary.totalXL} pcs")
        appendLine("• Adult 2XL: ${summary.totalXXL} pcs")
        appendLine("• Adult 3XL: ${summary.totalXXXL} pcs")
        appendLine()
        appendLine("ORDER STATUS:")
        appendLine("• Processing: ${summary.underProcessingCount} (${CurrencyUtils.formatTaka(summary.underProcessingAmount)})")
        appendLine("• Paid: ${summary.paidCount} (${CurrencyUtils.formatTaka(summary.paidAmount)})")
        appendLine("• Delivered: ${summary.deliveredCount} (${CurrencyUtils.formatTaka(summary.deliveredAmount)})")
        if (summary.cancelledCount > 0) {
            appendLine("• Cancelled: ${summary.cancelledCount} (${CurrencyUtils.formatTaka(summary.cancelledAmount)})")
        }
        appendLine("==========================================")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Apparel Production Summary")
        putExtra(Intent.EXTRA_TEXT, shareBody)
    }
    context.startActivity(Intent.createChooser(intent, "Share Data Sheet"))
}
