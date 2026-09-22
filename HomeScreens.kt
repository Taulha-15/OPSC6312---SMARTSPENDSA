package com.example.smartspendsa.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartspendsa.data.Category
import com.example.smartspendsa.viewmodel.SmartSpendViewModel
import java.time.LocalDate

@Composable
fun DashboardScreen(vm: SmartSpendViewModel, onAdd: () -> Unit, onBudget: () -> Unit) {
    val user by vm.user.collectAsState()
    val transactions by vm.transactions.collectAsState()
    val categories by vm.categories.collectAsState()
    val income = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val expense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val balance = income - expense

    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Welcome, ${user?.displayName ?: ""}", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("Current Balance")
                    Text("R %.2f".format(balance), style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text("Income"); Text("R %.2f".format(income)) }
                        Column { Text("Expenses"); Text("R %.2f".format(expense)) }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAdd, modifier = Modifier.weight(1f)) { Text("+ Add") }
                OutlinedButton(onClick = onBudget, modifier = Modifier.weight(1f)) { Text("Budget") }
            }
            Spacer(Modifier.height(16.dp))
            Text("Budget Progress", style = MaterialTheme.typography.titleMedium)
            categories.filter { it.budgetLimit > 0 }.forEach { category ->
                val spent = transactions.filter { it.categoryId == category.categoryId && it.type == "EXPENSE" }.sumOf { it.amount }
                val pct = (spent / category.budgetLimit).coerceIn(0.0, 1.0)
                Text("${category.categoryName}: ${(pct * 100).toInt()}%")
                LinearProgressIndicator(progress = { pct.toFloat() }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun TransactionsScreen(vm: SmartSpendViewModel, onAdd: () -> Unit) {
    val transactions by vm.transactions.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text("Add Transaction") }
        Spacer(Modifier.height(8.dp))
        LazyColumn {
            items(transactions) { t ->
                ListItem(
                    headlineContent = { Text("${t.categoryName} - R %.2f".format(t.amount)) },
                    supportingContent = { Text("${t.type} • ${t.date} • ${if (t.syncStatus) "Synced" else "Local"}") },
                    trailingContent = { Text(t.notes) }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun AnalyticsScreen(vm: SmartSpendViewModel) {
    val transactions by vm.transactions.collectAsState()
    val expenses = transactions.filter { it.type == "EXPENSE" }
    val total = expenses.sumOf { it.amount }
    val groups = expenses.groupBy { it.categoryName }.mapValues { it.value.sumOf { t -> t.amount } }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Total Spending", style = MaterialTheme.typography.titleMedium)
            Text("R %.2f".format(total), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Text("Spending by Category", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
        }
        items(groups.entries.toList()) { entry ->
            val pct = if (total == 0.0) 0.0 else entry.value / total
            Text("${entry.key}: R %.2f".format(entry.value))
            LinearProgressIndicator(progress = { pct.toFloat() }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
fun SettingsScreen(vm: SmartSpendViewModel, onLogout: () -> Unit) {
    val user by vm.user.collectAsState()
    var language by remember { mutableStateOf(user?.language ?: "English") }
    var currency by remember { mutableStateOf("ZAR") }
    var notifications by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Account", style = MaterialTheme.typography.titleMedium)
        Text(user?.displayName ?: "")
        Text(user?.email ?: "")
        Spacer(Modifier.height(20.dp))
        Text("Language")
        Row {
            listOf("English", "Afrikaans", "isiZulu").forEach {
                FilterChip(selected = language == it, onClick = { language = it }, label = { Text(it) }, modifier = Modifier.padding(end = 4.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Currency")
        Row {
            listOf("ZAR", "USD").forEach {
                FilterChip(selected = currency == it, onClick = { currency = it }, label = { Text(it) }, modifier = Modifier.padding(end = 4.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Notifications")
            Switch(checked = notifications, onCheckedChange = { notifications = it })
        }
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = { vm.logout(); onLogout() }, modifier = Modifier.fillMaxWidth()) {
            Text("LOG OUT")
        }
    }
}

@Composable
fun AddTransactionDialog(vm: SmartSpendViewModel, onDismiss: () -> Unit) {
    val categories by vm.categories.collectAsState()
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("EXPENSE") }
    var selected by remember { mutableStateOf<Category?>(null) }
    var notes by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add ${if (type == "EXPENSE") "Expense" else "Income"}") },
        text = {
            Column {
                OutlinedTextField(amount, { amount = it }, label = { Text("Amount") })
                Spacer(Modifier.height(8.dp))
                Row {
                    FilterChip(selected = type == "EXPENSE", onClick = { type = "EXPENSE" }, label = { Text("Expense") })
                    Spacer(Modifier.width(8.dp))
                    FilterChip(selected = type == "INCOME", onClick = { type = "INCOME" }, label = { Text("Income") })
                }
                Spacer(Modifier.height(8.dp))
                Text("Category")
                Row {
                    categories.take(4).forEach { c ->
                        FilterChip(selected = selected?.categoryId == c.categoryId, onClick = { selected = c }, label = { Text(c.categoryName) }, modifier = Modifier.padding(end = 3.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(notes, { notes = it }, label = { Text("Notes") })
            }
        },
        confirmButton = {
            Button(onClick = {
                vm.addTransaction(selected, amount, type, LocalDate.now().toString(), notes) { onDismiss() }
            }) { Text("SAVE") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } }
    )
}


@Composable
fun BudgetDialog(vm: SmartSpendViewModel, onDismiss: () -> Unit) {
    val categories by vm.categories.collectAsState()
    var selected by remember { mutableStateOf<Category?>(null) }
    var amount by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Category Budget") },
        text = {
            Column {
                Text("Select a category")
                Row {
                    categories.forEach { c ->
                        FilterChip(
                            selected = selected?.categoryId == c.categoryId,
                            onClick = { selected = c },
                            label = { Text(c.categoryName) },
                            modifier = Modifier.padding(end = 3.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(amount, { amount = it }, label = { Text("Budget amount") })
            }
        },
        confirmButton = {
            Button(onClick = {
                selected?.let { vm.setBudget(it, amount) }
                onDismiss()
            }) { Text("SAVE") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } }
    )
}
