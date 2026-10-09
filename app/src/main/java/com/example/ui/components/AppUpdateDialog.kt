package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AppBorder
import com.example.ui.theme.AppSurface
import com.example.ui.theme.AppSurfaceSubtle
import com.example.ui.theme.AppTextMuted
import com.example.ui.theme.AppTextPrimary
import com.example.ui.theme.AppTextSecondary
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSuccess
import com.example.ui.theme.BrandSuccessLight
import com.example.ui.theme.BrandWarning
import com.example.update.AppUpdateManager
import com.example.update.UpdateStatus
import kotlinx.coroutines.launch

@Composable
fun AppUpdateDialog(
    status: UpdateStatus,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (status is UpdateStatus.Idle) return

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("app_update_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = AppSurface,
            border = BorderStroke(1.dp, AppBorder),
            shadowElevation = 10.dp
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
                            .size(42.dp)
                            .background(BrandPrimary, shape = RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "অ্যাপ আপডেট (App Update)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = AppTextPrimary
                        )
                        Text(
                            text = "বর্তমান সংস্করণ: v${AppUpdateManager.CURRENT_VERSION_NAME}",
                            fontSize = 11.5.sp,
                            color = AppTextSecondary
                        )
                    }
                }

                HorizontalDivider(thickness = 0.8.dp, color = AppBorder)

                when (status) {
                    is UpdateStatus.Checking -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = BrandPrimary,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "নতুন রিলিজ চেক করা হচ্ছে...",
                                    fontSize = 12.sp,
                                    color = AppTextSecondary
                                )
                            }
                        }
                    }

                    is UpdateStatus.UpToDate -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = BrandSuccess,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = "আপনার অ্যাপটি সর্বাধুনিক সংস্করণে আছে!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = AppTextPrimary
                            )
                            Text(
                                text = "কোনো নতুন আপডেট প্রয়োজন নেই (v${status.currentVersion})",
                                fontSize = 11.5.sp,
                                color = AppTextSecondary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            SleekButton(
                                text = "ঠিক আছে (OK)",
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    is UpdateStatus.Available -> {
                        val info = status.info
                        val isDownloaded = status.isDownloaded
                        val localFile = status.localFile

                        // Version badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BrandPrimary.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, BrandPrimary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "নতুন সংস্করণ: v${info.versionName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = BrandPrimary
                                    )
                                    Text(
                                        text = "বিল্ড কোড: #${info.versionCode}",
                                        fontSize = 11.sp,
                                        color = AppTextSecondary
                                    )
                                }

                                if (isDownloaded) {
                                    Surface(
                                        color = BrandSuccessLight,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(0.8.dp, BrandSuccess.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = "ডাউনলোড করা আছে",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandSuccess,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Release Notes
                        Text(
                            text = "নতুন যা যুক্ত হয়েছে:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTextSecondary
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AppSurfaceSubtle,
                            border = BorderStroke(0.6.dp, AppBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = info.releaseNotes,
                                fontSize = 11.sp,
                                color = AppTextPrimary,
                                lineHeight = 15.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        // Actions
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (isDownloaded && localFile != null) {
                                // Already downloaded APK exists! Install immediately
                                SleekButton(
                                    text = "এখনই ইনস্টল করুন (Install Now)",
                                    onClick = {
                                        val started = AppUpdateManager.installApk(context, localFile)
                                        if (started) {
                                            Toast.makeText(context, "ইনস্টলার চালু হয়েছে...", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    icon = Icons.Default.InstallMobile,
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "install_now_btn"
                                )

                                SleekOutlinedButton(
                                    text = "পুনরায় ডাউনলোড করুন",
                                    onClick = {
                                        scope.launch {
                                            AppUpdateManager.downloadApk(context, info)
                                        }
                                    },
                                    icon = Icons.Default.Refresh,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                // Start downloading and auto-install on completion
                                SleekButton(
                                    text = "ডাউনলোড ও ইনস্টল (Download & Install)",
                                    onClick = {
                                        scope.launch {
                                            val file = AppUpdateManager.downloadApk(context, info)
                                            if (file != null) {
                                                AppUpdateManager.installApk(context, file)
                                            }
                                        }
                                    },
                                    icon = Icons.Default.Download,
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "download_and_install_btn"
                                )
                            }

                            SleekOutlinedButton(
                                text = "পরে (Later)",
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    is UpdateStatus.Downloading -> {
                        val info = status.info
                        val percent = status.progress
                        val downloadedMb = String.format(java.util.Locale.US, "%.1f", status.downloadedBytes / (1024.0 * 1024.0))
                        val totalMb = String.format(java.util.Locale.US, "%.1f", status.totalBytes / (1024.0 * 1024.0))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "নতুন আপডেট ডাউনলোড হচ্ছে...",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp,
                                    color = AppTextPrimary
                                )
                                Text(
                                    text = "$percent%",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = BrandPrimary
                                )
                            }

                            LinearProgressIndicator(
                                progress = { (percent / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = BrandPrimary,
                                trackColor = AppSurfaceSubtle,
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "v${info.versionName} APK",
                                    fontSize = 10.5.sp,
                                    color = AppTextSecondary
                                )
                                Text(
                                    text = "$downloadedMb MB / $totalMb MB",
                                    fontSize = 10.5.sp,
                                    color = AppTextMuted
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BrandPrimary.copy(alpha = 0.06f), shape = RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "💡 ডাউনলোড শেষ হলেই সিস্টেম ইনস্টলার স্বয়ংক্রিয়ভাবে চালু হবে।",
                                    fontSize = 10.sp,
                                    color = AppTextPrimary
                                )
                            }
                        }
                    }

                    is UpdateStatus.ReadyToInstall -> {
                        val info = status.info
                        val file = status.file

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = BrandSuccess,
                                modifier = Modifier.size(44.dp)
                            )

                            Text(
                                text = "ডাউনলোড সম্পন্ন! প্রস্তুত আছে",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AppTextPrimary
                            )

                            Text(
                                text = "সংস্করণ v${info.versionName} এখনই ইনস্টল করতে নিচের বাটনে চাপ দিন। ইনস্টল শেষে অ্যাপের সংস্করণ স্বয়ংক্রিয়ভাবে আপডেট হবে।",
                                fontSize = 11.5.sp,
                                color = AppTextSecondary,
                                lineHeight = 15.sp
                            )

                            SleekButton(
                                text = "এখনই ইনস্টল করুন (Install Now)",
                                onClick = {
                                    val started = AppUpdateManager.installApk(context, file)
                                    if (started) {
                                        Toast.makeText(context, "ইনস্টলার চালু হয়েছে...", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                icon = Icons.Default.InstallMobile,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "ready_install_btn"
                            )

                            SleekOutlinedButton(
                                text = "বন্ধ করুন (Close)",
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    is UpdateStatus.Error -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = BrandWarning,
                                modifier = Modifier.size(40.dp)
                            )

                            Text(
                                text = "আপডেট চেক/ডাউনলোডে সমস্যা",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = AppTextPrimary
                            )

                            Text(
                                text = status.message,
                                fontSize = 11.sp,
                                color = AppTextMuted
                            )

                            SleekButton(
                                text = "আবার চেষ্টা করুন",
                                onClick = {
                                    scope.launch {
                                        AppUpdateManager.checkForUpdates(context)
                                    }
                                },
                                icon = Icons.Default.Refresh,
                                modifier = Modifier.fillMaxWidth()
                            )

                            SleekOutlinedButton(
                                text = "বন্ধ করুন",
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}
