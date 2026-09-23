/** app/src/main/java/com/bowling/drilling/data/local/BowlingDao.kt – Room DAO 인터페이스 */
package com.bowling.drilling.data.local

import androidx.room.*
import com.bowling.drilling.data.entity.BowlingRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface BowlingDao {
    @Query("SELECT * FROM bowling_records ORDER BY id DESC")
    fun getAllRecords(): Flow<List<BowlingRecord>>

    @Query("SELECT * FROM bowling_records WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY id DESC")
    fun searchRecords(query: String): Flow<List<BowlingRecord>>

    @Query("SELECT * FROM bowling_records ORDER BY name ASC")
    fun getAllRecordsByName(): Flow<List<BowlingRecord>>

    @Query("SELECT * FROM bowling_records WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchRecordsByName(query: String): Flow<List<BowlingRecord>>

    @Query("SELECT * FROM bowling_records ORDER BY date DESC")
    fun getAllRecordsByDate(): Flow<List<BowlingRecord>>

    @Query("SELECT * FROM bowling_records WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchRecordsByDate(query: String): Flow<List<BowlingRecord>>

    @Query("SELECT * FROM bowling_records ORDER BY lastModified DESC")
    fun getAllRecordsByLastModified(): Flow<List<BowlingRecord>>

    @Query("SELECT * FROM bowling_records WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY lastModified DESC")
    fun searchRecordsByLastModified(query: String): Flow<List<BowlingRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: BowlingRecord)

    @Update
    suspend fun update(record: BowlingRecord)

    @Delete
    suspend fun delete(record: BowlingRecord)

    @Query("SELECT * FROM bowling_records WHERE id = :id")
    suspend fun getRecordById(id: Long): BowlingRecord?

    @Query("SELECT * FROM bowling_records WHERE name = :name AND phone = :phone LIMIT 1")
    suspend fun getByNameAndPhone(name: String, phone: String): BowlingRecord?

    @Query("SELECT * FROM bowling_records ORDER BY id DESC")
    suspend fun getAllRecordsOnce(): List<BowlingRecord>
}
