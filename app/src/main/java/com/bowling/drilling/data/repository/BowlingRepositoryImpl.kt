/** app/src/main/java/com/bowling/drilling/data/repository/BowlingRepositoryImpl.kt – Room 구현체 */
package com.bowling.drilling.data.repository

import com.bowling.drilling.data.entity.BowlingRecord
import com.bowling.drilling.data.local.BowlingDao
import kotlinx.coroutines.flow.Flow

class BowlingRepositoryImpl(private val dao: BowlingDao) : BowlingRepository {

    override fun getAllRecords(searchQuery: String?): Flow<List<BowlingRecord>> {
        return if (searchQuery.isNullOrEmpty()) {
            dao.getAllRecords()
        } else {
            dao.searchRecords(searchQuery)
        }
    }

    override suspend fun insert(record: BowlingRecord) = dao.insert(record)

    override suspend fun update(record: BowlingRecord) = dao.update(record)

    override suspend fun delete(record: BowlingRecord) = dao.delete(record)

    override suspend fun getRecordById(id: Long): BowlingRecord? = dao.getRecordById(id)

    override suspend fun importRecords(records: List<BowlingRecord>) {
        records.forEach { dao.insert(it) }
    }
}
