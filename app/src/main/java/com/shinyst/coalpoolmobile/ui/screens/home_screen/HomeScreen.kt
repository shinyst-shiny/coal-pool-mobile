package com.shinyst.coalpoolmobile.ui.screens.home_screen

import android.widget.ScrollView
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.ui.text.TextStyle
import com.shinyst.coalpoolmobile.data.daos.SubmissionResultDao
import com.shinyst.coalpoolmobile.data.entities.SubmissionResult
import com.shinyst.coalpoolmobile.ui.screens.CoalPoolMobileScaffold
import com.shinyst.coalpoolmobile.ui.theme.CoalPoolMobileTheme
import java.util.Calendar
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    homeUiState: HomeUiState,
    serviceRunning: Boolean,
    threadCount: Int,
    hashpower: UInt,
    difficulty: UInt,
    powerSlider: Float,
    setPowerSlider: (Float) -> Unit,
    onToggleMining: () -> Unit,
    onClickSignup: () -> Unit,
    onClickConnectWallet: () -> Unit,
    onClickDisconnectWallet: () -> Unit,
    onClickClaim: () -> Unit,
    onUpdateSelectedThreads: (Int) -> Unit,
) {
    CoalPoolMobileScaffold(title = "Home", displayTopBar = true) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (homeUiState.isLoadingUi) {
                CircularProgressIndicator()
            } else {
                if (homeUiState.isSignedUp && homeUiState.secureWalletPubkey != null) {
                    MiningScreen(
                        homeUiState = homeUiState,
                        serviceRunning = serviceRunning,
                        threadCount = threadCount,
                        hashpower = hashpower,
                        difficulty = difficulty,
                        powerSlider = powerSlider,
                        setPowerSlider = setPowerSlider,
                        onToggleMining = onToggleMining,
                        onClickClaim = onClickClaim,
                        onUpdateSelectedThreads = onUpdateSelectedThreads,
                    )
                } else {
                    SignUpScreen(
                        homeUiState = homeUiState,
                        onClickSignUp = onClickSignup,
                        onClickConnectWallet = onClickConnectWallet,
                        onClickDisconnectWallet = onClickDisconnectWallet,
                    )
                }

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    CoalPoolMobileTheme(darkTheme = true) {
        HomeScreen(
            HomeUiState(
                availableThreads = 1,
                hashRate = 0u,
                difficulty = 0u,
                selectedThreads =  1,
                claimableBalanceCoal = 0.0,
                claimableBalanceOre = 0.0,
                solBalance = 0.0,
                walletTokenBalanceCoal = 0.0,
                walletTokenBalanceOre = 0.0,
                activeMiners = 0,
                poolBalanceCoal = 0.0,
                poolBalanceOre = 0.0,
                isSignedUp = true,
                isProcessingSignup = false,
                isLoadingUi = false,
                secureWalletPubkey = "",
                minerPubkey = "",
                submissionResults = emptyList()
            ),
            serviceRunning = false,
            threadCount = 1,
            hashpower = 0u,
            difficulty = 0u,
            powerSlider = 4f,
            setPowerSlider = {},
            onToggleMining = {},
            onClickSignup = {},
            onClickConnectWallet = {},
            onClickDisconnectWallet = {},
            onClickClaim = {},
            onUpdateSelectedThreads = {},
        )
    }
}

@Composable
fun MiningScreen(
    homeUiState: HomeUiState,
    serviceRunning: Boolean,
    threadCount: Int,
    hashpower: UInt,
    powerSlider: Float,
    difficulty: UInt,
    setPowerSlider: (Float) -> Unit,
    onToggleMining: () -> Unit,
    onClickClaim: () -> Unit,
    onUpdateSelectedThreads: (Int) -> Unit,
) {
    val difficulty = difficulty
    val availableThreads = homeUiState.availableThreads
    val claimableBalanceCoal = homeUiState.claimableBalanceCoal
    val claimableBalanceOre = homeUiState.claimableBalanceOre
    val activeMiners = homeUiState.activeMiners
    val poolBalanceCoal = homeUiState.poolBalanceCoal
    val poolBalanceOre = homeUiState.poolBalanceOre

    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var isPublicKeyCopied by remember { mutableStateOf(false) }

    val screenHeight = LocalConfiguration.current.screenHeightDp
    val listHeight = screenHeight / 3

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Wallet COAL Balance
            val pubkeyString = homeUiState.minerPubkey?.let {
                it
            } ?: "Error"
            PublicKeySection(
                pubkey = pubkeyString,
                onCopyPubkey = {
                    clipboardManager.setText(AnnotatedString(pubkeyString))
                    isPublicKeyCopied = true
                },
                isPublicKeyCopied = isPublicKeyCopied
            )

            Text(text = "Active Miners: $activeMiners", modifier = Modifier.padding(bottom = 8.dp))
            Text(
                text = "Pool Balance COAL: ${String.format("%.11f", poolBalanceCoal)}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "Pool Balance ORE: ${String.format("%.11f", poolBalanceOre)}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(text = "Hashpower: $hashpower", modifier = Modifier.padding(bottom = 8.dp))
            Text(text = "Difficulty: $difficulty")

            // Thread count selector
            Column(
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(
                    Modifier.height(36.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Mining Power:",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    if (powerSlider >= 3) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning, high power usage!",
                                tint = Color.Yellow
                            )
                            Box(
                                Modifier.height(60.dp).padding(start = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Warning: High power usage can degrade device!",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (powerSlider == 3f) Color.Yellow else Color.Red,
                                )
                            }
                        }
                    }
                }

                Slider(
                    value = powerSlider,
                    enabled = serviceRunning,
                    onValueChange = { newValue ->
                        setPowerSlider(newValue)
                        val newThreadCount = newValue.roundToInt()
                        onUpdateSelectedThreads(newThreadCount)
                    },
                    steps = 3,
                    valueRange = 0f..4f,
                    colors = SliderDefaults.colors(
                        thumbColor = lerp(Color.Green, Color.Red, powerSlider / 4f),
                        activeTrackColor = lerp(Color.Green, Color.Red, powerSlider / 4f)
                    )
                )

                val steps = listOf("Min", "Low", "Med", "High", "Max")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    steps.forEachIndexed { index, label ->
                        Text(
                            text = label,
                            color = when (index) {
                                0 -> Color.Green
                                2 -> Color.Yellow
                                4 -> Color.Red
                                else -> Color.Unspecified
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Text(
                    text = "Threads: $threadCount / $availableThreads",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Mining toggle button
            Button(
                onClick = onToggleMining,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (serviceRunning) "Stop Mining" else "Start Mining")
            }

            Text(
                text = "Claimable COAL: ${String.format("%.11f", claimableBalanceCoal)}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = "Claimable ORE: ${String.format("%.11f", claimableBalanceOre)}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )

            val minimumBalanceReachedCoal = claimableBalanceCoal >= 1
            val minimumBalanceReachedOre = claimableBalanceOre >= 0.005

            if (!minimumBalanceReachedCoal) {
                Text("Minimum claim amount is 1 COAL", style = MaterialTheme.typography.labelSmall)
            }
            if (!minimumBalanceReachedOre) {
                Text("Minimum claim amount is 0.005 ORE", style = MaterialTheme.typography.labelSmall)
            }
            Button(
                onClick = onClickClaim,
                enabled = minimumBalanceReachedCoal && minimumBalanceReachedOre,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Claim All")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text(text = "Earned")
                Text(text = "Diff")
                Text(text = "Date")
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(listHeight.dp)
                    .border(4.dp, Color.DarkGray)
            ) {
                items(homeUiState.submissionResults) { result ->
                    SubmissionResultItem(result)
                }
            }
        }

    }
}

@Composable
fun SubmissionResultItem(result: SubmissionResult) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val earningsCoal = (result.minerEarnedCoal.toDouble() / 10.0.pow(11.0))
        val earningsOre = (result.minerEarnedOre.toDouble() / 10.0.pow(11.0))
        val calendar = Calendar.getInstance(Locale.getDefault())
        //get current date from ts
        calendar.timeInMillis = result.createdAt
        //return formatted date
        val date = android.text.format.DateFormat.format("dd-MM-yy-HH:mm:ss", calendar).toString()
        Text(text = "${"%.11f".format(earningsCoal)} C" + "\n" + "${"%.11f".format(earningsOre)} O", style = MaterialTheme.typography.bodyLarge)
        Text(text = "${result.minerDifficulty}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "$date", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun SignUpScreen(
    homeUiState: HomeUiState,
    onClickSignUp: () -> Unit,
    onClickConnectWallet: () -> Unit,
    onClickDisconnectWallet: () -> Unit,
) {
    Text("Sol Balance: ${homeUiState.solBalance}")

    if (!homeUiState.isSignedUp && homeUiState.secureWalletPubkey != null) {
        Button(
            onClick = onClickSignUp,
            enabled = !homeUiState.isProcessingSignup,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register Miner")
        }
    }

    if (homeUiState.secureWalletPubkey != null) {
        Button(
            onClick = onClickDisconnectWallet,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Disconnect Wallet")
        }
    } else {
        Button(
            onClick = onClickConnectWallet,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Connect Wallet")
        }
    }
}


@Composable
fun PublicKeySection(pubkey: String, onCopyPubkey: () -> Unit, isPublicKeyCopied: Boolean) {
    Row {
        Text(
            text = "Miner Pubkey: ",
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .wrapContentWidth()
                .clickable(onClick = onCopyPubkey)
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val pubkeyFirst5 = pubkey.take(5)
                val pubkeyLast5 = pubkey.takeLast(5)
                Text(
                    text = "$pubkeyFirst5...$pubkeyLast5",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (isPublicKeyCopied) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Copied",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}