package habit.frontend.domain.mapper

import habit.frontend.data.dto.HabitDTO
import habit.frontend.domain.model.Habit
import java.time.LocalDate
import java.time.OffsetDateTime


fun HabitDTO.toDomain() = Habit(id, title, description, frequency, createdAt, completedDates)
fun Habit.toDto() = HabitDTO(id, title, description, frequency, createdAt, completedDates)
