/** app/src/main/java/com/bowling/drilling/di/RepositoryModule.kt – 수동 DI (Repository 싱글톤 제공) */
package com.bowling.drilling.di

import android.content.Context
import com.bowling.drilling.data.local.BowlingDatabase
import com.bowling.drilling.data.repository.BowlingRepository
import com.bowling.drilling.data.repository.BowlingRepositoryImpl

object RepositoryModule {
    private lateinit var repository: BowlingRepository

    fun init(context: Context) {
        val database = BowlingDatabase.getInstance(context)
        val dao = database.bowlingDao()
        repository = BowlingRepositoryImpl(dao)
    }

    fun provideRepository(): BowlingRepository = repository
}
