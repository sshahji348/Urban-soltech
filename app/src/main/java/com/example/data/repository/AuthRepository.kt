package com.example.data.repository

import com.example.data.model.SolarQuotation
import com.example.data.model.UserAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository {

    // Default registered demo account for immediate testing if desired, or sign in/up
    private val defaultUser = UserAccount(
        email = "sshahji348@gmail.com",
        fullName = "Syed Shah",
        phone = "+92 333 6919922",
        city = "Lahore, Pakistan"
    )

    private val _currentUser = MutableStateFlow<UserAccount?>(defaultUser)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    private val _savedQuotations = MutableStateFlow<List<SolarQuotation>>(emptyList())
    val savedQuotations: StateFlow<List<SolarQuotation>> = _savedQuotations.asStateFlow()

    fun signIn(email: String, password: String):Result<UserAccount> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address (e.g., yourname@gmail.com)."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }
        val nameFromEmail = trimmedEmail.substringBefore("@").replace(".", " ").capitalizeWords()
        val account = UserAccount(
            email = trimmedEmail,
            fullName = nameFromEmail,
            city = "Pakistan"
        )
        _currentUser.value = account
        return Result.success(account)
    }

    fun signUp(email: String, password: String, fullName: String, phone: String, city: String): Result<UserAccount> {
        val trimmedEmail = email.trim()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return Result.failure(IllegalArgumentException("Please enter a valid Gmail / Email address."))
        }
        // Strong password check: min 8 characters, at least one digit and one letter
        val hasMinLength = password.length >= 8
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        if (!hasMinLength || !hasLetter || !hasDigit) {
            return Result.failure(IllegalArgumentException("Strong password required: at least 8 characters including letters and numbers."))
        }
        val account = UserAccount(
            email = trimmedEmail,
            fullName = fullName.ifBlank { trimmedEmail.substringBefore("@") },
            phone = phone.ifBlank { "+92 333 6919922" },
            city = city.ifBlank { "Lahore, Pakistan" }
        )
        _currentUser.value = account
        return Result.success(account)
    }

    fun signOut() {
        _currentUser.value = null
    }

    fun saveQuotation(quotation: SolarQuotation) {
        val current = _savedQuotations.value.toMutableList()
        current.removeAll { it.id == quotation.id }
        current.add(0, quotation)
        _savedQuotations.value = current
    }

    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
}
