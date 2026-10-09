package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.NewOrderFormState
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
import com.example.util.CurrencyUtils

@Composable
fun OrderConfirmationDialog(
    formState: NewOrderFormState,
    isOnline: Boolean,
    autoSendSms: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val selectedSizes = listOf(
        "Child 1-2 years" to formState.qtyChild12,
        "Adult S" to formState.qtyS,
        "Adult M" to formState.qtyM,
        "Adult L" to formState.qtyL,
        "Adult XL" to formState.qtyXL,
        "Adult 2XL" to formState.qtyXXL,
        "Adult 3XL" to formState.qtyXXXL
    ).filter { it.second > 0 }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("order_confirmation_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = AppSurface,
            border = BorderStroke(1.dp, AppBorder),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
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
                                .size(36.dp)
                                .background(BrandPrimary, shape = RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "পোদ্দারপাড়া দুর্গা মন্দির",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = AppTextPrimary
                            )
                            Text(
                                text = "৭ অক্ষরের আইডি ও podderpara.shop লিংক যাবে",
                                fontSize = 11.sp,
                                color = AppTextSecondary
                            )
                        }
                    }

                    // Cloud state badge
                    Surface(
                        color = if (isOnline) BrandSuccess.copy(alpha = 0.1f) else AppSurfaceSubtle,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, if (isOnline) BrandSuccess.copy(alpha = 0.3f) else AppBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = if (isOnline) BrandSuccess else AppTextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOnline) "Cloud Synced" else "Offline First",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOnline) BrandSuccess else AppTextSecondary
                            )
                        }
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = AppBorder)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Customer Details Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppSurfaceSubtle,
                        border = BorderStroke(1.dp, AppBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = formState.customerName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = AppTextPrimary
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = AppTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = formState.mobileNumber,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = AppTextSecondary
                                )
                            }
                        }
                    }

                    // Order Sizes Breakdown
                    Text(
                        text = "ORDER BREAKDOWN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        color = AppTextSecondary
                    )

                    selectedSizes.forEach { (size, qty) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = size,
                                fontSize = 13.sp,
                                color = AppTextPrimary
                            )
                            Text(
                                text = "$qty pcs • ${CurrencyUtils.formatTaka(qty * formState.unitPrice)}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = AppTextPrimary
                            )
                        }
                    }

                    // SMS Notice Info
                    if (autoSendSms) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BrandPrimary.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, BrandPrimary.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sms,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SMS Gateway will send a confirmation in Bengali with live tracking link to ${formState.mobileNumber}",
                                    fontSize = 11.sp,
                                    color = BrandPrimary,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    // Total Calculation Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BrandDark,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL QUANTITY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = "${formState.totalQuantity} Pieces",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "TOTAL AMOUNT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = CurrencyUtils.formatTaka(formState.totalAmount),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SleekOutlinedButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    SleekButton(
                        text = "Place & Dispatch",
                        onClick = onConfirm,
                        icon = Icons.Default.Check,
                        modifier = Modifier.weight(1.3f)
                    )
                }
            }
        }
    }
}
