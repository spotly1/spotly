package com.example.spotly.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.example.spotly.ui.state.PostLikesController
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PostDetailViewModel(
    private val postId: String,
    private val getPost: GetPostUseCase,
    private val updatePost: UpdatePostUseCase,
    private val deletePost: DeletePostUseCase,
    private val currentUserId: GetCurrentUserIdUseCase,
    private val savedState: SavedStateHandle,
    observeLikes: ObservePostLikesUseCase,
    setLike: SetPostLikeUseCase
) : ViewModel() {
    private val state = MutableStateFlow<PostDetailUiState>(PostDetailUiState.Loading)
    private val likes = PostLikesController(viewModelScope, observeLikes, setLike)
    private val postLikes = state.map { it is PostDetailUiState.Content }.distinctUntilChanged().flatMapLatest { visible ->
        likes.observe(if (visible) listOf(postId) else emptyList())
    }
    val uiState = combine(state, postLikes) { current, summaries ->
        if (current is PostDetailUiState.Content) current.copy(likes = summaries[postId] ?: com.example.spotly.ui.state.LikeUiState())
        else current
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000, 0), PostDetailUiState.Loading)

    fun toggleLike() {
        val content = uiState.value as? PostDetailUiState.Content ?: return
        val summary = content.likes.summary ?: return
        if (content.likes.busy || content.action != PostAction.None) return
        likes.set(postId, !summary.likedByMe)
    }

    fun retryLikes() = likes.retry(postId)

    init { load() }

    fun retry() {
        if (state.value is PostDetailUiState.Error) load()
    }

    fun refreshAfterReconnect() {
        val content = state.value as? PostDetailUiState.Content
        if (state.value is PostDetailUiState.Error ||
            (content != null && !content.isEditing && content.action == PostAction.None)) load()
    }

    private fun load() {
        state.value = PostDetailUiState.Loading
        viewModelScope.launch {
            state.value = when (val result = getPost(postId)) {
                is AppResult.Error -> PostDetailUiState.Error(result.error)
                is AppResult.Success -> {
                    val owner = result.data.authorId == currentUserId()
                    val editing = owner && savedState.get<Boolean>("editing") == true
                    PostDetailUiState.Content(
                        post = result.data, isOwner = owner, isEditing = editing,
                        description = if (editing) savedState["description"] ?: result.data.description
                            else result.data.description
                    )
                }
            }
        }
    }

    fun edit() {
        val content = idleOwner() ?: return
        savedState["editing"] = true
        savedState["description"] = content.post.description
        state.value = content.copy(isEditing = true, description = content.post.description, error = null, saved = false)
    }

    fun changeDescription(text: String) {
        val content = idleOwner() ?: return
        if (!content.isEditing || text.length > 1000) return
        savedState["description"] = text
        state.value = content.copy(description = text, error = null)
    }

    fun cancelEdit() {
        val content = idleOwner() ?: return
        clearDraft()
        state.value = content.copy(isEditing = false, description = content.post.description, error = null)
    }

    fun save() {
        val content = idleOwner() ?: return
        if (!content.isEditing) return
        state.value = content.copy(action = PostAction.Saving, error = null)
        viewModelScope.launch {
            state.value = when (val result = updatePost(postId, content.description)) {
                is AppResult.Error -> content.copy(error = result.error)
                is AppResult.Success -> {
                    clearDraft()
                    val text = content.description.trim()
                    content.copy(post = content.post.copy(description = text), description = text,
                        isEditing = false, saved = true, error = null)
                }
            }
        }
    }

    // Solo se llama después de la confirmación de la pantalla.
    fun delete() {
        val content = idleOwner() ?: return
        state.value = content.copy(action = PostAction.Deleting, error = null)
        viewModelScope.launch {
            state.value = when (val result = deletePost(postId)) {
                is AppResult.Error -> content.copy(error = result.error)
                is AppResult.Success -> { clearDraft(); PostDetailUiState.Deleted }
            }
        }
    }

    fun onDeletedHandled(): Boolean {
        if (state.value != PostDetailUiState.Deleted) return false
        state.value = PostDetailUiState.Loading
        return true
    }

    private fun idleOwner() = (state.value as? PostDetailUiState.Content)
        ?.takeIf { it.isOwner && it.action == PostAction.None }

    private fun clearDraft() {
        savedState.remove<Boolean>("editing")
        savedState.remove<String>("description")
    }
}
