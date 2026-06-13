package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SuperAppRepository(private val superAppDao: SuperAppDao) {

    val allBookings: Flow<List<BookingEntity>> = superAppDao.getAllBookings()
    val allProviders: Flow<List<ProviderEntity>> = superAppDao.getAllProviders()
    val allNotifications: Flow<List<NotificationEntity>> = superAppDao.getAllNotifications()
    val saasConfig: Flow<SaasConfigEntity?> = superAppDao.getSaasConfig()

    fun getBookingsByCustomer(phone: String): Flow<List<BookingEntity>> =
        superAppDao.getBookingsByCustomer(phone)

    fun getBookingsByProvider(phone: String): Flow<List<BookingEntity>> =
        superAppDao.getBookingsByProvider(phone)

    fun getActiveProvidersByService(serviceType: String): Flow<List<ProviderEntity>> =
        superAppDao.getActiveProvidersByService(serviceType)

    suspend fun createBooking(booking: BookingEntity): Long = withContext(Dispatchers.IO) {
        superAppDao.insertBooking(booking)
    }

    suspend fun updateBooking(booking: BookingEntity) = withContext(Dispatchers.IO) {
        superAppDao.updateBooking(booking)
    }

    suspend fun deleteBooking(id: Int) = withContext(Dispatchers.IO) {
        superAppDao.deleteBookingById(id)
    }

    suspend fun saveProvider(provider: ProviderEntity): Long = withContext(Dispatchers.IO) {
        superAppDao.insertProvider(provider)
    }

    suspend fun updateProvider(provider: ProviderEntity) = withContext(Dispatchers.IO) {
        superAppDao.updateProvider(provider)
    }

    suspend fun deleteProvider(id: Int) = withContext(Dispatchers.IO) {
        superAppDao.deleteProviderById(id)
    }

    suspend fun addNotification(notification: NotificationEntity) = withContext(Dispatchers.IO) {
        superAppDao.insertNotification(notification)
    }

    suspend fun markNotificationsAsRead() = withContext(Dispatchers.IO) {
        superAppDao.markAllNotificationsAsRead()
    }

    suspend fun updateSaasConfig(config: SaasConfigEntity) = withContext(Dispatchers.IO) {
        superAppDao.insertSaasConfig(config)
    }

    // Preseed mock Iraqi providers to make the application immediately useful and visual!
    suspend fun preseedDataIfEmpty() = withContext(Dispatchers.IO) {
        val count = superAppDao.getAllProviders().firstOrNull()?.size ?: 0
        if (count == 0) {
            // Seed SaaS Config
            superAppDao.insertSaasConfig(SaasConfigEntity())

            // Seed Drivers/Providers for each Iraqi super service
            val mockProviders = listOf(
                // TAXI
                ProviderEntity(
                    name = "Mustafa Al-Saeedi",
                    phone = "07701234567",
                    serviceType = "TAXI",
                    vehiclePlate = "Baghdad 12345-A",
                    city = "Baghdad",
                    rating = 4.9,
                    currentLatOffset = 0.012f,
                    currentLngOffset = -0.008f
                ),
                ProviderEntity(
                    name = "Ali Al-Basrawi",
                    phone = "07801234567",
                    serviceType = "TAXI",
                    vehiclePlate = "Basra 9876-B",
                    city = "Basra",
                    rating = 4.7,
                    currentLatOffset = -0.015f,
                    currentLngOffset = 0.011f
                ),
                ProviderEntity(
                    name = "Karar Al-Hilli",
                    phone = "07712345678",
                    serviceType = "TAXI",
                    vehiclePlate = "Babylon 45677-C",
                    city = "Karbala",
                    rating = 4.8,
                    currentLatOffset = 0.005f,
                    currentLngOffset = 0.022f
                ),
                // TOKTOK
                ProviderEntity(
                    name = "Sajjad Al-Sadr",
                    phone = "07501234567",
                    serviceType = "TOKTOK",
                    vehiclePlate = "Baghdad 7733-Tok",
                    city = "Baghdad",
                    rating = 4.6,
                    currentLatOffset = -0.022f,
                    currentLngOffset = -0.015f
                ),
                ProviderEntity(
                    name = "Hussein Tuktukji",
                    phone = "07709876543",
                    serviceType = "TOKTOK",
                    vehiclePlate = "Maysan 4321-Tok",
                    city = "Maysan",
                    rating = 4.9,
                    currentLatOffset = 0.025f,
                    currentLngOffset = 0.008f
                ),
                // TOWING (Krin)
                ProviderEntity(
                    name = "Raid Krin (Abu Fahad)",
                    phone = "07812345678",
                    serviceType = "TOWING",
                    vehiclePlate = "Anbar 4412-Krin",
                    city = "Baghdad",
                    rating = 4.9,
                    currentLatOffset = 0.032f,
                    currentLngOffset = 0.021f
                ),
                ProviderEntity(
                    name = "Laith Towing (Abu Ali)",
                    phone = "07703344556",
                    serviceType = "TOWING",
                    vehiclePlate = "Erbil 99440-Krin",
                    city = "Erbil",
                    rating = 4.8,
                    currentLatOffset = -0.035f,
                    currentLngOffset = -0.027f
                ),
                // GAS Cylinder
                ProviderEntity(
                    name = "Ahmed Gaz",
                    phone = "07711223344",
                    serviceType = "GAS",
                    vehiclePlate = "Baghdad 8874-Gas",
                    city = "Baghdad",
                    rating = 4.9,
                    currentLatOffset = -0.009f,
                    currentLngOffset = 0.019f
                ),
                ProviderEntity(
                    name = "Haider Al-Waz",
                    phone = "07505566778",
                    serviceType = "GAS",
                    vehiclePlate = "Nineveh 7715-Gas",
                    city = "Mosul",
                    rating = 4.7,
                    currentLatOffset = 0.019f,
                    currentLngOffset = -0.031f
                ),
                // WATER (RO distributed)
                ProviderEntity(
                    name = "Abbas RO Water",
                    phone = "07722334455",
                    serviceType = "WATER",
                    vehiclePlate = "Baghdad 5412-H2O",
                    city = "Baghdad",
                    rating = 4.5,
                    currentLatOffset = 0.018f,
                    currentLngOffset = -0.018f
                ),
                // STREET VENDORS
                ProviderEntity(
                    name = "Abu Raad Al-Khaddar",
                    phone = "07799887766",
                    serviceType = "VENDOR",
                    subCategory = "Fruites & Veggies",
                    vehiclePlate = "Mobile Cart 21",
                    city = "Najaf",
                    rating = 4.9,
                    currentLatOffset = -0.011f,
                    currentLngOffset = 0.016f
                ),
                ProviderEntity(
                    name = "Abu Jasim Al-Halawani",
                    phone = "07811223344",
                    serviceType = "VENDOR",
                    subCategory = "Traditional Kleicha & Sweets",
                    vehiclePlate = "Baghdad Cart 04",
                    city = "Baghdad",
                    rating = 4.8,
                    currentLatOffset = 0.002f,
                    currentLngOffset = 0.004f
                )
            )

            mockProviders.forEach { superAppDao.insertProvider(it) }

            // Seed initial welcome notification
            superAppDao.insertNotification(
                NotificationEntity(
                    titleAr = "مرحباً بكم في منصة سوا العراق!",
                    titleEn = "Welcome to Sawa Iraq Super App!",
                    messageAr = "المنصة المتكاملة الأولى لتوفير خدمات التاكسي، التوكتوك، الغاز، سحب السيارات، ومياه RO في جميع محافظات العراق.",
                    messageEn = "The first integrated platform providing Taxi, Toktok, Gas delivery, Towing, and RO Water across all Iraqi provinces."
                )
            )
        }
    }
}
