/** app/src/main/java/com/bowling/drilling/ui/main/MainViewModel.kt – 목록 및 검색 ViewModel */
package com.bowling.drilling.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bowling.drilling.data.entity.BowlingRecord
import com.bowling.drilling.data.repository.BowlingRepository
import com.bowling.drilling.di.RepositoryModule
import com.bowling.drilling.utils.Constants
import com.bowling.drilling.utils.SortOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val repository: BowlingRepository = RepositoryModule.provideRepository()

    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(SortOrder.BY_LAST_MODIFIED)

    private val _records = _searchQuery
        .debounce(Constants.SEARCH_DEBOUNCE_MS)
        .flatMapLatest { query ->
            _sortOrder.flatMapLatest { sort ->
                repository.getAllRecords(query, sort)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val records: StateFlow<List<BowlingRecord>> = _records
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(sortOrder: SortOrder) {
        _sortOrder.value = sortOrder
    }

    fun deleteRecord(record: BowlingRecord) {
        viewModelScope.launch {
            repository.delete(record)
        }
    }
}
