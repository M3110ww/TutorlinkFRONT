package com.tutorlink.app.viewmodel.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorlink.app.data.remote.dto.TutorResponse
import com.tutorlink.app.repository.TutorRepository
import com.tutorlink.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TutorListViewModel @Inject constructor(
    private val repository: TutorRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _tutorsResource = MutableStateFlow<Resource<List<TutorResponse>>>(Resource.Loading())
    
    // Filtro reactivo: Combina la respuesta del API con el texto de búsqueda
    val tutors: StateFlow<Resource<List<TutorResponse>>> = combine(_tutorsResource, _searchQuery) { res, query ->
        if (res is Resource.Success) {
            val list = res.data ?: emptyList()
            if (query.isBlank()) {
                Resource.Success(list)
            } else {
                val filtered = list.filter { 
                    it.tutorName.contains(query, ignoreCase = true) || 
                    it.specialty?.contains(query, ignoreCase = true) == true
                }
                Resource.Success(filtered)
            }
        } else {
            res
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading())

    init {
        getTutors()
    }

    fun getTutors() {
        viewModelScope.launch {
            _tutorsResource.value = Resource.Loading()
            repository.getActiveTutors().collect {
                _tutorsResource.value = it
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}
