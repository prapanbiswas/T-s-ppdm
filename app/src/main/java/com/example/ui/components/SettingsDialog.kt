package com.example.ui.components

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.runtime.rememberCoroutineScope
import com.example.update.AppUpdateManager
import kotlinx.coroutines.launch
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sms.SimCardInfo
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AppBorder
import com.example.ui.theme.AppSurface
import com.example.ui.theme.AppSurfaceSubtle
import com.example.ui.theme.AppTextMuted
import com.example.ui.theme.AppTextPrimary
import com.example.ui.theme.AppTextSecondary
import com.example.ui.theme.BrandDark
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSuccess

@Composable
fun SettingsDialog(
    currentStoreName: String,
    currentStorePhone: String,
    currentUnitPrice: Double,
    currentTrackingBaseUrl: String,
    currentAutoSendSms: Boolean,
    currentSelectedSimId: Int,
    availableSimCards: List<SimCardInfo>,
    onSave: (name: String, phone: String, unitPrice: Double, trackingUrl: String, autoSms: Boolean, simId: Int) -> Unit,
    onTestReachability: () -> Unit,
    onSyncNow: () -> Unit,
    onRestoreFromCloud: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var storeName by remember { mutableStateOf(currentStoreName) }
    var storePhone by remember { mutableStateOf(currentStorePhone) }
    var trackingBaseUrl by remember { mutableStateOf(currentTrackingBaseUrl) }
    var autoSendSms by remember { mutableStateOf(currentAutoSendSms) }
    var selectedSimId by remember { mutableStateOf(currentSelectedSimId) }
    var unitPriceText by remember {
        mutableStateOf(if (currentUnitPrice % 1.0 == 0.0) currentUnitPrice.toLong().toString() else currentUnitPrice.toString())
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = AppSurface,
            border = BorderStroke(1.dp, AppBorder),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                .size(36.dp)
                                .background(BrandDark, shape = RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Settings & Gateway",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = AppTextPrimary
                            )
                            Text(
                                text = "SMS dispatch & tracking domain",
                                fontSize = 12.sp,
                                color = AppTextSecondary
                            )
                        }
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = AppBorder)

                // 1. Store Details & Pricing
                Text(
                    text = "STORE & PRICING",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = AppTextSecondary
                )

                SleekTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = "মন্দির / প্রতিষ্ঠানের নাম",
                    placeholder = "পোদ্দারপাড়া সর্বজনীন দুর্গা মন্দির",
                    leadingIcon = Icons.Default.Store,
                    testTag = "settings_store_name_input"
                )

                SleekTextField(
                    value = storePhone,
                    onValueChange = { storePhone = it },
                    label = "যোগাযোগের ফোন নম্বর",
                    placeholder = "+880 1700-000000",
                    leadingIcon = Icons.Default.Phone,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    testTag = "settings_store_phone_input"
                )

                SleekTextField(
                    value = unitPriceText,
                    onValueChange = { unitPriceText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = "টি-শার্টের নির্ধারিত মূল্য (৳)",
                    placeholder = "350",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    testTag = "settings_unit_price_input"
                )

                HorizontalDivider(thickness = 1.dp, color = AppBorder)

                // 2. Tracking Domain
                Text(
                    text = "CUSTOMER TRACKING URL",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = AppTextSecondary
                )

                SleekTextField(
                    value = trackingBaseUrl,
                    onValueChange = { trackingBaseUrl = it },
                    label = "ট্র্যাকিং ডোমেন / বেস ইউআরএল",
                    placeholder = "https://podderpara.shop",
                    leadingIcon = Icons.Default.Language,
                    testTag = "settings_tracking_url_input"
                )

                val previewTrackingUrl = run {
                    val clean = trackingBaseUrl.trim().removeSuffix("/")
                    when {
                        clean.contains("?id=") || clean.endsWith("?id") -> "$clean" + "7K2B9X4"
                        clean.contains("?") -> "$clean&id=7K2B9X4"
                        clean.endsWith("/tracker") -> "$clean/?id=7K2B9X4"
                        else -> "$clean/?id=7K2B9X4"
                    }
                }

                Text(
                    text = "গ্রাহক এসএমএস লিংক ফরম্যাট: $previewTrackingUrl",
                    fontSize = 11.sp,
                    color = AppTextMuted
                )

                HorizontalDivider(thickness = 1.dp, color = AppBorder)

                // 3. Android SMS Gateway Settings
                Text(
                    text = "SMS GATEWAY & SIM SELECTION",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = AppTextSecondary
                )

                // Auto-SMS Toggle
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AppSurfaceSubtle,
                    border = BorderStroke(1.dp, AppBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Send Confirmation SMS",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = AppTextPrimary
                            )
                            Text(
                                text = "Automatically text customer order summary & tracking URL",
                                fontSize = 11.sp,
                                color = AppTextSecondary
                            )
                        }

                        Switch(
                            checked = autoSendSms,
                            onCheckedChange = { autoSendSms = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BrandPrimary
                            )
                        )
                    }
                }

                // SIM Card Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Preferred Sending SIM Card:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = AppTextPrimary
                    )

                    // Option: Default SIM
                    SimSelectionRow(
                        title = "System Default SIM",
                        subtitle = "Uses the device's default outgoing SIM",
                        isSelected = selectedSimId == -1,
                        onClick = { selectedSimId = -1 }
                    )

                    // List physical detected SIMs
                    availableSimCards.forEach { sim ->
                        SimSelectionRow(
                            title = "${sim.displayName} (${sim.carrierName})",
                            subtitle = "Slot ${sim.simSlotIndex + 1} (SubId: ${sim.subscriptionId})",
                            isSelected = selectedSimId == sim.subscriptionId,
                            onClick = { selectedSimId = sim.subscriptionId }
                        )
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = AppBorder)

                // 4. Cloud Diagnostics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SleekOutlinedButton(
                        text = "Ping Cloud",
                        onClick = onTestReachability,
                        icon = Icons.Default.NetworkCheck,
                        modifier = Modifier.weight(1f),
                        testTag = "test_reachability_btn"
                    )
                    SleekOutlinedButton(
                        text = "Sync Orders",
                        onClick = onSyncNow,
                        icon = Icons.Default.CloudSync,
                        modifier = Modifier.weight(1f),
                        testTag = "force_sync_btn"
                    )
                    SleekOutlinedButton(
                        text = "Restore",
                        onClick = onRestoreFromCloud,
                        icon = Icons.Default.CloudDownload,
                        modifier = Modifier.weight(1f),
                        testTag = "restore_cloud_btn"
                    )
                }

                // 5. Database JSON Structure (Source of Truth Format)
                var showJsonSchema by remember { mutableStateOf(false) }
                val context = LocalContext.current
                val jsonSchema = """
{
  "orders": {
    "7K2B9X4": {
      "orderId": "7K2B9X4",
      "customerName": "সুজন মণ্ডল",
      "mobileNumber": "01711000000",
      "unitPrice": 250.0,
      "totalQuantity": 5,
      "totalAmount": 1250.0,
      "status": "UNDER_PROCESSING",
      "smsStatus": "SENT",
      "trackingUrl": "https://podderpara.shop/?id=7K2B9X4",
      "currency": "BDT",
      "createdAt": 1759132800000,
      "updatedAt": 1759132800000,
      "qtyChild12": 0,
      "qtyS": 1,
      "qtyM": 2,
      "qtyL": 2,
      "qtyXL": 0,
      "qtyXXL": 0,
      "qtyXXXL": 0,
      "sizes": {
        "qtyChild12": 0,
        "qtyS": 1,
        "qtyM": 2,
        "qtyL": 2,
        "qtyXL": 0,
        "qtyXXL": 0,
        "qtyXXXL": 0
      }
    }
  }
}""".trimIndent()

                Surface(
                    color = AppSurfaceSubtle,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AppBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showJsonSchema = !showJsonSchema },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ডাটাবেজ ফরম্যাট (Firebase 'orders/' JSON)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTextPrimary
                                )
                            }
                            Text(
                                text = if (showJsonSchema) "লুকান" else "দেখুন",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandPrimary
                            )
                        }

                        if (showJsonSchema) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = Color(0xFF1E1E1E),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = jsonSchema,
                                    color = Color(0xFF80CBC4),
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                SleekOutlinedButton(
                                    text = "JSON ফরম্যাট কপি",
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Firebase JSON Schema", jsonSchema))
                                        Toast.makeText(context, "ডাটাবেজ ফরম্যাট ক্লিপবোর্ডে কপি হয়েছে", Toast.LENGTH_SHORT).show()
                                    },
                                    icon = Icons.Default.ContentCopy
                                )
                            }
                        }
                    }
                }

                // App Version & Updates Section
                SleekCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SystemUpdate,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "অ্যাপ সংস্করণ ও আপডেট (APP UPDATE)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    letterSpacing = 0.5.sp,
                                    color = AppTextPrimary
                                )
                            }

                            Text(
                                text = "v${AppUpdateManager.CURRENT_VERSION_NAME}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = BrandPrimary
                            )
                        }

                        Text(
                            text = "GitHub Release থেকে স্বয়ংক্রিয়ভাবে নতুন সংস্করণ ডাউনলোড ও সরাসরি ইনস্টল করার সুবিধা।",
                            fontSize = 10.5.sp,
                            color = AppTextSecondary,
                            lineHeight = 14.sp
                        )

                        // Check if a downloaded APK already exists locally!
                        val existingApk = AppUpdateManager.getDownloadedApkFile(context, "1.1")
                        if (existingApk != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BrandSuccess.copy(alpha = 0.1f),
                                border = BorderStroke(0.8.dp, BrandSuccess.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "✅ v1.1 APK ডাউনলোড করা আছে",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            color = BrandSuccess
                                        )
                                        Text(
                                            text = "ইনস্টল করার জন্য প্রস্তুত",
                                            fontSize = 10.sp,
                                            color = AppTextSecondary
                                        )
                                    }
                                    SleekButton(
                                        text = "ইনস্টল করুন",
                                        onClick = {
                                            AppUpdateManager.installApk(context, existingApk)
                                        },
                                        icon = Icons.Default.InstallMobile
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SleekButton(
                                text = "আপডেট চেক করুন",
                                onClick = {
                                    scope.launch {
                                        AppUpdateManager.checkForUpdates(context)
                                    }
                                },
                                icon = Icons.Default.SystemUpdate,
                                modifier = Modifier.weight(1f),
                                testTag = "check_updates_btn"
                            )

                            SleekOutlinedButton(
                                text = "টেস্ট সিমুলেশন (v1.1)",
                                onClick = {
                                    AppUpdateManager.triggerSimulatedUpdate(context, "1.1")
                                },
                                modifier = Modifier.weight(1f),
                                testTag = "simulate_update_btn"
                            )
                        }
                    }
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SleekOutlinedButton(
                        text = "Close",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    SleekButton(
                        text = "Save Settings",
                        onClick = {
                            val price = unitPriceText.toDoubleOrNull() ?: currentUnitPrice
                            onSave(storeName, storePhone, price, trackingBaseUrl, autoSendSms, selectedSimId)
                        },
                        icon = Icons.Default.Save,
                        modifier = Modifier.weight(1f),
                        testTag = "save_settings_btn"
                    )
                }
            }
        }
    }
}

@Composable
private fun SimSelectionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = if (isSelected) BrandPrimary.copy(alpha = 0.08f) else AppSurface,
        border = BorderStroke(1.dp, if (isSelected) BrandPrimary.copy(alpha = 0.5f) else AppBorder),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = BrandPrimary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = AppTextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = AppTextSecondary
                )
            }
        }
    }
}
