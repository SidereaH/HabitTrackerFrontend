package habit.frontend.domain.mapper

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateAdapter {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    @ToJson
    fun toJson(value: LocalDate): String = value.format(formatter)

    @FromJson
    fun fromJson(value: String): LocalDate = LocalDate.parse(value, formatter)
}

class LocalDateTimeAdapter {
    private val formatter = DateTimeFormatter.ISO_DATE_TIME

    @ToJson
    fun toJson(value: LocalDateTime): String = value.format(formatter)

    @FromJson
    fun fromJson(value: String): LocalDateTime = LocalDateTime.parse(value, formatter)
}
