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

    override suspend fun planImport(records: List<BowlingRecord>): ImportSummary {
        val existingKeys = dao.getAllRecordsOnce().map(::matchKey).toMutableSet()
        var updated = 0
        var inserted = 0
        var duplicatedInFile = 0
        val seenInFile = mutableSetOf<String>()

        records.forEach { record ->
            val key = matchKey(record)
            when {
                !seenInFile.add(key) -> duplicatedInFile++
                existingKeys.contains(key) -> updated++
                else -> { existingKeys.add(key); inserted++ }
            }
        }
        return ImportSummary(updated, inserted, duplicatedInFile)
    }

    override suspend fun importRecords(records: List<BowlingRecord>): ImportSummary {
        val index = dao.getAllRecordsOnce().associateBy(::matchKey).toMutableMap()
        var updated = 0
        var inserted = 0
        var duplicatedInFile = 0
        val seenInFile = mutableSetOf<String>()

        records.forEach { record ->
            val key = matchKey(record)
            val existing = index[key]
            val isDuplicateInFile = !seenInFile.add(key)

            if (existing != null) {
                val merged = record.copy(id = existing.id)
                dao.update(merged)
                index[key] = merged
                if (isDuplicateInFile) duplicatedInFile++ else updated++
            } else {
                // 새 레코드는 id가 필요하므로 삽입 후 다시 조회해 색인에 넣는다
                dao.insert(record)
                val saved = dao.getByNameAndPhone(record.name, record.phone)
                if (saved != null) index[key] = saved
                if (isDuplicateInFile) duplicatedInFile++ else inserted++
            }
        }
        return ImportSummary(updated, inserted, duplicatedInFile)
    }

    override suspend fun getAllRecordsOnce(): List<BowlingRecord> = dao.getAllRecordsOnce()

    /**
     * 동일인 판정 키: 이름 + 전화번호.
     * 전화번호는 숫자만 남겨 "010-1234-5678"과 "01012345678"을 같게 본다.
     */
    private fun matchKey(record: BowlingRecord): String {
        val name = record.name.trim().replace(Regex("\\s+"), "")
        val phone = record.phone.filter { it.isDigit() }
        return "$name|$phone"
    }
}
