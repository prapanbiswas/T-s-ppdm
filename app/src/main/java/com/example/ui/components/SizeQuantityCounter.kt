package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.theme.BrandSuccess
import com.example.ui.theme.MandirCrimson
import com.example.ui.theme.MandirCrimsonDark
import com.example.ui.theme.MandirGold
import com.example.ui.theme.MandirGoldLight
import com.example.util.CurrencyUtils

/**
 * Material 3 Expressive T-Shirt Size Counter widget with smooth spring micro-animations,
 * tactile plus/minus controls, quick-add preset chips, and live item subtotal calculation.
 */
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

    // Smooth animation transitions for selection state
    val animatedBg by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFFFF9F5) else AppSurface,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "bg_anim"
    )
    val animatedBorder by animateColorAsState(
        targetValue = if (isSelected) MandirCrimson.copy(alpha = 0.7f) else AppBorder,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "border_anim"
    )
    val animatedElevation by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 1.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "elevation_anim"
    )

    // Button press scale feedback
    val minusInteractionSource = remember { MutableInteractionSource() }
    val isMinusPressed by minusInteractionSource.collectIsPressedAsState()
    val minusScale by animateFloatAsState(
        targetValue = if (isMinusPressed) 0.88f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "minus_scale"
    )

    val plusInteractionSource = remember { MutableInteractionSource() }
    val isPlusPressed by plusInteractionSource.collectIsPressedAsState()
    val plusScale by animateFloatAsState(
        targetValue = if (isPlusPressed) 0.88f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "plus_scale"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = animatedBg),
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, animatedBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Size Tag & Detailed Description
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Woven garment label badge
                    Surface(
                        color = if (isSelected) MandirCrimson else AppSurfaceSubtle,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MandirGold.copy(alpha = 0.8f) else AppBorder
                        ),
                        modifier = Modifier.size(width = 48.dp, height = 38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = sizeLabel,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = if (isSelected) Color.White else AppTextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = categoryDescription,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
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
                            label = "qty_subtotal_anim"
                        ) { currentQty ->
                            if (currentQty > 0) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = MandirGoldLight,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = "$currentQty টি",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MandirCrimson,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "= ${CurrencyUtils.formatTaka(currentQty * unitPrice)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MandirCrimson
                                    )
                                }
                            } else {
                                Text(
                                    text = "কোনো পিস নির্বাচন করা হয়নি",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = AppTextMuted
                                )
                            }
                        }
                    }
                }

                // Right: Tactile Stepper Control
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            color = if (isSelected) Color(0xFFF7ECE4) else AppSurfaceSubtle,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            BorderStroke(
                                0.8.dp,
                                if (isSelected) MandirCrimson.copy(alpha = 0.3f) else AppBorder
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(3.dp)
                ) {
                    // Minus Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .scale(minusScale)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (quantity > 0) AppSurface else Color.Transparent)
                            .clickable(
                                enabled = quantity > 0,
                                interactionSource = minusInteractionSource,
                                indication = null
                            ) {
                                if (quantity > 0) onQuantityChange(quantity - 1)
                            }
                            .testTag("minus_btn_$sizeLabel"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease $sizeLabel",
                            tint = if (quantity > 0) MandirCrimson else AppTextMuted.copy(alpha = 0.5f),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Direct Numeric Input Field
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
                            .width(44.dp)
                            .height(34.dp)
                            .testTag("input_qty_$sizeLabel"),
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = if (isSelected) MandirCrimson else AppTextPrimary
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        cursorBrush = SolidColor(MandirCrimson),
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                innerTextField()
                            }
                        }
                    )

                    // Plus Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .scale(plusScale)
                            .clip(RoundedCornerShape(9.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(MandirCrimson, MandirCrimsonDark)
                                )
                            )
                            .clickable(
                                interactionSource = plusInteractionSource,
                                indication = null
                            ) {
                                onQuantityChange(quantity + 1)
                            }
                            .testTag("plus_btn_$sizeLabel"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase $sizeLabel",
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            // Quick Preset Chips when size is active or being edited
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 60.dp, top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick Add:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTextMuted
                    )

                    listOf(1, 2, 5).forEach { addQty ->
                        Surface(
                            color = AppSurfaceSubtle,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.6.dp, AppBorder),
                            modifier = Modifier.clickable { onQuantityChange(quantity + addQty) }
                        ) {
                            Text(
                                text = "+$addQty",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandPrimary,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Clear button
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { onQuantityChange(0) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear $sizeLabel",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "মুছুন",
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
}
