package com.example.spotly.domain.usecase

import com.example.spotly.domain.model.*
import com.example.spotly.domain.repository.*

class GetExplorePageUseCase(private val repository: ExploreRepository, private val auth: AuthRepository) {
    suspend operator fun invoke(following: Set<String>, cursor: ExploreCursor?): AppResult<ExplorePage> {
        val uid = auth.currentUserId() ?: return AppResult.Error(AppError.Unauthenticated)
        val excluded = following + uid
        var next = cursor
        // Una tanda filtrada vacía no significa que se haya terminado la colección.
        while (true) {
            when (val result = repository.load(excluded, next)) {
                is AppResult.Error -> return result
                is AppResult.Success -> {
                    if (auth.currentUserId() != uid) return AppResult.Error(AppError.Unauthenticated)
                    val page = result.data.copy(posts = result.data.posts.filter { it.authorId !in excluded })
                    if (page.posts.isNotEmpty() || page.next == null) return AppResult.Success(page)
                    if (page.next == next) return AppResult.Error(AppError.ExploreLoadFailed)
                    next = page.next
                }
            }
        }
    }
}
