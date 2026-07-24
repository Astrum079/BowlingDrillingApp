/** app/src/main/java/com/bowling/drilling/data/mapper/ExcelMapper.kt – 엑셀 Row → BowlingRecord 변환 */
package com.bowling.drilling.data.mapper

import com.bowling.drilling.data.entity.BowlingRecord
import org.apache.poi.ss.usermodel.Row

object ExcelMapper {
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
        val name = row.getCell(COL_NAME)?.toString()?.trim().orEmpty()
        if (name.isEmpty()) return null

        return BowlingRecord(
            name = name,
            phone = row.getCell(COL_PHONE)?.toString().orEmpty(),
            date = formatDate(row.getCell(COL_DATE)?.toString().orEmpty()),
            notes = row.getCell(COL_NOTES)?.toString().orEmpty(),
            hand = row.getCell(COL_HAND)?.toString()?.takeIf { it.isNotBlank() } ?: "RH",
            midSize = row.getCell(COL_MID_SIZE)?.toString().orEmpty(),
            midX = row.getCell(COL_MID_X)?.toString().orEmpty(),
            midY = row.getCell(COL_MID_Y)?.toString().orEmpty(),
            ringSize = row.getCell(COL_RING_SIZE)?.toString().orEmpty(),
            ringX = row.getCell(COL_RING_X)?.toString().orEmpty(),
            ringY = row.getCell(COL_RING_Y)?.toString().orEmpty(),
            thumbSize = row.getCell(COL_THUMB_SIZE)?.toString().orEmpty(),
            thumbX = row.getCell(COL_THUMB_X)?.toString().orEmpty(),
            thumbY = row.getCell(COL_THUMB_Y)?.toString().orEmpty(),
            spanMid = row.getCell(COL_SPAN_MID)?.toString().orEmpty(),
            spanRing = row.getCell(COL_SPAN_RING)?.toString().orEmpty(),
            bridgeSize = row.getCell(COL_BRIDGE)?.toString().orEmpty()
        )
    }

    private fun formatDate(rawDate: String): String {
        if (rawDate.isBlank()) return ""
        val normalized = rawDate.replace("/", "-")
        return if (normalized.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) normalized else rawDate
    }
}
