package habit.frontend.data.api


import habit.frontend.data.dto.HabitDTO
import habit.frontend.data.dto.HabitStatsDTO
import retrofit2.http.*

interface HabitApi {
    @GET("habits")
    suspend fun getAllHabits(): List<HabitDTO>

    @POST("habits")
    suspend fun addHabit(@Body habit: HabitDTO): HabitDTO

    @PUT("habits/{id}")
    suspend fun updateHabit(@Path("id") id: Long, @Body habit: HabitDTO): HabitDTO

    @DELETE("habits/{id}")
    suspend fun deleteHabit(@Path("id") id: Long)

    // mark done for specific date (optional param date=YYYY-MM-DD)
    @POST("habits/{id}/done")
    suspend fun markHabitDone(
        @Path("id") id: Long,
        @Query("date") date: String? = null
    ): HabitDTO

    // toggle done on date
    @POST("habits/{id}/toggle")
    suspend fun toggleHabitDone(
        @Path("id") id: Long,
        @Query("date") date: String
    ): HabitDTO

    // stats
    @GET("habits/{id}/stats")
    suspend fun getStats(@Path("id") id: Long): HabitStatsDTO
}
