package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TShirtOrder
import com.example.ui.theme.AppBorder
import com.example.ui.theme.AppSurface
import com.example.ui.theme.AppSurfaceSubtle
import com.example.ui.theme.AppTextMuted
import com.example.ui.theme.AppTextPrimary
import com.example.ui.theme.AppTextSecondary
import com.example.ui.theme.BrandDark
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSuccess
import com.example.ui.theme.BrandSuccessLight
import com.example.ui.theme.BrandWarning
import com.example.util.CurrencyUtils
import com.example.util.PdfGenerationResult
import com.example.util.PdfReportGenerator
import com.example.util.PdfReportType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PdfExportScope(val title: String, val subtitle: String) {
    ALL_ORDERS("সকল অর্ডার (All Orders)", "ডাটাবেজের সকল অর্ডারের সম্পূর্ণ হিসাব"),
    FILTERED_ORDERS("ফিল্টারকৃত অর্ডার (Filtered)", "বর্তমান সার্চ এবং স্ট্যাটাস ফিল্টারে প্রদর্শিত অর্ডারসমূহ")
}

@Composable
fun PdfPreviewDialog(
    allOrders: List<TShirtOrder>,
    filteredOrders: List<TShirtOrder>,
    storeName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedReportType by remember { mutableStateOf(PdfReportType.INDIVIDUAL_ORDERS) }

    var selectedScope by remember {
        mutableStateOf(if (filteredOrders.size < allOrders.size && filteredOrders.isNotEmpty()) PdfExportScope.FILTERED_ORDERS else PdfExportScope.ALL_ORDERS)
    }

    val activeList = if (selectedScope == PdfExportScope.ALL_ORDERS) allOrders else filteredOrders

    var isGenerating by remember { mutableStateOf(false) }
    var generationResult by remember { mutableStateOf<PdfGenerationResult?>(null) }
    var savedDownloadUri by remember { mutableStateOf<String?>(null) }

    // Generate or update PDF whenever report type, scope, or orders change
    fun generatePdf() {
        if (activeList.isEmpty()) {
            generationResult = null
            return
        }
        isGenerating = true
        scope.launch(Dispatchers.IO) {
            val result = PdfReportGenerator.createReportPdf(
                context = context,
                orders = activeList,
                storeName = storeName,
                scopeLabel = selectedScope.title,
                reportType = selectedReportType
            )
            withContext(Dispatchers.Main) {
                generationResult = result
                isGenerating = false
            }
        }
    }

    LaunchedEffect(selectedReportType, selectedScope, activeList.size) {
        generatePdf()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pdf_preview_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = AppSurface,
            border = BorderStroke(1.dp, AppBorder),
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(BrandPrimary, shape = RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "PDF রিপোর্ট জেনারেটর",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = AppTextPrimary
                            )
                            Text(
                                text = "সহজ ও পরিষ্কার ফরম্যাটে ডাউনলোড ও শেয়ার",
                                fontSize = 11.sp,
                                color = AppTextSecondary
                            )
                        }
                    }
                }

                HorizontalDivider(thickness = 0.8.dp, color = AppBorder)

                // 1. REPORT TYPE SELECTION (Individual Orders vs Manufacturing Subtotal vs Combined)
                Text(
                    text = "রিপোর্টের ধরন নির্বাচন করুন (CHOOSE REPORT FORMAT)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp,
                    color = AppTextSecondary
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val reportTypes = listOf(
                        Triple(
                            PdfReportType.INDIVIDUAL_ORDERS,
                            "১. গ্রাহক অর্ডার তালিকা শিট (Individual Orders)",
                            "প্রতিটি অর্ডারের একক রো: আইডি, নাম, মোবাইল, প্রতি সাইজের সংখ্যা (1, 2, 3), মোট পিস ও পেমেন্ট"
                        ),
                        Triple(
                            PdfReportType.MANUFACTURING_SUBTOTAL,
                            "২. কারখানা উৎপাদন সাবটোটাল শিট (Factory Subtotal)",
                            "কারখানার সহজ হিসাব: মোট অর্ডার ও সব সাইজের সর্বমোট পিস (যেমন L: 9 pcs, M: 6 pcs ইত্যাদি)"
                        ),
                        Triple(
                            PdfReportType.COMBINED_REPORT,
                            "৩. সম্পূর্ণ প্যাকেজ (উভয় শিট এক সাথে - 2 in 1)",
                            "গ্রাহক অর্ডার তালিকা এবং কারখানার সাইজ সাবটোটাল শিট উভয়ই একত্রে এক ফাইলে"
                        )
                    )

                    reportTypes.forEach { (type, title, desc) ->
                        val isSelected = selectedReportType == type
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) BrandPrimary.copy(alpha = 0.08f) else AppSurfaceSubtle,
                            border = BorderStroke(1.dp, if (isSelected) BrandPrimary else AppBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedReportType = type
                                    savedDownloadUri = null
                                }
                                .testTag("report_type_${type.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        selectedReportType = type
                                        savedDownloadUri = null
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = BrandPrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) BrandPrimary else AppTextPrimary
                                    )
                                    Text(
                                        text = desc,
                                        fontSize = 10.sp,
                                        color = AppTextSecondary,
                                        lineHeight = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. SCOPE SELECTOR: All Orders vs Current Filters
                Text(
                    text = "রিপোর্টের পরিধি (ORDER SCOPE)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp,
                    color = AppTextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PdfExportScope.values().forEach { scopeOption ->
                        val isSelected = selectedScope == scopeOption
                        val count = if (scopeOption == PdfExportScope.ALL_ORDERS) allOrders.size else filteredOrders.size

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) BrandPrimary.copy(alpha = 0.08f) else AppSurfaceSubtle,
                            border = BorderStroke(1.dp, if (isSelected) BrandPrimary else AppBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedScope = scopeOption
                                    savedDownloadUri = null
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        selectedScope = scopeOption
                                        savedDownloadUri = null
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = BrandPrimary)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(
                                        text = if (scopeOption == PdfExportScope.ALL_ORDERS) "সকল ($count)" else "ফিল্টারকৃত ($count)",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = AppTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. LIVE DOCUMENT & PRODUCTION PREVIEW
                val result = generationResult
                if (result != null) {
                    // Document Overview Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppSurfaceSubtle,
                        border = BorderStroke(1.dp, AppBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("নির্বাচিত রিপোর্ট:", fontSize = 11.sp, color = AppTextSecondary)
                                Text(result.reportType.titleBengali, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandPrimary)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("মোট সম্মিলিত অর্ডার:", fontSize = 11.sp, color = AppTextSecondary)
                                Text("${result.totalOrders} টি অর্ডার", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppTextPrimary)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("কারখানা উৎপাদন সংখ্যা:", fontSize = 11.sp, color = AppTextSecondary)
                                Text("${result.totalPieces} টি টি-শার্ট", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandPrimary)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("পরিশোধিত বনাম বাকি:", fontSize = 11.sp, color = AppTextSecondary)
                                Text("${result.paidOrdersCount} Paid • ${result.dueOrdersCount} Due", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandSuccess)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("পিডিএফ সাইজ ও পৃষ্ঠা:", fontSize = 11.sp, color = AppTextSecondary)
                                Text("${result.pageCount} পৃষ্ঠা • A4 ফরম্যাট", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = AppTextMuted)
                            }
                        }
                    }

                    // FACTORY PIECES MATRIX REVIEW CARD
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppSurface,
                        border = BorderStroke(1.dp, BrandPrimary.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "কারখানা উৎপাদন সাইজ হিসাব (MANUFACTURING)",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandPrimary,
                                    letterSpacing = 0.4.sp
                                )
                                Text(
                                    text = "${result.totalPieces} pcs",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandPrimary
                                )
                            }

                            val sizeItems = listOf(
                                "L size" to (result.sizeBreakdown["L"] ?: 0),
                                "M size" to (result.sizeBreakdown["M"] ?: 0),
                                "XL size" to (result.sizeBreakdown["XL"] ?: 0),
                                "S size" to (result.sizeBreakdown["S"] ?: 0),
                                "2XL size" to (result.sizeBreakdown["2XL"] ?: 0),
                                "3XL size" to (result.sizeBreakdown["3XL"] ?: 0),
                                "Child (1-2y)" to (result.sizeBreakdown["Child (1-2y)"] ?: 0)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                sizeItems.chunked(2).forEach { rowPair ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowPair.forEach { (sizeName, count) ->
                                            Surface(
                                                color = AppSurfaceSubtle,
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(0.6.dp, AppBorder),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(sizeName, fontSize = 11.sp, color = AppTextSecondary)
                                                    Text(
                                                        "$count pcs",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (count > 0) BrandPrimary else AppTextMuted
                                                    )
                                                }
                                            }
                                        }
                                        if (rowPair.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }

                            // Subtotal Plain-Language note
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BrandPrimary.copy(alpha = 0.05f), shape = RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "📌 নির্দেশিকা: সম্মিলিত ${result.totalOrders} টি অর্ডারের জন্য কারখানা থেকে উল্লেখিত সংখ্যায় মোট ${result.totalPieces} টি টি-শার্ট তৈরি করা হবে।",
                                    fontSize = 10.sp,
                                    color = AppTextPrimary,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                } else if (isGenerating) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                color = BrandPrimary,
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "PDF তৈরি করা হচ্ছে...",
                                fontSize = 12.sp,
                                color = AppTextSecondary
                            )
                        }
                    }
                }

                // Download Success Alert
                AnimatedVisibility(
                    visible = savedDownloadUri != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BrandSuccessLight,
                        border = BorderStroke(1.dp, BrandSuccess.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = BrandSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PDF সফলভাবে ডাউনলোড ফোল্ডারে সেভ হয়েছে!",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandSuccess
                            )
                        }
                    }
                }

                // 4. ACTION BUTTONS (Download, Share, Open, Dismiss)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Save to Downloads Button
                    SleekButton(
                        text = "PDF ডাউনলোড করুন (Download PDF)",
                        onClick = {
                            val res = generationResult
                            if (res != null) {
                                val dateStamp = SimpleDateFormat("ddMMM_HHmm", Locale.US).format(Date())
                                val targetFileName = "${res.reportType.filePrefix}_$dateStamp.pdf"
                                val savedUri = PdfReportGenerator.savePdfToDownloads(context, res.file, targetFileName)
                                if (savedUri != null) {
                                    savedDownloadUri = savedUri.toString()
                                    Toast.makeText(context, "ডাউনলোড ফোল্ডারে সংরক্ষিত হয়েছে: $targetFileName", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "ফাইল সংরক্ষণ করতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        icon = Icons.Default.Download,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = generationResult != null && !isGenerating,
                        testTag = "pdf_download_button"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Share Intent Button
                        SleekOutlinedButton(
                            text = "শেয়ার করুন",
                            onClick = {
                                val res = generationResult
                                if (res != null) {
                                    val shareIntent = PdfReportGenerator.createShareIntent(
                                        context = context,
                                        pdfFile = res.file,
                                        subjectTitle = res.reportType.titleBengali
                                    )
                                    context.startActivity(Intent.createChooser(shareIntent, "PDF রিপোর্ট শেয়ার করুন"))
                                }
                            },
                            icon = Icons.Default.Share,
                            modifier = Modifier.weight(1f),
                            enabled = generationResult != null && !isGenerating,
                            testTag = "pdf_share_button"
                        )

                        // Open in Default PDF Viewer
                        SleekOutlinedButton(
                            text = "PDF দেখুন",
                            onClick = {
                                val res = generationResult
                                if (res != null) {
                                    try {
                                        val openIntent = PdfReportGenerator.createOpenIntent(context, res.file)
                                        context.startActivity(openIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "PDF ভিউয়ার অ্যাপ পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            icon = Icons.Default.OpenInNew,
                            modifier = Modifier.weight(1f),
                            enabled = generationResult != null && !isGenerating,
                            testTag = "pdf_open_button"
                        )
                    }

                    // Close Dialog Button
                    SleekOutlinedButton(
                        text = "বন্ধ করুন (Close)",
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "pdf_dialog_close_button"
                    )
                }
            }
        }
    }
}
