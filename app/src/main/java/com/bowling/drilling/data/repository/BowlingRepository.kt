/** app/src/main/java/com/bowling/drilling/data/repository/BowlingRepository.kt – 저장소 인터페이스 */
package com.bowling.drilling.data.repository

import com.bowling.drilling.data.entity.BowlingRecord
import com.bowling.drilling.utils.SortOrder
import kotlinx.coroutines.flow.Flow

/** 가져오기 결과 요약 (덮어쓸 건수 / 새로 추가할 건수 / 파일 안에서 중복된 건수) */
data class ImportSummary(
    val updated: Int = 0,
    val inserted: Int = 0,
    val duplicatedInFile: Int = 0
) {
    val total: Int get() = updated + inserted + duplicatedInFile
}

interface BowlingRepository {
    fun getAllRecords(searchQuery: String? = null, sortBy: SortOrder = SortOrder.BY_LAST_MODIFIED): Flow<List<BowlingRecord>>
    suspend fun insert(record: BowlingRecord)
    suspend fun update(record: BowlingRecord)
    suspend fun delete(record: BowlingRecord)
    suspend fun getRecordById(id: Long): BowlingRecord?

    /** 실제로 쓰지 않고 결과만 미리 계산한다 (확인 다이얼로그용) */
    suspend fun planImport(records: List<BowlingRecord>): ImportSummary

    /** 이름+전화번호가 일치하면 덮어쓰고, 없으면 새로 추가한다 */
    suspend fun importRecords(records: List<BowlingRecord>): ImportSummary

    suspend fun getAllRecordsOnce(): List<BowlingRecord>
}
