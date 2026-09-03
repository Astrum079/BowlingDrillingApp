/** app/src/main/java/com/bowling/drilling/data/importer/ExcelExporter.kt – SAF URI로 엑셀 파일 쓰기 */
package com.bowling.drilling.data.importer

import android.content.ContentResolver
import android.net.Uri
import com.bowling.drilling.data.entity.BowlingRecord
import com.bowling.drilling.data.mapper.ExcelMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class ExcelExporter(private val contentResolver: ContentResolver) {

    suspend fun exportToUri(uri: Uri, records: List<BowlingRecord>) = withContext(Dispatchers.IO) {
        val workbook = XSSFWorkbook()
        try {
            val sheet = workbook.createSheet("지공데이터")
            ExcelMapper.writeHeaderRow(sheet)
            records.forEachIndexed { index, record ->
                ExcelMapper.toExcelRow(sheet, index + 1, record)
            }

            val outputStream = contentResolver.openOutputStream(uri)
                ?: throw IllegalArgumentException("파일을 열 수 없습니다: $uri")
            outputStream.use { workbook.write(it) }
        } catch (e: Exception) {
            throw Exception("엑셀 파일 저장 실패: ${e.message}")
        } finally {
            workbook.close()
        }
    }
}
