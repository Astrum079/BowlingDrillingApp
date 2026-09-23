package com.bowling.drilling.ui.detail

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import android.widget.TextView
import android.util.TypedValue
import kotlin.math.roundToInt
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bowling.drilling.data.entity.BowlingRecord
import com.bowling.drilling.databinding.ActivityDetailBinding
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {
    private enum class AnchorRole { CENTER, LEFT, NUMERATOR, DENOMINATOR }
    private data class PreviewAnchor(val xRatio: Float, val yRatio: Float, val role: AnchorRole)
    private lateinit var binding: ActivityDetailBinding
    private val viewModel: DetailViewModel by viewModels()
    private var currentId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        currentId = intent.getLongExtra("record_id", 0L)
        setupPreviewUpdates()
        setupPreviewScaling()
        // 화면 회전 시에는 DB를 다시 읽지 않는다. 입력 중이던 내용을 덮어써 버리기 때문
        if (currentId != 0L && savedInstanceState == null) {
            lifecycleScope.launch {
                viewModel.loadRecord(currentId)
                viewModel.record.value?.let(::bindRecordToUi)
            }
        }
        binding.root.post { updatePreview() }
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

    /** 분자·분모가 짝을 이루지 못하면 분수는 버리되, 정수 부분은 살린다 */
    private fun buildInch(whole: String, numerator: String, denominator: String): String {
        val w = whole.trim(); val n = numerator.trim(); val d = denominator.trim()
        val fraction = if (n.isNotEmpty() && d.isNotEmpty()) "$n/$d" else ""
        return when {
            w.isNotEmpty() && fraction.isNotEmpty() -> "$w $fraction"
            w.isNotEmpty() -> w
            else -> fraction
        }
    }

    private fun buildSigned(negative: Boolean, numerator: String, denominator: String): String {
        val signedNumerator = if (negative && numerator.trim().isNotEmpty()) "-${numerator.trim()}" else numerator
        return buildInch("", signedNumerator, denominator)
    }

    private fun setFraction(value: String, whole: android.widget.EditText, numerator: android.widget.EditText, denominator: android.widget.EditText) { val parsed = parseInch(value); whole.setText(parsed.first); numerator.setText(parsed.second); denominator.setText(parsed.third) }

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

    // 좌표는 drilling_sheet_bg.png(463x689) 원본에서 각 "(-)" 분수선/원의 실제 중심을 픽셀 단위로 측정한 값
    private fun centerAnchor(x: Float, y: Float) = PreviewAnchor(x / 463f, y / 689f, AnchorRole.CENTER)
    private fun leftAnchor(x: Float, y: Float) = PreviewAnchor(x / 463f, y / 689f, AnchorRole.LEFT)
    private fun numAnchor(x: Float, y: Float) = PreviewAnchor(x / 463f, y / 689f, AnchorRole.NUMERATOR)
    private fun denAnchor(x: Float, y: Float) = PreviewAnchor(x / 463f, y / 689f, AnchorRole.DENOMINATOR)

    private fun setupPreviewScaling() {
        val overlays = (0 until binding.previewCanvas.childCount)
            .map { binding.previewCanvas.getChildAt(it) }
            .filterIsInstance<TextView>()
        val anchors = mapOf<TextView, PreviewAnchor>(
            binding.tvPreviewMidXNum to numAnchor(150f, 70f),
            binding.tvPreviewMidXDen to denAnchor(150f, 70f),
            binding.tvPreviewRingXNum to numAnchor(300f, 70f),
            binding.tvPreviewRingXDen to denAnchor(300f, 70f),
            binding.tvPreviewMidYNum to numAnchor(57f, 185f),
            binding.tvPreviewMidYDen to denAnchor(57f, 185f),
            binding.tvPreviewMidSize to centerAnchor(150f, 179f),
            binding.tvPreviewRingSize to centerAnchor(299f, 179f),
            binding.tvPreviewRingYNum to numAnchor(394f, 194f),
            binding.tvPreviewRingYDen to denAnchor(394f, 194f),
            binding.tvPreviewSpanMidInt to centerAnchor(107f, 345f),
            binding.tvPreviewSpanMidNum to numAnchor(160f, 345f),
            binding.tvPreviewSpanMidDen to denAnchor(160f, 345f),
            binding.tvPreviewSpanRingInt to centerAnchor(288f, 344f),
            binding.tvPreviewSpanRingNum to numAnchor(340f, 344f),
            binding.tvPreviewSpanRingDen to denAnchor(340f, 344f),
            binding.tvPreviewThumbSize to centerAnchor(216f, 482f),
            binding.tvPreviewThumbXNum to numAnchor(391f, 482f),
            binding.tvPreviewThumbXDen to denAnchor(391f, 482f),
            binding.tvPreviewThumbYNum to numAnchor(306f, 582f),
            binding.tvPreviewThumbYDen to denAnchor(306f, 582f),
            binding.tvPreviewHand to leftAnchor(8f, 480f)
        )
        val density = resources.displayMetrics.density
        val baseTextSizes = overlays.associateWith { it.textSize }
        val basePaddings = overlays.associateWith { intArrayOf(it.paddingLeft, it.paddingTop, it.paddingRight, it.paddingBottom) }
        fun applyScale() {
            val imageWidth = binding.ivPreviewSheet.width
            val imageHeight = binding.ivPreviewSheet.height
            if (imageWidth <= 0 || imageHeight <= 0) return
            val positionScaleX = imageWidth.toFloat()
            val positionScaleY = imageHeight.toFloat()
            val textScale = (imageWidth.toFloat() / density) / 463f
            overlays.forEach { view ->
                val anchor = anchors[view] ?: return@forEach
                view.setTextSize(TypedValue.COMPLEX_UNIT_PX, baseTextSizes.getValue(view) * textScale)
                val padding = basePaddings.getValue(view)
                view.setPadding(
                    (padding[0] * textScale).roundToInt(),
                    (padding[1] * textScale).roundToInt(),
                    (padding[2] * textScale).roundToInt(),
                    (padding[3] * textScale).roundToInt()
                )
                val cx = anchor.xRatio * positionScaleX
                val cy = anchor.yRatio * positionScaleY
                val w = view.width.toFloat()
                val h = view.height.toFloat()
                view.translationX = if (anchor.role == AnchorRole.LEFT) cx else cx - w / 2f
                view.translationY = when (anchor.role) {
                    AnchorRole.NUMERATOR -> cy - h
                    AnchorRole.DENOMINATOR -> cy
                    else -> cy - h / 2f
                }
            }
        }
        binding.ivPreviewSheet.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> applyScale() }
        overlays.forEach { it.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> applyScale() } }
        binding.previewCanvas.post { applyScale() }
    }

    private fun updatePreview() {
        if (!::binding.isInitialized) return
        fun v(id: android.widget.EditText) = id.text.toString()
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
    /** 분자·분모 중 한쪽만 채워진 항목을 찾는다. 그대로 저장하면 그 분수는 버려지므로 미리 알린다 */
    private fun findIncompleteFractions(): List<String> {
        val pairs = listOf(
            Triple("중지 X", binding.etMidXNum, binding.etMidXDen),
            Triple("중지 Y", binding.etMidYNum, binding.etMidYDen),
            Triple("약지 X", binding.etRingXNum, binding.etRingXDen),
            Triple("약지 Y", binding.etRingYNum, binding.etRingYDen),
            Triple("엄지 X", binding.etThumbXNum, binding.etThumbXDen),
            Triple("엄지 Y", binding.etThumbYNum, binding.etThumbYDen),
            Triple("스팬(엄지-중지)", binding.etSpanMidNum, binding.etSpanMidDen),
            Triple("스팬(엄지-약지)", binding.etSpanRingNum, binding.etSpanRingDen),
            Triple("브릿지", binding.etBridgeNum, binding.etBridgeDen)
        )
        return pairs.filter { (_, num, den) ->
            num.text.toString().isBlank() != den.text.toString().isBlank()
        }.map { it.first }
    }

    private fun saveRecord() {
        val incomplete = findIncompleteFractions()
        if (incomplete.isEmpty()) {
            performSave()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("입력이 덜 된 항목이 있습니다")
            .setMessage("${incomplete.joinToString(", ")}\n\n분자와 분모 중 한쪽만 입력되어 있습니다. 이대로 저장하면 해당 값은 저장되지 않습니다.")
            .setPositiveButton("그대로 저장") { _, _ -> performSave() }
            .setNegativeButton("계속 입력", null)
            .show()
    }

    private fun performSave() {
        fun v(id: android.widget.EditText) = id.text.toString()
        val record = BowlingRecord(id = currentId, name = v(binding.etName), phone = v(binding.etPhone), date = v(binding.etDate), notes = v(binding.etNotes), hand = if (binding.rbLeftHand.isChecked) "LH" else "RH", midSize = v(binding.etMidSize), midX = buildSigned(binding.cbMidXNegative.isChecked, v(binding.etMidXNum), v(binding.etMidXDen)), midY = buildSigned(binding.cbMidYNegative.isChecked, v(binding.etMidYNum), v(binding.etMidYDen)), ringSize = v(binding.etRingSize), ringX = buildSigned(binding.cbRingXNegative.isChecked, v(binding.etRingXNum), v(binding.etRingXDen)), ringY = buildSigned(binding.cbRingYNegative.isChecked, v(binding.etRingYNum), v(binding.etRingYDen)), thumbSize = v(binding.etThumbSize), thumbX = buildSigned(binding.cbThumbXNegative.isChecked, v(binding.etThumbXNum), v(binding.etThumbXDen)), thumbY = buildSigned(binding.cbThumbYNegative.isChecked, v(binding.etThumbYNum), v(binding.etThumbYDen)), spanMid = buildInch(v(binding.etSpanMidInt), v(binding.etSpanMidNum), v(binding.etSpanMidDen)), spanRing = buildInch(v(binding.etSpanRingInt), v(binding.etSpanRingNum), v(binding.etSpanRingDen)), bridgeSize = buildInch(v(binding.etBridgeInt), v(binding.etBridgeNum), v(binding.etBridgeDen)), lastModified = System.currentTimeMillis())
        viewModel.saveRecord(record) { Toast.makeText(this, "저장 완료", Toast.LENGTH_SHORT).show(); finish() }
    }
}