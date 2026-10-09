package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import com.example.sms.SmsGatewayManager
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.OrderStatus
import com.example.data.model.SmsStatus
import com.example.data.model.TShirtOrder
import com.example.ui.OrderViewModel
import com.example.ui.components.CloudSyncBadge
import com.example.ui.components.EditOrderDialog
import com.example.ui.components.OrderStatusBadge
import com.example.ui.components.PdfPreviewDialog
import com.example.ui.components.SleekButton
import com.example.ui.components.SleekCard
import com.example.ui.components.SleekOutlinedButton
import com.example.ui.components.SleekTextField
import com.example.ui.components.SmsDeliveryBadge
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AppBorder
import com.example.ui.theme.AppSurface
import com.example.ui.theme.AppSurfaceSubtle
import com.example.ui.theme.AppTextMuted
import com.example.ui.theme.AppTextPrimary
import com.example.ui.theme.AppTextSecondary
import com.example.ui.theme.BrandDark
import com.example.ui.theme.BrandError
import com.example.ui.theme.BrandErrorBorder
import com.example.ui.theme.BrandErrorLight
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandPrimaryLight
import com.example.ui.theme.BrandSuccess
import com.example.ui.theme.MandirCrimson
import com.example.ui.theme.MandirGold
import com.example.util.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderListScreen(
    viewModel: OrderViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()
    val editingOrder by viewModel.editingOrder.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrdersForSummary.collectAsStateWithLifecycle()
    val storeName by viewModel.storeName.collectAsStateWithLifecycle()

    var orderToDelete by remember { mutableStateOf<TShirtOrder?>(null) }
    var orderForStatusChange by remember { mutableStateOf<TShirtOrder?>(null) }
    var showPdfDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
        ) {
            // Search and Filters Bar
            Surface(
                color = AppSurface,
                border = BorderStroke(1.dp, AppBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SleekTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        label = "অর্ডার খুঁজুন (Search)",
                        placeholder = "গ্রাহকের নাম, মোবাইল বা ৭ অক্ষরের আইডি...",
                        leadingIcon = Icons.Default.Search,
                        testTag = "order_search_input"
                    )

                    // Filter Chips Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedFilter == null,
                                onClick = { viewModel.setStatusFilter(null) },
                                label = { Text("All Orders") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("filter_all")
                            )
                        }
                        OrderStatus.values().forEach { status ->
                            item {
                                FilterChip(
                                    selected = selectedFilter == status,
                                    onClick = { viewModel.setStatusFilter(if (selectedFilter == status) null else status) },
                                    label = { Text(status.displayName) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("filter_${status.name}")
                                )
                            }
                        }
                    }

                    // Orders count & Total BDT sum & PDF Report Button
                    val totalFilteredAmount = orders.sumOf { it.totalAmount }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${orders.size} Orders Listed",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTextSecondary
                            )
                            Text(
                                text = "Total: ${CurrencyUtils.formatTaka(totalFilteredAmount)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandPrimary
                            )
                        }

                        SleekOutlinedButton(
                            text = "PDF রিপোর্ট তৈরি",
                            onClick = { showPdfDialog = true },
                            icon = Icons.Default.PictureAsPdf,
                            testTag = "order_list_pdf_export_btn"
                        )
                    }
                }
            }

            // Orders List
            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(AppSurfaceSubtle, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = AppTextMuted
                            )
                        }
                        Text(
                            text = if (searchQuery.isNotBlank()) "No orders match '$searchQuery'" else "No orders placed yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = AppTextPrimary
                        )
                        Text(
                            text = "Placed orders will automatically dispatch customer tracking SMS",
                            fontSize = 12.sp,
                            color = AppTextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(orders, key = { it.id }) { order ->
                        OrderCard(
                            order = order,
                            onStatusClick = { orderForStatusChange = order },
                            onEdit = { viewModel.startEditingOrder(order) },
                            onCopyTracking = { viewModel.copyTrackingLink(context, order) },
                            onShareTracking = { viewModel.shareTrackingLink(context, order) },
                            onResendSms = { viewModel.resendOrderSms(order) },
                            onCancel = { viewModel.cancelOrder(order) },
                            onDelete = { orderToDelete = order }
                        )
                    }
                }
            }
        }
    }

    // Status Change Dialog
    orderForStatusChange?.let { order ->
        Dialog(onDismissRequest = { orderForStatusChange = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AppSurface,
                border = BorderStroke(1.dp, AppBorder),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Update Status for #${order.orderId}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = AppTextPrimary
                    )

                    HorizontalDivider(thickness = 1.dp, color = AppBorder)

                    OrderStatus.values().forEach { status ->
                        val isCurrent = order.status == status
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCurrent) BrandPrimaryLight else AppSurfaceSubtle,
                            border = BorderStroke(1.dp, if (isCurrent) BrandPrimary else AppBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateOrderStatus(order.id, status)
                                    orderForStatusChange = null
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = status.displayName,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCurrent) BrandPrimary else AppTextPrimary,
                                    fontSize = 14.sp
                                )
                                if (isCurrent) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = BrandPrimary)
                                }
                            }
                        }
                    }

                    SleekOutlinedButton(
                        text = "Close",
                        onClick = { orderForStatusChange = null },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // Edit Order Dialog
    editingOrder?.let { order ->
        EditOrderDialog(
            order = order,
            onSave = { updated -> viewModel.saveEditedOrder(updated) },
            onDismiss = { viewModel.dismissEditingOrder() }
        )
    }

    // Delete Confirmation Dialog
    orderToDelete?.let { order ->
        AlertDialog(
            onDismissRequest = { orderToDelete = null },
            title = { Text("Delete Order #${order.orderId}?") },
            text = { Text("Are you sure you want to permanently delete the order for ${order.customerName}? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteOrder(order)
                        orderToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandError)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { orderToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // PDF Preview & Export Dialog
    if (showPdfDialog) {
        PdfPreviewDialog(
            allOrders = allOrders,
            filteredOrders = orders,
            storeName = storeName,
            onDismiss = { showPdfDialog = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OrderCard(
    order: TShirtOrder,
    onStatusClick: () -> Unit,
    onEdit: () -> Unit,
    onCopyTracking: () -> Unit,
    onShareTracking: () -> Unit,
    onResendSms: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    SleekCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("order_card_${order.orderId}")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row: Alphanumeric Order ID, Date, Status Badge, Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MandirCrimson,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "#${order.orderId}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date(order.createdAt))
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = AppTextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Clickable Status Badge
                    Box(modifier = Modifier.clickable { onStatusClick() }) {
                        OrderStatusBadge(status = order.status)
                    }

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(32.dp).testTag("order_menu_${order.orderId}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Order actions",
                                tint = AppTextSecondary
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Copy Tracking URL") },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onCopyTracking()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Share Tracking via SMS") },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onShareTracking()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Resend SMS Confirmation") },
                                leadingIcon = { Icon(Icons.Default.Sms, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onResendSms()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit Order") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            if (order.status != OrderStatus.CANCELLED) {
                                DropdownMenuItem(
                                    text = { Text("Cancel Order") },
                                    leadingIcon = { Icon(Icons.Default.Cancel, contentDescription = null, tint = BrandError) },
                                    onClick = {
                                        menuExpanded = false
                                        onCancel()
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Delete Order", color = BrandError) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = BrandError) },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 0.8.dp, color = AppBorder)

            // Customer Name & Total Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.customerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AppTextPrimary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                try {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.mobileNumber}"))
                                    context.startActivity(dialIntent)
                                } catch (_: Exception) {}
                            }
                            .padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call",
                            tint = BrandPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = order.mobileNumber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = BrandPrimary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyUtils.formatTaka(order.totalAmount),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = BrandPrimary
                    )
                    Text(
                        text = "${order.totalQuantity} items • ${CurrencyUtils.formatTaka(order.unitPrice)}/pc",
                        fontSize = 11.sp,
                        color = AppTextSecondary
                    )
                }
            }

            // Size Breakdown Badges
            val sizeList = listOf(
                "Child 1-2y" to order.qtyChild12,
                "S" to order.qtyS,
                "M" to order.qtyM,
                "L" to order.qtyL,
                "XL" to order.qtyXL,
                "2XL" to order.qtyXXL,
                "3XL" to order.qtyXXXL
            ).filter { it.second > 0 }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                sizeList.forEach { (label, count) ->
                    Surface(
                        color = AppSurfaceSubtle,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, AppBorder)
                    ) {
                        Text(
                            text = "$label: $count",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTextPrimary,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Real-Time Status Badges & Tracking Link Surface
            Surface(
                color = AppSurfaceSubtle,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AppBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status Badges Row: SMS Delivery (PendingIntent) & Cloud Sync
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SmsDeliveryBadge(
                            status = order.smsStatus,
                            errorMessage = order.smsErrorMessage,
                            onRetryClick = onResendSms
                        )

                        CloudSyncBadge(
                            status = order.syncStatus
                        )
                    }

                    HorizontalDivider(thickness = 0.6.dp, color = AppBorder)

                    // Tracking URL
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCopyTracking() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy link",
                                tint = AppTextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = order.trackingUrl,
                                fontSize = 11.sp,
                                color = BrandPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // If carrier rejected SMS (e.g. zero balance / no network), show prominent action banner
            if (order.smsStatus == SmsStatus.FAILED) {
                Surface(
                    color = BrandErrorLight,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BrandErrorBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = BrandError,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "এসএমএস পৌঁছায়নি (ব্যালেন্স বা সিম নেটওয়ার্ক সমস্যা)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandError
                            )
                        }
                        if (!order.smsErrorMessage.isNullOrBlank()) {
                            Text(
                                text = order.smsErrorMessage,
                                fontSize = 10.sp,
                                color = AppTextSecondary
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val msg = remember(order) {
                                SmsGatewayManager.generateBengaliSmsMessage(order, "পোদ্দারপাড়া সর্বজনীন দুর্গা মন্দির")
                            }
                            Button(
                                onClick = {
                                    SmsGatewayManager.openWhatsAppShare(context, order.mobileNumber, msg)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF25D366),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(34.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("হোয়াটসঅ্যাপে পাঠান", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    SmsGatewayManager.openSystemSmsApp(context, order.mobileNumber, msg)
                                },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .weight(0.9f)
                                    .height(34.dp)
                            ) {
                                Text("মেসেজ অ্যাপ", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            // Quick Actions: Resend SMS & Update Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SleekOutlinedButton(
                    text = "Share Link",
                    onClick = onShareTracking,
                    icon = Icons.Default.Share,
                    modifier = Modifier.weight(1f)
                )

                SleekOutlinedButton(
                    text = if (order.smsStatus == SmsStatus.FAILED) "Retry SMS" else "Resend SMS",
                    onClick = onResendSms,
                    icon = Icons.Default.Sms,
                    modifier = Modifier.weight(1f)
                )

                SleekOutlinedButton(
                    text = "Status",
                    onClick = onStatusClick,
                    icon = Icons.Default.SwapHoriz,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
