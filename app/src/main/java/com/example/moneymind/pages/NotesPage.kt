package com.example.moneymind.pages

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.moneymind.AuthViewModel
import com.example.moneymind.NotesViewModel
import com.example.moneymind.R
import com.example.moneymind.data.Note
import com.example.moneymind.utils.accessibilityHeading

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotesPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    notesViewModel: NotesViewModel
) {
    val userNotes by notesViewModel.userNotes.observeAsState(emptyList())
    val inputText by notesViewModel.inputText.observeAsState("")
    val errorMessage by notesViewModel.errorMessage.observeAsState()
    val context = LocalContext.current

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var dialogNoteText by remember { mutableStateOf("") }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            notesViewModel.clearErrorMessage()
        }
    }

    val isAuthenticated = authViewModel.isAuthenticated.value
    if (!isAuthenticated) {
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
                    text = stringResource(R.string.please_login_to_view_notes),
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
                    Text(stringResource(R.string.login_now))
                }
            }
        }
        return
    }

    if (showAddNoteDialog) {
        Dialog(
            onDismissRequest = {
                showAddNoteDialog = false
                dialogNoteText = ""
            }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1C1C1C)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.add_new_note),
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        color = Color(0xFF7FBB92),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    TextField(
                        value = dialogNoteText,
                        onValueChange = { dialogNoteText = it },
                        placeholder = {
                            Text(
                                stringResource(R.string.enter_note_placeholder),
                                color = Color.Gray
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .semantics {
                                contentDescription = "Note text input field"
                            },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF262626),
                            unfocusedContainerColor = Color(0xFF262626),
                            cursorColor = Color(0xFF7FBB92),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = false
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showAddNoteDialog = false
                                dialogNoteText = ""
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF7FBB92)
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = SolidColor(Color(0xFF7FBB92))
                            )
                        ) {
                            Text(stringResource(R.string.cancel))
                        }

                        Button(
                            onClick = {
                                if (dialogNoteText.isNotBlank()) {
                                    notesViewModel.insertNote(dialogNoteText)
                                    dialogNoteText = ""
                                    showAddNoteDialog = false
                                } else {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.note_empty_warning),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7FBB92)
                            )
                        ) {
                            Text(stringResource(R.string.save))
                        }
                    }
                }
            }
        }
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
            Text(
                text = stringResource(R.string.notes_heading),
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

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable {
                        showAddNoteDialog = true
                        dialogNoteText = ""
                    },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1C1C1C)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.add_note),
                        color = Color.Gray,
                        modifier = Modifier.semantics {
                            contentDescription = "Click to add a new note"
                        }
                    )
                }
            }

            if (userNotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_notes_message),
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
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