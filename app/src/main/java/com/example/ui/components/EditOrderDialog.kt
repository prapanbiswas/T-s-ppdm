package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TShirtOrder
import com.example.ui.theme.AppBorder
import com.example.ui.theme.AppSurface
import com.example.ui.theme.AppTextPrimary
import com.example.ui.theme.AppTextSecondary
import com.example.ui.theme.BrandDark
import com.example.ui.theme.BrandPrimary
import com.example.util.CurrencyUtils

@Composable
fun EditOrderDialog(
    order: TShirtOrder,
    onSave: (TShirtOrder) -> Unit,
    onDismiss: () -> Unit
) {
    var customerName by remember { mutableStateOf(order.customerName) }
    var mobileNumber by remember { mutableStateOf(order.mobileNumber) }
    var qtyChild12 by remember { mutableStateOf(order.qtyChild12) }
    var qtyS by remember { mutableStateOf(order.qtyS) }
    var qtyM by remember { mutableStateOf(order.qtyM) }
    var qtyL by remember { mutableStateOf(order.qtyL) }
    var qtyXL by remember { mutableStateOf(order.qtyXL) }
    var qtyXXL by remember { mutableStateOf(order.qtyXXL) }
    var qtyXXXL by remember { mutableStateOf(order.qtyXXXL) }

    val totalQty = qtyChild12 + qtyS + qtyM + qtyL + qtyXL + qtyXXL + qtyXXXL
    val totalAmount = totalQty * order.unitPrice

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("edit_order_dialog"),
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(BrandPrimary, shape = RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Edit Order #${order.orderId}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = AppTextPrimary
                        )
                        Text(
                            text = "Update customer details or size quantities",
                            fontSize = 12.sp,
                            color = AppTextSecondary
                        )
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = AppBorder)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SleekTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = "Customer Name",
                        placeholder = "Name",
                        leadingIcon = Icons.Default.Person,
                        testTag = "edit_customer_name"
                    )

                    SleekTextField(
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it },
                        label = "Mobile Number",
                        placeholder = "Phone",
                        leadingIcon = Icons.Default.Phone,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        testTag = "edit_mobile_number"
                    )

                    Text(
                        text = "UPDATE SIZE QUANTITIES",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        color = AppTextSecondary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SizeQuantityCounter("Child 1-2y", "Toddler Fit", qtyChild12, order.unitPrice, { qtyChild12 = it })
                        SizeQuantityCounter("S", "Adult Small (36\")", qtyS, order.unitPrice, { qtyS = it })
                        SizeQuantityCounter("M", "Adult Medium (38-40\")", qtyM, order.unitPrice, { qtyM = it })
                        SizeQuantityCounter("L", "Adult Large (42-44\")", qtyL, order.unitPrice, { qtyL = it })
                        SizeQuantityCounter("XL", "Adult XL (46\")", qtyXL, order.unitPrice, { qtyXL = it })
                        SizeQuantityCounter("2XL", "Adult 2XL (48-50\")", qtyXXL, order.unitPrice, { qtyXXL = it })
                        SizeQuantityCounter("3XL", "Adult 3XL (52-54\")", qtyXXXL, order.unitPrice, { qtyXXXL = it })
                    }

                    // Total summary block
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
                            Text(
                                text = "TOTAL: $totalQty PCS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Text(
                                text = CurrencyUtils.formatTaka(totalAmount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
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
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    SleekButton(
                        text = "Save Changes",
                        onClick = {
                            if (customerName.isNotBlank() && mobileNumber.isNotBlank() && totalQty > 0) {
                                val updated = order.copy(
                                    customerName = customerName.trim(),
                                    mobileNumber = mobileNumber.trim(),
                                    qtyChild12 = qtyChild12,
                                    qtyS = qtyS,
                                    qtyM = qtyM,
                                    qtyL = qtyL,
                                    qtyXL = qtyXL,
                                    qtyXXL = qtyXXL,
                                    qtyXXXL = qtyXXXL,
                                    totalQuantity = totalQty,
                                    totalAmount = totalAmount,
                                    updatedAt = System.currentTimeMillis()
                                )
                                onSave(updated)
                            }
                        },
                        icon = Icons.Default.Save,
                        enabled = customerName.isNotBlank() && mobileNumber.isNotBlank() && totalQty > 0,
                        modifier = Modifier.weight(1.3f),
                        testTag = "save_edit_button"
                    )
                }
            }
        }
    }
}
