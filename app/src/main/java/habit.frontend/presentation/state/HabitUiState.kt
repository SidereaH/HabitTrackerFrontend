package habit.frontend.presentation.state

import habit.frontend.domain.model.Habit

data class HabitUiState(
    val isLoading: Boolean = false,
    val habits: List<Habit> = emptyList(),
    val error: String? = null
)