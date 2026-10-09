package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Locale
import kotlin.random.Random

enum class OrderStatus(val displayName: String) {
    UNDER_PROCESSING("Under Processing"),
    PAID("Paid"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled")
}

enum class SyncStatus(val displayName: String) {
    SYNCED("Cloud Synced"),
    PENDING_SYNC("Pending Backup"),
    LOCAL_ONLY("Saved Locally"),
    SYNC_ERROR("Sync Error")
}

enum class SmsStatus(val displayName: String) {
    NOT_SENT("Pending Dispatch"),
    SENDING("Sending SMS..."),
    SENT("SMS Sent"),
    DELIVERED("SMS Delivered"),
    FAILED("SMS Failed")
}

@Entity(
    tableName = "tshirt_orders",
    indices = [Index(value = ["orderId"], unique = true)]
)
data class TShirtOrder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    // 6-digit alphanumeric unique identifier (e.g. 7K2B9X)
    val orderId: String = generateOrderId(),
    val customerName: String,
    val mobileNumber: String,
    val unitPrice: Double,
    // Quantities per size
    val qtyChild12: Int = 0, // Child 1-2 years
    val qtyS: Int = 0,
    val qtyM: Int = 0,
    val qtyL: Int = 0,
    val qtyXL: Int = 0,
    val qtyXXL: Int = 0,
    val qtyXXXL: Int = 0,
    // Totals
    val totalQuantity: Int = qtyChild12 + qtyS + qtyM + qtyL + qtyXL + qtyXXL + qtyXXXL,
    val totalAmount: Double = totalQuantity * unitPrice,
    // Tracking & Dispatch
    val trackingUrl: String = "https://podderpara.shop/?id=$orderId",
    // Status
    val status: OrderStatus = OrderStatus.UNDER_PROCESSING,
    val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC,
    val smsStatus: SmsStatus = SmsStatus.NOT_SENT,
    val smsErrorMessage: String? = null,
    val smsSentAt: Long? = null,
    val firebaseId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val note: String = ""
) {
    // Legacy support alias if needed
    val serialNumber: String get() = orderId

    fun formattedSerial(): String {
        return "#$orderId"
    }

    fun hasAnyItems(): Boolean = totalQuantity > 0

    fun sizeSummaryString(): String {
        val parts = mutableListOf<String>()
        if (qtyChild12 > 0) parts.add("Child 1-2y: $qtyChild12")
        if (qtyS > 0) parts.add("S: $qtyS")
        if (qtyM > 0) parts.add("M: $qtyM")
        if (qtyL > 0) parts.add("L: $qtyL")
        if (qtyXL > 0) parts.add("XL: $qtyXL")
        if (qtyXXL > 0) parts.add("2XL: $qtyXXL")
        if (qtyXXXL > 0) parts.add("3XL: $qtyXXXL")
        return if (parts.isEmpty()) "No items" else parts.joinToString(", ")
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "orderId" to orderId,
            "serialNumber" to orderId,
            "customerName" to customerName,
            "mobileNumber" to mobileNumber,
            "unitPrice" to unitPrice,
            "totalQuantity" to totalQuantity,
            "totalAmount" to totalAmount,
            "trackingUrl" to trackingUrl,
            "status" to status.name,
            "smsStatus" to smsStatus.name,
            "smsErrorMessage" to smsErrorMessage,
            "smsSentAt" to smsSentAt,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "sizes" to mapOf(
                "qtyChild12" to qtyChild12,
                "qtyS" to qtyS,
                "qtyM" to qtyM,
                "qtyL" to qtyL,
                "qtyXL" to qtyXL,
                "qtyXXL" to qtyXXL,
                "qtyXXXL" to qtyXXXL
            )
        )
    }

    companion object {
        // Unambiguous Base32 charset (excluding easily confused 0/O, 1/I)
        const val ALPHANUMERIC_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
        private val WEIGHTS_1 = intArrayOf(3, 7, 13, 19, 23)
        private val WEIGHTS_2 = intArrayOf(29, 17, 11, 5, 2)

        /**
         * Generates a 7-character mathematically verified Order ID:
         * First 5 characters: Random data payload from ALPHANUMERIC_CHARS (32^5 = 33.5M permutations)
         * Last 2 characters: Mathematical dual-check digits (modulo 32 weighted sums)
         */
        fun generateOrderId(): String {
            val dataChars = CharArray(5) {
                ALPHANUMERIC_CHARS[Random.nextInt(ALPHANUMERIC_CHARS.length)]
            }
            var sum1 = 0
            var sum2 = 0
            for (i in 0 until 5) {
                val index = ALPHANUMERIC_CHARS.indexOf(dataChars[i])
                sum1 += index * WEIGHTS_1[i]
                sum2 += index * WEIGHTS_2[i]
            }
            val c1 = (sum1 % 32)
            val c2 = ((sum2 + c1 * 31) % 32)
            return "${String(dataChars)}${ALPHANUMERIC_CHARS[c1]}${ALPHANUMERIC_CHARS[c2]}"
        }

        /**
         * Validates whether an Order ID has valid mathematical check digits.
         * Enables tracking website to verify authentic store tracking links even before cloud sync.
         */
        fun isValidOrderId(orderId: String): Boolean {
            val clean = orderId.trim().uppercase(Locale.ROOT)
            if (clean.length != 7) return false
            for (ch in clean) {
                if (ch !in ALPHANUMERIC_CHARS) return false
            }
            var sum1 = 0
            var sum2 = 0
            for (i in 0 until 5) {
                val index = ALPHANUMERIC_CHARS.indexOf(clean[i])
                if (index == -1) return false
                sum1 += index * WEIGHTS_1[i]
                sum2 += index * WEIGHTS_2[i]
            }
            val c1 = (sum1 % 32)
            val c2 = ((sum2 + c1 * 31) % 32)
            return clean[5] == ALPHANUMERIC_CHARS[c1] && clean[6] == ALPHANUMERIC_CHARS[c2]
        }

        /**
         * Deserializes an order map directly from Firebase Realtime Database or Cloud Firestore.
         */
        fun fromMap(map: Map<String, Any?>): TShirtOrder? {
            return try {
                val orderId = (map["orderId"] as? String)
                    ?: (map["serialNumber"] as? String)
                    ?: return null

                val customerName = (map["customerName"] as? String) ?: ""
                val mobileNumber = (map["mobileNumber"] as? String) ?: ""
                val unitPrice = (map["unitPrice"] as? Number)?.toDouble() ?: 250.0

                @Suppress("UNCHECKED_CAST")
                val sizesMap = map["sizes"] as? Map<String, Any?>
                val qtyChild12 = (map["qtyChild12"] as? Number)?.toInt()
                    ?: (sizesMap?.get("qtyChild12") as? Number)?.toInt() ?: 0
                val qtyS = (map["qtyS"] as? Number)?.toInt()
                    ?: (sizesMap?.get("qtyS") as? Number)?.toInt() ?: 0
                val qtyM = (map["qtyM"] as? Number)?.toInt()
                    ?: (sizesMap?.get("qtyM") as? Number)?.toInt() ?: 0
                val qtyL = (map["qtyL"] as? Number)?.toInt()
                    ?: (sizesMap?.get("qtyL") as? Number)?.toInt() ?: 0
                val qtyXL = (map["qtyXL"] as? Number)?.toInt()
                    ?: (sizesMap?.get("qtyXL") as? Number)?.toInt() ?: 0
                val qtyXXL = (map["qtyXXL"] as? Number)?.toInt()
                    ?: (sizesMap?.get("qtyXXL") as? Number)?.toInt() ?: 0
                val qtyXXXL = (map["qtyXXXL"] as? Number)?.toInt()
                    ?: (sizesMap?.get("qtyXXXL") as? Number)?.toInt() ?: 0

                val statusStr = (map["status"] as? String) ?: OrderStatus.UNDER_PROCESSING.name
                val status = try {
                    OrderStatus.valueOf(statusStr)
                } catch (_: Exception) {
                    OrderStatus.UNDER_PROCESSING
                }

                val smsStatusStr = (map["smsStatus"] as? String) ?: SmsStatus.NOT_SENT.name
                val smsStatus = try {
                    SmsStatus.valueOf(smsStatusStr)
                } catch (_: Exception) {
                    SmsStatus.NOT_SENT
                }

                val smsErrorMessage = map["smsErrorMessage"] as? String
                val smsSentAt = (map["smsSentAt"] as? Number)?.toLong()
                val createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                val updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                val trackingUrl = (map["trackingUrl"] as? String) ?: "https://podderpara.shop/?id=$orderId"
                val note = (map["note"] as? String) ?: ""

                TShirtOrder(
                    id = 0,
                    orderId = orderId,
                    customerName = customerName,
                    mobileNumber = mobileNumber,
                    unitPrice = unitPrice,
                    qtyChild12 = qtyChild12,
                    qtyS = qtyS,
                    qtyM = qtyM,
                    qtyL = qtyL,
                    qtyXL = qtyXL,
                    qtyXXL = qtyXXL,
                    qtyXXXL = qtyXXXL,
                    trackingUrl = trackingUrl,
                    status = status,
                    syncStatus = SyncStatus.SYNCED,
                    smsStatus = smsStatus,
                    smsErrorMessage = smsErrorMessage,
                    smsSentAt = smsSentAt,
                    firebaseId = orderId,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                    note = note
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}

data class OrderDataSheetSummary(
    val totalOrders: Int,
    val totalTShirts: Int,
    val grandTotalAmount: Double,
    val totalChild12: Int,
    val totalS: Int,
    val totalM: Int,
    val totalL: Int,
    val totalXL: Int,
    val totalXXL: Int,
    val totalXXXL: Int,
    val underProcessingCount: Int,
    val underProcessingAmount: Double,
    val paidCount: Int,
    val paidAmount: Double,
    val deliveredCount: Int,
    val deliveredAmount: Double,
    val cancelledCount: Int,
    val cancelledAmount: Double,
    val smsPendingCount: Int = 0,
    val smsSentCount: Int = 0,
    val smsDeliveredCount: Int = 0,
    val smsFailedCount: Int = 0,
    val cloudSyncedCount: Int = 0,
    val cloudPendingCount: Int = 0
)
