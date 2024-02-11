package com.example.audioplayer.player.service

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
// Kod, AudioServiceHandler adında bir sınıf tanımlıyor. Bu sınıf, @Inject anotasyonu ile Dagger Hilt tarafından bağımlılık enjeksiyonu için kullanılıyor.
// Bu, sınıfın ExoPlayer sınıfından bir nesne almasını sağlar
// ExoPlayer sınıfı, Muzik oynatmak için kullanılan bir medya oynatıcıdır
// Sınıf, Player.Listener arayüzünden kalıtım alır. Bu arayüz, Muzik oynatıcının durum değişikliklerini dinlemek için kullanılır

class MuzikAyarlari @Inject constructor(
    private val exoPlayer: ExoPlayer
): Player.Listener {
    // _audioState değişkeni, bir MutableStateFlow nesnesi tutar. Bu nesne, Muzik oynatıcının durumunu akış olarak yayınlar
    private val _audioState : MutableStateFlow<Muzik_Durumu> =
        MutableStateFlow(Muzik_Durumu.Initial)
    val audioState: StateFlow<Muzik_Durumu> = _audioState.asStateFlow()

    // job değişkeni, bir Job nesnesi tutar. Bu nesne, arka planda çalışan bir coroutine işini temsil eder
    private var job: Job? = null


    // Başlatıcı blok, exoPlayer.addListener() fonksiyonunu çağırarak, sınıfı Muzik oynatıcının bir dinleyicisi olarak ekler.
    init {
        exoPlayer.addListener(this )
    }


    // startProgressUpdate() adında bir fonksiyon tanımlıyor. Bu fonksiyon, Muzik oynatıcının ilerleme çubuğunu güncellemek için kullanılır.
    private suspend fun Guncelle() = job.run {
        while (true){
            delay(500)
            _audioState.value = Muzik_Durumu.Progress(exoPlayer.currentPosition)
        }
    }
    private fun GuncellemeyiDurdur(){
        job?.cancel()
        _audioState.value = Muzik_Durumu.Playing(isPlaying = false)
    }

    // setMediaItemList() fonksiyonu, bir List<MediaItem> alarak, Muzik oynatıcının oynatacağı medya öğelerini ayarlar
    fun MuzikleriKaydet(mediaItems:List<MediaItem>){
        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()
    }

    // playOrPause() adında bir fonksiyon tanımlıyor. Bu fonksiyon, Muzik oynatıcının oynatma veya duraklatma durumunu değiştirmek için kullanılır
    private suspend fun BaslatveyaDurdur(){
        if (exoPlayer.isPlaying){
            exoPlayer.pause()
            GuncellemeyiDurdur()
        }else{
            exoPlayer.play()
            _audioState.value = Muzik_Durumu.Playing(
                isPlaying = true
            )
            Guncelle()
        }
    }



    // onPlayerEvents() fonksiyonu, bir PlayerEvent alarak, Muzik oynatıcının farklı eylemleri gerçekleştirmesini sağlar. PlayerEvent sınıfı, Muzik oynatıcının denetimlerini temsil eder.
    suspend fun KullanıcıKomutu(
        playerEvent: Komut,
        selectedAudioIndex:Int = -1,
        seekPosition:Long = 0
    ) {
        when(playerEvent){
            Komut.BackWard -> exoPlayer.seekBack()
            Komut.Forward -> exoPlayer.seekForward()
            Komut.SeekToNext -> exoPlayer.seekToNext()
            Komut.SeekToPrevious -> exoPlayer.seekToPrevious()
            Komut.PlayPause -> BaslatveyaDurdur()
            Komut.SeekTo -> exoPlayer.seekTo(seekPosition)
            Komut.SelectedAudioChange -> {
                when(selectedAudioIndex){
                    exoPlayer.currentMediaItemIndex -> {
                        BaslatveyaDurdur()
                    }
                    else -> {
                        exoPlayer.seekToDefaultPosition(selectedAudioIndex)
                        _audioState.value = Muzik_Durumu.Playing(
                            isPlaying = true
                        )
                        exoPlayer.playWhenReady = true
                        Guncelle()
                    }
                }
            }
            Komut.Stop -> GuncellemeyiDurdur()
            is Komut.UpdateProgress -> {
                exoPlayer.seekTo(
                    (exoPlayer.duration * playerEvent.newProgress).toLong()
                )
            }
        }
    }

    // onPlaybackStateChanged() fonksiyonu, Muzik oynatıcının oynatma durumunun değiştiğinde çağrılır.
    override fun onPlaybackStateChanged(playbackState: Int) {
        when(playbackState){
            ExoPlayer.STATE_BUFFERING -> _audioState.value =
                Muzik_Durumu.Buffering(exoPlayer.currentPosition)

            ExoPlayer.STATE_READY -> _audioState.value =
                Muzik_Durumu.Ready(exoPlayer.duration)

            Player.STATE_ENDED -> {

            }

            Player.STATE_IDLE -> {

            }
        }
    }


    //onIsPlayingChanged() fonksiyonu, Muzik oynatıcının oynatma veya duraklatma durumunun değiştiğinde çağrılır.
    @OptIn(DelicateCoroutinesApi::class)
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _audioState.value = Muzik_Durumu.Playing(isPlaying = isPlaying)
        _audioState.value = Muzik_Durumu.CurrentPlaying(exoPlayer.currentMediaItemIndex)
        if (isPlaying){
            GlobalScope.launch(Dispatchers.Main) {
                Guncelle()
            }
        }else{
            GuncellemeyiDurdur()
        }
    }






    /*
    // clearMediaItemList() fonksiyonu, Muzik oynatıcının medya öğelerini temizler.
    fun MuzikleriTemizle(){
        exoPlayer.clearMediaItems()
    }
    */
}


// Bu sınıf, Muzik oynatıcının denetimlerini temsil eden farklı alt sınıflara sahiptir.
// Örneğin, PlayerEvent.PlayPause sınıfı, Muzik oynatıcının oynatma veya duraklatma durumunu değiştirmeyi temsil eder.
sealed class Komut{
    data object PlayPause:Komut()
    data object SelectedAudioChange:Komut()
    data object BackWard:Komut()
    data object SeekToNext:Komut()
    data object  SeekToPrevious:Komut()
    data object Forward:Komut()
    data object SeekTo:Komut()
    data object Stop:Komut()
    data class UpdateProgress(val newProgress:Float):Komut()
}


//Bu sınıf, Muzik oynatıcının durumunu temsil eden farklı alt sınıflara sahiptir.
// Örneğin, AudioState.Initial sınıfı, Muzik oynatıcının başlangıç durumunu temsil eder.
sealed class Muzik_Durumu{
    data object Initial:Muzik_Durumu()
    data class Ready(val duration: Long):Muzik_Durumu()
    data class Progress(val progress: Long):Muzik_Durumu()
    data class Buffering(val progress: Long):Muzik_Durumu()
    data class Playing(val isPlaying: Boolean):Muzik_Durumu()
    data class CurrentPlaying(val mediaItemIndex: Int):Muzik_Durumu()
}