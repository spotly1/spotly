package com.example.spotly.domain.usecase

import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.model.User
import com.example.spotly.domain.repository.UserSearchRepository

class SearchUsersUseCase(private val repository: UserSearchRepository) {
    suspend operator fun invoke(query: String): AppResult<List<User>> {
        val prefix = query.trim().lowercase()
        if (prefix.length !in 2..30) return AppResult.Success(emptyList())
        return repository.search(prefix)
    }
}
