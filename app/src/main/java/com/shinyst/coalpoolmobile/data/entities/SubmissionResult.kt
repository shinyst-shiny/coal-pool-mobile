package com.shinyst.coalpoolmobile.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "submission_results")
class SubmissionResult {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0

    @ColumnInfo(name = "poolDifficulty")
    var poolDifficulty: Int = 0
    @ColumnInfo(name = "poolEarnedCoal")
    var poolEarnedCoal: Long = 0
    @ColumnInfo(name = "poolEarnedOre")
    var poolEarnedOre: Long = 0
    @ColumnInfo(name = "minerPercentageCoal")
    var minerPercentageCoal: Double = 0.0
    @ColumnInfo(name = "minerPercentageOre")
    var minerPercentageOre: Double = 0.0
    @ColumnInfo(name = "minerDifficulty")
    var minerDifficulty: Int = 0
    @ColumnInfo(name = "minerEarnedCoal")
    var minerEarnedCoal: Long = 0
    @ColumnInfo(name = "minerEarnedOre")
    var minerEarnedOre: Long = 0
    @ColumnInfo(name = "createdAt", defaultValue = "CURRENT_TIMESTAMP")
    var createdAt: Long = 0

    constructor()

    constructor(
        poolDifficulty: Int,
        poolEarnedCoal: Long,
        poolEarnedOre: Long,
        minerPercentageCoal: Double,
        minerPercentageOre: Double,
        minerDifficulty: Int,
        minerEarnedCoal: Long,
        minerEarnedOre: Long,
    ) {
        this.poolDifficulty = poolDifficulty
        this.poolEarnedCoal = poolEarnedCoal
        this.poolEarnedOre = poolEarnedOre
        this.minerPercentageCoal = minerPercentageCoal
        this.minerPercentageOre = minerPercentageOre
        this.minerDifficulty = minerDifficulty
        this.minerEarnedCoal = minerEarnedCoal
        this.minerEarnedOre = minerEarnedOre
        this.createdAt = System.currentTimeMillis()
    }
}
