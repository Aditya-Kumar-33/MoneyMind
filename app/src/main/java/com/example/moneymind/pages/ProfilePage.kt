package com.example.moneymind.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moneymind.AuthViewModel
import com.example.moneymind.R
import com.example.moneymind.accessibility.AccessibilityViewModel
import com.example.moneymind.accessibility.TalkBackButton
import com.example.moneymind.language.AppLanguage
import com.example.moneymind.language.LanguageViewModel
import com.example.moneymind.utils.accessibilityHeading

@Composable
fun ProfilePage(
    modifier: Modifier = Modifier, 
    navController: NavController, 
    authViewModel: AuthViewModel,
    accessibilityViewModel: AccessibilityViewModel? = null,
    languageViewModel: LanguageViewModel? = null
) {
    val gradientColors = listOf(
        Color(0xFF7FBB92), // Light green
        Color(0xFF81A38A)  // Darker green
    )
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Profile header with gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        brush = Brush.verticalGradient(colors = gradientColors)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // User email if available
                    authViewModel.getCurrentUser()?.let { user ->
                        Text(
                            text = user.email ?: stringResource(id = R.string.profile_email),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.semantics {
                                contentDescription = "Your email is ${user.email}"
                            }
                        )
                    }
                }
            }
            
            // Profile & Settings heading outside any card
            Text(
                text = stringResource(id = R.string.profile_title),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .accessibilityHeading()
            )
            
            // Language section
            if (languageViewModel != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        LanguageSelector(languageViewModel = languageViewModel)
                    }
                }
            }
            
            // Accessibility Settings section
            if (accessibilityViewModel != null) {
                val accessibilitySettings by accessibilityViewModel.settings.collectAsState()
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.accessibility_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .accessibilityHeading()
                        )
                        
                        // TalkBack button
                        Text(
                            text = "TalkBack",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = "Enable TalkBack to get spoken feedback as you navigate",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        TalkBackButton(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = MaterialTheme.colorScheme.primary
                        )
                        
                        Divider(modifier = Modifier.padding(vertical = 16.dp))
                        
                        // Large Text toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .semantics {
                                    contentDescription = "Large text mode is ${if (accessibilitySettings.largeTextEnabled) "enabled" else "disabled"}"
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.TextFormat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.large_text),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp
                                )
                                
                                Text(
                                    text = "Increase text size for better readability",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Switch(
                                checked = accessibilitySettings.largeTextEnabled,
                                onCheckedChange = { accessibilityViewModel.setLargeTextEnabled(it) }
                            )
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        // High Contrast toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .semantics {
                                    contentDescription = "High contrast mode is ${if (accessibilitySettings.highContrastEnabled) "enabled" else "disabled"}"
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Contrast,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.high_contrast),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp
                                )
                                
                                Text(
                                    text = "Increase contrast for better visibility",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Switch(
                                checked = accessibilitySettings.highContrastEnabled,
                                onCheckedChange = { accessibilityViewModel.setHighContrastEnabled(it) }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sign out button
            Button(
                onClick = {
                    authViewModel.signout()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE57373) // Light red
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .semantics {
                        contentDescription = "Sign out from your account"
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.padding(4.dp))
                
                Text(
                    text = stringResource(id = R.string.profile_signout),
                    fontSize = 16.sp
                )
            }
            
            // Bottom spacer
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelector(
    languageViewModel: LanguageViewModel
) {
    // Get current language
    val currentLanguage by languageViewModel.selectedLanguage.collectAsState()
    val languages = remember { AppLanguage.values().toList() }
    
    // State for dropdown visibility
    var expanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(currentLanguage) }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .semantics {
                contentDescription = "Language selection, current language is ${currentLanguage.displayName}"
            }
    ) {
        // Language icon with colored background
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Language title
        Text(
            text = stringResource(id = R.string.profile_language),
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            fontSize = 16.sp
        )
        
        // Language dropdown
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.semantics {
                    contentDescription = "Current language: ${selectedLanguage.displayName}"
                }
            ) {
                Text(
                    text = selectedLanguage.displayName,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .width(180.dp)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                languages.forEach { language ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = language.displayName,
                                fontWeight = if (language == selectedLanguage) FontWeight.Bold else FontWeight.Normal
                            ) 
                        },
                        onClick = {
                            selectedLanguage = language
                            expanded = false
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = if (language == selectedLanguage) 
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        ),
                        leadingIcon = if (language == selectedLanguage) {
                            {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null
                    )
                }
            }
        }
    }
    
    // Apply button
    if (selectedLanguage != currentLanguage) {
        OutlinedButton(
            onClick = {
                languageViewModel.setLanguage(selectedLanguage)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp)
                .semantics {
                    contentDescription = "Apply language change to ${selectedLanguage.displayName}"
                },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Apply ${selectedLanguage.displayName}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
            .semantics {
                contentDescription = "$title, tap to open settings"
            }
    ) {
        // Icon with colored background
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Text(
            text = title,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            fontSize = 16.sp
        )
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
} 