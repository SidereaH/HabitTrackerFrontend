package habit.frontend.data.repository

import habit.frontend.data.api.HabitApi
import habit.frontend.data.dto.HabitDTO

class HabitRepository(private val api: HabitApi) {
    suspend fun getHabits() = api.getAllHabits()
    suspend fun addHabit(habit: HabitDTO) = api.addHabit(habit)
    suspend fun updateHabit(id: Long, habit: HabitDTO) = api.updateHabit(id, habit)
    suspend fun deleteHabit(id: Long) = api.deleteHabit(id)
    suspend fun markDone(id: Long, dateIso: String? = null) = api.markHabitDone(id, dateIso)
    suspend fun toggleDone(id: Long, dateIso: String) = api.toggleHabitDone(id, dateIso)
    suspend fun getStats(id: Long) = api.getStats(id)
}
