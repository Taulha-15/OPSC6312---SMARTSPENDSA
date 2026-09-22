package com.example.smartspendsa.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.smartspendsa.viewmodel.SmartSpendViewModel

@Composable
fun LoginScreen(vm: SmartSpendViewModel, onLogin: () -> Unit, onRegister: () -> Unit = {}) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val message by vm.message.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("SmartSpend SA", style = MaterialTheme.typography.headlineLarge)
        Text("Manage your money simply.", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (message.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(message, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { vm.login(email, password, onLogin) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("LOGIN")
        }

        TextButton(
            onClick = { vm.clearMessage(); onRegister() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create a new account")
        }
    }
}
