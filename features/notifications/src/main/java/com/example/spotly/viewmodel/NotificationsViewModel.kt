package com.example.spotly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotly.domain.model.AppNotification
import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.usecase.MarkNotificationReadUseCase
import com.example.spotly.domain.usecase.ObserveNotificationsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModel(observe: ObserveNotificationsUseCase,
    private val markRead: MarkNotificationReadUseCase) : ViewModel() {
    private val reload = MutableStateFlow(0)
    private val onlyUnread = MutableStateFlow(false)
    private val action = MutableStateFlow(NotificationsUiState())
    private val content = combine(reload, onlyUnread) { _, unread -> unread }.flatMapLatest { unread ->
        observe(unread).map { result ->
            unread to when (result) {
                is AppResult.Success -> NotificationsContent.Success(result.data)
                is AppResult.Error -> NotificationsContent.Error(result.error)
            }
        }.onStart { emit(unread to NotificationsContent.Loading) }
    }
    val uiState = combine(content, action) { (unread, list), pending ->
        pending.copy(content = list, onlyUnread = unread)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000, 0), NotificationsUiState())

    fun retry() { reload.value++ }
    fun filterUnread(value: Boolean) { onlyUnread.value = value }

    fun open(id: String) {
        if (action.value.openingId != null || action.value.destination != null) return
        val item = (uiState.value.content as? NotificationsContent.Success)?.items?.find { it.id == id } ?: return
        action.value = NotificationsUiState(openingId = id)
        viewModelScope.launch {
            val result = if (item.read) AppResult.Success(Unit) else markRead(id)
            action.value = when (result) {
                is AppResult.Success -> NotificationsUiState(destination = item)
                is AppResult.Error -> NotificationsUiState(actionError = result.error)
            }
        }
    }

    fun consumeDestination(): AppNotification? {
        val destination = action.value.destination ?: return null
        action.value = NotificationsUiState()
        return destination
    }
}
