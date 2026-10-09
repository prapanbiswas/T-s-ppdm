package com.example.sync

import android.content.Context
import android.util.Log
import com.example.data.local.TShirtOrderDao
import com.example.data.model.SyncStatus
import com.example.data.model.TShirtOrder
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Success(val count: Int, val timestamp: Long = System.currentTimeMillis()) : SyncState()
    data class Error(val message: String) : SyncState()
}

object FirebaseConfig {
    const val API_KEY = "AIzaSyBkvOLHK-_RmpAR_6dlVYiMXXnKaNGcyS0"
    const val PROJECT_ID = "ppdm-t-shirt-app"
    const val DATABASE_URL = "https://ppdm-t-shirt-app-default-rtdb.asia-southeast1.firebasedatabase.app"
    const val STORAGE_BUCKET = "ppdm-t-shirt-app.firebasestorage.app"
    const val SENDER_ID = "573732006391"
    const val APP_ID = "1:573732006391:android:caf53732aa422c97c666f2"
}

class FirebaseSyncManager(
    private val context: Context,
    private val orderDao: TShirtOrderDao
) {
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private var realtimeListenerAttached = false

    init {
        initDedicatedFirebaseApp()
    }

    private fun initDedicatedFirebaseApp(): FirebaseApp? {
        return try {
            val existing = FirebaseApp.getApps(context).find { it.name == "ppdmApp" }
            if (existing != null) return existing

            val options = FirebaseOptions.Builder()
                .setApiKey(FirebaseConfig.API_KEY)
                .setApplicationId(FirebaseConfig.APP_ID)
                .setProjectId(FirebaseConfig.PROJECT_ID)
                .setDatabaseUrl(FirebaseConfig.DATABASE_URL)
                .setStorageBucket(FirebaseConfig.STORAGE_BUCKET)
                .setGcmSenderId(FirebaseConfig.SENDER_ID)
                .build()

            FirebaseApp.initializeApp(context, options, "ppdmApp")
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Error initializing ppdmApp", e)
            null
        }
    }

    private fun getRealtimeDatabase(): FirebaseDatabase? {
        return try {
            val app = initDedicatedFirebaseApp()
            if (app != null) {
                FirebaseDatabase.getInstance(app, FirebaseConfig.DATABASE_URL)
            } else {
                FirebaseDatabase.getInstance(FirebaseConfig.DATABASE_URL)
            }
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Failed to obtain Realtime Database instance", e)
            null
        }
    }

    val isFirebaseAvailable: Boolean
        get() = FirebaseApp.getApps(context).isNotEmpty()

    /**
     * Starts continuous cloud synchronization with Firebase Realtime Database.
     * Firebase Realtime Database is the SOVEREIGN SOURCE OF TRUTH.
     * When local database is empty (e.g. after fresh install/reinstall),
     * this pulls all existing orders from the cloud and restores them to Room.
     * It NEVER deletes online data when the local database is empty.
     */
    fun startRealtimeCloudSync(scope: CoroutineScope) {
        if (!isFirebaseAvailable || realtimeListenerAttached) return
        val db = getRealtimeDatabase() ?: return
        realtimeListenerAttached = true

        val ordersRef = db.getReference("orders")

        // Safe cleanup: purge legacy duplicate 'order/' node if present
        scope.launch(Dispatchers.IO) {
            try {
                db.getReference("order").removeValue().await()
                Log.d("FirebaseSyncManager", "Purged deprecated legacy '/order' path in RTDB")
            } catch (e: Exception) {
                Log.w("FirebaseSyncManager", "Non-fatal legacy purge notice: ${e.message}")
            }
        }

        ordersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(Dispatchers.IO) {
                    try {
                        var restoredCount = 0
                        for (child in snapshot.children) {
                            val value = child.value
                            if (value is Map<*, *>) {
                                @Suppress("UNCHECKED_CAST")
                                val orderMap = value as Map<String, Any?>
                                val order = TShirtOrder.fromMap(orderMap)
                                if (order != null) {
                                    orderDao.upsertByOrderId(order)
                                    restoredCount++
                                }
                            }
                        }
                        if (restoredCount > 0) {
                            Log.d("FirebaseSyncManager", "Source of Truth restored $restoredCount orders from Firebase 'orders/'")
                        }
                    } catch (e: Exception) {
                        Log.e("FirebaseSyncManager", "Error restoring orders from Firebase", e)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("FirebaseSyncManager", "Realtime cloud listener cancelled: ${error.message}")
            }
        })
    }

    /**
     * One-time explicit pull of all orders from Firebase Realtime Database to local Room DB.
     */
    suspend fun pullOrdersFromCloudOnce(): Int = withContext(Dispatchers.IO) {
        if (!isFirebaseAvailable) return@withContext 0
        val db = getRealtimeDatabase() ?: return@withContext 0
        try {
            val snapshot = db.getReference("orders").get().await()
            var count = 0
            for (child in snapshot.children) {
                val value = child.value
                if (value is Map<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    val orderMap = value as Map<String, Any?>
                    val order = TShirtOrder.fromMap(orderMap)
                    if (order != null) {
                        orderDao.upsertByOrderId(order)
                        count++
                    }
                }
            }
            count
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Error in pullOrdersFromCloudOnce: ${e.message}")
            0
        }
    }

    /**
     * Pushes a single order to Firebase Realtime Database and Cloud Firestore under root 'orders/'.
     * Updates the local Room record with SyncStatus.
     */
    suspend fun syncSingleOrder(order: TShirtOrder): Boolean = withContext(Dispatchers.IO) {
        if (!isFirebaseAvailable) {
            Log.d("FirebaseSyncManager", "Firebase not available. Order kept in local Room database.")
            orderDao.updateSyncStatus(
                id = order.id,
                syncStatus = SyncStatus.LOCAL_ONLY,
                firebaseId = null,
                updatedAt = System.currentTimeMillis()
            )
            return@withContext false
        }

        try {
            val orderMap = order.toMap().toMutableMap()
            orderMap["currency"] = "BDT"
            val orderKey = order.orderId.ifEmpty { "ORDER_${order.id}" }

            var rtdbSuccess = false
            var firestoreSuccess = false

            // 1. Sync strictly to single root path: 'orders/{orderKey}'
            try {
                val db = getRealtimeDatabase()
                if (db != null) {
                    val orderNodeRef = db.getReference("orders").child(orderKey)
                    orderNodeRef.setValue(orderMap).await()
                    rtdbSuccess = true
                    Log.d("FirebaseSyncManager", "Successfully pushed order $orderKey to Realtime Database at /orders/$orderKey")
                }
            } catch (e: Exception) {
                Log.w("FirebaseSyncManager", "Realtime Database push failed: ${e.message}")
            }

            // 2. Sync to Cloud Firestore under single collection: 'orders'
            try {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("orders")
                    .document(orderKey)
                    .set(orderMap, SetOptions.merge())
                    .await()
                firestoreSuccess = true
                Log.d("FirebaseSyncManager", "Successfully pushed order $orderKey to Firestore collection 'orders'")
            } catch (e: Exception) {
                Log.w("FirebaseSyncManager", "Firestore push failed: ${e.message}")
            }

            if (rtdbSuccess || firestoreSuccess) {
                orderDao.updateSyncStatus(
                    id = order.id,
                    syncStatus = SyncStatus.SYNCED,
                    firebaseId = orderKey,
                    updatedAt = System.currentTimeMillis()
                )
                return@withContext true
            } else {
                orderDao.updateSyncStatus(
                    id = order.id,
                    syncStatus = SyncStatus.SYNC_ERROR,
                    firebaseId = null,
                    updatedAt = System.currentTimeMillis()
                )
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Error syncing order ${order.orderId}", e)
            orderDao.updateSyncStatus(
                id = order.id,
                syncStatus = SyncStatus.SYNC_ERROR,
                firebaseId = null,
                updatedAt = System.currentTimeMillis()
            )
            return@withContext false
        }
    }

    /**
     * Batch syncs all pending offline orders from Room database to Firebase
     */
    suspend fun syncPendingOrders(): Int = withContext(Dispatchers.IO) {
        val pendingOrders = orderDao.getPendingSyncOrders()
        if (pendingOrders.isEmpty()) {
            _syncState.value = SyncState.Idle
            return@withContext 0
        }

        _syncState.value = SyncState.Syncing

        if (!isFirebaseAvailable) {
            _syncState.value = SyncState.Error("Firebase not ready. Data safely kept in local Room DB.")
            return@withContext 0
        }

        var successCount = 0
        for (order in pendingOrders) {
            val synced = syncSingleOrder(order)
            if (synced) successCount++
        }

        if (successCount > 0) {
            _syncState.value = SyncState.Success(successCount)
        } else {
            _syncState.value = SyncState.Error("Failed to sync pending orders to Firebase")
        }
        return@withContext successCount
    }

    /**
     * Removes an order from Firebase Realtime Database and Firestore ONLY when explicitly deleted by the user.
     */
    suspend fun deleteOrderFromFirebase(orderKey: String) = withContext(Dispatchers.IO) {
        if (!isFirebaseAvailable) return@withContext
        try {
            val db = getRealtimeDatabase()
            db?.getReference("orders")?.child(orderKey)?.removeValue()?.await()
            // Clean up any lingering legacy path entry
            db?.getReference("order")?.child(orderKey)?.removeValue()?.await()
        } catch (e: Exception) {
            Log.w("FirebaseSyncManager", "Failed to remove from RTDB: ${e.message}")
        }
        try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("orders").document(orderKey).delete().await()
            firestore.collection("order").document(orderKey).delete().await()
        } catch (e: Exception) {
            Log.w("FirebaseSyncManager", "Failed to remove from Firestore: ${e.message}")
        }
    }
}
