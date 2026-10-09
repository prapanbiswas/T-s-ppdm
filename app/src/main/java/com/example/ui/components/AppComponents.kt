package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.OrderStatus
import com.example.data.model.SmsStatus
import com.example.data.model.SyncStatus
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AppBorder
import com.example.ui.theme.AppBorderStrong
import com.example.ui.theme.AppSurface
import com.example.ui.theme.AppSurfaceSubtle
import com.example.ui.theme.AppTextMuted
import com.example.ui.theme.AppTextPrimary
import com.example.ui.theme.AppTextSecondary
import com.example.ui.theme.BrandDark
import com.example.ui.theme.BrandError
import com.example.ui.theme.BrandErrorBorder
import com.example.ui.theme.BrandErrorLight
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.BrandIndigoBorder
import com.example.ui.theme.BrandIndigoLight
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandPrimaryLight
import com.example.ui.theme.BrandSuccess
import com.example.ui.theme.BrandSuccessBorder
import com.example.ui.theme.BrandSuccessLight
import com.example.ui.theme.BrandTeal
import com.example.ui.theme.BrandTealBorder
import com.example.ui.theme.BrandTealLight
import com.example.ui.theme.BrandWarning
import com.example.ui.theme.BrandWarningBorder
import com.example.ui.theme.BrandWarningLight
import com.example.ui.theme.M3SurfaceContainerLow
import com.example.ui.theme.MandirCrimson
import com.example.ui.theme.MandirGold
import com.example.ui.theme.MandirGoldDark

/**
 * Material 3 Expressive Card with generous rounded corners, tonal depth, and subtle border
 */
@Composable
fun SleekCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = AppSurface,
    borderColor: Color = AppBorder,
    elevation: Dp = 1.dp,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    contentPadding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    testTag: String = "",
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

/**
 * Material 3 Expressive Pill Button with high visual contrast and 48dp touch target
 */
@Composable
fun SleekButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    backgroundColor: Color = BrandPrimary,
    contentColor: Color = Color.White,
    enabled: Boolean = true,
    shape: RoundedCornerShape = RoundedCornerShape(50),
    testTag: String = ""
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(48.dp)
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.45f),
            disabledContentColor = contentColor.copy(alpha = 0.7f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp, pressedElevation = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 0.2.sp
            )
        }
    }
}

/**
 * Material 3 Expressive Outlined Pill Button
 */
@Composable
fun SleekOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    color: Color = AppTextPrimary,
    borderColor: Color = AppBorderStrong,
    enabled: Boolean = true,
    shape: RoundedCornerShape = RoundedCornerShape(50),
    testTag: String = ""
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(44.dp)
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier),
        shape = shape,
        border = BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = color
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
    }
}

/**
 * Material 3 Expressive Filled Tonal Pill Button
 */
@Composable
fun SleekTonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    backgroundColor: Color = BrandPrimaryLight,
    contentColor: Color = BrandPrimary,
    enabled: Boolean = true,
    shape: RoundedCornerShape = RoundedCornerShape(50),
    testTag: String = ""
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(44.dp)
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier),
        shape = shape,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

/**
 * Material 3 Expressive Outlined Text Field with rounded corners
 */
@Composable
fun SleekTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    testTag: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = AppTextMuted) },
        leadingIcon = leadingIcon?.let {
            { Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(20.dp), tint = AppTextSecondary) }
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp), tint = AppTextMuted)
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier),
        singleLine = singleLine,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AppSurface,
            unfocusedContainerColor = M3SurfaceContainerLow,
            focusedBorderColor = BrandPrimary,
            unfocusedBorderColor = AppBorder,
            focusedLabelColor = BrandPrimary,
            unfocusedLabelColor = AppTextSecondary
        ),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )
}

/**
 * Expressive Order Status Badge Pill
 */
@Composable
fun OrderStatusBadge(
    status: OrderStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor, textColor) = when (status) {
        OrderStatus.UNDER_PROCESSING -> Triple(BrandWarningLight, BrandWarningBorder, BrandWarning)
        OrderStatus.PAID -> Triple(BrandSuccessLight, BrandSuccessBorder, BrandSuccess)
        OrderStatus.DELIVERED -> Triple(BrandIndigoLight, BrandIndigoBorder, BrandIndigo)
        OrderStatus.CANCELLED -> Triple(BrandErrorLight, BrandErrorBorder, BrandError)
    }

    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = status.displayName,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/**
 * Material 3 Expressive SMS Gateway Status Badge
 * Real-time PendingIntent receiver reflection:
 * - Pending Dispatch / Sending (animated pulse)
 * - Sent (carrier confirmation)
 * - Delivered (SMSC receipt confirmation)
 * - Failed (carrier error with retry affordance)
 */
@Composable
fun SmsDeliveryBadge(
    status: SmsStatus,
    errorMessage: String? = null,
    onRetryClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isSending = status == SmsStatus.SENDING

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by pulseTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val style = when (status) {
        SmsStatus.DELIVERED -> ExpressiveBadgeStyle(BrandTealLight, BrandTealBorder, BrandTeal, Icons.Default.DoneAll, "SMS Delivered")
        SmsStatus.SENT -> ExpressiveBadgeStyle(BrandSuccessLight, BrandSuccessBorder, BrandSuccess, Icons.Default.Check, "SMS Sent")
        SmsStatus.SENDING -> ExpressiveBadgeStyle(BrandPrimaryLight, BrandPrimary.copy(alpha = 0.35f), BrandPrimary, Icons.Default.Schedule, "Sending SMS...")
        SmsStatus.NOT_SENT -> ExpressiveBadgeStyle(AppSurfaceSubtle, AppBorder, AppTextSecondary, Icons.Default.HourglassEmpty, "SMS Pending")
        SmsStatus.FAILED -> ExpressiveBadgeStyle(BrandErrorLight, BrandErrorBorder, BrandError, Icons.Default.ErrorOutline, "SMS Failed • Retry")
    }

    Surface(
        modifier = modifier
            .then(if (status == SmsStatus.FAILED && onRetryClick != null) Modifier.clickable(onClick = onRetryClick) else Modifier)
            .then(if (isSending) Modifier.alpha(alphaAnim) else Modifier),
        color = style.bgColor,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, style.borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(10.dp),
                    strokeWidth = 1.5.dp,
                    color = style.textColor
                )
            } else {
                Icon(
                    imageVector = style.icon,
                    contentDescription = null,
                    tint = style.textColor,
                    modifier = Modifier.size(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = style.label,
                color = style.textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}

private data class ExpressiveBadgeStyle(
    val bgColor: Color,
    val borderColor: Color,
    val textColor: Color,
    val icon: ImageVector,
    val label: String
)

/**
 * Material 3 Expressive Cloud Sync Status Badge
 * Real-time visual indicator for single-user offline-first cloud backup:
 * - Cloud Synced (backed up to Firebase)
 * - Pending Backup (offline, queued for connection)
 * - Saved Locally (local only storage)
 * - Sync Error (retry available)
 */
@Composable
fun CloudSyncBadge(
    status: SyncStatus,
    onSyncClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val style = when (status) {
        SyncStatus.SYNCED -> ExpressiveBadgeStyle(BrandSuccessLight, BrandSuccessBorder, BrandSuccess, Icons.Default.CloudDone, "Cloud Synced")
        SyncStatus.PENDING_SYNC -> ExpressiveBadgeStyle(BrandWarningLight, BrandWarningBorder, BrandWarning, Icons.Default.CloudSync, "Pending Backup")
        SyncStatus.LOCAL_ONLY -> ExpressiveBadgeStyle(AppSurfaceSubtle, AppBorder, AppTextSecondary, Icons.Default.CloudOff, "Saved Locally")
        SyncStatus.SYNC_ERROR -> ExpressiveBadgeStyle(BrandErrorLight, BrandErrorBorder, BrandError, Icons.Default.SyncProblem, "Sync Error • Retry")
    }

    Surface(
        modifier = modifier
            .then(if ((status == SyncStatus.SYNC_ERROR || status == SyncStatus.PENDING_SYNC) && onSyncClick != null) {
                Modifier.clickable(onClick = onSyncClick)
            } else Modifier),
        color = style.bgColor,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, style.borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = style.icon,
                contentDescription = null,
                tint = style.textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = style.label,
                color = style.textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}

/**
 * Material 3 Expressive Brand Header for App Bar
 */
@Composable
fun SleekBrandHeader(
    modifier: Modifier = Modifier,
    storeName: String = "পোদ্দারপাড়া সর্বজনীন দুর্গা মন্দির"
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Temple Emblem Icon with expressive border
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(MandirCrimson, shape = RoundedCornerShape(14.dp))
                .border(BorderStroke(1.2.dp, MandirGold.copy(alpha = 0.7f)), shape = RoundedCornerShape(14.dp))
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_mandir_logo),
                contentDescription = "Mandir Emblem",
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = storeName,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = AppTextPrimary,
                maxLines = 1
            )
            Text(
                text = "শারদীয় দুর্গোৎসব টি-শার্ট বিতরণ ও ট্র্যাকিং",
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = MandirGoldDark
            )
        }
    }
}
