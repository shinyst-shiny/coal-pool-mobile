package com.shinyst.coalpoolmobile.data

import android.content.Context
import com.shinyst.coalpoolmobile.data.database.AppRoomDatabase
import com.shinyst.coalpoolmobile.data.repositories.AppAccountRepository
import com.shinyst.coalpoolmobile.data.repositories.IKeypairRepository
import com.shinyst.coalpoolmobile.data.repositories.IPoolRepository
import com.shinyst.coalpoolmobile.data.repositories.ISolanaRepository
import com.shinyst.coalpoolmobile.data.repositories.KeypairRepository
import com.shinyst.coalpoolmobile.data.repositories.PoolRepository
import com.shinyst.coalpoolmobile.data.repositories.SolanaRepository
import com.shinyst.coalpoolmobile.data.repositories.SubmissionResultRepository
import com.shinyst.coalpoolmobile.data.repositories.WalletRepository

interface AppContainer {
    val solanaRepository: ISolanaRepository
    val poolRepository: IPoolRepository
    val keypairRepository: IKeypairRepository
    val walletRepository: WalletRepository
    val submissionResultRepository: SubmissionResultRepository
    val appAccountRepository: AppAccountRepository
}

class DefaultAppContainer(private val context: Context, private val appDb: AppRoomDatabase): AppContainer {
    override val solanaRepository: ISolanaRepository by lazy {
        SolanaRepository()
    }
    override val poolRepository: IPoolRepository by lazy {
        PoolRepository()
    }
    override val keypairRepository: IKeypairRepository by lazy {
        KeypairRepository(context)
    }

    override val walletRepository: WalletRepository by lazy {
        val appDb = AppRoomDatabase.getInstance(context)
        val walletDao = appDb.walletDao()
        WalletRepository(walletDao)
    }

    override val submissionResultRepository: SubmissionResultRepository by lazy {
        val appDb = AppRoomDatabase.getInstance(context)
        val submissionResultDao = appDb.submissionResultDao()
        SubmissionResultRepository(submissionResultDao)
    }

    override val appAccountRepository: AppAccountRepository by lazy {
        val appDb = AppRoomDatabase.getInstance(context)
        val appAccountDao = appDb.appAccountDao()
        AppAccountRepository(appAccountDao)
    }
}