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
import androidx.compose.foundation.BorderStroke

@Composable
fun ProfilePage(
    modifier: Modifier = Modifier, 
    navController: NavController, 
    authViewModel: AuthViewModel,
    accessibilityViewModel: AccessibilityViewModel? = null,
    languageViewModel: LanguageViewModel? = null
) {
    val gradientColors = listOf(
        Color(0xFF161C18), // Dark green-black
        Color(0xFF0A0F0C)  // Darker green-black
    )
    
    Box(
        modifier = modifier.fillMaxSize()
            .background(Color(0xFF070906)) // Dark background like ChartPage
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
                        val emailDesc = stringResource(R.string.content_desc_email, user.email ?: "")
                        Text(
                            text = user.email ?: stringResource(id = R.string.profile_email),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.semantics {
                                contentDescription = emailDesc
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
                color = Color.White,
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
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF161C18) // Dark card background
                    )
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
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF161C18) // Dark card background
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.accessibility_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White,
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .accessibilityHeading()
                        )
                        
                        // TalkBack button
                        Text(
                            text = "TalkBack",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = stringResource(R.string.enable_talkback_description),
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        TalkBackButton(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = MaterialTheme.colorScheme.primary
                        )
                        
                        Divider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = Color.DarkGray
                        )
                        
                        // Large Text toggle
                        val largeTextEnabled = stringResource(if (accessibilitySettings.largeTextEnabled) R.string.enabled else R.string.disabled)
                        val largeTextDesc = stringResource(R.string.content_desc_large_text, largeTextEnabled)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .semantics {
                                    contentDescription = largeTextDesc
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.TextFormat,
                                contentDescription = null,
                                tint = Color(0xFF81A38A),
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
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                
                                Text(
                                    text = stringResource(id = R.string.large_text_desc),
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                            
                            Switch(
                                checked = accessibilitySettings.largeTextEnabled,
                                onCheckedChange = { accessibilityViewModel.setLargeTextEnabled(it) }
                            )
                        }
                        
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.DarkGray
                        )
                        
                        // High Contrast toggle
                        val enabledText = stringResource(if (accessibilitySettings.highContrastEnabled) R.string.enabled else R.string.disabled)
                        val highContrastDesc = stringResource(R.string.content_desc_high_contrast, enabledText)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .semantics {
                                    contentDescription = highContrastDesc
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Contrast,
                                contentDescription = null,
                                tint = Color(0xFF81A38A),
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
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                
                                Text(
                                    text = stringResource(id = R.string.high_contrast_desc),
                                    fontSize = 14.sp,
                                    color = Color.Gray
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
            val signoutDesc = stringResource(R.string.content_desc_signout)
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
                        contentDescription = signoutDesc
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
    
    val languageSelectionDesc = stringResource(
        R.string.content_desc_language_selection,
        currentLanguage.displayName
    )
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .semantics {
                contentDescription = languageSelectionDesc
            }
    ) {
        // Language icon with colored background
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF81A38A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Language title
        Text(
            text = stringResource(id = R.string.profile_language),
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            fontSize = 16.sp,
            color = Color.White
        )
        
        // Language dropdown
        Box(
            modifier = Modifier
                .background(
                    color = Color(0xFF81A38A).copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            val currentLanguageDesc = stringResource(
                R.string.content_desc_current_language,
                selectedLanguage.displayName
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.semantics {
                    contentDescription = currentLanguageDesc
                }
            ) {
                Text(
                    text = selectedLanguage.displayName,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .width(180.dp)
                    .background(Color(0xFF212121))
            ) {
                languages.forEach { language ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = language.displayName,
                                fontWeight = if (language == selectedLanguage) FontWeight.Bold else FontWeight.Normal,
                                color = if (language == selectedLanguage) 
                                    Color(0xFF81A38A)
                                else
                                    Color.White
                            ) 
                        },
                        onClick = {
                            selectedLanguage = language
                            expanded = false
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = if (language == selectedLanguage) 
                                Color(0xFF81A38A)
                            else
                                Color.White
                        ),
                        leadingIcon = if (language == selectedLanguage) {
                            {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF81A38A),
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
        val applyLanguageDesc = stringResource(
            R.string.content_desc_apply_language,
            selectedLanguage.displayName
        )
        OutlinedButton(
            onClick = {
                languageViewModel.setLanguage(selectedLanguage)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp)
                .semantics {
                    contentDescription = applyLanguageDesc
                },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF81A38A),
                containerColor = Color.Transparent
            ),
            border = BorderStroke(1.dp, Color(0xFF81A38A))
        ) {
            Text(
                text = stringResource(id = R.string.language_apply, selectedLanguage.displayName),
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
    val settingDesc = stringResource(R.string.content_desc_setting_tap, title)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
            .semantics {
                contentDescription = settingDesc
            }
    ) {
        // Icon with colored background
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF81A38A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Text(
            text = title,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            fontSize = 16.sp,
            color = Color.White
        )
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
} 