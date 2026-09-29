package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.data.AccountRepository
import com.example.ui.screens.AccountsVaultScreen
import com.example.ui.screens.EmulatorDashboardScreen
import com.example.ui.screens.SettingsAiScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AccountViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = AccountRepository(database.accountDao())
        val viewModel = AccountViewModel(repository)

        setContent {
            MyApplicationTheme {
                var selectedTab by remember { mutableIntStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Emulator") },
                                label = { Text("50x Emulator") },
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Folder, contentDescription = "Vault & PDF") },
                                label = { Text("Accounts & PDF") },
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Settings, contentDescription = "AI & Settings") },
                                label = { Text("AI & Settings") },
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        when (selectedTab) {
                            0 -> EmulatorDashboardScreen(viewModel)
                            1 -> AccountsVaultScreen(viewModel)
                            2 -> SettingsAiScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}
