package com.focusflow.ai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.ai.ui.components.FocusFlowTopBar
import com.focusflow.ai.ui.viewmodel.NoteEditViewModel

@Composable
fun NoteEditScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: NoteEditViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onDone()
    }

    Scaffold(
        topBar = {
            FocusFlowTopBar(
                title = if (state.isEditing) "Edit note" else "New note",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::setTitle,
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.content,
                onValueChange = viewModel::setContent,
                label = { Text("Write your note…") },
                minLines = 8,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isEditing) "Save changes" else "Create note")
            }
        }
    }
}
