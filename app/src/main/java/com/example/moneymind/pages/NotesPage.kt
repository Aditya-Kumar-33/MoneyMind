package com.example.moneymind.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moneymind.AuthViewModel
import com.example.moneymind.NotesViewModel
import com.example.moneymind.data.Note
import com.example.moneymind.utils.accessibilityHeading

@Composable
fun NotesPage(
    modifier: Modifier = Modifier, 
    navController: NavController, 
    authViewModel: AuthViewModel,
    notesViewModel: NotesViewModel
) {
    // Get notes for the current user
    val userNotes by notesViewModel.userNotes.observeAsState(emptyList())
    val inputText by notesViewModel.inputText.observeAsState("")
    val errorMessage by notesViewModel.errorMessage.observeAsState()
    val context = LocalContext.current
    
    // Show error message if any
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            notesViewModel.clearErrorMessage()
        }
    }
    
    // Check if user is authenticated
    val isAuthenticated = authViewModel.isAuthenticated.value
    if (!isAuthenticated) {
        // Show login prompt if not authenticated
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Please Login to View Your Notes",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { navController.navigate("login") },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7FBB92)
                    )
                ) {
                    Text("Login Now")
                }
            }
        }
        return
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "NOTES",
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                color = Color(0xFF7FBB92),
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .accessibilityHeading()
                    .semantics {
                        heading()
                        contentDescription = "Notes heading"
                    }
            )
            
            // Add Note Input
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1C1C1C)
                )
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { notesViewModel.updateInputText(it) },
                    placeholder = { Text("Add Note...", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "Input field to add a new note"
                        },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Color(0xFF7FBB92),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }
            
            // Empty state message when no notes
            if (userNotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No notes yet. Add your first note!",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Notes List
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(userNotes) { note ->
                        NoteItem(
                            note = note,
                            onDelete = { notesViewModel.deleteNote(note.id) }
                        )
                    }
                }
            }
        }
        
        // FAB with "+" icon at bottom center
        FloatingActionButton(
            onClick = { notesViewModel.insertNote(inputText) },
            shape = CircleShape,
            containerColor = Color(0xFF7FBB92),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .size(56.dp)
                .semantics {
                    contentDescription = "Add note button"
                }
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
fun NoteItem(
    note: Note,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1C1C1C)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = note.text,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier.semantics {
                    contentDescription = "Delete note"
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color.LightGray
                )
            }
        }
    }
} 