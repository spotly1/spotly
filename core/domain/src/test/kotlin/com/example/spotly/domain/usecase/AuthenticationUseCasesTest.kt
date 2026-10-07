package com.example.spotly.domain.usecase

import com.example.spotly.domain.model.*
import com.example.spotly.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Test

class AuthenticationUseCasesTest {
    private class FakeAuth : AuthRepository {
        var calls = 0
        var credentials: List<String>? = null
        var failure: AppError? = null
        val session = MutableStateFlow(true)
        override fun currentUserId(): String? = if (session.value) "alice" else null
        override fun observeAuthentication() = session
        override fun logout() { session.value = false }
        override fun login(email: String, password: String, onResult: (AppResult<Unit>) -> Unit) {
            calls++
            credentials = listOf(email, password)
            onResult(failure?.let { AppResult.Error(it) } ?: AppResult.Success(Unit))
        }
        override fun register(username: String, email: String, password: String, onResult: (AppResult<Unit>) -> Unit) {
            calls++
            credentials = listOf(username, email, password)
            onResult(failure?.let { AppResult.Error(it) } ?: AppResult.Success(Unit))
        }
    }
    private val validator = EmailValidator { it == "alice@example.com" }

    @Test fun loginRejectsMissingOrInvalidFieldsBeforeRepository() {
        val repository = FakeAuth()
        val login = LoginUseCase(repository, validator)
        var error: AppError? = null
        login("", "secret") { result -> result.fold({ fail("No debe iniciar sesión") }, { error = it }) }
        assertEquals(AppError.RequiredFields, error)
        login("invalid", "secret") { result -> result.fold({ fail("No debe iniciar sesión") }, { error = it }) }
        assertEquals(AppError.InvalidEmail, error)
        assertEquals(0, repository.calls)
    }

    @Test fun registrationValidatesEveryRuleWithoutWriting() {
        val repository = FakeAuth()
        val register = RegisterUseCase(repository, validator)
        val cases = listOf(
            listOf("", "alice@example.com", "secret", "secret") to AppError.RequiredFields,
            listOf("ab", "alice@example.com", "secret", "secret") to AppError.UsernameTooShort,
            listOf("alice", "bad", "secret", "secret") to AppError.InvalidEmail,
            listOf("alice", "alice@example.com", "12345", "12345") to AppError.PasswordTooShort,
            listOf("alice", "alice@example.com", "secret", "different") to AppError.PasswordsDoNotMatch
        )
        for ((input, expected) in cases) {
            var error: AppError? = null
            register(input[0], input[1], input[2], input[3]) { result -> result.fold({ fail("No debe registrar") }, { error = it }) }
            assertEquals(expected, error)
        }
        assertEquals(0, repository.calls)
    }

    @Test fun registrationNormalizesUsernameButNotPassword() {
        val repository = FakeAuth()
        var saved = false
        RegisterUseCase(repository, validator)(" Alice ", "alice@example.com", " secret ", " secret ") { result -> result.fold({ saved = true }, { fail(it.name) }) }
        assertTrue(saved)
        assertEquals(listOf("alice", "alice@example.com", " secret "), repository.credentials)
    }

}
