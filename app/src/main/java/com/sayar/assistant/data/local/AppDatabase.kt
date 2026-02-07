package com.sayar.assistant.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sayar.assistant.domain.model.Student

@Database(
    entities = [Student::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
}
