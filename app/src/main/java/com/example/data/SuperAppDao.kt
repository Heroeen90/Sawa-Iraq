package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SuperAppDao {
    // Bookings
    @Query("SELECT * FROM bookings ORDER BY timestamp DESC")
    fun getAllBookings(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE customerPhone = :phone ORDER BY timestamp DESC")
    fun getBookingsByCustomer(phone: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE providerPhone = :phone ORDER BY timestamp DESC")
    fun getBookingsByProvider(phone: String): Flow<List<BookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity): Long

    @Update
    suspend fun updateBooking(booking: BookingEntity)

    @Query("DELETE FROM bookings WHERE id = :id")
    suspend fun deleteBookingById(id: Int)

    // Providers
    @Query("SELECT * FROM providers")
    fun getAllProviders(): Flow<List<ProviderEntity>>

    @Query("SELECT * FROM providers WHERE serviceType = :serviceType AND isAvailable = 1")
    fun getActiveProvidersByService(serviceType: String): Flow<List<ProviderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: ProviderEntity): Long

    @Update
    suspend fun updateProvider(provider: ProviderEntity)

    @Query("DELETE FROM providers WHERE id = :id")
    suspend fun deleteProviderById(id: Int)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    // Config
    @Query("SELECT * FROM saas_config WHERE id = 'main_config' LIMIT 1")
    fun getSaasConfig(): Flow<SaasConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaasConfig(config: SaasConfigEntity)
}
