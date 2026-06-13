package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val serviceType: String, // "TAXI", "TOKTOK", "TOWING", "GAS", "WATER", "VENDOR"
    val customerName: String,
    val customerPhone: String,
    val providerName: String,
    val providerPhone: String,
    val startLocation: String,
    val endLocation: String,
    val status: String, // "PENDING", "ACCEPTED", "ON_THE_WAY", "COMPLETED", "CANCELLED"
    val price: Double,
    val distanceKm: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val remarks: String = "",
    val rating: Int = 0, // 1 to 5, 0 means unrated
    val paymentMethod: String = "CASH" // "CASH", "ZAIN_CASH", "ASIA_PAY", "CREDIT_CARD"
)

@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val serviceType: String, // "TAXI", "TOKTOK", "TOWING", "GAS", "WATER", "VENDOR"
    val subCategory: String = "", // e.g. "Vegetables", "Fridge Repair"
    val vehiclePlate: String,
    val isAvailable: Boolean = true,
    val rating: Double = 4.8,
    val currentLatOffset: Float = 0f, // For simulated maps movement
    val currentLngOffset: Float = 0f,
    val city: String = "Baghdad"
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titleAr: String,
    val titleEn: String,
    val messageAr: String,
    val messageEn: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saas_config")
data class SaasConfigEntity(
    @PrimaryKey val id: String = "main_config",
    val commissionPercentCat1: Double = 10.0, // Taxi
    val commissionPercentCat2: Double = 5.0,  // Toktok
    val driverSubscriptionFeeIqd: Double = 15000.0, // Monthly fee block
    val basePriceTaxiIqd: Double = 3000.0,
    val basePriceToktokIqd: Double = 1500.0,
    val basePriceTowingIqd: Double = 25000.0,
    val currencyUnit: String = "IQD"
)
