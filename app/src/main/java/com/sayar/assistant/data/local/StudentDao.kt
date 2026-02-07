package com.sayar.assistant.data.local

import androidx.room.*
import com.sayar.assistant.domain.model.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY grade, rollNumber")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE grade = :grade ORDER BY rollNumber")
    fun getStudentsByGrade(grade: String): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE name LIKE '%' || :query || '%' OR rollNumber LIKE '%' || :query || '%'")
    fun searchStudents(query: String): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentById(id: String): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("DELETE FROM students")
    suspend fun deleteAllStudents()
}
