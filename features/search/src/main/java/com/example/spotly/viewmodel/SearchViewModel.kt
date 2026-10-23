package com.example.spotly.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.usecase.SearchUsersUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchUsers: SearchUsersUseCase,
    private val savedState: SavedStateHandle
) : ViewModel() {
    private val state = MutableStateFlow(SearchUiState(query = savedState.get<String>("query").orEmpty().take(30)))
    val uiState = state.asStateFlow()
    private var request: Job? = null

    init { search() }

    fun changeQuery(query: String) {
        if (query.length > 30 || query == state.value.query) return
        savedState["query"] = query
        state.value = SearchUiState(query)
        search()
    }

    fun retry() {
        if (state.value.result is UserSearchState.Error) search()
    }

    private fun search() {
        request?.cancel()
        val query = state.value.query
        if (query.trim().length < 2) {
            state.value = SearchUiState(query)
            return
        }
        state.value = SearchUiState(query, UserSearchState.Loading)
        request = viewModelScope.launch {
            delay(300)
            val result = searchUsers(query)
            // Una consulta anterior no puede reemplazar los resultados del texto actual.
            ensureActive()
            state.value = SearchUiState(query, when (result) {
                is AppResult.Success -> UserSearchState.Success(result.data)
                is AppResult.Error -> UserSearchState.Error(result.error)
            })
        }
    }
}
