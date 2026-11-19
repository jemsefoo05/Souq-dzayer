package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.MarketplaceRepository
import com.example.ui.navigation.SouqMainContainer
import com.example.ui.theme.SouqDZTheme
import com.example.ui.viewmodel.MarketplaceViewModel
import com.example.ui.viewmodel.MarketplaceViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = AppDatabase.getDatabase(applicationContext)
    val repository = MarketplaceRepository(
      listingDao = database.listingDao(),
      orderDao = database.orderDao(),
      chatDao = database.chatDao()
    )
    val viewModelFactory = MarketplaceViewModelFactory(repository)
    val viewModel = ViewModelProvider(this, viewModelFactory)[MarketplaceViewModel::class.java]

    setContent {
      SouqDZTheme {
        SouqMainContainer(viewModel = viewModel)
      }
    }
  }
}

