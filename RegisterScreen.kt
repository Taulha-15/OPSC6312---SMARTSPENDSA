package com.example.smartspendsa.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.smartspendsa.viewmodel.SmartSpendViewModel

@Composable
fun RegisterScreen(vm: SmartSpendViewModel, onDone: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("English") }
    val message by vm.message.collectAsState()
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Create account", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(name, { name = it }, label = { Text("Display name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(password, { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(confirm, { confirm = it }, label = { Text("Confirm password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Text("Language")
        Row {
            listOf("English", "Afrikaans", "isiZulu").forEach {
                FilterChip(selected = language == it, onClick = { language = it }, label = { Text(it) }, modifier = Modifier.padding(end = 4.dp))
            }
        }
        if (message.isNotBlank()) {
            Text(message, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { vm.register(email, password, confirm, name, language, onDone) }, modifier = Modifier.fillMaxWidth()) {
            Text("CREATE ACCOUNT")
        }
        TextButton(onClick = onDone) { Text("Back to login") }
    }
}
