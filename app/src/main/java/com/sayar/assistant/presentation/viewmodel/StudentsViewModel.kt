package com.sayar.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sayar.assistant.data.local.StudentDao
import com.sayar.assistant.domain.model.Student
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentsViewModel @Inject constructor(
    private val studentDao: StudentDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val students: StateFlow<List<Student>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                studentDao.getAllStudents()
            } else {
                studentDao.searchStudents(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addStudent(student: Student) {
        viewModelScope.launch {
            studentDao.insertStudent(student)
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            studentDao.updateStudent(student)
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            studentDao.deleteStudent(student)
        }
    }
}
