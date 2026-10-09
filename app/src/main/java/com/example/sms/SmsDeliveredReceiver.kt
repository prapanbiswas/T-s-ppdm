package com.example.sms

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.model.SmsStatus
import com.example.sync.FirebaseSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver triggered by delivery PendingIntent when the customer's handset
 * receives and acknowledges the SMS from the carrier network (SMSC delivery receipt).
 */
class SmsDeliveredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        val orderDbId = intent.getLongExtra(EXTRA_ORDER_DB_ID, -1L)
        val orderIdString = intent.getStringExtra(EXTRA_ORDER_ID_STRING) ?: ""
        val resultCode = resultCode

        Log.d(TAG, "SmsDeliveredReceiver received for order #$orderIdString (dbId: $orderDbId), resultCode: $resultCode")

        if (orderDbId <= 0) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val orderDao = database.tshirtOrderDao()
                val now = System.currentTimeMillis()

                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "Customer handset confirmed SMS DELIVERED for order #$orderIdString")
                    orderDao.updateSmsStatus(
                        id = orderDbId,
                        smsStatus = SmsStatus.DELIVERED,
                        errorMessage = null,
                        sentAt = now,
                        updatedAt = now
                    )
                }

                // Sync status to Firebase
                val updatedOrder = orderDao.getOrderByIdDirect(orderDbId)
                if (updatedOrder != null) {
                    try {
                        val syncManager = FirebaseSyncManager(context, orderDao)
                        syncManager.syncSingleOrder(updatedOrder)
                    } catch (e: Exception) {
                        Log.d(TAG, "Sync after delivery callback skipped: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling SMS delivered callback: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_SMS_DELIVERED = "com.example.sms.SMS_DELIVERED_ACTION"
        const val EXTRA_ORDER_DB_ID = "order_db_id"
        const val EXTRA_ORDER_ID_STRING = "order_id_string"
        private const val TAG = "SmsDeliveredReceiver"
    }
}
