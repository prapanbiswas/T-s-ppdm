package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.OrderDataSheetSummary
import com.example.data.model.OrderStatus
import com.example.data.model.SmsStatus
import com.example.data.model.TShirtOrder
import com.example.data.repository.OrderRepository
import com.example.network.NetworkMonitor
import com.example.network.NetworkStatus
import com.example.sms.SimCardInfo
import com.example.sms.SmsGatewayManager
import com.example.sms.SmsSendResult
import com.example.sync.FirebaseSyncManager
import com.example.sync.SyncState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class NewOrderFormState(
    val customerName: String = "",
    val mobileNumber: String = "",
    val unitPrice: Double = 350.0,
    val qtyChild12: Int = 0,
    val qtyS: Int = 0,
    val qtyM: Int = 0,
    val qtyL: Int = 0,
    val qtyXL: Int = 0,
    val qtyXXL: Int = 0,
    val qtyXXXL: Int = 0,
    val note: String = ""
) {
    val totalQuantity: Int
        get() = qtyChild12 + qtyS + qtyM + qtyL + qtyXL + qtyXXL + qtyXXXL

    val totalAmount: Double
        get() = totalQuantity * unitPrice

    val isValid: Boolean
        get() = customerName.isNotBlank() && mobileNumber.isNotBlank() && totalQuantity > 0
}

class OrderViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val networkMonitor = NetworkMonitor(application)
    private val syncManager = FirebaseSyncManager(application, database.tshirtOrderDao())
    private val repository = OrderRepository(
        orderDao = database.tshirtOrderDao(),
        syncManager = syncManager,
        networkMonitor = networkMonitor,
        appScope = viewModelScope
    )

    private val prefs = application.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)

    // Network & Sync
    val networkStatus: StateFlow<NetworkStatus> = networkMonitor.status
    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
    val syncState: StateFlow<SyncState> = syncManager.syncState

    // Form State
    private val _formState = MutableStateFlow(
        NewOrderFormState(unitPrice = prefs.getFloat("default_unit_price", 350f).toDouble())
    )
    val formState: StateFlow<NewOrderFormState> = _formState.asStateFlow()

    // Confirmation Dialog State
    private val _showConfirmationDialog = MutableStateFlow(false)
    val showConfirmationDialog: StateFlow<Boolean> = _showConfirmationDialog.asStateFlow()

    // Search and Filter State for Orders List
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow<OrderStatus?>(null)
    val selectedStatusFilter: StateFlow<OrderStatus?> = _selectedStatusFilter.asStateFlow()

    // Active Edit Dialog State
    private val _editingOrder = MutableStateFlow<TShirtOrder?>(null)
    val editingOrder: StateFlow<TShirtOrder?> = _editingOrder.asStateFlow()

    // Store & Dispatch Settings (Temple Branding)
    private val _storeName = MutableStateFlow(
        prefs.getString("store_name", null).let {
            if (it.isNullOrBlank() || it == "Apparel Store") "পোদ্দারপাড়া সর্বজনীন দুর্গা মন্দির" else it
        }
    )
    val storeName: StateFlow<String> = _storeName.asStateFlow()

    private val _storePhone = MutableStateFlow(
        prefs.getString("store_phone", "+880 1700-000000") ?: "+880 1700-000000"
    )
    val storePhone: StateFlow<String> = _storePhone.asStateFlow()

    private val _trackingBaseUrl = MutableStateFlow(
        prefs.getString("tracking_base_url", null).let {
            if (it.isNullOrBlank() || it.contains("example.com")) "https://podderpara.shop" else it
        }
    )
    val trackingBaseUrl: StateFlow<String> = _trackingBaseUrl.asStateFlow()

    private val _autoSendSms = MutableStateFlow(
        prefs.getBoolean("auto_send_sms", true)
    )
    val autoSendSms: StateFlow<Boolean> = _autoSendSms.asStateFlow()

    private val _selectedSimId = MutableStateFlow(
        prefs.getInt("selected_sim_id", -1)
    )
    val selectedSimId: StateFlow<Int> = _selectedSimId.asStateFlow()

    private val _availableSimCards = MutableStateFlow<List<SimCardInfo>>(emptyList())
    val availableSimCards: StateFlow<List<SimCardInfo>> = _availableSimCards.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    init {
        refreshSimCards()
    }

    fun refreshSimCards() {
        _availableSimCards.value = SmsGatewayManager.getAvailableSimCards(getApplication())
    }

    // Combined Orders Flow with Search & Filter
    @OptIn(ExperimentalCoroutinesApi::class)
    val orders: StateFlow<List<TShirtOrder>> = _searchQuery
        .flatMapLatest { query ->
            repository.searchOrders(query)
        }
        .combine(_selectedStatusFilter) { list, statusFilter ->
            if (statusFilter == null) list else list.filter { it.status == statusFilter }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Unfiltered all orders for Data Sheet calculations
    val allOrdersForSummary: StateFlow<List<TShirtOrder>> = repository.allOrders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // DataSheet summary calculation
    val dataSheetSummary: StateFlow<OrderDataSheetSummary> = allOrdersForSummary
        .combine(_searchQuery) { ordersList, _ ->
            repository.calculateDataSheetSummary(ordersList)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = repository.calculateDataSheetSummary(emptyList())
        )

    // Form Actions
    fun updateCustomerName(name: String) {
        _formState.value = _formState.value.copy(customerName = name)
    }

    fun updateMobileNumber(phone: String) {
        _formState.value = _formState.value.copy(mobileNumber = phone)
    }

    fun updateUnitPrice(price: Double) {
        if (price >= 0.0) {
            _formState.value = _formState.value.copy(unitPrice = price)
        }
    }

    fun updateNote(note: String) {
        _formState.value = _formState.value.copy(note = note)
    }

    fun updateQuantity(sizeKey: String, delta: Int) {
        val current = _formState.value
        val updated = when (sizeKey) {
            "Child 1-2" -> current.copy(qtyChild12 = maxOf(0, current.qtyChild12 + delta))
            "S" -> current.copy(qtyS = maxOf(0, current.qtyS + delta))
            "M" -> current.copy(qtyM = maxOf(0, current.qtyM + delta))
            "L" -> current.copy(qtyL = maxOf(0, current.qtyL + delta))
            "XL" -> current.copy(qtyXL = maxOf(0, current.qtyXL + delta))
            "XXL" -> current.copy(qtyXXL = maxOf(0, current.qtyXXL + delta))
            "XXXL" -> current.copy(qtyXXXL = maxOf(0, current.qtyXXXL + delta))
            else -> current
        }
        _formState.value = updated
    }

    fun setExactQuantity(sizeKey: String, exactQty: Int) {
        val qty = maxOf(0, exactQty)
        val current = _formState.value
        val updated = when (sizeKey) {
            "Child 1-2" -> current.copy(qtyChild12 = qty)
            "S" -> current.copy(qtyS = qty)
            "M" -> current.copy(qtyM = qty)
            "L" -> current.copy(qtyL = qty)
            "XL" -> current.copy(qtyXL = qty)
            "XXL" -> current.copy(qtyXXL = qty)
            "XXXL" -> current.copy(qtyXXXL = qty)
            else -> current
        }
        _formState.value = updated
    }

    fun openConfirmation() {
        if (_formState.value.isValid) {
            networkMonitor.triggerReachabilityCheck("Order placement check")
            _showConfirmationDialog.value = true
        }
    }

    fun dismissConfirmation() {
        _showConfirmationDialog.value = false
    }

    fun confirmAndPlaceOrder(onSuccess: (TShirtOrder) -> Unit) {
        val form = _formState.value
        if (!form.isValid) return

        viewModelScope.launch {
            val savedOrder = repository.placeOrder(
                customerName = form.customerName,
                mobileNumber = form.mobileNumber,
                unitPrice = form.unitPrice,
                qtyChild12 = form.qtyChild12,
                qtyS = form.qtyS,
                qtyM = form.qtyM,
                qtyL = form.qtyL,
                qtyXL = form.qtyXL,
                qtyXXL = form.qtyXXL,
                qtyXXXL = form.qtyXXXL,
                trackingBaseUrl = _trackingBaseUrl.value,
                note = form.note
            )

            // Reset form
            _formState.value = NewOrderFormState(unitPrice = form.unitPrice)
            _showConfirmationDialog.value = false
            onSuccess(savedOrder)

            // Trigger Automated Background SMS Gateway (cellular SMS works offline)
            if (_autoSendSms.value) {
                dispatchSmsForOrder(savedOrder)
            }

            // Enqueue background Firebase sync worker with NetworkType.CONNECTED constraint
            com.example.sync.FirebaseSyncWorker.enqueue(getApplication())
        }
    }

    /**
     * Background SMS dispatch worker (enqueued via WorkManager for reliable background execution)
     */
    fun dispatchSmsForOrder(order: TShirtOrder) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSmsStatus(order.id, SmsStatus.SENDING)
            com.example.sms.OrderSmsWorker.enqueue(getApplication(), order.id)
        }
    }

    fun resendOrderSms(order: TShirtOrder) {
        dispatchSmsForOrder(order)
    }

    // Search & Filter Actions
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: OrderStatus?) {
        _selectedStatusFilter.value = status
    }

    // Order Modification Actions
    fun updateOrderStatus(orderId: Long, newStatus: OrderStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
        }
    }

    fun cancelOrder(order: TShirtOrder) {
        viewModelScope.launch {
            repository.updateOrderStatus(order.id, OrderStatus.CANCELLED)
        }
    }

    fun deleteOrder(order: TShirtOrder) {
        viewModelScope.launch {
            repository.deleteOrder(order)
        }
    }

    fun startEditingOrder(order: TShirtOrder) {
        _editingOrder.value = order
    }

    fun dismissEditingOrder() {
        _editingOrder.value = null
    }

    fun saveEditedOrder(updatedOrder: TShirtOrder) {
        viewModelScope.launch {
            repository.updateOrder(updatedOrder)
            _editingOrder.value = null
        }
    }

    // Reachability & Sync Actions
    fun forceReachabilityCheck() {
        networkMonitor.triggerReachabilityCheck("User manual trigger")
    }

    fun forceSyncNow() {
        viewModelScope.launch {
            val count = repository.syncNow()
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    getApplication(),
                    if (count > 0) "Synced $count pending orders to Firebase" else "All orders up to date",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun restoreFromCloud() {
        viewModelScope.launch {
            val count = repository.restoreFromCloud()
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    getApplication(),
                    if (count > 0) "ক্লাউড থেকে $count টি অর্ডার রিস্টোর হয়েছে" else "ক্লাউড থেকে সকল অর্ডার আপডেট করা হয়েছে",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Settings
    fun toggleSettingsDialog(show: Boolean) {
        if (show) refreshSimCards()
        _showSettingsDialog.value = show
    }

    fun saveSettings(
        name: String,
        phone: String,
        defaultPrice: Double,
        trackingBaseUrl: String,
        autoSendSms: Boolean,
        selectedSimId: Int
    ) {
        _storeName.value = name
        _storePhone.value = phone
        _trackingBaseUrl.value = trackingBaseUrl
        _autoSendSms.value = autoSendSms
        _selectedSimId.value = selectedSimId
        _formState.value = _formState.value.copy(unitPrice = defaultPrice)

        prefs.edit()
            .putString("store_name", name)
            .putString("store_phone", phone)
            .putString("tracking_base_url", trackingBaseUrl)
            .putBoolean("auto_send_sms", autoSendSms)
            .putInt("selected_sim_id", selectedSimId)
            .putFloat("default_unit_price", defaultPrice.toFloat())
            .apply()

        _showSettingsDialog.value = false
    }

    fun copyTrackingLink(context: Context, order: TShirtOrder) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Order Tracking Link", order.trackingUrl)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Tracking link copied: ${order.trackingUrl}", Toast.LENGTH_SHORT).show()
    }

    fun shareTrackingLink(context: Context, order: TShirtOrder) {
        val message = SmsGatewayManager.generateBengaliSmsMessage(order, _storeName.value)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Order #${order.orderId}")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
