package habit.frontend.data.dto

import com.squareup.moshi.JsonClass
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
@JsonClass(generateAdapter = true)
data class HabitDTO(
    val id: Long?,
    val title: String,
    val description: String,
    val frequency: Int,
    val createdAt: LocalDateTime,
    val completedDates: List<LocalDate>
)