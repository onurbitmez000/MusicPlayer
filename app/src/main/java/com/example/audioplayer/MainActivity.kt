package com.example.audioplayer

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TopAppBar
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.TopAppBar
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.audioplayer.data.local.model.Sarki
import com.example.audioplayer.player.service.MuzikServisi
import com.example.audioplayer.ui.Ekran
import com.example.audioplayer.ui.audio.SarkiArayuzu
import com.example.audioplayer.ui.audio.AnaEkran
import com.example.audioplayer.ui.audio.ListeEkrani
import com.example.audioplayer.ui.audio.UIEvents
import com.example.audioplayer.ui.items
import com.example.audioplayer.ui.theme.AudioPlayerTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: SarkiArayuzu by viewModels()
    private var isServiceRunning = false
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AudioPlayerTheme {
                val permissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    rememberPermissionState(
                        permission = Manifest.permission.READ_MEDIA_AUDIO
                    )
                } else {
                    rememberPermissionState(
                        permission = Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                }
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(key1 = lifecycleOwner){
                    val observer = LifecycleEventObserver{ _, event->
                        if (event == Lifecycle.Event.ON_RESUME){
                            permissionState.launchPermissionRequest()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose{
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }
                @Composable
                fun EkranNavigation() {
                    val musicList = remember { mutableStateListOf<Sarki>()}
                    val navController = rememberNavController()
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                backgroundColor = MaterialTheme.colors.primary,
                            ) {
                                val navBackStackEntry by navController.currentBackStackEntryAsState()
                                val currentDestination = navBackStackEntry?.destination
                                items.forEach { Ekran ->
                                    NavigationBarItem(
                                        icon = { Icon(Ekran.icon, contentDescription = null) },
                                        label = { Text(Ekran.title) },
                                        selected = currentDestination?.hierarchy?.any { it.route == Ekran.route} == true,
                                        onClick = {
                                            navController.navigate(Ekran.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(navController, startDestination = Ekran.Ana.route, Modifier.padding(innerPadding)) {
                            composable(Ekran.Ana.route) { AnaEkran(
                                progress = viewModel.progress,
                                onProgress = {viewModel.ArayuzKomutlari(UIEvents.SeekTo(it))},
                                isAudioPlaying = viewModel.isPlaying,
                                audioList = viewModel.audioList,
                                currentPlayingAudio = viewModel.currentSelectedAudio,
                                onStart = {
                                    viewModel.ArayuzKomutlari(UIEvents.PlayPause)
                                },
                                onItemClick = {
                                    viewModel.ArayuzKomutlari(UIEvents.SelectedAudioChange(it))
                                    startService()
                                },
                                onNext = {
                                    viewModel.ArayuzKomutlari(UIEvents.SeekToNext)
                                },
                                onPrevious = {
                                    viewModel.ArayuzKomutlari(UIEvents.SeekToPrevious)
                                },
                                addedAudioList = musicList
                            ) }
                            composable(Ekran.Liste.route) { ListeEkrani(
                                progress = viewModel.progress,
                                onProgress = {viewModel.ArayuzKomutlari(UIEvents.SeekTo(it))},
                                isAudioPlaying = viewModel.isPlaying,
                                currentPlayingAudio = viewModel.currentSelectedAudio,
                                onStart = {
                                    viewModel.ArayuzKomutlari(UIEvents.PlayPause)
                                },
                                onItemClick = {
                                    viewModel.ArayuzKomutlari(UIEvents.SelectedAudioChange(it))
                                    startService()
                                },
                                onNext = {
                                    viewModel.ArayuzKomutlari(UIEvents.SeekToNext)
                                },
                                onPrevious = {
                                    viewModel.ArayuzKomutlari(UIEvents.SeekToPrevious)
                                },
                                addedAudioList = musicList,
                                audioList = viewModel.audioList,
                            )}
                        }
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    EkranNavigation()
                }
            }

        }
    }

    private fun startService(){
        if (!isServiceRunning){
            val intent = Intent(this, MuzikServisi::class.java)
            startForegroundService(intent)
        }else{
            startService(intent)
        }
        isServiceRunning = true
    }
}


