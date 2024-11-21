package com.shinyst.coalpoolmobile.data.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.shinyst.coalpoolmobile.data.daos.AppAccountDao
import com.shinyst.coalpoolmobile.data.daos.SubmissionResultDao
import com.shinyst.coalpoolmobile.data.daos.WalletDao
import com.shinyst.coalpoolmobile.data.entities.AppAccount
import com.shinyst.coalpoolmobile.data.entities.SubmissionResult
import com.shinyst.coalpoolmobile.data.entities.Wallet

@Database(
    version = 1,
    entities = [(Wallet::class), (SubmissionResult::class), (AppAccount::class)],
    exportSchema = true
)
abstract class AppRoomDatabase: RoomDatabase() {

    abstract fun walletDao(): WalletDao
    abstract fun submissionResultDao(): SubmissionResultDao
    abstract fun appAccountDao(): AppAccountDao

    companion object {
        private var INSTANCE: AppRoomDatabase? = null

        fun getInstance(context: Context): AppRoomDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppRoomDatabase::class.java,
                        "app_database_2"
                    ).fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = instance
                }

                instance.openHelper.writableDatabase

                return instance
            }
        }
    }
}