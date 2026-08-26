package com.bowling.drilling.ui.detail

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bowling.drilling.data.entity.BowlingRecord
import com.bowling.drilling.databinding.ActivityDetailBinding
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private val viewModel = DetailViewModel()
    private var currentId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        currentId = intent.getLongExtra("record_id", 0L)
        setupPreviewUpdates()
        if (currentId != 0L) lifecycleScope.launch { viewModel.loadRecord(currentId); viewModel.record.collect { it?.let(::bindRecordToUi) } } else updatePreview()
        binding.btnSave.setOnClickListener { saveRecord() }
    }

    private fun setupPreviewUpdates() {
        listOf(binding.etMidSize, binding.etMidXNum, binding.etMidXDen, binding.etMidYNum, binding.etMidYDen, binding.etRingSize, binding.etRingXNum, binding.etRingXDen, binding.etRingYNum, binding.etRingYDen, binding.etThumbSize, binding.etThumbXNum, binding.etThumbXDen, binding.etThumbYNum, binding.etThumbYDen, binding.etSpanMidInt, binding.etSpanMidNum, binding.etSpanMidDen, binding.etSpanRingInt, binding.etSpanRingNum, binding.etSpanRingDen, binding.etBridgeInt, binding.etBridgeNum, binding.etBridgeDen).forEach { field ->
            field.addTextChangedListener(object : TextWatcher { override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit; override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = updatePreview(); override fun afterTextChanged(s: Editable?) = Unit })
        }
        binding.cbMidXNegative.setOnCheckedChangeListener { _, _ -> updatePreview() }; binding.cbMidYNegative.setOnCheckedChangeListener { _, _ -> updatePreview() }; binding.cbRingXNegative.setOnCheckedChangeListener { _, _ -> updatePreview() }; binding.cbRingYNegative.setOnCheckedChangeListener { _, _ -> updatePreview() }; binding.cbThumbXNegative.setOnCheckedChangeListener { _, _ -> updatePreview() }; binding.cbThumbYNegative.setOnCheckedChangeListener { _, _ -> updatePreview() }; binding.rbLeftHand.setOnCheckedChangeListener { _, _ -> updatePreview() }; binding.rbRightHand.setOnCheckedChangeListener { _, _ -> updatePreview() }
    }

    private fun parseInch(value: String): Triple<String, String, String> {
        val parts = value.trim().split(" ", limit = 2)
        val fraction = if (parts.size == 2) parts[1] else parts[0]
        val whole = if (parts.size == 2) parts[0] else if (fraction.contains("/")) "" else fraction
        val nums = if (fraction.contains("/")) fraction.split("/", limit = 2) else listOf("", "")
        return Triple(whole, nums.getOrElse(0) { "" }, nums.getOrElse(1) { "" })
    }

    private fun buildInch(whole: String, numerator: String, denominator: String): String {
        val w = whole.trim(); val n = numerator.trim(); val d = denominator.trim()
        return when { w.isNotEmpty() && n.isNotEmpty() && d.isNotEmpty() -> "$w $n/$d"; w.isNotEmpty() && n.isEmpty() && d.isEmpty() -> w; w.isEmpty() && n.isNotEmpty() && d.isNotEmpty() -> "$n/$d"; else -> "" }
    }

    private fun buildSigned(negative: Boolean, numerator: String, denominator: String): String {
        val signedNumerator = if (negative && numerator.trim().isNotEmpty()) "-${numerator.trim()}" else numerator
        return buildInch("", signedNumerator, denominator)
    }

    private fun setFraction(value: String, whole: android.widget.EditText, numerator: android.widget.EditText, denominator: android.widget.EditText) { val parsed = parseInch(value); whole.setText(parsed.first); numerator.setText(parsed.second); denominator.setText(parsed.third) }

    private fun setFractionOnly(value: String, numerator: android.widget.EditText, denominator: android.widget.EditText) {
        val parsed = parseInch(value)
        numerator.setText(parsed.second)
        denominator.setText(parsed.third)
    }
    private fun setSignedFraction(value: String, negative: android.widget.CheckBox, numerator: android.widget.EditText, denominator: android.widget.EditText) {
        val parsed = parseInch(value)
        negative.isChecked = parsed.second.trim().startsWith("-")
        numerator.setText(parsed.second.trimStart('-'))
        denominator.setText(parsed.third)
    }
    private fun bindRecordToUi(record: BowlingRecord) {
        binding.etName.setText(record.name); binding.etPhone.setText(record.phone); binding.etDate.setText(record.date); binding.etNotes.setText(record.notes)
        binding.rbLeftHand.isChecked = record.hand == "LH"; binding.rbRightHand.isChecked = record.hand != "LH"; binding.etMidSize.setText(record.midSize)
        setSignedFraction(record.midX, binding.cbMidXNegative, binding.etMidXNum, binding.etMidXDen); setSignedFraction(record.midY, binding.cbMidYNegative, binding.etMidYNum, binding.etMidYDen); binding.etRingSize.setText(record.ringSize)
        setSignedFraction(record.ringX, binding.cbRingXNegative, binding.etRingXNum, binding.etRingXDen); setSignedFraction(record.ringY, binding.cbRingYNegative, binding.etRingYNum, binding.etRingYDen); binding.etThumbSize.setText(record.thumbSize)
        setSignedFraction(record.thumbX, binding.cbThumbXNegative, binding.etThumbXNum, binding.etThumbXDen); setSignedFraction(record.thumbY, binding.cbThumbYNegative, binding.etThumbYNum, binding.etThumbYDen)
        setFraction(record.spanMid, binding.etSpanMidInt, binding.etSpanMidNum, binding.etSpanMidDen); setFraction(record.spanRing, binding.etSpanRingInt, binding.etSpanRingNum, binding.etSpanRingDen); setFraction(record.bridgeSize, binding.etBridgeInt, binding.etBridgeNum, binding.etBridgeDen); updatePreview()
    }

    private fun updatePreview() {
        if (!::binding.isInitialized) return
        fun v(id: android.widget.EditText) = id.text.toString()
        fun setPair(numeratorView: android.widget.TextView, denominatorView: android.widget.TextView, numerator: android.widget.EditText, denominator: android.widget.EditText) {
            numeratorView.text = v(numerator); denominatorView.text = v(denominator)
        }
        fun setSignedPair(numeratorView: android.widget.TextView, denominatorView: android.widget.TextView, negative: android.widget.CheckBox, numerator: android.widget.EditText, denominator: android.widget.EditText) {
            val value = v(numerator)
            numeratorView.text = if (negative.isChecked && value.isNotBlank()) "-$value" else value
            denominatorView.text = v(denominator)
        }
        fun setPreviewFraction(wholeView: android.widget.TextView, numeratorView: android.widget.TextView, denominatorView: android.widget.TextView, whole: android.widget.EditText, numerator: android.widget.EditText, denominator: android.widget.EditText) {
            wholeView.text = v(whole); numeratorView.text = v(numerator); denominatorView.text = v(denominator)
        }
        binding.tvPreviewMidSize.text = v(binding.etMidSize); setSignedPair(binding.tvPreviewMidXNum, binding.tvPreviewMidXDen, binding.cbMidXNegative, binding.etMidXNum, binding.etMidXDen); setSignedPair(binding.tvPreviewMidYNum, binding.tvPreviewMidYDen, binding.cbMidYNegative, binding.etMidYNum, binding.etMidYDen)
        binding.tvPreviewRingSize.text = v(binding.etRingSize); setSignedPair(binding.tvPreviewRingXNum, binding.tvPreviewRingXDen, binding.cbRingXNegative, binding.etRingXNum, binding.etRingXDen); setSignedPair(binding.tvPreviewRingYNum, binding.tvPreviewRingYDen, binding.cbRingYNegative, binding.etRingYNum, binding.etRingYDen)
        binding.tvPreviewThumbSize.text = v(binding.etThumbSize); setSignedPair(binding.tvPreviewThumbXNum, binding.tvPreviewThumbXDen, binding.cbThumbXNegative, binding.etThumbXNum, binding.etThumbXDen); setSignedPair(binding.tvPreviewThumbYNum, binding.tvPreviewThumbYDen, binding.cbThumbYNegative, binding.etThumbYNum, binding.etThumbYDen)
        setPreviewFraction(binding.tvPreviewSpanMidInt, binding.tvPreviewSpanMidNum, binding.tvPreviewSpanMidDen, binding.etSpanMidInt, binding.etSpanMidNum, binding.etSpanMidDen); setPreviewFraction(binding.tvPreviewSpanRingInt, binding.tvPreviewSpanRingNum, binding.tvPreviewSpanRingDen, binding.etSpanRingInt, binding.etSpanRingNum, binding.etSpanRingDen);
        binding.tvPreviewHand.text = if (binding.rbLeftHand.isChecked) "LH" else "RH"
    }
    private fun saveRecord() {
        fun v(id: android.widget.EditText) = id.text.toString()
        val record = BowlingRecord(id = currentId, name = v(binding.etName), phone = v(binding.etPhone), date = v(binding.etDate), notes = v(binding.etNotes), hand = if (binding.rbLeftHand.isChecked) "LH" else "RH", midSize = v(binding.etMidSize), midX = buildSigned(binding.cbMidXNegative.isChecked, v(binding.etMidXNum), v(binding.etMidXDen)), midY = buildSigned(binding.cbMidYNegative.isChecked, v(binding.etMidYNum), v(binding.etMidYDen)), ringSize = v(binding.etRingSize), ringX = buildSigned(binding.cbRingXNegative.isChecked, v(binding.etRingXNum), v(binding.etRingXDen)), ringY = buildSigned(binding.cbRingYNegative.isChecked, v(binding.etRingYNum), v(binding.etRingYDen)), thumbSize = v(binding.etThumbSize), thumbX = buildSigned(binding.cbThumbXNegative.isChecked, v(binding.etThumbXNum), v(binding.etThumbXDen)), thumbY = buildSigned(binding.cbThumbYNegative.isChecked, v(binding.etThumbYNum), v(binding.etThumbYDen)), spanMid = buildInch(v(binding.etSpanMidInt), v(binding.etSpanMidNum), v(binding.etSpanMidDen)), spanRing = buildInch(v(binding.etSpanRingInt), v(binding.etSpanRingNum), v(binding.etSpanRingDen)), bridgeSize = buildInch(v(binding.etBridgeInt), v(binding.etBridgeNum), v(binding.etBridgeDen)))
        viewModel.saveRecord(record) { Toast.makeText(this, "저장 완료", Toast.LENGTH_SHORT).show(); finish() }
    }
}