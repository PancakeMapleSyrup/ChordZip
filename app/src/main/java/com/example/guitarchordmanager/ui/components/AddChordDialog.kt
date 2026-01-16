package com.example.guitarchordmanager.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.guitarchordmanager.ui.theme.*

@Composable
fun AddChordDialog(
    title: String,
    placeholder: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title, style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        },
        text = {
            SimpleTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = placeholder
            )
        },
        confirmButton = {
            Button(
                onClick = { if (text.isNotBlank()) onConfirm(text) },
                colors = ButtonDefaults.buttonColors(containerColor = TossBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("추가")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = Gray400)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}