package com.example.sms

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.data.local.AppDatabase
import com.example.data.model.SmsStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Background worker that reliably dispatches customer order confirmation SMS
 * with order details, total BDT value, and tracking link in Bengali.
 */
class OrderSmsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val orderDbId = inputData.getLong(KEY_ORDER_DB_ID, -1L)
        if (orderDbId <= 0) {
            Log.e(TAG, "OrderSmsWorker aborted: Invalid order ID ($orderDbId)")
            return@withContext Result.failure()
        }

        val database = AppDatabase.getDatabase(applicationContext)
        val orderDao = database.tshirtOrderDao()
        val order = orderDao.getOrderByIdDirect(orderDbId)

        if (order == null) {
            Log.e(TAG, "OrderSmsWorker aborted: Order #$orderDbId not found in local Room database.")
            return@withContext Result.failure()
        }

        // Read user-configured store details and SIM preference from SharedPreferences
        val prefs = applicationContext.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)
        val storeName = prefs.getString("store_name", "পোদ্দারপাড়া সর্বজনীন দুর্গা মন্দির") ?: "পোদ্দারপাড়া সর্বজনীন দুর্গা মন্দির"
        val selectedSimId = prefs.getInt("selected_sim_id", -1)

        Log.d(TAG, "Worker waking up for Order #${order.orderId}, recipient: ${order.mobileNumber}, SIM Sub: $selectedSimId")

        // Mark as SENDING
        orderDao.updateSmsStatus(
            id = order.id,
            smsStatus = SmsStatus.SENDING,
            errorMessage = null,
            sentAt = null,
            updatedAt = System.currentTimeMillis()
        )

        // Dispatch via SmsGatewayManager
        val result = SmsGatewayManager.dispatchOrderSms(
            context = applicationContext,
            order = order,
            storeName = storeName,
            selectedSubscriptionId = selectedSimId
        )

        return@withContext when (result) {
            is SmsSendResult.Success -> {
                Log.i(TAG, "Worker successfully handed SMS to SmsManager with carrier listeners for order #${order.orderId}")
                // SmsSentReceiver will receive carrier acknowledgement and update to SENT or FAILED
                Result.success()
            }
            is SmsSendResult.Failure -> {
                Log.w(TAG, "Worker failed to dispatch SMS for order #${order.orderId}: ${result.reason}")
                orderDao.updateSmsStatus(
                    id = order.id,
                    smsStatus = SmsStatus.FAILED,
                    errorMessage = result.reason,
                    sentAt = null,
                    updatedAt = System.currentTimeMillis()
                )
                Result.failure(workDataOf("error" to result.reason))
            }
        }
    }

    companion object {
        private const val TAG = "OrderSmsWorker"
        const val KEY_ORDER_DB_ID = "order_db_id"

        /**
         * Enqueues the background worker to dispatch the order confirmation SMS.
         */
        fun enqueue(context: Context, orderDbId: Long) {
            val inputData = Data.Builder()
                .putLong(KEY_ORDER_DB_ID, orderDbId)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<OrderSmsWorker>()
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "sms_dispatch_order_$orderDbId",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            Log.d(TAG, "Enqueued background OrderSmsWorker for order #$orderDbId")
        }
    }
}
