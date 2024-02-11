package com.example.audioplayer.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SavedSearch
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Ekran(val route: String, val title: String, val icon: ImageVector) {
    data object Ana : Ekran("home_route", "Ara", Icons.Default.SavedSearch)
    data object Liste : Ekran("list_route", "Listed", Icons.Default.ViewList)
}

val items = listOf(
    Ekran.Ana,
    Ekran.Liste
)
