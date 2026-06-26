package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.MotorcycleDatabase
import com.example.data.MotorcycleRepository
import com.example.ui.MotorcycleViewModel
import com.example.ui.MotorcycleViewModelFactory
import com.example.ui.screens.MainAppContainer
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = MotorcycleDatabase.getDatabase(applicationContext)
    val repository = MotorcycleRepository(database.dao())
    val factory = MotorcycleViewModelFactory(repository)
    val viewModel = ViewModelProvider(this, factory)[MotorcycleViewModel::class.java]

    setContent {
      MyApplicationTheme {
        MainAppContainer(viewModel = viewModel)
      }
    }
  }
}
