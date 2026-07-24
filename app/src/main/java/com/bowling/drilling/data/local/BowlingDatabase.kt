/** app/src/main/java/com/bowling/drilling/data/local/BowlingDatabase.kt – Room DB 인스턴스 (버전 1) */
package com.bowling.drilling.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bowling.drilling.data.entity.BowlingRecord

@Database(
    entities = [BowlingRecord::class],
    version = 1,
    exportSchema = false
)
abstract class BowlingDatabase : RoomDatabase() {
    abstract fun bowlingDao(): BowlingDao

    companion object {
        @Volatile
        private var INSTANCE: BowlingDatabase? = null

        fun getInstance(context: Context): BowlingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BowlingDatabase::class.java,
                    "bowling_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
