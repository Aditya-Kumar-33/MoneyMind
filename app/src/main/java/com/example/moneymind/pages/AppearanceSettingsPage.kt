package com.example.moneymind.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moneymind.R
import com.example.moneymind.utils.accessibilityHeading

// Route constant for appearance settings
const val APPEARANCE_SETTINGS_ROUTE = "appearance_settings"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsPage(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    // Theme state (would typically come from a ViewModel)
    var isDarkMode by remember { mutableStateOf(false) }
    var useSystemTheme by remember { mutableStateOf(true) }
    var selectedColorTheme by remember { mutableStateOf("Green") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.profile_appearance),
                        modifier = Modifier.accessibilityHeading()
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.semantics {
                            contentDescription = "Back"
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Text(
                        text = stringResource(id = R.string.profile_appearance),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .semantics { heading() }
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Theme section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Theme",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .accessibilityHeading()
                        )
                        
                        // System theme switch
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "Use system theme",
                                modifier = Modifier.weight(1f)
                            )
                            
                            Switch(
                                checked = useSystemTheme,
                                onCheckedChange = { 
                                    useSystemTheme = it
                                },
                                modifier = Modifier.semantics {
                                    contentDescription = "Use system theme, currently ${if (useSystemTheme) "enabled" else "disabled"}"
                                }
                            )
                        }
                        
                        if (!useSystemTheme) {
                            // Dark mode switch
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                
                                Text(
                                    text = if (isDarkMode) "Dark Mode" else "Light Mode",
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 16.dp)
                                )
                                
                                Switch(
                                    checked = isDarkMode,
                                    onCheckedChange = { 
                                        isDarkMode = it
                                    },
                                    modifier = Modifier.semantics {
                                        contentDescription = "Dark mode, currently ${if (isDarkMode) "enabled" else "disabled"}"
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Color theme section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Color Theme",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .accessibilityHeading()
                        )
                        
                        // Green theme option
                        ThemeOption(
                            name = "Green",
                            color = Color(0xFF81A38A),
                            isSelected = selectedColorTheme == "Green",
                            onSelect = { selectedColorTheme = "Green" }
                        )
                        
                        // Blue theme option
                        ThemeOption(
                            name = "Blue",
                            color = Color(0xFF4285F4),
                            isSelected = selectedColorTheme == "Blue",
                            onSelect = { selectedColorTheme = "Blue" }
                        )
                        
                        // Purple theme option
                        ThemeOption(
                            name = "Purple",
                            color = Color(0xFF9C27B0),
                            isSelected = selectedColorTheme == "Purple",
                            onSelect = { selectedColorTheme = "Purple" }
                        )
                        
                        // Orange theme option
                        ThemeOption(
                            name = "Orange",
                            color = Color(0xFFFF9800),
                            isSelected = selectedColorTheme == "Orange",
                            onSelect = { selectedColorTheme = "Orange" }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
fun ThemeOption(
    name: String,
    color: Color,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .semantics {
                contentDescription = "$name theme, ${if (isSelected) "selected" else "not selected"}"
            }
    ) {
        // Color preview
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color)
        )
        
        Text(
            text = name,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        )
        
        RadioButton(
            selected = isSelected,
            onClick = onSelect
        )
    }
} 