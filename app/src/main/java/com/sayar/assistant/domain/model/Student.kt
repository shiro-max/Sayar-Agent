package com.sayar.assistant.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "students")
data class Student(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val rollNumber: String,
    val grade: String,
    val parentContact: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
