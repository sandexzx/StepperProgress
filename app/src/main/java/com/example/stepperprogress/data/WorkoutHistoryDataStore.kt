package com.example.stepperprogress.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

// Расширение для DataStore, чтобы получить экземпляр
val Context.workoutHistoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "workout_history")

class WorkoutHistoryDataStore(private val context: Context) {

    private val WORKOUT_RECORDS_KEY = stringPreferencesKey("workout_records")
    private val LAST_CLEANUP_DATE_KEY = stringPreferencesKey("last_cleanup_date")
    private val gson = Gson()

    suspend fun saveWorkoutRecord(record: WorkoutRecord) {
        context.workoutHistoryDataStore.edit { preferences ->
            val currentRecordsJson = preferences[WORKOUT_RECORDS_KEY] ?: "[]"
            val type = object : TypeToken<MutableList<WorkoutRecord>>() {}.type
            val currentRecords: MutableList<WorkoutRecord> = gson.fromJson(currentRecordsJson, type)

            // Добавляем новую запись
            currentRecords.add(record)

            // Сохраняем обновленный список
            preferences[WORKOUT_RECORDS_KEY] = gson.toJson(currentRecords)
        }
    }

    suspend fun loadWorkoutRecords(): List<WorkoutRecord> {
        // Проверяем и очищаем данные, если наступил новый день
        cleanupOldRecords()

        return context.workoutHistoryDataStore.data.map { preferences ->
            val recordsJson = preferences[WORKOUT_RECORDS_KEY] ?: "[]"
            val type = object : TypeToken<List<WorkoutRecord>>() {}.type
            val records: List<WorkoutRecord> = gson.fromJson(recordsJson, type)
            records
        }.first()
    }

    private suspend fun cleanupOldRecords() {
        val currentDay = LocalDate.now(ZoneId.systemDefault())
        context.workoutHistoryDataStore.edit { preferences ->
            val lastCleanupDateStr = preferences[LAST_CLEANUP_DATE_KEY]
            val lastCleanupDate: LocalDate? = if (lastCleanupDateStr != null) {
                LocalDate.parse(lastCleanupDateStr)
            } else {
                null
            }

            // Если последний раз чистили не сегодня, то чистим
            if (lastCleanupDate != currentDay) {
                // Если lastCleanupDate == null, это первый запуск, не чистим, а просто устанавливаем дату
                if (lastCleanupDate != null) {
                    preferences[WORKOUT_RECORDS_KEY] = "[]" // Очищаем все записи
                }
                preferences[LAST_CLEANUP_DATE_KEY] = currentDay.toString() // Обновляем дату последней очистки
            }
        }
    }
}
