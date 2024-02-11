package com.example.audioplayer.player.service

import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MuzikServisi: MediaSessionService() {

    @Inject
    lateinit var mediaSession: MediaSession



    // . Bu fonksiyon, mediaSession değişkenini döndürür. onDestroy() fonksiyonu, servis yok edildiğinde çağrılır.
    // Bu fonksiyon, mediaSession nesnesini serbest bırakır ve Muzik oynatıcısını durdurur.

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession =
        mediaSession

    override fun onDestroy() {
        mediaSession.apply {
            release()
            if (player.playbackState != Player.STATE_IDLE){
                player.seekTo(0)
                player.playWhenReady = false
                player.stop()
            }
        }
        super.onDestroy()
    }
}