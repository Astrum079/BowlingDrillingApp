/** app/src/main/java/com/bowling/drilling/ui/main/MainActivity.kt – 회원 목록 및 검색 화면 */
package com.bowling.drilling.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bowling.drilling.data.importer.ExcelImporter
import com.bowling.drilling.di.RepositoryModule
import com.bowling.drilling.databinding.ActivityMainBinding
import com.bowling.drilling.ui.detail.DetailActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: RecordAdapter

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            lifecycleScope.launch {
                try {
                    val importer = ExcelImporter(contentResolver)
                    val records = importer.importFromUri(it)
                    val repository = RepositoryModule.provideRepository()
                    repository.importRecords(records)
                    android.widget.Toast.makeText(
                        this@MainActivity,
                        "가져오기 완료: ${records.size}개",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    android.widget.Toast.makeText(
                        this@MainActivity,
                        "오류: ${e.message}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = MainViewModel()
        setupRecyclerView()
        setupSearch()
        setupFab()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.records.collect { records ->
                    adapter.submitList(records)
                }
            }
        }
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

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, DetailActivity::class.java))
        }
        binding.fabImport.setOnClickListener {
            filePickerLauncher.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        }
    }
}
