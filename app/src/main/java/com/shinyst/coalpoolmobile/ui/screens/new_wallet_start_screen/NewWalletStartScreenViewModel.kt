package com.shinyst.coalpoolmobile.ui.screens.new_wallet_start_screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.shinyst.coalpoolmobile.CoalPoolMobileApplication
import com.shinyst.coalpoolmobile.data.repositories.IKeypairRepository
import com.shinyst.coalpoolmobile.data.repositories.IPoolRepository
import com.shinyst.coalpoolmobile.data.repositories.ISolanaRepository

class NewWalletStartScreenViewModel(
    application: CoalPoolMobileApplication,
    private val solanaRepository: ISolanaRepository,
    private val poolRepository: IPoolRepository,
    private val keypairRepository: IKeypairRepository,
) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CoalPoolMobileApplication)
                val solanaRepository = application.container.solanaRepository
                val poolRepository = application.container.poolRepository
                val keypairRepository = application.container.keypairRepository
                NewWalletStartScreenViewModel(
                    application = application,
                    solanaRepository = solanaRepository,
                    poolRepository = poolRepository,
                    keypairRepository = keypairRepository,
                )
            }
        }
    }
}