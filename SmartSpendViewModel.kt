package com.example.smartspendsa.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartspendsa.data.*
import com.example.smartspendsa.repository.SmartSpendRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SmartSpendViewModel(app: Application) : AndroidViewModel(app) {
    private val db = DatabaseProvider.get(app)
    private val repo = SmartSpendRepository(app, db)

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _message = MutableStateFlow("")
    val message = _message.asStateFlow()

    val categories = repo.observeCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val transactions = user.flatMapLatest { u ->
        if (u == null) flowOf(emptyList()) else repo.observeTransactions(u.userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch { repo.seedCategories() }
    }

    fun register(email: String, password: String, confirm: String, displayName: String, language: String, onDone: () -> Unit) {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _message.value = "Please enter a valid email."
            return
        }
        if (password.length < 6) {
            _message.value = "Password must be at least 6 characters."
            return
        }
        if (password != confirm) {
            _message.value = "Passwords do not match."
            return
        }
        if (displayName.isBlank()) {
            _message.value = "Please enter your display name."
            return
        }
        viewModelScope.launch {
            repo.register(email.trim().lowercase(), password, displayName.trim(), language)
                .onSuccess { _message.value = "Account created. Please log in."; onDone() }
                .onFailure { _message.value = it.message ?: "Registration failed." }
        }
    }

    fun login(email: String, password: String, onDone: () -> Unit) {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() || password.isBlank()) {
            _message.value = "Enter a valid email and password."
            return
        }
        viewModelScope.launch {
            repo.login(email.trim().lowercase(), password)
                .onSuccess { _user.value = it; _message.value = ""; onDone() }
                .onFailure { _message.value = it.message ?: "Login failed." }
        }
    }

    fun logout() {
        _user.value = null
        _message.value = ""
    }

    fun addTransaction(
        category: Category?, amountText: String, type: String, date: String, notes: String, onDone: () -> Unit
    ) {
        val u = _user.value ?: return
        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _message.value = "Enter a valid amount greater than zero."
            return
        }
        if (category == null) {
            _message.value = "Select a category."
            return
        }
        viewModelScope.launch {
            repo.addTransaction(u.userId, category.categoryId, category.categoryName, amount, type, date, notes)
            _message.value = "Transaction saved. It will sync when online."
            onDone()
        }
    }

    fun sync() {
        val u = _user.value ?: return
        viewModelScope.launch {
            repo.sync(u.userId).onSuccess {
                _message.value = if (it == 0) "Everything is already synced." else "$it transaction(s) synced."
            }.onFailure {
                _message.value = "Offline mode: saved locally. Sync will retry later."
            }
        }
    }

    fun setBudget(category: Category, budgetText: String) {
        val budget = budgetText.toDoubleOrNull()
        if (budget == null || budget < 0) {
            _message.value = "Enter a valid budget."
            return
        }
        viewModelScope.launch {
            repo.setBudget(category, budget)
            _message.value = "Budget updated."
        }
    }

    fun clearMessage() { _message.value = "" }
}
