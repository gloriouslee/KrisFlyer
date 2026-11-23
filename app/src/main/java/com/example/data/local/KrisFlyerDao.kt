package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface KrisFlyerDao {
    @Query("SELECT * FROM simulator_profile WHERE id = 1")
    fun observeProfile(): Flow<SimulatorProfileEntity?>

    @Query("SELECT * FROM simulator_profile WHERE id = 1")
    suspend fun getProfileOnce(): SimulatorProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: SimulatorProfileEntity)

    @Query("SELECT * FROM mileage_transactions ORDER BY timestampMillis DESC, id DESC")
    fun observeTransactions(): Flow<List<MileageTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: MileageTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(txs: List<MileageTransactionEntity>)

    @Query("DELETE FROM mileage_transactions")
    suspend fun clearTransactions()

    @Query("SELECT * FROM award_bookings ORDER BY createdAtMillis DESC, id DESC")
    fun observeBookings(): Flow<List<AwardBookingEntity>>

    @Query("SELECT * FROM award_bookings WHERE id = :id")
    suspend fun getBookingById(id: Long): AwardBookingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: AwardBookingEntity)

    @Update
    suspend fun updateBooking(booking: AwardBookingEntity)

    @Query("DELETE FROM award_bookings")
    suspend fun clearBookings()
}
