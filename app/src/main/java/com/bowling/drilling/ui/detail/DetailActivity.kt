package com.bowling.drilling.ui.detail

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bowling.drilling.databinding.ActivityDetailBinding
import com.bowling.drilling.data.entity.BowlingRecord
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val viewModel = DetailViewModel()
    private var currentId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentId = intent.getLongExtra("record_id", 0L)

        if (currentId != 0L) {
            lifecycleScope.launch {
                viewModel.loadRecord(currentId)
                viewModel.record.collect { record ->
                    record?.let { bindRecordToUi(it) }
                }
            }
        }

        binding.btnSave.setOnClickListener { saveRecord() }
    }

    /**
     * 세 개의 입력 필드(자연수, 분자, 분모)를 하나의 인치 문자열로 조합
     * 예) 4, 1, 2 → "4 1/2"
     * 예) "", 1, 16 → "1/16"
     * 예) 0, "", "" → "0"
     */
    private fun buildInchString(intPart: String, numPart: String, denPart: String): String {
        val int = intPart.trim()
        val num = numPart.trim()
        val den = denPart.trim()

        return when {
            int.isNotEmpty() && num.isNotEmpty() && den.isNotEmpty() -> "$int $num/$den"
            int.isNotEmpty() && num.isEmpty() && den.isEmpty() -> int
            int.isEmpty() && num.isNotEmpty() && den.isNotEmpty() -> "$num/$den"
            else -> ""
        }
    }

    /**
     * 인치 문자열을 세 부분(자연수, 분자, 분모)으로 분리
     * 예) "4 1/2" → ("4", "1", "2")
     * 예) "1/16" → ("", "1", "16")
     * 예) "0" → ("0", "", "")
     */
    private fun parseInchString(value: String): Triple<String, String, String> {
        if (value.isBlank()) return Triple("", "", "")

        val trimmed = value.trim()
        val parts = trimmed.split(" ")

        return when (parts.size) {
            1 -> {
                if (parts[0].contains("/")) {
                    val frac = parts[0].split("/")
                    Triple("", frac.getOrNull(0) ?: "", frac.getOrNull(1) ?: "")
                } else {
                    Triple(parts[0], "", "")
                }
            }
            2 -> {
                val frac = parts[1].split("/")
                Triple(parts[0], frac.getOrNull(0) ?: "", frac.getOrNull(1) ?: "")
            }
            else -> Triple("", "", "")
        }
    }

    private fun bindRecordToUi(record: BowlingRecord) {
        // 개인정보
        binding.etName.setText(record.name)
        binding.etPhone.setText(record.phone)
        binding.etDate.setText(record.date)
        binding.etNotes.setText(record.notes)

        // 주 손 방향
        if (record.hand == "LH") {
            binding.rbLeftHand.isChecked = true
        } else {
            binding.rbRightHand.isChecked = true
        }

        // 중지
        binding.etMidSize.setText(record.midSize)

        val (midXInt, midXNum, midXDen) = parseInchString(record.midX)
        binding.etMidXInt.setText(midXInt)
        binding.etMidXNum.setText(midXNum)
        binding.etMidXDen.setText(midXDen)

        val (midYInt, midYNum, midYDen) = parseInchString(record.midY)
        binding.etMidYInt.setText(midYInt)
        binding.etMidYNum.setText(midYNum)
        binding.etMidYDen.setText(midYDen)

        // 약지
        binding.etRingSize.setText(record.ringSize)

        val (ringXInt, ringXNum, ringXDen) = parseInchString(record.ringX)
        binding.etRingXInt.setText(ringXInt)
        binding.etRingXNum.setText(ringXNum)
        binding.etRingXDen.setText(ringXDen)

        val (ringYInt, ringYNum, ringYDen) = parseInchString(record.ringY)
        binding.etRingYInt.setText(ringYInt)
        binding.etRingYNum.setText(ringYNum)
        binding.etRingYDen.setText(ringYDen)

        // 엄지
        binding.etThumbSize.setText(record.thumbSize)

        val (thumbXInt, thumbXNum, thumbXDen) = parseInchString(record.thumbX)
        binding.etThumbXInt.setText(thumbXInt)
        binding.etThumbXNum.setText(thumbXNum)
        binding.etThumbXDen.setText(thumbXDen)

        val (thumbYInt, thumbYNum, thumbYDen) = parseInchString(record.thumbY)
        binding.etThumbYInt.setText(thumbYInt)
        binding.etThumbYNum.setText(thumbYNum)
        binding.etThumbYDen.setText(thumbYDen)

        // 스팬
        val (spanMidInt, spanMidNum, spanMidDen) = parseInchString(record.spanMid)
        binding.etSpanMidInt.setText(spanMidInt)
        binding.etSpanMidNum.setText(spanMidNum)
        binding.etSpanMidDen.setText(spanMidDen)

        val (spanRingInt, spanRingNum, spanRingDen) = parseInchString(record.spanRing)
        binding.etSpanRingInt.setText(spanRingInt)
        binding.etSpanRingNum.setText(spanRingNum)
        binding.etSpanRingDen.setText(spanRingDen)

        val (bridgeInt, bridgeNum, bridgeDen) = parseInchString(record.bridgeSize)
        binding.etBridgeInt.setText(bridgeInt)
        binding.etBridgeNum.setText(bridgeNum)
        binding.etBridgeDen.setText(bridgeDen)
    }

    private fun saveRecord() {
        val hand = if (binding.rbLeftHand.isChecked) "LH" else "RH"

        // 모든 인치 필드를 조합
        val midX = buildInchString(
            binding.etMidXInt.text.toString(),
            binding.etMidXNum.text.toString(),
            binding.etMidXDen.text.toString()
        )
        val midY = buildInchString(
            binding.etMidYInt.text.toString(),
            binding.etMidYNum.text.toString(),
            binding.etMidYDen.text.toString()
        )
        val ringX = buildInchString(
            binding.etRingXInt.text.toString(),
            binding.etRingXNum.text.toString(),
            binding.etRingXDen.text.toString()
        )
        val ringY = buildInchString(
            binding.etRingYInt.text.toString(),
            binding.etRingYNum.text.toString(),
            binding.etRingYDen.text.toString()
        )
        val thumbX = buildInchString(
            binding.etThumbXInt.text.toString(),
            binding.etThumbXNum.text.toString(),
            binding.etThumbXDen.text.toString()
        )
        val thumbY = buildInchString(
            binding.etThumbYInt.text.toString(),
            binding.etThumbYNum.text.toString(),
            binding.etThumbYDen.text.toString()
        )
        val spanMid = buildInchString(
            binding.etSpanMidInt.text.toString(),
            binding.etSpanMidNum.text.toString(),
            binding.etSpanMidDen.text.toString()
        )
        val spanRing = buildInchString(
            binding.etSpanRingInt.text.toString(),
            binding.etSpanRingNum.text.toString(),
            binding.etSpanRingDen.text.toString()
        )
        val bridgeSize = buildInchString(
            binding.etBridgeInt.text.toString(),
            binding.etBridgeNum.text.toString(),
            binding.etBridgeDen.text.toString()
        )

        val record = BowlingRecord(
            id = currentId,
            name = binding.etName.text.toString(),
            phone = binding.etPhone.text.toString(),
            date = binding.etDate.text.toString(),
            notes = binding.etNotes.text.toString(),
            hand = hand,
            midSize = binding.etMidSize.text.toString(),
            midX = midX,
            midY = midY,
            ringSize = binding.etRingSize.text.toString(),
            ringX = ringX,
            ringY = ringY,
            thumbSize = binding.etThumbSize.text.toString(),
            thumbX = thumbX,
            thumbY = thumbY,
            spanMid = spanMid,
            spanRing = spanRing,
            bridgeSize = bridgeSize
        )

        viewModel.saveRecord(record) {
            Toast.makeText(this, "저장 완료", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}