/** app/src/main/java/com/bowling/drilling/ui/main/MainActivity.kt – 회원 목록 및 검색 화면 */
package com.bowling.drilling.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bowling.drilling.data.entity.BowlingRecord
import com.bowling.drilling.data.importer.ExcelExporter
import com.bowling.drilling.data.importer.ExcelImporter
import com.bowling.drilling.databinding.ActivityMainBinding
import com.bowling.drilling.di.RepositoryModule
import com.bowling.drilling.ui.detail.DetailActivity
import com.bowling.drilling.utils.SortOrder
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: RecordAdapter

    private val importFilePickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { confirmAndImport(it) }
        }

    private val exportFilePickerLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        uri?.let { exportTo(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearch()
        setupSort()
        setupFab()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.records.collect { records ->
                    adapter.submitList(records)
                }
            }
        }
    }

    /** 엑셀을 읽어 결과를 미리 보여주고, 사용자가 확인해야 실제로 덮어쓴다 */
    private fun confirmAndImport(uri: Uri) {
        lifecycleScope.launch {
            val records = try {
                ExcelImporter(contentResolver).importFromUri(uri)
            } catch (e: Exception) {
                showError("가져오기 실패: ${e.message}")
                return@launch
            }

            if (records.isEmpty()) {
                showError("가져올 데이터가 없습니다. 첫 줄은 제목 줄이어야 하고, 이름 칸이 비어 있으면 건너뜁니다.")
                return@launch
            }

            val repository = RepositoryModule.provideRepository()
            val plan = try {
                repository.planImport(records)
            } catch (e: Exception) {
                showError("가져오기 실패: ${e.message}")
                return@launch
            }

            val details = buildString {
                appendLine("총 ${plan.total}건을 읽었습니다.")
                appendLine()
                appendLine("• 덮어쓰기: ${plan.updated}건 (이름+전화번호 일치)")
                appendLine("• 새로 추가: ${plan.inserted}건")
                if (plan.duplicatedInFile > 0) {
                    appendLine("• 파일 내 중복: ${plan.duplicatedInFile}건 (마지막 줄만 반영)")
                }
                if (plan.updated > 0) {
                    appendLine()
                    append("덮어쓴 데이터는 되돌릴 수 없습니다.")
                }
            }

            AlertDialog.Builder(this@MainActivity)
                .setTitle("가져오기 확인")
                .setMessage(details)
                .setPositiveButton("가져오기") { _, _ -> runImport(records) }
                .setNegativeButton("취소", null)
                .show()
        }
    }

    private fun runImport(records: List<BowlingRecord>) {
        lifecycleScope.launch {
            try {
                val result = RepositoryModule.provideRepository().importRecords(records)
                Toast.makeText(
                    this@MainActivity,
                    "가져오기 완료 · 덮어쓰기 ${result.updated}건, 추가 ${result.inserted}건",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                showError("가져오기 실패: ${e.message}")
            }
        }
    }

    private fun exportTo(uri: Uri) {
        lifecycleScope.launch {
            try {
                val repository = RepositoryModule.provideRepository()
                val records = repository.getAllRecordsOnce()
                ExcelExporter(contentResolver).exportToUri(uri, records)
                Toast.makeText(this@MainActivity, "내보내기 완료: ${records.size}건", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                showError("내보내기 실패: ${e.message}")
            }
        }
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("오류")
            .setMessage(message)
            .setPositiveButton("확인", null)
            .show()
    }

    private fun setupRecyclerView() {
        adapter = RecordAdapter(
            onClick = { record ->
                val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra("record_id", record.id)
                }
                startActivity(intent)
            },
            onLongClick = { record ->
                AlertDialog.Builder(this)
                    .setTitle("삭제")
                    .setMessage("${record.name} 회원의 데이터를 삭제하시겠습니까?")
                    .setPositiveButton("삭제") { _, _ -> viewModel.deleteRecord(record) }
                    .setNegativeButton("취소", null)
                    .show()
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })
    }

    private fun setupSort() {
        val sortOptions = SortOrder.values().map { it.displayName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.sortSpinner.adapter = adapter
        binding.sortSpinner.setSelection(SortOrder.BY_LAST_MODIFIED.ordinal)

        binding.sortSpinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedSort = SortOrder.values()[position]
                viewModel.setSortOrder(selectedSort)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, DetailActivity::class.java))
        }
        binding.fabImport.setOnClickListener {
            // 일부 파일 관리자/메신저는 xlsx를 octet-stream으로 넘겨서 MIME을 좁히면 선택 자체가 막힌다
            importFilePickerLauncher.launch(arrayOf("*/*"))
        }
        binding.fabExport.setOnClickListener {
            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.getDefault())
                .format(java.util.Date())
            exportFilePickerLauncher.launch("지공데이터_$timestamp.xlsx")
        }
    }
}
