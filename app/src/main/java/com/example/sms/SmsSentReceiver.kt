package com.example.sms

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.model.SmsStatus
import com.example.sync.FirebaseSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver triggered by PendingIntent when Android SmsManager
 * finishes transmitting an SMS part to the mobile carrier tower.
 * Reliably captures carrier confirmation or failure reasons (e.g. zero balance).
 */
class SmsSentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        val orderDbId = intent.getLongExtra(EXTRA_ORDER_DB_ID, -1L)
        val orderIdString = intent.getStringExtra(EXTRA_ORDER_ID_STRING) ?: ""
        val partIndex = intent.getIntExtra(EXTRA_PART_INDEX, 0)
        val totalParts = intent.getIntExtra(EXTRA_TOTAL_PARTS, 1)
        val resultCode = resultCode

        Log.d(
            TAG,
            "SmsSentReceiver triggered: order #$orderIdString (dbId: $orderDbId), part ${partIndex + 1}/$totalParts, resultCode: $resultCode"
        )

        if (orderDbId <= 0) {
            Log.w(TAG, "SmsSentReceiver ignored broadcast without valid orderDbId")
            return
        }

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val orderDao = database.tshirtOrderDao()
                val now = System.currentTimeMillis()

                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "Carrier confirmed SMS sent for order #$orderIdString (part ${partIndex + 1}/$totalParts)")
                    orderDao.updateSmsStatus(
                        id = orderDbId,
                        smsStatus = SmsStatus.SENT,
                        errorMessage = null,
                        sentAt = now,
                        updatedAt = now
                    )
                } else {
                    val failureReason = when (resultCode) {
                        SmsManager.RESULT_ERROR_GENERIC_FAILURE ->
                            "সিম কার্ডে ব্যালেন্স নেই বা অপারেটর মেসেজ পাঠাতে বাধা দিয়েছে (Generic Failure)"
                        SmsManager.RESULT_ERROR_NO_SERVICE ->
                            "মোবাইল নেটওয়ার্ক কভারেজ নেই (No Service)"
                        SmsManager.RESULT_ERROR_RADIO_OFF ->
                            "মোবাইল রেডিও অফ / এয়ারপ্লেন মোড সক্রিয় (Radio Off)"
                        SmsManager.RESULT_ERROR_NULL_PDU ->
                            "মেসেজ ফরম্যাট ত্রুটি (Null PDU)"
                        else ->
                            "এসএমএস প্রেরণ ব্যর্থ হয়েছে (অপারেটর কোড: $resultCode)"
                    }

                    Log.w(TAG, "SMS carrier rejected order #$orderIdString: $failureReason")
                    orderDao.updateSmsStatus(
                        id = orderDbId,
                        smsStatus = SmsStatus.FAILED,
                        errorMessage = failureReason,
                        sentAt = null,
                        updatedAt = now
                    )
                }

                // Sync the latest SMS status to Firebase if available
                val updatedOrder = orderDao.getOrderByIdDirect(orderDbId)
                if (updatedOrder != null) {
                    try {
                        val syncManager = FirebaseSyncManager(context, orderDao)
                        syncManager.syncSingleOrder(updatedOrder)
                    } catch (e: Exception) {
                        Log.d(TAG, "Background sync after SMS status update skipped: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling SMS sent callback: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_SMS_SENT = "com.example.sms.SMS_SENT_ACTION"
        const val EXTRA_ORDER_DB_ID = "order_db_id"
        const val EXTRA_ORDER_ID_STRING = "order_id_string"
        const val EXTRA_PART_INDEX = "part_index"
        const val EXTRA_TOTAL_PARTS = "total_parts"
        private const val TAG = "SmsSentReceiver"
    }
}
