package com.example.orehqmobile.ui

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.orehqmobile.service.OreHQMobileForegroundService
import com.example.orehqmobile.ui.screens.home_screen.HomeScreen
import com.example.orehqmobile.ui.screens.home_screen.HomeScreenViewModel
import com.example.orehqmobile.ui.screens.new_created_wallet_screen.CreatedWalletScreen
import com.example.orehqmobile.ui.screens.new_created_wallet_screen.CreatedWalletScreenViewModel
import com.example.orehqmobile.ui.screens.new_wallet_start_screen.NewWalletScreen
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender

@Composable
fun OreHQMobileApp(
    activity_sender: ActivityResultSender,
    hasEncryptedKey: Boolean = false,
    threadCount: Int,
    hashpower: UInt,
    difficulty: UInt,
    lastDifficulty: UInt,
    oreHqMobileService: OreHQMobileForegroundService? = null,
    onClickService: () -> Unit,
    serviceRunning: Boolean,
    homeScreenViewModel: HomeScreenViewModel = viewModel(factory = HomeScreenViewModel.Factory),
    createdWalletScreenViewModel: CreatedWalletScreenViewModel = viewModel(factory = CreatedWalletScreenViewModel.Factory),
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val startDestination = if(hasEncryptedKey) {
        homeScreenViewModel.fetchUiState()
        "homeScreen"
    } else {
        "newWalletStartScreen"
    }

    NavHost(navController, startDestination) {
        composable("homeScreen") {
            HomeScreen(
                homeUiState = homeScreenViewModel.homeUiState,
                serviceRunning = serviceRunning,
                threadCount = threadCount,
                hashpower = hashpower,
                difficulty = difficulty,
                lastDifficulty = lastDifficulty,
                onIncreaseSelectedThreads = { oreHqMobileService?.increaseSelectedThreads() },
                onDecreaseSelectedThreads = { oreHqMobileService?.decreaseSelectedThreads() },
                onToggleMining = onClickService,
                onClickSignup = { homeScreenViewModel.signUpClicked(activity_sender) },
                onClickConnectWallet = { homeScreenViewModel.connectSecureWallet(activity_sender) },
                onClickDisconnectWallet = { homeScreenViewModel.disconnectSecureWallet(activity_sender) },
                onClickClaim = { homeScreenViewModel.onClaimClicked() },
            )
        }
        composable("newWalletStartScreen") {
            NewWalletScreen(
                onButtonGenerate = {
                    createdWalletScreenViewModel.generateNewWallet()
                    navController.navigate("createdWalletScreen")
                }
            )
        }
        composable("createdWalletScreen") {
            CreatedWalletScreen(
                createdWalletScreenState = createdWalletScreenViewModel.createdWalletScreenState,
                onClickContinue = {
                    createdWalletScreenViewModel.saveWallet()
                    homeScreenViewModel.fetchUiState()
                    navController.navigate("homeScreen")
                }
            )
        }
    }
}