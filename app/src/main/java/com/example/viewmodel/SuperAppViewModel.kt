package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SuperAppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SuperAppRepository

    init {
        val database = SuperAppDatabase.getDatabase(application)
        repository = SuperAppRepository(database.superAppDao())
        
        // Seed default Iraqi providers and configs immediately
        viewModelScope.launch {
            repository.preseedDataIfEmpty()
        }
    }

    // Role switcher: "CUSTOMER", "PROVIDER", "ADMIN"
    private val _currentRole = MutableStateFlow("CUSTOMER")
    val currentRole: StateFlow<String> = _currentRole

    // Localization switcher: "AR" (Arabic), "EN" (English)
    private val _currentLanguage = MutableStateFlow("AR")
    val currentLanguage: StateFlow<String> = _currentLanguage

    // UI Active Service selection
    private val _selectedServiceType = MutableStateFlow("TAXI")
    val selectedServiceType: StateFlow<String> = _selectedServiceType

    // Live Map routing calculations
    private val _mapDistanceKm = MutableStateFlow(5.5)
    val mapDistanceKm: StateFlow<Double> = _mapDistanceKm

    private val _mapPriceIqd = MutableStateFlow(7125.0)
    val mapPriceIqd: StateFlow<Double> = _mapPriceIqd

    private val _mapEtaMinutes = MutableStateFlow(14)
    val mapEtaMinutes: StateFlow<Int> = _mapEtaMinutes

    private val _mapStartLabel = MutableStateFlow("Bab Al-Sharqi, Baghdad")
    val mapStartLabel: StateFlow<String> = _mapStartLabel

    private val _mapEndLabel = MutableStateFlow("Mansour Mall, Baghdad")
    val mapEndLabel: StateFlow<String> = _mapEndLabel

    // Authentication Profile Simulate
    val customerName = "Ameer Al-Kinani"
    val customerPhone = "07711223344"
    val customerBalanceIqd = MutableStateFlow(50000.0)

    val providerName = "Karrar Al-Mandalawi"
    val providerPhone = "07802233445"
    val providerServiceType = "TAXI"
    val providerVehiclePlate = "Baghdad 18375-H"
    val providerBalanceIqd = MutableStateFlow(325000.0)

    // Flows from ROOM
    val bookings: StateFlow<List<BookingEntity>> = repository.allBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val providers: StateFlow<List<ProviderEntity>> = repository.allProviders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val saasConfig: StateFlow<SaasConfigEntity?> = repository.saasConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SaasConfigEntity())

    // Filtered providers based on active service type selected
    val activeProvidersByService: StateFlow<List<ProviderEntity>> = combine(
        providers, _selectedServiceType
    ) { all, service ->
        all.filter { it.serviceType == service && it.isAvailable }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Set Role
    fun setRole(role: String) {
        _currentRole.value = role
    }

    // Toggle Language
    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == "AR") "EN" else "AR"
    }

    // Set Service Type
    fun setSelectedServiceType(serviceType: String) {
        _selectedServiceType.value = serviceType
    }

    // Update Route calculation parameters from live map taps
    fun setRouteCalculations(distance: Double, price: Double, eta: Int, startLabel: String, endLabel: String) {
        _mapDistanceKm.value = distance
        _mapPriceIqd.value = price
        _mapEtaMinutes.value = eta
        _mapStartLabel.value = startLabel
        _mapEndLabel.value = endLabel
    }

    // Actions
    fun requestService(paymentMethod: String = "CASH") {
        viewModelScope.launch {
            val matchingProvider = activeProvidersByService.value.firstOrNull() ?: ProviderEntity(
                name = "Ali Al-Fartousi",
                phone = "07705566771",
                serviceType = _selectedServiceType.value,
                vehiclePlate = "Iraq 77838-M",
                city = "Baghdad"
            )

            val booking = BookingEntity(
                serviceType = _selectedServiceType.value,
                customerName = customerName,
                customerPhone = customerPhone,
                providerName = matchingProvider.name,
                providerPhone = matchingProvider.phone,
                startLocation = _mapStartLabel.value,
                endLocation = _mapEndLabel.value,
                status = "ACCEPTED",
                price = _mapPriceIqd.value,
                distanceKm = _mapDistanceKm.value,
                paymentMethod = paymentMethod
            )

            // Save booking, withdraw balance if paying electronically
            repository.createBooking(booking)
            if (paymentMethod != "CASH") {
                customerBalanceIqd.value = (customerBalanceIqd.value - _mapPriceIqd.value).coerceAtLeast(0.0)
            }

            // Create notification helper
            val arMsg = "تم تأكيد طلبك لخدمة ${_selectedServiceType.value}. السائق ${matchingProvider.name} في الطريق إليك."
            val enMsg = "Your request for ${_selectedServiceType.value} is accepted! Provider ${matchingProvider.name} is on the way."
            
            repository.addNotification(
                NotificationEntity(
                    titleAr = "طلب مقبول",
                    titleEn = "Booking Accepted",
                    messageAr = arMsg,
                    messageEn = enMsg
                )
            )
        }
    }

    // Complete Booking
    fun completeBooking(booking: BookingEntity) {
        viewModelScope.launch {
            val updated = booking.copy(status = "COMPLETED")
            repository.updateBooking(updated)

            // Settle provider commission (SaaS model)
            val commission = when (booking.serviceType) {
                "TAXI" -> booking.price * 0.10
                "TOKTOK" -> booking.price * 0.05
                else -> booking.price * 0.08
            }
            val netEarning = booking.price - commission
            providerBalanceIqd.value += netEarning

            // Notify driver earnings
            val arMsg = "لقد أكملت الرحلة بنجاح. صافي ربحك في محفظة سوا: ${netEarning} دينار بعد خصم العمولة."
            val enMsg = "Booking completed! Net earning recorded in your Sawa Wallet: ${netEarning} IQD."
            repository.addNotification(
                NotificationEntity(
                    titleAr = "اكتمال الطلب بالنجاح",
                    titleEn = "Order Completed Successfully",
                    messageAr = arMsg,
                    messageEn = enMsg
                )
            )
        }
    }

    // Cancel Booking
    fun cancelBooking(booking: BookingEntity) {
        viewModelScope.launch {
            val updated = booking.copy(status = "CANCELLED")
            repository.updateBooking(updated)

            repository.addNotification(
                NotificationEntity(
                    titleAr = "إلغاء الطلب",
                    titleEn = "Order Cancelled",
                    messageAr = "تم إلغاء الطلب بنجاح.",
                    messageEn = "The order has been cancelled."
                )
            )
        }
    }

    // Trigger Zain Cash or Asia Pay payout
    fun executeProviderPayout(providerId: Int, amount: Double, method: String) {
        viewModelScope.launch {
            if (providerBalanceIqd.value >= amount) {
                providerBalanceIqd.value -= amount
                val arMsg = "تم تحويل $amount دينار عراقي من محفظتك إلى حسابك في $method بنجاح."
                val enMsg = "Successfully wired $amount IQD from your Sawa wallet to your $method account."
                repository.addNotification(
                    NotificationEntity(
                        titleAr = "طلب سحب النقود مقبول",
                        titleEn = "Payout Request Executed",
                        messageAr = arMsg,
                        messageEn = enMsg
                    )
                )
            }
        }
    }

    // Toggle Driver Availability
    fun toggleDriverAvailability(provider: ProviderEntity) {
        viewModelScope.launch {
            val updated = provider.copy(isAvailable = !provider.isAvailable)
            repository.updateProvider(updated)
        }
    }

    // Configure SaaS parameters (Admin)
    fun updateSaaSAdminConfig(taxiPercent: Double, toktokPercent: Double, monthlyFee: Double) {
        viewModelScope.launch {
            val entity = SaasConfigEntity(
                id = "main_config",
                commissionPercentCat1 = taxiPercent,
                commissionPercentCat2 = toktokPercent,
                driverSubscriptionFeeIqd = monthlyFee
            )
            repository.updateSaasConfig(entity)

            repository.addNotification(
                NotificationEntity(
                    titleAr = "تحديث إعدادات SaaS للنظام",
                    titleEn = "SaaS Admin Policy Updated",
                    messageAr = "تم تطبيق الرسوم والعمولات الجديدة في النظام العراقي المالي.",
                    messageEn = "New commission and subscription rates applied."
                )
            )
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            repository.markNotificationsAsRead()
        }
    }
}
