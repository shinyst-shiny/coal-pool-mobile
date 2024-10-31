package com.shinyst.coalpoolmobile

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.shinyst.coalpoolmobile.data.repositories.KeypairRepository
import com.shinyst.coalpoolmobile.service.CoalPoolMobileForegroundService
import com.shinyst.coalpoolmobile.ui.CoalPoolMobileApp
import com.shinyst.coalpoolmobile.ui.screens.home_screen.HomeScreenViewModel
import com.shinyst.coalpoolmobile.ui.theme.CoalPoolMobileTheme
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.ConnectionIdentity
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.Solana
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var homeScreenViewModel: HomeScreenViewModel
    private var CoalPoolMobileService: CoalPoolMobileForegroundService? = null

    private var serviceBoundState by mutableStateOf(false)

    private var powerSlider by mutableStateOf(0f)
    private var threadCount by mutableStateOf<Int>(1)
    private var hashpower by mutableStateOf<UInt>(0u)
    private var difficulty by mutableStateOf<UInt>(0u)

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d(TAG, "onServiceConnected")

            val binder = service as CoalPoolMobileForegroundService.LocalBinder
            CoalPoolMobileService = binder.getService()

            if (powerSlider > 0f) {
                CoalPoolMobileService?.updateSelectedThreads(powerSlider.toInt())
            } else {
                powerSlider = CoalPoolMobileService?.powerSlider ?: 0f
            }
            serviceBoundState = true

            onServiceConnected()
        }

        override fun onServiceDisconnected(arg0: ComponentName?) {
            Log.d(TAG, "onServiceDisconnected")

            serviceBoundState = false
            CoalPoolMobileService = null
        }
    }

    // we need notification permission to be able to display a notification for the foreground service
    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            // if permission was denied, the service can still run only the notification won't be visible
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()

        val keypairRepository = KeypairRepository(this);

        val hasEncryptedKeypair = keypairRepository.encryptedKeypairExists();

        val sender = ActivityResultSender(this)

        homeScreenViewModel = ViewModelProvider(this, HomeScreenViewModel.Factory)[HomeScreenViewModel::class.java]

        setContent {
            CoalPoolMobileTheme(true) {
                CoalPoolMobileApp(
                    sender,
                    hasEncryptedKeypair,
                    threadCount = threadCount,
                    hashpower = hashpower,
                    difficulty = difficulty,
                    powerSlider = powerSlider,
                    setPowerSlider = ::onSetPowerSlider,
                    CoalPoolMobileService,
                    onClickService = ::onStartOrStopForegroundServiceClick,
                    serviceRunning = serviceBoundState,
                    homeScreenViewModel
                )
            }
        }

        checkAndRequestNotificationPermission()
        tryToBindToServiceIfRunning()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }

    private fun onSetPowerSlider(newValue: Float) {
        powerSlider = newValue
    }

    /**
     * Check for notification permission before starting the service so that the notification is visible
     */
    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)) {
                android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                    // permission already granted
                }

                else -> {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun onStartOrStopForegroundServiceClick() {
        if (CoalPoolMobileService == null) {
            startForegroundService()
        } else {
            // service is already running, stop it
            CoalPoolMobileService?.stopForegroundService()
        }
    }

    /**
     * Creates and starts the CoalPoolMobileForgroundService as a foreground service.
     *
     * It also tries to bind to the service to update the UI with location updates.
     */
    private fun startForegroundService() {
        // start the service
        startForegroundService(Intent(this, CoalPoolMobileForegroundService::class.java))

        // bind to the service to update UI
        tryToBindToServiceIfRunning()
    }

    private fun tryToBindToServiceIfRunning() {
        Intent(this, CoalPoolMobileForegroundService::class.java).also { intent ->
            bindService(intent, connection, 0)
        }
    }

    private fun onServiceConnected() {
        lifecycleScope.launch {
            CoalPoolMobileService?.threadCount?.collectLatest { it ->
                threadCount = it
            }
        }
        lifecycleScope.launch {
            CoalPoolMobileService?.hashpower?.collectLatest { it ->
                hashpower = it
            }
        }
        lifecycleScope.launch {
            CoalPoolMobileService?.difficulty?.collectLatest { it ->
                difficulty = it
            }
        }
        lifecycleScope.launch {
            CoalPoolMobileService?.poolBalance?.collectLatest { it ->
                if (it > 0) {
                    homeScreenViewModel.setPoolbalance(it)
                }
            }
        }
        lifecycleScope.launch {
            CoalPoolMobileService?.topStake?.collectLatest { it ->
                if (it > 0) {
                    homeScreenViewModel.setTopStake(it)
                }
            }
        }
        lifecycleScope.launch {
            CoalPoolMobileService?.poolMultiplier?.collectLatest { it ->
                if (it > 0) {
                    homeScreenViewModel.setPoolMultiplier(it)
                }
            }
        }
        lifecycleScope.launch {
            CoalPoolMobileService?.activeMiners?.collectLatest { it ->
                if (it > 0u) {
                    homeScreenViewModel.setActiveMiners(it.toInt())
                }
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
