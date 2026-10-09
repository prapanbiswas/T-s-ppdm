package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sms.SmsGatewayManager
import com.example.ui.OrderViewModel
import com.example.ui.components.NetworkStatusBar
import com.example.ui.components.NetworkStatusBadge
import com.example.ui.components.AppUpdateDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SleekBrandHeader
import com.example.update.AppUpdateManager
import com.example.update.UpdateStatus
import com.example.ui.screens.DataSheetScreen
import com.example.ui.screens.NewOrderScreen
import com.example.ui.screens.OrderListScreen
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AppBorder
import com.example.ui.theme.AppSurface
import com.example.ui.theme.AppSurfaceSubtle
import com.example.ui.theme.AppTextPrimary
import com.example.ui.theme.AppTextSecondary
import com.example.ui.theme.BrandDark
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandPrimaryLight
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

enum class AppTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    NEW_ORDER("নতুন অর্ডার", Icons.Filled.AddShoppingCart, Icons.Outlined.AddShoppingCart),
    ALL_ORDERS("সকল অর্ডার", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    DATA_SHEET("হিসাব খাতা", Icons.Filled.Assessment, Icons.Outlined.Assessment)
}

class MainActivity : ComponentActivity() {

    private val viewModel: OrderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Strictly 100% Light Theme
            MyApplicationTheme {
                val context = LocalContext.current
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                var currentTab by remember { mutableStateOf(AppTab.NEW_ORDER) }

                val networkStatus by viewModel.networkStatus.collectAsStateWithLifecycle()
                val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
                val showSettings by viewModel.showSettingsDialog.collectAsStateWithLifecycle()
                val storeName by viewModel.storeName.collectAsStateWithLifecycle()
                val storePhone by viewModel.storePhone.collectAsStateWithLifecycle()
                val trackingBaseUrl by viewModel.trackingBaseUrl.collectAsStateWithLifecycle()
                val autoSendSms by viewModel.autoSendSms.collectAsStateWithLifecycle()
                val selectedSimId by viewModel.selectedSimId.collectAsStateWithLifecycle()
                val availableSimCards by viewModel.availableSimCards.collectAsStateWithLifecycle()
                val formState by viewModel.formState.collectAsStateWithLifecycle()
                val updateStatus by AppUpdateManager.updateStatus.collectAsStateWithLifecycle()

                // Check for updates on startup
                LaunchedEffect(Unit) {
                    AppUpdateManager.checkForUpdates(context)
                }

                // SMS & Telephony Permission state & launcher
                var hasSmsPermission by remember {
                    mutableStateOf(SmsGatewayManager.hasSmsPermission(context))
                }

                val permissionsToRequest = remember {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        arrayOf(
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    } else {
                        arrayOf(
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.READ_PHONE_STATE
                        )
                    }
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    hasSmsPermission = permissions[Manifest.permission.SEND_SMS] == true
                    viewModel.refreshSimCards()
                }

                LaunchedEffect(Unit) {
                    if (!hasSmsPermission) {
                        permissionLauncher.launch(permissionsToRequest)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = AppBackground,
                    topBar = {
                        Column {
                            // Top Bar Header
                            Surface(
                                color = AppSurface,
                                border = BorderStroke(1.dp, AppBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .statusBarsPadding()
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Sleek Brand Header with folded apparel logo
                                        SleekBrandHeader(
                                            storeName = storeName
                                        )

                                        // Network Pill & Settings
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            NetworkStatusBadge(
                                                status = networkStatus,
                                                isOnline = isOnline,
                                                onRefreshClick = { viewModel.forceReachabilityCheck() }
                                            )

                                            IconButton(
                                                onClick = { viewModel.toggleSettingsDialog(true) },
                                                modifier = Modifier.testTag("settings_btn")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Settings,
                                                    contentDescription = "Settings",
                                                    tint = AppTextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Offline / Checking alert banner
                            NetworkStatusBar(
                                status = networkStatus,
                                isOnline = isOnline,
                                onRefreshClick = { viewModel.forceReachabilityCheck() }
                            )

                            // SMS Permission Prompt Banner if needed
                            AnimatedVisibility(
                                visible = !hasSmsPermission,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Surface(
                                    color = BrandPrimaryLight,
                                    border = BorderStroke(1.dp, BrandPrimary.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Sms,
                                                contentDescription = null,
                                                tint = BrandPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "SMS Gateway requires permission to dispatch tracking links",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = BrandPrimary
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                permissionLauncher.launch(permissionsToRequest)
                                            },
                                            shape = RoundedCornerShape(50),
                                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Grant Access", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    },
                    bottomBar = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppSurface)
                                .windowInsetsPadding(WindowInsets.navigationBars),
                            contentAlignment = Alignment.Center
                        ) {
                            NavigationBar(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 680.dp)
                                    .testTag("bottom_nav_bar"),
                                containerColor = AppSurface,
                                tonalElevation = 2.dp
                            ) {
                                AppTab.values().forEach { tab ->
                                    val isSelected = currentTab == tab
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { currentTab = tab },
                                        icon = {
                                            Icon(
                                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.title,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = tab.title,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.sp
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = BrandPrimary,
                                            selectedTextColor = BrandPrimary,
                                            indicatorColor = BrandPrimaryLight,
                                            unselectedIconColor = AppTextSecondary,
                                            unselectedTextColor = AppTextSecondary
                                        ),
                                        modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                                    )
                                }
                            }
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "tab_transition"
                        ) { tab ->
                            when (tab) {
                                AppTab.NEW_ORDER -> {
                                    NewOrderScreen(
                                        viewModel = viewModel,
                                        onOrderPlaced = { orderId ->
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "Order #$orderId placed & tracking dispatched!",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                            currentTab = AppTab.ALL_ORDERS
                                        }
                                    )
                                }
                                AppTab.ALL_ORDERS -> {
                                    OrderListScreen(viewModel = viewModel)
                                }
                                AppTab.DATA_SHEET -> {
                                    DataSheetScreen(viewModel = viewModel)
                                }
                            }
                        }
                    }
                }

                // Settings & SIM Selection Dialog
                if (showSettings) {
                    SettingsDialog(
                        currentStoreName = storeName,
                        currentStorePhone = storePhone,
                        currentUnitPrice = formState.unitPrice,
                        currentTrackingBaseUrl = trackingBaseUrl,
                        currentAutoSendSms = autoSendSms,
                        currentSelectedSimId = selectedSimId,
                        availableSimCards = availableSimCards,
                        onSave = { name, phone, price, trackingUrl, autoSms, simId ->
                            viewModel.saveSettings(name, phone, price, trackingUrl, autoSms, simId)
                        },
                        onTestReachability = { viewModel.forceReachabilityCheck() },
                        onSyncNow = { viewModel.forceSyncNow() },
                        onRestoreFromCloud = { viewModel.restoreFromCloud() },
                        onDismiss = { viewModel.toggleSettingsDialog(false) }
                    )
                }

                // In-App Auto Update Dialog
                if (updateStatus !is UpdateStatus.Idle) {
                    AppUpdateDialog(
                        status = updateStatus,
                        onDismiss = { AppUpdateManager.dismissUpdate() }
                    )
                }
            }
        }
    }
}
