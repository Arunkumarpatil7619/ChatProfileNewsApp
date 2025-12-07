package com.assisment.newschatprofileapp.presentation.common

// common/ConnectivityObserver.kt


import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

interface ConnectivityObserver {
    fun observe(): Flow<Status>

    enum class Status {
        Available, Unavailable, Losing, Lost
    }
}

class NetworkConnectivityObserver @Inject constructor(
    private val context: Context
) : ConnectivityObserver {

    override fun observe(): Flow<ConnectivityObserver.Status> = callbackFlow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                trySend(ConnectivityObserver.Status.Available)
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                trySend(ConnectivityObserver.Status.Losing)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                trySend(ConnectivityObserver.Status.Lost)
            }

            override fun onUnavailable() {
                super.onUnavailable()
                trySend(ConnectivityObserver.Status.Unavailable)
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)

        // Send initial status
        val currentStatus = getCurrentConnectivityStatus(connectivityManager)
        trySend(currentStatus)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()

    private fun getCurrentConnectivityStatus(
        connectivityManager: ConnectivityManager
    ): ConnectivityObserver.Status {
        val network = connectivityManager.activeNetwork ?: return ConnectivityObserver.Status.Unavailable
        val capabilities = connectivityManager.getNetworkCapabilities(network)
            ?: return ConnectivityObserver.Status.Unavailable

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ->
                ConnectivityObserver.Status.Available
            else -> ConnectivityObserver.Status.Unavailable
        }
    }
}