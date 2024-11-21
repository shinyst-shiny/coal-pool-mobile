package com.shinyst.coalpoolmobile.data.models

import android.util.Log
import kotlin.math.pow

sealed class ServerMessage {
    companion object {
        fun fromUByteArray(data: UByteArray): ServerMessage? {
            if (data.isEmpty()) return null

            return when (data[0].toUInt()) {
                0U -> parseStartMining(data)
                1U -> parsePoolSubmissionResult(data)
                else -> {
                    Log.w("ServerMessage", "Unknown message type: ${data[0]}")
                    null
                }
            }
        }

        private fun parseStartMining(data: UByteArray): StartMining? {
            if (data.size < 57) {
                Log.w("ServerMessage", "Invalid data for StartMining message")
                return null
            }

            val challenge = data.slice(1..32).toUByteArray()
            val cutoff = data.slice(33..40).toUByteArray().toULong()
            val nonceStart = data.slice(41..48).toUByteArray().toULong()
            Log.d("ServerMessage", "Nonce Start: $nonceStart")
            val nonceEnd = data.slice(49..56).toUByteArray().toULong()
            Log.d("ServerMessage", "Nonce End: $nonceEnd")

            return StartMining(challenge, nonceStart until nonceEnd, cutoff)
        }

        const val coalDetailsSize = 76

        @OptIn(ExperimentalUnsignedTypes::class)
        private fun parsePoolSubmissionResult(data: UByteArray): PoolSubmissionResult? {
            if (data.size < 193) {
                Log.w("ServerMessage", "Invalid data for PoolSubmissionResult message")
                return null
            }

            var offset = 1

            // Extract difficulty (u32)
            val difficulty = data.slice(offset..offset + 3).toUByteArray().toUInt()
            offset += 4

            // Extract challenge ([u8; 32])
            val challenge = data.slice(offset..offset + 31).toUByteArray()
            offset += 32

            // Extract bestNonce (u64)
            val bestNonce = data.slice(offset..offset + 7).toUByteArray().toULong()
            offset += 8

            // Extract activeMiners (u32)
            val activeMiners = data.slice(offset..offset + 3).toUByteArray().toUInt()
            offset += 4

            // Extract CoalDetails
            val coalDetails = parseCoalDetails(data, offset)
            offset += coalDetailsSize // coalDetailsSize is a constant representing the size of CoalDetails in bytes

            // Extract OreDetails
            val oreDetails = parseOreDetails(data, offset)

            return PoolSubmissionResult(
                difficulty,
                coalDetails.rewardDetails.totalBalance,
                oreDetails.rewardDetails.totalBalance,
                coalDetails.rewardDetails.totalRewards,
                oreDetails.rewardDetails.totalRewards,
                activeMiners,
                challenge,
                bestNonce,
                coalDetails.rewardDetails.minerSuppliedDifficulty,
                coalDetails.rewardDetails.minerEarnedRewards,
                oreDetails.rewardDetails.minerEarnedRewards,
                coalDetails.rewardDetails.minerPercentage,
                oreDetails.rewardDetails.minerPercentage,
            )
        }

        @OptIn(ExperimentalUnsignedTypes::class)
        private fun parseCoalDetails(data: UByteArray, offset: Int): CoalDetails {
            var currentOffset = offset

            // Extract RewardDetails
            val rewardDetails = parseRewardDetails(data, currentOffset)
            currentOffset += 36 // Size of RewardDetails

            // Extract topStake (f64)
            val topStake = data.slice(currentOffset..currentOffset + 7).toUByteArray().toDouble()
            currentOffset += 8

            // Extract stakeMultiplier (f64)
            val stakeMultiplier = data.slice(currentOffset..currentOffset + 7).toUByteArray().toDouble()
            currentOffset += 8

            // Extract guildTotalStake (f64)
            val guildTotalStake = data.slice(currentOffset..currentOffset + 7).toUByteArray().toDouble()
            currentOffset += 8

            // Extract guildMultiplier (f64)
            val guildMultiplier = data.slice(currentOffset..currentOffset + 7).toUByteArray().toDouble()
            currentOffset += 8

            // Extract toolMultiplier (f64)
            val toolMultiplier = data.slice(currentOffset..currentOffset + 7).toUByteArray().toDouble()
            currentOffset += 8

            return CoalDetails(
                rewardDetails,
                topStake,
                stakeMultiplier,
                guildTotalStake,
                guildMultiplier,
                toolMultiplier
            )
        }

        @OptIn(ExperimentalUnsignedTypes::class)
        private fun parseOreDetails(data: UByteArray, offset: Int): OreDetails {
            var currentOffset = offset

            // Extract RewardDetails
            val rewardDetails = parseRewardDetails(data, currentOffset)
            currentOffset += 36 // Size of RewardDetails

            // Extract topStake (f64)
            val topStake = data.slice(currentOffset..currentOffset + 7).toUByteArray().toDouble()
            currentOffset += 8

            // Extract stakeMultiplier (f64)
            val stakeMultiplier = data.slice(currentOffset..currentOffset + 7).toUByteArray().toDouble()
            currentOffset += 8

            // Extract oreBoosts (List<OreBoost>)
            val oreBoosts = mutableListOf<OreBoost>()
            // Assuming you know the number of OreBoosts or have a way to determine it
            val numOreBoosts = 3/* Get the number of OreBoosts */
                for (i in 0 until numOreBoosts) {
                    val oreBoost = parseOreBoost(data, currentOffset)
                    oreBoosts.add(oreBoost)
                    currentOffset += 57/* Size of OreBoost */ // You'll need to calculate this based on OreBoost structure
                }

            return OreDetails(
                rewardDetails,
                topStake,
                stakeMultiplier,
                oreBoosts
            )
        }

        @OptIn(ExperimentalUnsignedTypes::class)
        private fun parseRewardDetails(data: UByteArray, offset: Int): RewardDetails {
            var currentOffset = offset

            // Extract totalBalance (f64)
            val totalBalance = data.slice(currentOffset..currentOffset + 7).toUByteArray().toDouble()
            currentOffset += 8

            // Extract totalRewards (f64)
            val totalRewards = data.slice(currentOffset..currentOffset + 7).toUByteArray().toDouble()
            currentOffset += 8

            // Extract minerSuppliedDifficulty (u32)
            val minerSuppliedDifficulty = data.slice(currentOffset..currentOffset + 3).toUByteArray().toUInt()
            currentOffset += 4

            // Extract minerEarnedRewards (f64)
            val minerEarnedRewards = data.slice(currentOffset..currentOffset + 7).toUByteArray().toDouble()
            currentOffset += 8

            // Extract minerPercentage (f64)
            val minerPercentage = data.slice(currentOffset..currentOffset + 7).toUByteArray().toDouble()
            currentOffset += 8

            return RewardDetails(
                totalBalance,
                totalRewards,
                minerSuppliedDifficulty,
                minerEarnedRewards,
                minerPercentage
            )
        }

        @OptIn(ExperimentalUnsignedTypes::class)
        private fun parseOreBoost(data: UByteArray, offset: Int): OreBoost {
            var currentOffset = offset

            // Extract topStake (f64)
            val topStake = data.slice(currentOffset..currentOffset + 7).toUByteArray().toDouble()
            currentOffset += 8

            // Extract totalStake (f64)
            val totalStake = data.slice(currentOffset..currentOffset + 7).toUByteArray().toDouble()
            currentOffset += 8

            // Extract stakeMultiplier (f64)
            val stakeMultiplier = data.slice(currentOffset..currentOffset + 7).toUByteArray().toDouble()
            currentOffset += 8

            // Extract mintAddress ([u8; 32])
            val mintAddress = data.slice(currentOffset..currentOffset + 31).toUByteArray()
            currentOffset += 32

            // Extract name (String, currently "") - Assuming 1 byte for length or null terminator
            // You might need to adjust this based on the actual string representation
            // used by the server.
            // val nameLength = data[currentOffset].toInt() // If length-prefixed
            // currentOffset += 1 + nameLength // Update offset based on length
            // val name = String(data.sliceArray(currentOffset..currentOffset + nameLength -1 ))
            currentOffset += 1 // Assuming 1 byte for null terminator or length 0

            return OreBoost(
                topStake,
                totalStake,
                stakeMultiplier,
                mintAddress,
                "" // name is currently ""
            )
        }
    }

    data class StartMining(
        val challenge: UByteArray,
        val nonceRange: ULongRange,
        val cutoff: ULong
    ) : ServerMessage() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as StartMining

            if (!challenge.contentEquals(other.challenge)) return false
            if (nonceRange != other.nonceRange) return false
            if (cutoff != other.cutoff) return false

            return true
        }

        override fun hashCode(): Int {
            var result = challenge.contentHashCode()
            result = 31 * result + nonceRange.hashCode()
            result = 31 * result + cutoff.hashCode()
            return result
        }
    }

    data class RewardDetails(
        val totalBalance: Double,
        val totalRewards: Double,
        val minerSuppliedDifficulty: UInt,
        val minerEarnedRewards: Double,
        val minerPercentage: Double
    )

    data class CoalDetails(
        val rewardDetails: RewardDetails,
        val topStake: Double,
        val stakeMultiplier: Double,
        val guildTotalStake: Double,
        val guildMultiplier: Double,
        val toolMultiplier: Double
    )

    data class OreBoost(
        val topStake: Double,
        val totalStake: Double,
        val stakeMultiplier: Double,
        val mintAddress: UByteArray, // Use UByteArray for [u8; 32]
        val name: String
    )

    data class OreDetails(
        val rewardDetails: RewardDetails,
        val topStake: Double,
        val stakeMultiplier: Double,
        val oreBoosts: List<OreBoost>
    )

    data class ServerMessagePoolSubmissionResult(
        val difficulty: UInt,
        val challenge: UByteArray, // Use UByteArray for [u8; 32]
        val bestNonce: ULong,
        val activeMiners: UInt,
        val coalDetails: CoalDetails,
        val oreDetails: OreDetails
    )


    data class PoolSubmissionResult(
        val difficulty: UInt,
        val totalBalanceCoal: Double,
        val totalBalanceOre: Double,
        val totalRewardsCoal: Double,
        val totalRewardsOre: Double,
        val activeMiners: UInt,
        val challenge: UByteArray,
        val bestNonce: ULong,
        val minerSuppliedDifficulty: UInt,
        val minerEarnedRewardsCoal: Double,
        val minerEarnedRewardsOre: Double,
        val minerPercentageCoal: Double,
        val minerPercentageOre: Double
    ) : ServerMessage() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PoolSubmissionResult

            return difficulty == other.difficulty &&
                   totalBalanceCoal == other.totalBalanceCoal &&
                    totalBalanceOre == other.totalBalanceOre &&
                   totalRewardsCoal == other.totalRewardsCoal &&
                    totalRewardsOre == other.totalRewardsOre &&
                   activeMiners == other.activeMiners &&
                   challenge.contentEquals(other.challenge) &&
                   bestNonce == other.bestNonce &&
                   minerSuppliedDifficulty == other.minerSuppliedDifficulty &&
                   minerEarnedRewardsCoal == other.minerEarnedRewardsCoal &&
                    minerEarnedRewardsOre == other.minerEarnedRewardsOre &&
                   minerPercentageCoal == other.minerPercentageCoal &&
                    minerPercentageOre == other.minerPercentageOre
        }

        override fun hashCode(): Int {
            var result = difficulty.hashCode()
            result = 31 * result + totalBalanceCoal.hashCode()
            result = 31 * result + totalBalanceOre.hashCode()
            result = 31 * result + totalRewardsCoal.hashCode()
            result = 31 * result + totalRewardsOre.hashCode()
            result = 31 * result + activeMiners.hashCode()
            result = 31 * result + challenge.hashCode()
            result = 31 * result + bestNonce.hashCode()
            result = 31 * result + minerSuppliedDifficulty.hashCode()
            result = 31 * result + minerEarnedRewardsCoal.hashCode()
            result = 31 * result + minerEarnedRewardsOre.hashCode()
            result = 31 * result + minerPercentageCoal.hashCode()
            result = 31 * result + minerPercentageOre.hashCode()
            return result
        }
    }
}

// Helper conversion functions
public fun ULong.toLittleEndianByteArray(): ByteArray {
    return ByteArray(8) { i -> (this shr (8 * i)).toByte() }
}

fun Long.toLittleEndianByteArray(): ByteArray {
    return ByteArray(8) { i -> (this shr (8 * i) and 0xFFL).toByte() }
}

fun UByteArray.toULong(): ULong = this.foldIndexed(0UL) { index, acc, byte ->
    acc or (byte.toULong() shl (index * 8))
}

private fun List<UByte>.toUByteArray(): UByteArray {
    return UByteArray(size) { this[it] }
}

fun ULong.toLittleEndianUByteArray(): UByteArray {
    return UByteArray(8) { i -> ((this shr (8 * i)) and 0xFFU).toUByte() }
}

fun UByteArray.toUInt(): UInt = this.foldIndexed(0U) { index, acc, byte ->
    acc or (byte.toUInt() shl (index * 8))
}

fun UByteArray.toDouble(): Double {
  val bits = this.foldIndexed(0L) { index, acc, byte ->
      acc or ((byte.toLong() and 0xFFL) shl (index * 8))
  }
  return Double.fromBits(bits)
}

