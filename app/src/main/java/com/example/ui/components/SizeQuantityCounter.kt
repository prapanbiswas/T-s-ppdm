package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppBorder
import com.example.ui.theme.AppSurface
import com.example.ui.theme.AppSurfaceSubtle
import com.example.ui.theme.AppTextMuted
import com.example.ui.theme.AppTextPrimary
import com.example.ui.theme.AppTextSecondary
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandPrimaryLight
import com.example.util.CurrencyUtils

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith

@Composable
fun SizeQuantityCounter(
    sizeLabel: String,
    categoryDescription: String,
    quantity: Int,
    unitPrice: Double,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val isSelected = quantity > 0

    val animatedBg by animateColorAsState(
        targetValue = if (isSelected) BrandPrimaryLight.copy(alpha = 0.6f) else AppSurface,
        label = "bg_anim"
    )
    val animatedBorder by animateColorAsState(
        targetValue = if (isSelected) BrandPrimary.copy(alpha = 0.6f) else AppBorder,
        label = "border_anim"
    )
    val animatedElevation by animateDpAsState(
        targetValue = if (isSelected) 2.5.dp else 0.5.dp,
        label = "elevation_anim"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = animatedBg),
        border = BorderStroke(1.dp, animatedBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Size Tag & Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = if (isSelected) BrandPrimary else AppSurfaceSubtle,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(width = 44.dp, height = 34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = sizeLabel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isSelected) Color.White else AppTextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = categoryDescription,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTextPrimary
                    )
                    AnimatedContent(
                        targetState = quantity,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInVertically { height -> height } + fadeIn()).togetherWith(
                                    slideOutVertically { height -> -height } + fadeOut())
                            } else {
                                (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                                    slideOutVertically { height -> height } + fadeOut())
                            }
                        },
                        label = "qty_anim"
                    ) { currentQty ->
                        if (currentQty > 0) {
                            Text(
                                text = "$currentQty pcs • ${CurrencyUtils.formatTaka(currentQty * unitPrice)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandPrimary
                            )
                        } else {
                            Text(
                                text = "0 selected",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = AppTextMuted
                            )
                        }
                    }
                }
            }

            // Right: Stepper
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(AppSurfaceSubtle, shape = RoundedCornerShape(10.dp))
                    .padding(3.dp)
            ) {
                // Minus
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(if (quantity > 0) AppSurface else Color.Transparent, shape = RoundedCornerShape(7.dp))
                        .clickable(enabled = quantity > 0) {
                            if (quantity > 0) onQuantityChange(quantity - 1)
                        }
                        .testTag("minus_btn_$sizeLabel"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease $sizeLabel",
                        tint = if (quantity > 0) AppTextPrimary else AppTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Direct Input
                var textFieldValue by remember(quantity) {
                    val text = if (quantity > 0) quantity.toString() else "0"
                    mutableStateOf(TextFieldValue(text = text, selection = TextRange(text.length)))
                }

                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newVal ->
                        val digits = newVal.text.filter { it.isDigit() }
                        textFieldValue = newVal.copy(text = digits)
                        val parsed = digits.toIntOrNull() ?: 0
                        onQuantityChange(parsed)
                    },
                    modifier = Modifier
                        .width(42.dp)
                        .height(32.dp)
                        .testTag("input_qty_$sizeLabel"),
                    textStyle = TextStyle(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isSelected) BrandPrimary else AppTextPrimary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    cursorBrush = SolidColor(BrandPrimary),
                    decorationBox = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            innerTextField()
                        }
                    }
                )

                // Plus
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(BrandPrimary, shape = RoundedCornerShape(7.dp))
                        .clickable { onQuantityChange(quantity + 1) }
                        .testTag("plus_btn_$sizeLabel"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase $sizeLabel",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
