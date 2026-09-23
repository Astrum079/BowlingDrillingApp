/** app/src/main/java/com/bowling/drilling/data/entity/BowlingRecord.kt – Room 엔티티 (최종 버전) */
package com.bowling.drilling.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bowling_records")
data class BowlingRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 개인정보
    var name: String = "",
    var phone: String = "",
    var date: String = "",
    var notes: String = "",
    var hand: String = "RH",

    // 중지 (Middle Finger)
    var midSize: String = "",
    var midX: String = "",
    var midY: String = "",

    // 약지 (Ring Finger)
    var ringSize: String = "",
    var ringX: String = "",
    var ringY: String = "",

    // 엄지 (Thumb)
    var thumbSize: String = "",
    var thumbX: String = "",
    var thumbY: String = "",

    // 스팬 (Span)
    var spanMid: String = "",
    var spanRing: String = "",
    var bridgeSize: String = "",

    // 타임스탐프
    var lastModified: Long = System.currentTimeMillis()
)
