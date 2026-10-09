package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.NetworkStatus
import com.example.ui.theme.BrandError
import com.example.ui.theme.BrandErrorBorder
import com.example.ui.theme.BrandErrorLight
import com.example.ui.theme.BrandSuccess
import com.example.ui.theme.BrandSuccessBorder
import com.example.ui.theme.BrandSuccessLight
import com.example.ui.theme.BrandWarning
import com.example.ui.theme.BrandWarningBorder
import com.example.ui.theme.BrandWarningLight

@Composable
fun NetworkStatusBar(
    status: NetworkStatus,
    isOnline: Boolean,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showBanner = !isOnline || status is NetworkStatus.Checking

    AnimatedVisibility(
        visible = showBanner,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier.fillMaxWidth()
    ) {
        val (bg, border, textCol) = if (status is NetworkStatus.Checking) {
            Triple(BrandWarningLight, BrandWarningBorder, BrandWarning)
        } else {
            Triple(BrandErrorLight, BrandErrorBorder, BrandError)
        }

        Surface(
            modifier = Modifier.fillMaxWidth().testTag("network_status_banner"),
            color = bg,
            border = BorderStroke(1.dp, border)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (status is NetworkStatus.Checking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = textCol
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = textCol,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (status is NetworkStatus.Checking)
                            "Verifying Firebase cloud connection..."
                        else
                            "Offline Mode • Orders safe in local Room DB",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = textCol
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onRefreshClick() }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry",
                        tint = textCol,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Retry",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textCol
                    )
                }
            }
        }
    }
}

@Composable
fun NetworkStatusBadge(
    status: NetworkStatus,
    isOnline: Boolean,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (dotColor, text, bg) = when {
        status is NetworkStatus.Checking -> Triple(BrandWarning, "Checking", BrandWarningLight)
        isOnline -> Triple(BrandSuccess, "Online", BrandSuccessLight)
        else -> Triple(BrandError, "Offline", BrandErrorLight)
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onRefreshClick() }
            .testTag("network_status_badge"),
        color = bg,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, dotColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = dotColor
            )
        }
    }
}
