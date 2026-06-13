package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        BookingEntity::class,
        ProviderEntity::class,
        NotificationEntity::class,
        SaasConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SuperAppDatabase : RoomDatabase() {
    abstract fun superAppDao(): SuperAppDao

    companion object {
        @Volatile
        private var INSTANCE: SuperAppDatabase? = null

        fun getDatabase(context: Context): SuperAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SuperAppDatabase::class.java,
                    "sawa_iraq_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
