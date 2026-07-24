/** app/src/main/java/com/bowling/drilling/data/repository/BowlingRepository.kt – 저장소 인터페이스 */
package com.bowling.drilling.data.repository

import com.bowling.drilling.data.entity.BowlingRecord
import kotlinx.coroutines.flow.Flow

interface BowlingRepository {
    fun getAllRecords(searchQuery: String? = null): Flow<List<BowlingRecord>>
    suspend fun insert(record: BowlingRecord)
    suspend fun update(record: BowlingRecord)
    suspend fun delete(record: BowlingRecord)
    suspend fun getRecordById(id: Long): BowlingRecord?
    suspend fun importRecords(records: List<BowlingRecord>)
}
