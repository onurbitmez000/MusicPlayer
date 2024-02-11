package com.example.audioplayer.ui.audio

import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.audioplayer.data.local.model.Sarki
import com.example.audioplayer.data.repository.SarkiReposu
import com.example.audioplayer.player.service.MuzikAyarlari
import com.example.audioplayer.player.service.Muzik_Durumu
import com.example.audioplayer.player.service.Komut
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private val DummySarki = Sarki(
    "".toUri(),"",0L,"","",0,""
)

// UI olaylarını temsil eden mühürlü sınıflar
sealed class UIEvents{
    data object PlayPause:UIEvents()
    data class SelectedAudioChange(val index:Int):UIEvents()
    data class SeekTo(val position:Float):UIEvents()
    data object SeekToNext:UIEvents()
    data object SeekToPrevious:UIEvents()
    data object BackWard:UIEvents()
    data object Forward:UIEvents()
    data class UpdateProgress(val newProgress:Float):UIEvents()
}

// UI durumunu temsil eden mühürlü sınıflar
sealed class UIState{
    data object Initial: UIState()
    data object Ready: UIState()
}
@OptIn(SavedStateHandleSaveableApi::class)
@HiltViewModel
class SarkiArayuzu @Inject constructor(
    private val MuzikAyarlari: MuzikAyarlari, // Ses hizmeti işleyicisi
    private val repository: SarkiReposu, // Ses veri deposu
    savedStateHandle: SavedStateHandle // Saklanan durum tutucusu
):ViewModel() {
    // Saklanan durumla ilişkilendirilmiş değişkenler
    private var duration by savedStateHandle.saveable { mutableLongStateOf(0L) }
    var progress by savedStateHandle.saveable { mutableFloatStateOf(0f) }
    private var progressString by savedStateHandle.saveable { mutableStateOf("00:00")}
    var isPlaying by savedStateHandle.saveable { mutableStateOf(false)}
    var currentSelectedAudio by savedStateHandle.saveable { mutableStateOf(DummySarki)}
    var audioList by savedStateHandle.saveable { mutableStateOf(listOf<Sarki>())}
    // UI durumunu yayınlayan bir akış
    private val _uiState : MutableStateFlow<UIState> = MutableStateFlow(UIState.Initial)
    //val uiState: StateFlow<UIState> = _uiState.asStateFlow()
// Verilerin yüklenmesi
    init {
        MuzikleriYukle()
    }
    // Ses hizmeti durumu için akışı izleme
    init {
        viewModelScope.launch {
            MuzikAyarlari.audioState.collectLatest { mediaState ->
                when(mediaState){
                    Muzik_Durumu.Initial -> _uiState.value = UIState.Initial
                    is Muzik_Durumu.Buffering -> Ilerleme(mediaState.progress)
                    is Muzik_Durumu.Playing -> isPlaying = mediaState.isPlaying
                    is Muzik_Durumu.Progress -> Ilerleme(mediaState.progress)
                    is Muzik_Durumu.CurrentPlaying -> {
                        currentSelectedAudio = audioList[mediaState.mediaItemIndex]
                    }
                    is Muzik_Durumu.Ready -> {
                        duration = mediaState.duration
                        _uiState.value = UIState.Ready
                    }
                }
            }
        }
    }

    // Medya öğelerini ayarlama
    private fun MuzikleriAyarla(){
        audioList.map{audio ->
            MediaItem.Builder()
                .setUri(audio.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setAlbumArtist(audio.Sanatcı)
                        .setDisplayTitle(audio.baslık)
                        .setSubtitle(audio.İsim)
                        .build()
                )
                .build()
        }.also {
            MuzikAyarlari.MuzikleriKaydet(it)
        }
    }

    // Ses verilerini yükleme
    private fun MuzikleriYukle(){
        viewModelScope.launch {
            val audio = repository.SarkilariCek()
            audioList = audio
            MuzikleriAyarla()
        }
    }
    // Süreyi biçimlendirme işlevi
    private fun SureyiAyarla(duration:Long):String{
        val minute = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
        val seconds = (minute) - minute * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES)
        return  String.format("%02d:%02d", minute, seconds)
    }

    // İlerleme değerini hesaplama
    private fun Ilerleme(currentProgress:Long){
        progress =
            if (currentProgress > 0) ((currentProgress.toFloat() / duration.toFloat()) * 100f)
        else 0f
        progressString = SureyiAyarla(currentProgress)
    }
    // UI olaylarını işleme
    fun ArayuzKomutlari(uiEvents: UIEvents) = viewModelScope.launch {
        when(uiEvents){
            UIEvents.BackWard -> MuzikAyarlari.KullanıcıKomutu(Komut.BackWard)
            UIEvents.Forward -> MuzikAyarlari.KullanıcıKomutu(Komut.Forward)
            UIEvents.SeekToNext -> MuzikAyarlari.KullanıcıKomutu(Komut.SeekToNext)
            UIEvents.SeekToPrevious -> MuzikAyarlari.KullanıcıKomutu(Komut.SeekToPrevious)

            is UIEvents.PlayPause -> {
                MuzikAyarlari.KullanıcıKomutu(
                    Komut.PlayPause
                )
            }
            is UIEvents.SeekTo -> {
                MuzikAyarlari.KullanıcıKomutu(
                    Komut.SeekTo,
                    seekPosition = ((duration * uiEvents.position) / 100f).toLong()
                )
            }
            is UIEvents.SelectedAudioChange -> {
                MuzikAyarlari.KullanıcıKomutu(
                    Komut.SelectedAudioChange,
                    selectedAudioIndex = uiEvents.index
                )
            }
            is UIEvents.UpdateProgress -> {
                MuzikAyarlari.KullanıcıKomutu(
                    Komut.UpdateProgress(
                        uiEvents.newProgress
                    )
                )
                progress = uiEvents.newProgress
            }
        }
    }

    // Temizleme işlevi
    override fun onCleared() {
        viewModelScope.launch {
            MuzikAyarlari.KullanıcıKomutu(Komut.Stop)
        }
        super.onCleared()
    }
}
