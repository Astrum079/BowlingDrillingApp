/** app/src/main/java/com/bowling/drilling/data/mapper/ExcelMapper.kt – 엑셀 Row ↔ BowlingRecord 변환 */
package com.bowling.drilling.data.mapper

import com.bowling.drilling.data.entity.BowlingRecord
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor

object ExcelMapper {
    val HEADERS = arrayOf(
        "이름", "전화번호", "측정일", "메모", "손",
        "중지 사이즈", "중지 X", "중지 Y",
        "약지 사이즈", "약지 X", "약지 Y",
        "엄지 사이즈", "엄지 X", "엄지 Y",
        "스팬(엄지-중지)", "스팬(엄지-약지)", "브릿지"
    )

    private const val COL_NAME = 0
    private const val COL_PHONE = 1
    private const val COL_DATE = 2
    private const val COL_NOTES = 3
    private const val COL_HAND = 4
    private const val COL_MID_SIZE = 5
    private const val COL_MID_X = 6
    private const val COL_MID_Y = 7
    private const val COL_RING_SIZE = 8
    private const val COL_RING_X = 9
    private const val COL_RING_Y = 10
    private const val COL_THUMB_SIZE = 11
    private const val COL_THUMB_X = 12
    private const val COL_THUMB_Y = 13
    private const val COL_SPAN_MID = 14
    private const val COL_SPAN_RING = 15
    private const val COL_BRIDGE = 16

    fun fromExcelRow(row: Row): BowlingRecord? {
        val name = cellText(row.getCell(COL_NAME))
        if (name.isEmpty()) return null

        return BowlingRecord(
            name = name,
            phone = cellText(row.getCell(COL_PHONE)),
            date = formatDate(cellText(row.getCell(COL_DATE))),
            notes = cellText(row.getCell(COL_NOTES)),
            hand = normalizeHand(cellText(row.getCell(COL_HAND))),
            midSize = cellText(row.getCell(COL_MID_SIZE)),
            midX = cellText(row.getCell(COL_MID_X)),
            midY = cellText(row.getCell(COL_MID_Y)),
            ringSize = cellText(row.getCell(COL_RING_SIZE)),
            ringX = cellText(row.getCell(COL_RING_X)),
            ringY = cellText(row.getCell(COL_RING_Y)),
            thumbSize = cellText(row.getCell(COL_THUMB_SIZE)),
            thumbX = cellText(row.getCell(COL_THUMB_X)),
            thumbY = cellText(row.getCell(COL_THUMB_Y)),
            spanMid = cellText(row.getCell(COL_SPAN_MID)),
            spanRing = cellText(row.getCell(COL_SPAN_RING)),
            bridgeSize = cellText(row.getCell(COL_BRIDGE))
        )
    }

    fun writeHeaderRow(sheet: Sheet) {
        val row = sheet.createRow(0)
        HEADERS.forEachIndexed { index, title -> row.createCell(index).setCellValue(title) }
    }

    fun toExcelRow(sheet: Sheet, rowIndex: Int, record: BowlingRecord) {
        val row = sheet.createRow(rowIndex)
        row.createCell(COL_NAME).setCellValue(record.name)
        row.createCell(COL_PHONE).setCellValue(record.phone)
        row.createCell(COL_DATE).setCellValue(record.date)
        row.createCell(COL_NOTES).setCellValue(record.notes)
        row.createCell(COL_HAND).setCellValue(record.hand)
        row.createCell(COL_MID_SIZE).setCellValue(record.midSize)
        row.createCell(COL_MID_X).setCellValue(record.midX)
        row.createCell(COL_MID_Y).setCellValue(record.midY)
        row.createCell(COL_RING_SIZE).setCellValue(record.ringSize)
        row.createCell(COL_RING_X).setCellValue(record.ringX)
        row.createCell(COL_RING_Y).setCellValue(record.ringY)
        row.createCell(COL_THUMB_SIZE).setCellValue(record.thumbSize)
        row.createCell(COL_THUMB_X).setCellValue(record.thumbX)
        row.createCell(COL_THUMB_Y).setCellValue(record.thumbY)
        row.createCell(COL_SPAN_MID).setCellValue(record.spanMid)
        row.createCell(COL_SPAN_RING).setCellValue(record.spanRing)
        row.createCell(COL_BRIDGE).setCellValue(record.bridgeSize)
    }

    /**
     * 셀 타입별로 문자열을 뽑아낸다.
     * Cell.toString()을 그대로 쓰면 숫자 셀이 "31.0", 전화번호가 "1.012345678E9"처럼 깨지므로
     * 정수는 정수로, 날짜는 yyyy-MM-dd로 변환한다.
     */
    private fun cellText(cell: Cell?): String {
        if (cell == null) return ""
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue.trim()
            CellType.NUMERIC -> numericText(cell)
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> when (cell.cachedFormulaResultType) {
                CellType.STRING -> cell.stringCellValue.trim()
                CellType.NUMERIC -> numericText(cell)
                CellType.BOOLEAN -> cell.booleanCellValue.toString()
                else -> ""
            }
            else -> ""
        }
    }

    private fun numericText(cell: Cell): String {
        val isDate = runCatching { DateUtil.isCellDateFormatted(cell) }.getOrDefault(false)
        if (isDate) {
            val date = runCatching { cell.dateCellValue }.getOrNull()
            if (date != null) return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
        }
        val value = cell.numericCellValue
        if (value.isNaN() || value.isInfinite()) return ""
        // 정수면 소수점/지수표기 없이, 소수면 지수표기 없는 평문으로
        return if (value == floor(value) && abs(value) < 1e15) {
            value.toLong().toString()
        } else {
            BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
        }
    }

    /** "왼손", "left", "L", "lh" 등을 모두 LH로 통일. 그 외에는 RH. */
    private fun normalizeHand(raw: String): String {
        val value = raw.trim()
        if (value.isEmpty()) return "RH"
        val upper = value.uppercase(Locale.ROOT)
        return if (upper.startsWith("L") || value.contains("왼")) "LH" else "RH"
    }

    private fun formatDate(rawDate: String): String {
        if (rawDate.isBlank()) return ""
        val normalized = rawDate.replace("/", "-")
        return if (normalized.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) normalized else rawDate
    }
}
