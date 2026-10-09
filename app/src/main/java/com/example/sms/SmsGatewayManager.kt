package com.example.sms

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.model.TShirtOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class SmsSendResult {
    object Success : SmsSendResult()
    data class Failure(val reason: String, val canFallbackToWhatsApp: Boolean = true) : SmsSendResult()
}

data class SimCardInfo(
    val subscriptionId: Int,
    val displayName: String,
    val carrierName: String,
    val simSlotIndex: Int
)

/**
 * Background SMS Gateway managing automated dispatch via native Android Telephony stack.
 * Strictly uses SmsManager API without opening system messaging apps or UI intents.
 * Supports multi-part text messages and explicit SIM routing via SubscriptionManager.
 */
object SmsGatewayManager {

    private const val TAG = "SmsGatewayManager"

    /**
     * Checks if SEND_SMS permission is currently granted.
     */
    fun hasSmsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if phone state permission is granted for reading SIM details.
     */
    fun hasPhoneStatePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Detects installed and active SIM cards (Dual-SIM routing support).
     */
    fun getAvailableSimCards(context: Context): List<SimCardInfo> {
        val list = mutableListOf<SimCardInfo>()
        try {
            val subManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
                ?: return emptyList()

            if (hasPhoneStatePermission(context)) {
                val activeList: List<SubscriptionInfo>? = subManager.activeSubscriptionInfoList
                activeList?.forEach { info ->
                    list.add(
                        SimCardInfo(
                            subscriptionId = info.subscriptionId,
                            displayName = info.displayName?.toString() ?: "SIM ${info.simSlotIndex + 1}",
                            carrierName = info.carrierName?.toString() ?: "Carrier",
                            simSlotIndex = info.simSlotIndex
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to read SIM cards: ${e.message}")
        }
        return list
    }

    /**
     * Formats official customer confirmation SMS in clear, polite Bengali with live tracking URL.
     */
    fun generateBengaliSmsMessage(order: TShirtOrder, storeName: String = "পোদ্দারপাড়া সর্বজনীন দুর্গা মন্দির"): String {
        val formattedAmount = if (order.totalAmount % 1.0 == 0.0) {
            order.totalAmount.toLong().toString()
        } else {
            String.format(java.util.Locale.US, "%.2f", order.totalAmount)
        }

        return """
        শুভ শারদীয়া!
        প্রিয় ${order.customerName.trim()},
        $storeName-এর উৎসবের টি-শার্ট অর্ডার #${order.orderId} সফলভাবে গৃহীত হয়েছে।
        মোট সংখ্যা: ${order.totalQuantity} টি
        মোট মূল্য: $formattedAmount টাকা

        লাইভ ট্র্যাকিং লিংক:
        ${order.trackingUrl}

        ধন্যবাদ!
        """.trimIndent()
    }

    /**
     * Dispatches the SMS confirmation completely in the background via native SmsManager.
     * Generates multipart SMS and binds explicit PendingIntent broadcast receivers for Sent
     * and Delivery reporting to update the database in real time.
     */
    suspend fun dispatchOrderSms(
        context: Context,
        order: TShirtOrder,
        storeName: String,
        selectedSubscriptionId: Int = -1
    ): SmsSendResult = withContext(Dispatchers.IO) {
        if (!hasSmsPermission(context)) {
            val error = "মোবাইলে SMS প্রেরণের পারমিশন দেওয়া নেই (SEND_SMS missing)"
            Log.e(TAG, error)
            return@withContext SmsSendResult.Failure(error)
        }

        val rawPhone = order.mobileNumber.replace(Regex("[^0-9+]"), "").trim()
        if (rawPhone.length < 10) {
            val error = "ভুল ফোন নম্বর: ${order.mobileNumber}"
            Log.e(TAG, error)
            return@withContext SmsSendResult.Failure(error)
        }

        val smsText = generateBengaliSmsMessage(order, storeName)

        try {
            val smsManager: SmsManager = getTargetSmsManager(context, selectedSubscriptionId)
            val messageParts = smsManager.divideMessage(smsText)
            val totalParts = messageParts.size

            Log.d(
                TAG,
                "Preparing background multipart SMS (${totalParts} parts) for order #${order.orderId} to $rawPhone via subId: $selectedSubscriptionId"
            )

            val sentIntents = ArrayList<PendingIntent>(totalParts)
            val deliveryIntents = ArrayList<PendingIntent>(totalParts)

            val baseRequestCode = (order.id.toInt() * 100).coerceAtLeast(1)

            for (i in 0 until totalParts) {
                // Sent PendingIntent targeting SmsSentReceiver
                val sentIntent = Intent(context, SmsSentReceiver::class.java).apply {
                    action = SmsSentReceiver.ACTION_SMS_SENT
                    putExtra(SmsSentReceiver.EXTRA_ORDER_DB_ID, order.id)
                    putExtra(SmsSentReceiver.EXTRA_ORDER_ID_STRING, order.orderId)
                    putExtra(SmsSentReceiver.EXTRA_PART_INDEX, i)
                    putExtra(SmsSentReceiver.EXTRA_TOTAL_PARTS, totalParts)
                    setPackage(context.packageName)
                }
                val sentPendingIntent = PendingIntent.getBroadcast(
                    context,
                    baseRequestCode + i,
                    sentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                sentIntents.add(sentPendingIntent)

                // Delivery PendingIntent targeting SmsDeliveredReceiver
                val deliveryIntent = Intent(context, SmsDeliveredReceiver::class.java).apply {
                    action = SmsDeliveredReceiver.ACTION_SMS_DELIVERED
                    putExtra(SmsDeliveredReceiver.EXTRA_ORDER_DB_ID, order.id)
                    putExtra(SmsDeliveredReceiver.EXTRA_ORDER_ID_STRING, order.orderId)
                    setPackage(context.packageName)
                }
                val deliveryPendingIntent = PendingIntent.getBroadcast(
                    context,
                    baseRequestCode + 50 + i,
                    deliveryIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                deliveryIntents.add(deliveryPendingIntent)
            }

            // Strictly call native SmsManager sendMultipartTextMessage API in background
            smsManager.sendMultipartTextMessage(
                rawPhone,
                null,
                messageParts,
                sentIntents,
                deliveryIntents
            )

            Log.i(TAG, "Multipart SMS handed over to Android Telephony stack for order #${order.orderId}")
            return@withContext SmsSendResult.Success

        } catch (e: Exception) {
            val errorMsg = e.message ?: "এসএমএস প্রেরণে ত্রুটি ঘটেছে"
            Log.e(TAG, "SMS dispatch exception for #${order.orderId}: $errorMsg", e)
            return@withContext SmsSendResult.Failure(errorMsg, canFallbackToWhatsApp = true)
        }
    }

    /**
     * Fallback UI share helper: Opens WhatsApp if user manually requests after SMS carrier failure.
     */
    fun openWhatsAppShare(context: Context, mobileNumber: String, message: String) {
        try {
            val digits = mobileNumber.replace(Regex("[^0-9]"), "")
            val internationalPhone = when {
                digits.startsWith("880") -> digits
                digits.startsWith("01") -> "88$digits"
                digits.length == 10 -> "880$digits"
                else -> digits
            }
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$internationalPhone&text=${Uri.encode(message)}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, message)
                type = "text/plain"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(sendIntent, "হোয়াটসঅ্যাপে শেয়ার করুন").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }

    /**
     * Fallback UI share helper: Opens system SMS composer if user manually wants to inspect.
     */
    fun openSystemSmsApp(context: Context, mobileNumber: String, message: String) {
        try {
            val uri = Uri.parse("smsto:${mobileNumber.trim()}")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, message)
                type = "text/plain"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(sendIntent, "মেসেজ পাঠান").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }

    /**
     * Resolves the target SmsManager based on the user's preferred subscriptionId.
     */
    @Suppress("DEPRECATION")
    fun getTargetSmsManager(context: Context, subId: Int): SmsManager {
        return if (subId >= 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val base = context.getSystemService(SmsManager::class.java)
                base.createForSubscriptionId(subId)
            } else {
                SmsManager.getSmsManagerForSubscriptionId(subId)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                SmsManager.getDefault()
            }
        }
    }
}
