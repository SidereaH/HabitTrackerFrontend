package habit.frontend.data.dto

data class HabitStatsDTO(
    val habitId: Long,
    val totalDone: Int,
    val successRate: Double,
    val currentStreak: Int,
    val longestStreak: Int
)