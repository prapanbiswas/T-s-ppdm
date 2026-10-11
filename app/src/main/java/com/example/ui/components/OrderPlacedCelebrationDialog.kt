package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SmsStatus
import com.example.data.model.TShirtOrder
import com.example.ui.theme.AppBorder
import com.example.ui.theme.AppSurface
import com.example.ui.theme.AppSurfaceSubtle
import com.example.ui.theme.AppTextMuted
import com.example.ui.theme.AppTextPrimary
import com.example.ui.theme.AppTextSecondary
import com.example.ui.theme.BrandError
import com.example.ui.theme.BrandErrorLight
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSuccess
import com.example.ui.theme.BrandSuccessBorder
import com.example.ui.theme.BrandSuccessLight
import com.example.ui.theme.BrandWarning
import com.example.ui.theme.BrandWarningLight
import com.example.ui.theme.MandirCrimson
import com.example.ui.theme.MandirGold
import com.example.ui.theme.MandirGoldLight
import com.example.util.CurrencyUtils
import kotlinx.coroutines.delay

/**
 * Animated Material 3 Order Placed Success Pop-up with bouncy spring tick mark,
 * live cellular SMS progress tracking, and 1-2s auto-close / dismiss-on-touch.
 */
@Composable
fun OrderPlacedCelebrationDialog(
    order: TShirtOrder,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var hasAppeared by remember { mutableStateOf(false) }

    // Spring animation for tick mark popping into place
    val tickScale by animateFloatAsState(
        targetValue = if (hasAppeared) 1f else 0.1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tick_spring"
    )

    val tickRotate by animateFloatAsState(
        targetValue = if (hasAppeared) 0f else -60f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tick_rot"
    )

    // Gentle pulsing halo around success circle
    val pulseTransition = rememberInfiniteTransition(label = "pulse_trans")
    val pulseRingScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseRingAlpha by pulseTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    // Trigger appear immediately on entry
    LaunchedEffect(Unit) {
        hasAppeared = true
    }

    // Auto-close: automatically dismisses after ~1-1.2s or user can tap outside
    LaunchedEffect(order.id, order.smsStatus) {
        if (order.smsStatus == SmsStatus.SENT) {
            delay(1000)
            onDismiss()
        } else {
            delay(1200)
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .testTag("order_placed_celebration_popup"),
                shape = RoundedCornerShape(28.dp),
                color = AppSurface,
                border = BorderStroke(1.2.dp, MandirGold.copy(alpha = 0.4f)),
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Top Bar with Close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MandirGoldLight,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.8.dp, MandirGold.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "শারদীয় দুর্গোৎসব",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MandirCrimson,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = AppTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Celebratory Animated Tick Mark
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Expanding Pulse Halo Wave
                        Canvas(modifier = Modifier.size(100.dp)) {
                            drawCircle(
                                color = BrandSuccess.copy(alpha = pulseRingAlpha),
                                radius = (size.minDimension / 2f) * pulseRingScale,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }

                        // Gold Sparkle Accents
                        Canvas(modifier = Modifier.size(90.dp)) {
                            val centerOffset = Offset(size.width / 2, size.height / 2)
                            val dotRadius = 3.dp.toPx()
                            val dist = 38.dp.toPx()
                            val angles = listOf(45.0, 135.0, 225.0, 315.0)
                            for (angle in angles) {
                                val rad = Math.toRadians(angle)
                                val x = centerOffset.x + (dist * Math.cos(rad)).toFloat()
                                val y = centerOffset.y + (dist * Math.sin(rad)).toFloat()
                                drawCircle(
                                    color = MandirGold.copy(alpha = 0.7f),
                                    radius = dotRadius,
                                    center = Offset(x, y)
                                )
                            }
                        }

                        // Main Vibrant Green Circle with Spring Bouncing Tick
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .scale(tickScale)
                                .rotate(tickRotate)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFF10B981), Color(0xFF047857))
                                    ),
                                    shape = CircleShape
                                )
                                .border(BorderStroke(2.dp, Color.White), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Success Tick",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    // Main Title & Subtitle
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "অর্ডার সফলভাবে সংরক্ষিত!",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            color = AppTextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Order Placed & Dispatched",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandSuccess,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Order Details Pill & Customer Summary
                    Surface(
                        color = AppSurfaceSubtle,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, AppBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Order ID", order.orderId))
                                        Toast.makeText(context, "ID copied: #${order.orderId}", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text(
                                        text = "ORDER ID: ",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppTextSecondary
                                    )
                                    Text(
                                        text = "#${order.orderId}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BrandPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = BrandPrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }

                                Text(
                                    text = "${order.totalQuantity} pcs",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTextPrimary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = order.customerName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTextPrimary
                                )

                                Text(
                                    text = CurrencyUtils.formatTaka(order.totalAmount),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BrandPrimary
                                )
                            }
                        }
                    }

                    // Live SMS Progress Indicator Section
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = when (order.smsStatus) {
                            SmsStatus.SENT, SmsStatus.DELIVERED -> BrandSuccessLight
                            SmsStatus.FAILED -> BrandErrorLight
                            SmsStatus.SENDING, SmsStatus.NOT_SENT -> BrandWarningLight
                        },
                        border = BorderStroke(
                            1.dp,
                            when (order.smsStatus) {
                                SmsStatus.SENT, SmsStatus.DELIVERED -> BrandSuccessBorder
                                SmsStatus.FAILED -> BrandError.copy(alpha = 0.3f)
                                SmsStatus.SENDING, SmsStatus.NOT_SENT -> BrandWarning.copy(alpha = 0.4f)
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("live_sms_progress_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = when (order.smsStatus) {
                                            SmsStatus.SENT, SmsStatus.DELIVERED -> Icons.Default.CheckCircle
                                            SmsStatus.FAILED -> Icons.Default.ErrorOutline
                                            else -> Icons.Default.Sms
                                        },
                                        contentDescription = null,
                                        tint = when (order.smsStatus) {
                                            SmsStatus.SENT, SmsStatus.DELIVERED -> BrandSuccess
                                            SmsStatus.FAILED -> BrandError
                                            else -> BrandWarning
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (order.smsStatus) {
                                            SmsStatus.SENT -> "এসএমএস প্রেরিত (SMS Sent)"
                                            SmsStatus.DELIVERED -> "এসএমএস ডেলিভার্ড (Delivered)"
                                            SmsStatus.FAILED -> "এসএমএস প্রেরণে সমস্যা"
                                            SmsStatus.SENDING -> "লাইভ ট্র্যাকিং এসএমএস পাঠানো হচ্ছে..."
                                            SmsStatus.NOT_SENT -> "এসএমএস প্রেরণের অপেক্ষায়..."
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (order.smsStatus) {
                                            SmsStatus.SENT, SmsStatus.DELIVERED -> BrandSuccess
                                            SmsStatus.FAILED -> BrandError
                                            else -> BrandWarning
                                        }
                                    )
                                }
                            }

                            if (order.smsStatus == SmsStatus.SENDING || order.smsStatus == SmsStatus.NOT_SENT) {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = BrandWarning,
                                    trackColor = BrandWarning.copy(alpha = 0.2f)
                                )
                            }

                            Text(
                                text = when (order.smsStatus) {
                                    SmsStatus.SENT -> "গ্রাহক ${order.mobileNumber}-এ podderpara.shop লাইভ লিংক সহ বাংলা এসএমএস ডেলিভার হয়েছে।"
                                    SmsStatus.FAILED -> "সিম নেটওয়ার্ক বা ব্যালেন্সের কারণে এসএমএস আটকে গেছে। আপনি অর্ডার তালিকা থেকে হোয়াটসঅ্যাপেও পাঠাতে পারেন।"
                                    else -> "${order.mobileNumber} নম্বরে স্বয়ংক্রিয়ভাবে বাংলা কনফার্মেশন ও ট্র্যাকিং লিংক যাচ্ছে।"
                                },
                                fontSize = 10.sp,
                                color = AppTextSecondary,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    // Tap Outside or Dismiss Hint Button
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("celebration_done_btn")
                    ) {
                        Text(
                            text = "পরবর্তী অর্ডার করুন (Done)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
