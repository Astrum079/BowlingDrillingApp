/** app/src/main/java/com/bowling/drilling/data/importer/ExcelImporter.kt – SAF URI로 엑셀 파일 읽기 */
package com.bowling.drilling.data.importer

import android.content.ContentResolver
import android.net.Uri
import com.bowling.drilling.data.entity.BowlingRecord
import com.bowling.drilling.data.mapper.ExcelMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream

class ExcelImporter(private val contentResolver: ContentResolver) {

    suspend fun importFromUri(uri: Uri): List<BowlingRecord> = withContext(Dispatchers.IO) {
        val records = mutableListOf<BowlingRecord>()
        var inputStream: InputStream? = null

        try {
            inputStream = contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("파일을 열 수 없습니다: $uri")

            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)

            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i) ?: continue
                val record = ExcelMapper.fromExcelRow(row)
                record?.let { records.add(it) }
            }

            workbook.close()
        } catch (e: Exception) {
            throw Exception("엑셀 파일 파싱 실패: ${e.message}")
        } finally {
            inputStream?.close()
        }

        records
    }
}
