package com.example.spotly.ui.state

import com.example.spotly.domain.model.*
import com.example.spotly.domain.usecase.ObservePostLikesUseCase
import com.example.spotly.domain.usecase.SetPostLikeUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LikeUiState(
    val summary: PostLikes? = null,
    val busy: Boolean = false,
    val error: AppError? = null
)

/** Estado de presentación compartido por Inicio y detalle; cada ViewModel controla su ciclo de vida. */
@OptIn(ExperimentalCoroutinesApi::class)
class PostLikesController(
    private val scope: CoroutineScope,
    private val observeLikes: ObservePostLikesUseCase,
    private val setLike: SetPostLikeUseCase
) {
    private data class Action(val busy: Boolean = false, val error: AppError? = null)
    private val actions = MutableStateFlow<Map<String, Action>>(emptyMap())
    private val retries = MutableStateFlow<Map<String, Int>>(emptyMap())

    fun observe(postIds: List<String>): Flow<Map<String, LikeUiState>> {
        if (postIds.isEmpty()) return flowOf(emptyMap())
        val sources = postIds.distinct().map { id ->
            retries.map { it[id] ?: 0 }.distinctUntilChanged().flatMapLatest {
                observeLikes(id).map { result ->
                    id to when (result) {
                        is AppResult.Success -> LikeUiState(summary = result.data)
                        is AppResult.Error -> LikeUiState(error = result.error)
                    }
                }.onStart { emit(id to LikeUiState()) }
            }
        }
        return combine(combine(sources) { it.toMap() }, actions) { summaries, pending ->
            summaries.mapValues { (id, state) ->
                state.copy(busy = pending[id]?.busy == true, error = pending[id]?.error ?: state.error)
            }
        }
    }

    fun set(postId: String, liked: Boolean) {
        if (actions.value[postId]?.busy == true) return
        actions.update { it + (postId to Action(busy = true)) }
        scope.launch {
            val result = setLike(postId, liked)
            actions.update {
                if (result is AppResult.Error) it + (postId to Action(error = result.error)) else it - postId
            }
        }
    }

    fun retry(postId: String) {
        if (actions.value[postId]?.busy == true) return
        actions.update { it - postId }
        retries.update { it + (postId to ((it[postId] ?: 0) + 1)) }
    }
}
