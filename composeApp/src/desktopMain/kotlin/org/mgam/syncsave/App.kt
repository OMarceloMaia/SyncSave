package org.mgam.syncsave

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        var isAuthenticated by remember { mutableStateOf(false) }
        var inputText by remember { mutableStateOf("") }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { backup() }
                ) {
                    Text("Backup")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {  }
                ) {
                    Text("Restore")
                }
                Spacer(modifier = Modifier.width(32.dp))
                Button(
                    onClick = { login { success -> isAuthenticated = success} },
                ) {
                    Text(if (isAuthenticated) "Authenticated!" else "Login with Google")
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Game Name") },
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Windows Path") },
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Linux Path") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {  }
            ) {
                Text("Add Game")
            }
        }
    }
}