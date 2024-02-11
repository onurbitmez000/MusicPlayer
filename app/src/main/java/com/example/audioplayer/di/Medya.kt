package com.example.audioplayer.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.MediaSession
import com.example.audioplayer.player.service.MuzikAyarlari
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Medya { // Bu nesne, @Module ve @InstallIn(SingletonComponent::class) anotasyonları ile Dagger Hilt tarafından
    // bağımlılık enjeksiyonu için kullanılıyor. Bu, nesnenin uygulama ömrü boyunca tek bir örneğinin oluşturulmasını
    // ve diğer sınıflara sağlanmasını sağlar.

    // Nesnenin içinde, çeşitli fonksiyonlar var. Bu fonksiyonlar, @Provides ve @Singleton anotasyonları ile işaretlenmiştir.
    // Bu, fonksiyonların Dagger Hilt tarafından bağımlılık olarak sağlanabileceğini ve
    // uygulama ömrü boyunca tek bir örneğinin oluşturulacağını gösterir

    @Provides
    @Singleton
    // provideAudioAttributes() fonksiyonu, bir AudioAttributes nesnesi oluşturur. Bu nesne, Muzik oynatıcının ses özelliklerini belirler
    fun MuzikÖzellikleri():AudioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @Provides
    @Singleton
    // provideMediaSession() fonksiyonu, bir MediaSession nesnesi oluşturur. Bu nesne, Muzik oynatıcının medya denetimlerini
    // ve durumunu yönetir
    fun MedyaObjesiOlustur(
        @ApplicationContext context: Context,
        player: ExoPlayer
    ):MediaSession = MediaSession.Builder(context, player).build()


    @OptIn(UnstableApi::class) @Provides
    @Singleton
    // provideExoPlayer() fonksiyonu, bir ExoPlayer nesnesi oluşturur. Bu nesne, Muzik oynatmak için kullanılan bir medya oynatıcıdır
    fun MuzikCalar(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes
    ):ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(audioAttributes, true)
        .setHandleAudioBecomingNoisy(true)
        .setTrackSelector(DefaultTrackSelector(context))
        .build()


    @Provides
    @Singleton
    // provideServiceHandler() fonksiyonu, bir AudioServiceHandler nesnesi oluşturur. Bu nesne, Muzik oynatıcının arka planda çalışmasını sağlar.
    fun MuzikCalarAyari(exoPlayer: ExoPlayer):MuzikAyarlari =
        MuzikAyarlari(exoPlayer)

}