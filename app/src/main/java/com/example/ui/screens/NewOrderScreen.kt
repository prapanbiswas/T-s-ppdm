package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.OrderViewModel
import com.example.ui.components.OrderConfirmationDialog
import com.example.ui.components.SizeQuantityCounter
import com.example.ui.components.SleekButton
import com.example.ui.components.SleekCard
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AppBorder
import com.example.ui.theme.AppSurface
import com.example.ui.theme.AppSurfaceSubtle
import com.example.ui.theme.AppTextMuted
import com.example.ui.theme.AppTextPrimary
import com.example.ui.theme.AppTextSecondary
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSuccess
import com.example.ui.theme.BrandSuccessLight
import com.example.ui.theme.MandirCrimson
import com.example.ui.theme.MandirCrimsonBorder
import com.example.ui.theme.MandirCrimsonDark
import com.example.ui.theme.MandirGold
import com.example.ui.theme.MandirGoldLight
import com.example.util.CurrencyUtils

/**
 * Material 3 Expressive New Order Entry Screen with smooth reactive animations,
 * duplicate mobile detection with inline order modify/create actions, and tactile size steppers.
 */
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
    val existingOrder by viewModel.existingOrderForEnteredPhone.collectAsStateWithLifecycle()
    val anyExistingOrder by viewModel.anyExistingOrderForEnteredPhone.collectAsStateWithLifecycle()
    val isDuplicateAcknowledged by viewModel.isDuplicatePhoneAcknowledged.collectAsStateWithLifecycle()

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
                // 1. Hero Brand Banner Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hero_brand_banner"),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = AppSurface),
                        border = BorderStroke(1.dp, MandirGold.copy(alpha = 0.35f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFFFFDF9),
                                            Color(0xFFFAF4EB)
                                        )
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Temple Official Emblem Box
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(
                                                    brush = Brush.linearGradient(
                                                        colors = listOf(MandirCrimson, MandirCrimsonDark)
                                                    ),
                                                    shape = RoundedCornerShape(14.dp)
                                                )
                                                .border(
                                                    BorderStroke(1.2.dp, MandirGold.copy(alpha = 0.8f)),
                                                    shape = RoundedCornerShape(14.dp)
                                                )
                                                .clip(RoundedCornerShape(14.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.logo),
                                                contentDescription = "Temple Logo",
                                                modifier = Modifier.size(44.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .background(MandirGold, shape = CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text(
                                                    text = "পোদ্দারপাড়া সর্বজনীন দুর্গা মন্দির",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    letterSpacing = 0.4.sp,
                                                    color = MandirCrimson
                                                )
                                            }
                                            Text(
                                                text = "শারদীয় উৎসব টি-শার্ট অর্ডার",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 17.sp,
                                                color = AppTextPrimary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Unit price pill
                                    Surface(
                                        color = BrandSuccessLight,
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(1.dp, BrandSuccess.copy(alpha = 0.4f)),
                                        shadowElevation = 0.5.dp
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
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
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.sp,
                                                color = BrandSuccess
                                            )
                                        }
                                    }
                                }

                                if (autoSendSms) {
                                    Surface(
                                        color = AppSurface.copy(alpha = 0.85f),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(0.8.dp, MandirGold.copy(alpha = 0.25f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .background(MandirGoldLight, shape = CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Sms,
                                                    contentDescription = null,
                                                    tint = MandirCrimson,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "অর্ডার কনফার্ম হলেই podderpara.shop ট্র্যাকিং লিংক গ্রাহকের মোবাইলে লাইভ এসএমএস যাবে",
                                                fontSize = 11.sp,
                                                color = AppTextSecondary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Customer Details Card with Material 3 styling
                item {
                    SleekCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_info_card"),
                        elevation = 2.dp,
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(MandirCrimson, shape = CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "গ্রাহকের তথ্য (CUSTOMER DETAILS)",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        letterSpacing = 0.8.sp,
                                        color = AppTextSecondary
                                    )
                                }

                                Text(
                                    text = "* দুটি ফিল্ডই বাধ্যতামূলক",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppTextMuted
                                )
                            }

                            // Customer Name Field
                            OutlinedTextField(
                                value = formState.customerName,
                                onValueChange = { viewModel.updateCustomerName(it) },
                                label = { Text("গ্রাহকের নাম (Customer Name) *") },
                                placeholder = { Text("যেমন: সুজন মণ্ডল / Tanvir Hasan") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MandirCrimson
                                    )
                                },
                                trailingIcon = {
                                    if (formState.customerName.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.updateCustomerName("") }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear name", tint = AppTextMuted)
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MandirCrimson,
                                    unfocusedBorderColor = AppBorder,
                                    focusedLabelColor = MandirCrimson
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("customer_name_input")
                            )

                            // Mobile Number Field
                            OutlinedTextField(
                                value = formState.mobileNumber,
                                onValueChange = { viewModel.updateMobileNumber(it) },
                                label = { Text("মোবাইল নম্বর (SMS & Live Tracking) *") },
                                placeholder = { Text("যেমন: 01712-345678") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = MandirCrimson
                                    )
                                },
                                trailingIcon = {
                                    if (formState.mobileNumber.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.updateMobileNumber("") }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear phone", tint = AppTextMuted)
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Done
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MandirCrimson,
                                    unfocusedBorderColor = AppBorder,
                                    focusedLabelColor = MandirCrimson
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("mobile_number_input")
                            )

                            // Real-time Duplicate Check Alert Banner with "Modify Order" and "Create Another" buttons
                            AnimatedVisibility(
                                visible = existingOrder != null,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                existingOrder?.let { order ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("existing_order_banner"),
                                        color = Color(0xFFFFF7ED),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.2.dp, Color(0xFFF97316).copy(alpha = 0.6f)),
                                        shadowElevation = 1.dp
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .background(Color(0xFFEA580C), shape = RoundedCornerShape(8.dp)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.WarningAmber,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.width(10.dp))

                                                    Column {
                                                        Text(
                                                            text = "এই নম্বরে পূর্ববর্তী অর্ডার বিদ্যমান!",
                                                            fontWeight = FontWeight.ExtraBold,
                                                            fontSize = 13.sp,
                                                            color = Color(0xFF9A3412)
                                                        )
                                                        Text(
                                                            text = "Order #${order.orderId} • ${order.customerName}",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = AppTextPrimary
                                                        )
                                                    }
                                                }

                                                Surface(
                                                    color = Color(0xFFFED7AA),
                                                    shape = RoundedCornerShape(50)
                                                ) {
                                                    Text(
                                                        text = "${order.totalQuantity} pcs • ${CurrencyUtils.formatTaka(order.totalAmount)}",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFF9A3412),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "এই মোবাইল নম্বরে পূর্বে একটি অর্ডার করা হয়েছে। আপনি চাইলে পূর্বের অর্ডারটি সংশোধন করতে পারেন, অথবা এই একই নম্বরে নতুন আরেকটি অর্ডার তৈরি করতে পারেন।",
                                                fontSize = 11.sp,
                                                color = Color(0xFF7C2D12),
                                                lineHeight = 15.sp
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Button 1: Modify That Order
                                                Button(
                                                    onClick = { viewModel.modifyExistingOrderFromNewOrder(order) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = MandirCrimson),
                                                    shape = RoundedCornerShape(10.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                    modifier = Modifier
                                                        .weight(1.1f)
                                                        .height(38.dp)
                                                        .testTag("modify_existing_order_btn")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Modify Order",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                // Button 2: Create Another Order for same number
                                                OutlinedButton(
                                                    onClick = { viewModel.acknowledgeAndAllowDuplicateOrder() },
                                                    shape = RoundedCornerShape(10.dp),
                                                    border = BorderStroke(1.dp, Color(0xFFEA580C)),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9A3412)),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                    modifier = Modifier
                                                        .weight(1.1f)
                                                        .height(38.dp)
                                                        .testTag("create_another_order_btn")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.AddCircleOutline,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Create Another",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Duplicate Allowed Status Badge
                            AnimatedVisibility(
                                visible = isDuplicateAcknowledged,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Surface(
                                    color = BrandSuccessLight,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, BrandSuccess.copy(alpha = 0.35f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = BrandSuccess,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "একই নম্বরে অতিরিক্ত আরেকটি অর্ডার তৈরি করা হচ্ছে",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BrandSuccess
                                        )
                                    }
                                }
                            }

                            // Optional Note Field
                            OutlinedTextField(
                                value = formState.note,
                                onValueChange = { viewModel.updateNote(it) },
                                label = { Text("বিশেষ নির্দেশনা বা নোট (ঐচ্ছিক)") },
                                placeholder = { Text("যেমন: লাল পাড় টি-শার্ট / বাড়ি নং ইত্যাদি") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.NoteAlt,
                                        contentDescription = null,
                                        tint = AppTextMuted
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MandirCrimson,
                                    unfocusedBorderColor = AppBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // 3. Size Selection Matrix Header with Live Selected Counter & Reset
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(MandirCrimson, shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "টি-শার্ট সাইজ নির্বাচন (SIZE MATRIX)",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                letterSpacing = 0.8.sp,
                                color = AppTextSecondary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = if (formState.totalQuantity > 0) MandirGoldLight else AppSurfaceSubtle,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(
                                    0.8.dp,
                                    if (formState.totalQuantity > 0) MandirGold.copy(alpha = 0.6f) else AppBorder
                                )
                            ) {
                                Text(
                                    text = "মোট: ${formState.totalQuantity} টি পিস",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (formState.totalQuantity > 0) MandirCrimson else AppTextMuted,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                )
                            }

                            if (formState.totalQuantity > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFFFEE2E2),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.clickable {
                                        listOf("Child 1-2", "S", "M", "L", "XL", "XXL", "XXXL").forEach {
                                            viewModel.setExactQuantity(it, 0)
                                        }
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RestartAlt,
                                            contentDescription = "Reset all",
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "রিসেট",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFDC2626)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 1. Child 1-2
                item {
                    SizeQuantityCounter(
                        sizeLabel = "Child",
                        categoryDescription = "শিশু ১-২ বছর (Child 1-2 Years)",
                        quantity = formState.qtyChild12,
                        unitPrice = formState.unitPrice,
                        onQuantityChange = { viewModel.setExactQuantity("Child 1-2", it) }
                    )
                }

                // 2. Adult S
                item {
                    SizeQuantityCounter(
                        sizeLabel = "S",
                        categoryDescription = "ছোট স্মল (Adult Small • বুক ৩৬\")",
                        quantity = formState.qtyS,
                        unitPrice = formState.unitPrice,
                        onQuantityChange = { viewModel.setExactQuantity("S", it) }
                    )
                }

                // 3. Adult M
                item {
                    SizeQuantityCounter(
                        sizeLabel = "M",
                        categoryDescription = "মিডিয়াম (Adult Medium • বুক ৩৮-৪০\")",
                        quantity = formState.qtyM,
                        unitPrice = formState.unitPrice,
                        onQuantityChange = { viewModel.setExactQuantity("M", it) }
                    )
                }

                // 4. Adult L
                item {
                    SizeQuantityCounter(
                        sizeLabel = "L",
                        categoryDescription = "লার্জ (Adult Large • বুক ৪২-৪৪\")",
                        quantity = formState.qtyL,
                        unitPrice = formState.unitPrice,
                        onQuantityChange = { viewModel.setExactQuantity("L", it) }
                    )
                }

                // 5. Adult XL
                item {
                    SizeQuantityCounter(
                        sizeLabel = "XL",
                        categoryDescription = "এক্সট্রা লার্জ (Adult XL • বুক ৪৬\")",
                        quantity = formState.qtyXL,
                        unitPrice = formState.unitPrice,
                        onQuantityChange = { viewModel.setExactQuantity("XL", it) }
                    )
                }

                // 6. Adult 2XL
                item {
                    SizeQuantityCounter(
                        sizeLabel = "2XL",
                        categoryDescription = "ডাবল এক্সেল (Adult 2XL • বুক ৪৮-৫০\")",
                        quantity = formState.qtyXXL,
                        unitPrice = formState.unitPrice,
                        onQuantityChange = { viewModel.setExactQuantity("XXL", it) }
                    )
                }

                // 7. Adult 3XL
                item {
                    SizeQuantityCounter(
                        sizeLabel = "3XL",
                        categoryDescription = "ট্রিপল এক্সেল (Adult 3XL • বুক ৫২-৫৪\")",
                        quantity = formState.qtyXXXL,
                        unitPrice = formState.unitPrice,
                        onQuantityChange = { viewModel.setExactQuantity("XXXL", it) }
                    )
                }
            }

            // Bottom Floating Bar with Material 3 Expressive Design
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .imePadding(),
                color = AppSurface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                border = BorderStroke(1.dp, MandirGold.copy(alpha = 0.35f)),
                shadowElevation = 14.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        AnimatedContent(
                            targetState = formState.totalQuantity,
                            label = "total_qty_anim"
                        ) { qty ->
                            Text(
                                text = "সর্বমোট ($qty টি টি-শার্ট)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTextSecondary
                            )
                        }
                        AnimatedContent(
                            targetState = formState.totalAmount,
                            label = "total_amount_anim"
                        ) { amt ->
                            Text(
                                text = CurrencyUtils.formatTaka(amt),
                                fontSize = 21.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MandirCrimson
                            )
                        }
                    }

                    SleekButton(
                        text = "অর্ডার রিভিউ ও নিশ্চিত করুন",
                        onClick = { viewModel.openConfirmation() },
                        enabled = formState.isValid,
                        icon = Icons.Default.ArrowForward,
                        testTag = "place_order_button",
                        backgroundColor = MandirCrimson,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(48.dp)
                    )
                }
            }
        }
    }

    // Confirmation Popup Dialog (leads to animated Celebration pop-up on confirm)
    if (showConfirmDialog) {
        OrderConfirmationDialog(
            formState = formState,
            isOnline = isOnline,
            autoSendSms = autoSendSms,
            existingOrder = anyExistingOrder,
            onModifyExistingOrder = { order ->
                viewModel.dismissConfirmation()
                viewModel.modifyExistingOrderFromNewOrder(order)
            },
            onConfirm = {
                viewModel.confirmAndPlaceOrder { placedOrder ->
                    onOrderPlaced(placedOrder.orderId)
                }
            },
            onDismiss = { viewModel.dismissConfirmation() }
        )
    }
}
