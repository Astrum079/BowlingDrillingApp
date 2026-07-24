/** app/src/main/java/com/bowling/drilling/ui/detail/DetailViewModel.kt – 상세 입력 ViewModel */
package com.bowling.drilling.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bowling.drilling.data.entity.BowlingRecord
import com.bowling.drilling.data.repository.BowlingRepository
import com.bowling.drilling.di.RepositoryModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {

    private val repository: BowlingRepository = RepositoryModule.provideRepository()

    private val _record = MutableStateFlow<BowlingRecord?>(null)
    val record: StateFlow<BowlingRecord?> = _record

    suspend fun loadRecord(id: Long) {
        val loaded = repository.getRecordById(id)
        _record.value = loaded ?: BowlingRecord()
    }

    fun saveRecord(record: BowlingRecord, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (record.id == 0L) {
                repository.insert(record)
            } else {
                repository.update(record)
            }
            onSuccess()
        }
    }
}
