package habit.frontend.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

data class Habit(
    val id: Long?,
    val title: String,
    val description: String,
    val frequency: Int,
    val createdAt: LocalDateTime,
    val completedDates: List<LocalDate>
)