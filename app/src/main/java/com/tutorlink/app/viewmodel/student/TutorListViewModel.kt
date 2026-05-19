package com.tutorlink.app.viewmodel.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorlink.app.data.remote.dto.TutorResponse
import com.tutorlink.app.repository.TutorRepository
import com.tutorlink.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TutorListViewModel @Inject constructor(
    private val repository: TutorRepository
) : ViewModel() {

    private val _tutors = MutableStateFlow<Resource<List<TutorResponse>>>(Resource.Loading())
    val tutors: StateFlow<Resource<List<TutorResponse>>> = _tutors.asStateFlow()

    init {
        getTutors()
    }

    fun getTutors() {
        viewModelScope.launch {
            repository.getActiveTutors().collect {
                _tutors.value = it
            }
        }
    }

    fun searchTutors(query: String) {
        if (query.isBlank()) {
            getTutors()
            return
        }
        viewModelScope.launch {
            repository.searchTutors(query).collect {
                _tutors.value = it
            }
        }
    }
}
