package com.example.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

sealed class NetworkStatus {
    object Checking : NetworkStatus()
    data class Online(val lastCheckedAt: Long = System.currentTimeMillis()) : NetworkStatus()
    data class Offline(val reason: String = "No Internet Connection") : NetworkStatus()
}

class NetworkMonitor(context: Context) {
    private val appContext = context.applicationContext
    private val connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _status = MutableStateFlow<NetworkStatus>(NetworkStatus.Checking)
    val status: StateFlow<NetworkStatus> = _status.asStateFlow()

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // Event fired whenever internet is restored/verified online
    private val _onInternetRestored = MutableSharedFlow<Unit>(replay = 0)
    val onInternetRestored: SharedFlow<Unit> = _onInternetRestored.asSharedFlow()

    private var reachabilityJob: Job? = null

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            triggerReachabilityCheck("Network connection detected")
        }

        override fun onLost(network: Network) {
            _status.value = NetworkStatus.Offline("Connection lost")
            _isOnline.value = false
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            val hasInternetCap = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            val isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            if (hasInternetCap) {
                triggerReachabilityCheck(if (isValidated) "Network validated" else "Verifying actual server reachability...")
            } else {
                _status.value = NetworkStatus.Offline("No internet capability")
                _isOnline.value = false
            }
        }
    }

    init {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        try {
            connectivityManager.registerNetworkCallback(request, networkCallback)
        } catch (e: Exception) {
            Log.e("NetworkMonitor", "Error registering network callback", e)
        }

        // Initial check
        triggerReachabilityCheck("Initial check")

        // Periodic background poll every 30 seconds to guarantee freshness
        scope.launch {
            while (isActive) {
                delay(30_000)
                checkRealReachability()
            }
        }
    }

    fun triggerReachabilityCheck(reason: String = "") {
        reachabilityJob?.cancel()
        reachabilityJob = scope.launch {
            _status.value = NetworkStatus.Checking
            val reachable = checkRealReachability()
            val wasOnline = _isOnline.value
            _isOnline.value = reachable
            if (reachable) {
                _status.value = NetworkStatus.Online()
                if (!wasOnline) {
                    _onInternetRestored.emit(Unit)
                }
            } else {
                _status.value = NetworkStatus.Offline("Cannot reach verification server (No true internet)")
            }
        }
    }

    /**
     * Actively tests real internet reachability using Google's generate_204 endpoint
     * and fallback to Cloudflare trace endpoint.
     */
    suspend fun checkRealReachability(): Boolean = withContext(Dispatchers.IO) {
        val activeNetwork = connectivityManager.activeNetwork
        if (activeNetwork == null) {
            return@withContext false
        }
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
        if (caps == null || !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            return@withContext false
        }

        // 1. Primary check: Google generate_204 (Industry standard captive portal / internet reachability check)
        try {
            val url = URL("https://www.google.com/generate_204")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 3000
                instanceFollowRedirects = false
                useCaches = false
                requestMethod = "GET"
            }
            connection.connect()
            val responseCode = connection.responseCode
            connection.disconnect()
            if (responseCode == 204 || responseCode == 200) {
                return@withContext true
            }
        } catch (e: Exception) {
            Log.d("NetworkMonitor", "Google 204 reachability probe failed: ${e.message}")
        }

        // 2. Secondary fallback check: Cloudflare
        try {
            val url = URL("https://1.1.1.1")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 3000
                instanceFollowRedirects = false
                useCaches = false
                requestMethod = "HEAD"
            }
            connection.connect()
            val responseCode = connection.responseCode
            connection.disconnect()
            if (responseCode in 200..399) {
                return@withContext true
            }
        } catch (e: Exception) {
            Log.d("NetworkMonitor", "Cloudflare reachability probe failed: ${e.message}")
        }

        return@withContext false
    }
}
