package habit.frontend.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import habit.frontend.data.dto.HabitStatsDTO
import habit.frontend.data.repository.HabitRepository
import habit.frontend.domain.mapper.toDomain
import habit.frontend.domain.mapper.toDto
import habit.frontend.domain.model.Habit
import habit.frontend.presentation.state.HabitUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import javax.inject.Inject
data class HabitDetailUiState(
    val isLoading: Boolean = false,
    val stats: HabitStatsDTO? = null,
    val error: String? = null
)
@HiltViewModel
class HabitViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitUiState())
    val uiState: StateFlow<HabitUiState> = _uiState
    private val _detailState = MutableStateFlow(HabitDetailUiState())
    val detailState: StateFlow<HabitDetailUiState> = _detailState


    fun loadHabits() {
        viewModelScope.launch {
            _uiState.value = HabitUiState(isLoading = true)
            try {
                val habits = repository.getHabits().map { it.toDomain() }
                _uiState.value = HabitUiState(habits = habits)
            } catch (e: Exception) {
                _uiState.value = HabitUiState(error = e.message)
            }
        }
    }
    fun addHabit(title: String, description: String, frequency: Int) {
        viewModelScope.launch {
            try {
                val newHabit = Habit(
                    id = null,
                    title = title,
                    description = description,
                    frequency = frequency,
                    createdAt = LocalDateTime.now(),
                    completedDates = emptyList()
                )

                repository.addHabit(newHabit.toDto())
                loadHabits() // обновляем список после добавления
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    fun deleteHabit(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteHabit(id)
                loadHabits()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun markDone(habitId: Long, dateIso: String? = null) {
        viewModelScope.launch {
            try {
                repository.markDone(habitId, dateIso)
                loadHabits()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun toggleDone(habitId: Long, dateIso: String) {
        viewModelScope.launch {
            try {
                repository.toggleDone(habitId, dateIso)
                loadHabits()
                loadStats(habitId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteHabitById(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteHabit(id)
                loadHabits()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun loadStats(habitId: Long) {
        viewModelScope.launch {
            _detailState.value = HabitDetailUiState(isLoading = true)
            try {
                val stats = repository.getStats(habitId)
                _detailState.value = HabitDetailUiState(stats = stats)
            } catch (e: Exception) {
                _detailState.value = HabitDetailUiState(error = e.message)
            }
        }
    }

    fun updateHabit(id: Long, title: String, desc: String, freq: Int) {
        viewModelScope.launch {
            try {
                val updated = Habit(
                    id = id,
                    title = title,
                    description = desc,
                    frequency = freq,
                    createdAt = LocalDateTime.now(),
                    completedDates = emptyList()
                )
                repository.updateHabit(id, updated.toDto())
                loadHabits()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }


}