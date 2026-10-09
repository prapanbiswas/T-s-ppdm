package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.OrderViewModel
import com.example.ui.components.OrderConfirmationDialog
import com.example.ui.components.SizeQuantityCounter
import com.example.ui.components.SleekButton
import com.example.ui.components.SleekCard
import com.example.ui.components.SleekTextField
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
import com.example.ui.theme.BrandSuccessLight
import com.example.util.CurrencyUtils

@Composable
fun NewOrderScreen(
    viewModel: OrderViewModel,
    onOrderPlaced: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val autoSendSms by viewModel.autoSendSms.collectAsStateWithLifecycle()
    val showConfirmDialog by viewModel.showConfirmationDialog.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info Header Card
                item {
                    SleekCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hero_brand_banner"),
                        backgroundColor = AppSurface,
                        elevation = 1.dp
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                        color = BrandPrimary
                                    )
                                    Text(
                                        text = "উৎসবের টি-শার্ট অর্ডার এন্ট্রি",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = AppTextPrimary
                                    )
                                }

                                Surface(
                                    color = BrandSuccessLight,
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(1.dp, BrandSuccess.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Sell,
                                            contentDescription = null,
                                            tint = BrandSuccess,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${CurrencyUtils.formatTaka(formState.unitPrice)} / pc",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = BrandSuccess
                                        )
                                    }
                                }
                            }

                            if (autoSendSms) {
                                Surface(
                                    color = AppSurfaceSubtle,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Sms,
                                            contentDescription = null,
                                            tint = BrandPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "অর্ডার কনফার্ম হলেই podderpara.shop লাইভ ট্র্যাকিং লিংক গ্রাহককে এসএমএস যাবে",
                                            fontSize = 11.sp,
                                            color = AppTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Customer Details Card
                item {
                    SleekCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_info_card")
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "CUSTOMER DETAILS",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.8.sp,
                                    color = AppTextSecondary
                                )
                                Text(
                                    text = "* Required",
                                    fontSize = 11.sp,
                                    color = AppTextMuted
                                )
                            }

                            SleekTextField(
                                value = formState.customerName,
                                onValueChange = { viewModel.updateCustomerName(it) },
                                label = "Customer Name *",
                                placeholder = "e.g. Tanvir Hasan",
                                leadingIcon = Icons.Default.Person,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                testTag = "customer_name_input"
                            )

                            SleekTextField(
                                value = formState.mobileNumber,
                                onValueChange = { viewModel.updateMobileNumber(it) },
                                label = "Mobile Number (for SMS & Tracking) *",
                                placeholder = "e.g. 01712-345678",
                                leadingIcon = Icons.Default.Phone,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Done
                                ),
                                testTag = "mobile_number_input"
                            )
                        }
                    }
                }

                // Size Selection Matrix Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SELECT T-SHIRT SIZES",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.8.sp,
                            color = AppTextSecondary
                        )

                        Text(
                            text = "Total: ${formState.totalQuantity} pcs selected",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = if (formState.totalQuantity > 0) BrandPrimary else AppTextMuted
                        )
                    }
                }

                // 1. Child 1-2
                item {
                    SizeQuantityCounter(
                        sizeLabel = "Child",
                        categoryDescription = "Child 1-2 years",
                        quantity = formState.qtyChild12,
                        unitPrice = formState.unitPrice,
                        onQuantityChange = { viewModel.setExactQuantity("Child 1-2", it) }
                    )
                }

                // 2. Adult S
                item {
                    SizeQuantityCounter(
                        sizeLabel = "S",
                        categoryDescription = "Adult Small (Chest 36\")",
                        quantity = formState.qtyS,
                        unitPrice = formState.unitPrice,
                        onQuantityChange = { viewModel.setExactQuantity("S", it) }
                    )
                }

                // 3. Adult M
                item {
                    SizeQuantityCounter(
                        sizeLabel = "M",
                        categoryDescription = "Adult Medium (Chest 38-40\")",
                        quantity = formState.qtyM,
                        unitPrice = formState.unitPrice,
                        onQuantityChange = { viewModel.setExactQuantity("M", it) }
                    )
                }

                // 4. Adult L
                item {
                    SizeQuantityCounter(
                        sizeLabel = "L",
                        categoryDescription = "Adult Large (Chest 42-44\")",
                        quantity = formState.qtyL,
                        unitPrice = formState.unitPrice,
                        onQuantityChange = { viewModel.setExactQuantity("L", it) }
                    )
                }

                // 5. Adult XL
                item {
                    SizeQuantityCounter(
                        sizeLabel = "XL",
                        categoryDescription = "Adult Extra Large (Chest 46\")",
                        quantity = formState.qtyXL,
                        unitPrice = formState.unitPrice,
                        onQuantityChange = { viewModel.setExactQuantity("XL", it) }
                    )
                }

                // 6. Adult 2XL
                item {
                    SizeQuantityCounter(
                        sizeLabel = "2XL",
                        categoryDescription = "Adult 2X Large (Chest 48-50\")",
                        quantity = formState.qtyXXL,
                        unitPrice = formState.unitPrice,
                        onQuantityChange = { viewModel.setExactQuantity("XXL", it) }
                    )
                }

                // 7. Adult 3XL
                item {
                    SizeQuantityCounter(
                        sizeLabel = "3XL",
                        categoryDescription = "Adult 3X Large (Chest 52-54\")",
                        quantity = formState.qtyXXXL,
                        unitPrice = formState.unitPrice,
                        onQuantityChange = { viewModel.setExactQuantity("XXXL", it) }
                    )
                }
            }

            // Bottom Floating Bar
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .imePadding(),
                color = AppSurface,
                border = BorderStroke(1.dp, AppBorder),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        AnimatedContent(
                            targetState = formState.totalQuantity,
                            label = "total_qty_anim"
                        ) { qty ->
                            Text(
                                text = "TOTAL ($qty items)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTextSecondary
                            )
                        }
                        AnimatedContent(
                            targetState = formState.totalAmount,
                            label = "total_amount_anim"
                        ) { amt ->
                            Text(
                                text = CurrencyUtils.formatTaka(amt),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandPrimary
                            )
                        }
                    }

                    SleekButton(
                        text = "Review & Dispatch",
                        onClick = { viewModel.openConfirmation() },
                        enabled = formState.isValid,
                        icon = Icons.Default.ArrowForward,
                        testTag = "place_order_button"
                    )
                }
            }
        }
    }

    // Confirmation Popup Dialog
    if (showConfirmDialog) {
        OrderConfirmationDialog(
            formState = formState,
            isOnline = isOnline,
            autoSendSms = autoSendSms,
            onConfirm = {
                viewModel.confirmAndPlaceOrder { placedOrder ->
                    onOrderPlaced(placedOrder.orderId)
                }
            },
            onDismiss = { viewModel.dismissConfirmation() }
        )
    }
}
