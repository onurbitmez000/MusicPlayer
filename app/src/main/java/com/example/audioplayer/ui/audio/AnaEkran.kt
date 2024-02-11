package com.example.audioplayer.ui.audio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.audioplayer.data.local.model.Sarki
import kotlin.math.floor

// Ana ekrandaki ses çalıcı bileşeni. Progress çubuğu, ses çalma durumu ve
// mevcut çalınan ses gibi çeşitli durumları yönetir.


@Composable
fun İconObjesi(
    icon: ImageVector, // Gösterilecek simge
    borderStroke: BorderStroke? = null, // Kenarlık
    backgroundColor: Color = MaterialTheme.colorScheme.surface, // Arka plan rengi
    color: Color = MaterialTheme.colorScheme.onSurface, // Simge rengi
    onClick: () -> Unit // Tıklama işlevi
) {
    Surface(
        shape = CircleShape,
        border = borderStroke,
        modifier = Modifier
            .clip(CircleShape)
            .clickable {
                onClick()
            },
        contentColor = color,
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier.padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        }
    }
}


// Zaman damgasını süreye dönüştüren yardımcı işlev
private fun ZamanAyari(position: Long): String {
    val totalSecond = floor(position / 1E3).toInt()
    val minutes = totalSecond / 60
    val remainingSeconds = totalSecond - (minutes * 60)
    return if (position < 0) "--:--"
    else "%d:%02d".format(minutes, remainingSeconds)
}


@Composable
fun SanatciBilgisi(
    modifier: Modifier = Modifier,
    audio: Sarki
) {
    Row(
        modifier = modifier.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Spacer(modifier = Modifier.size(4.dp))
        Column {
            audio.baslık?.let {
                Text(
                    text = it,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.size(4.dp))
            audio.Sanatcı?.let {
                Text(
                    text = it,
                    fontWeight = FontWeight.Normal,
                    style = MaterialTheme.typography.bodySmall,
                    overflow = TextOverflow.Clip,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun MuzikObjesi(
    audio: Sarki, // Ses öğesi
    onItemClick: () -> Unit, // Ses öğesi üzerine tıklandığında çağrılan işlev
    onAddClick: () -> Unit, // Ses öğesini eklemek veya kaldırmak için çağrılan işlev
    icon: ImageVector // Ekleme veya silme simgesi
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                onItemClick()
            },

        ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            // En solda gösterilecek IconButton
            IconButton(
                onClick = onAddClick,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    icon,
                    contentDescription = "Add",
                    tint = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                audio.baslık?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                audio.Sanatcı?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = ZamanAyari(audio.uzunluk.toLong()),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun AnaEkran(
    progress: Float, // Ses çalma ilerlemesi
    onProgress: (Float) -> Unit, // İlerleme değişikliği gerçekleştiğinde çağrılan işlev
    isAudioPlaying: Boolean, // Ses çalınıyor mu?
    currentPlayingAudio: Sarki, // Şu anda çalınan ses
    audioList: List<Sarki>, // Oynatılabilir ses listesi
    onStart: () -> Unit, // Ses çalmayı başlatma işlevi
    onItemClick: (Int) -> Unit, // Ses öğesi üzerine tıklandığında çağrılan işlev
    onNext: () -> Unit, // Sonraki sese geçme işlevi
    onPrevious: () -> Unit, // Önceki sese geçme işlevi
    addedAudioList: MutableList<Sarki> // Eklenen ses listesi
) {
    Scaffold(
        bottomBar = {
            MuzikCalarObjesi(
                progress = progress,
                onProgress = onProgress,
                audio = currentPlayingAudio,
                onStart = onStart,
                onNext = onNext,
                onPrevious = onPrevious,
                isAudioPlaying = isAudioPlaying
            )
        }

    ) {
        // İçeriğin üst kısmındaki ve alt kısmındaki boşluğu belirler
        LazyColumn(
            contentPadding = it
        ) {
            itemsIndexed(audioList) { index, audio ->
                MuzikObjesi(
                    audio = audio,
                    onItemClick = { onItemClick(index) },
                    onAddClick = { addedAudioList.add(audio) },
                    icon = Icons.Default.Add
                )
            }
        }
    }
}

@Composable
fun ListeEkrani(
    audioList: List<Sarki>, // Oynatılabilir ses listesi
    progress: Float, // Ses çalma ilerlemesi
    onProgress: (Float) -> Unit, // İlerleme değişikliği gerçekleştiğinde çağrılan işlev
    isAudioPlaying: Boolean, // Ses çalınıyor mu?
    currentPlayingAudio: Sarki, // Şu anda çalınan ses
    onStart: () -> Unit, // Ses çalmayı başlatma işlevi
    onItemClick: (Int) -> Unit, // Ses öğesi üzerine tıklandığında çağrılan işlev
    onNext: () -> Unit, // Sonraki sese geçme işlevi
    onPrevious: () -> Unit, // Önceki sese geçme işlevi
    addedAudioList: MutableList<Sarki> // Eklenen ses listesi

) {
    Scaffold(
        bottomBar = {

            MuzikCalarObjesi(
                progress = progress,
                onProgress = onProgress,
                audio = currentPlayingAudio,
                onStart = onStart,
                onNext = onNext,
                onPrevious = onPrevious,
                isAudioPlaying = isAudioPlaying
            )
        }
    ) {
        LazyColumn(
            contentPadding = it
        ) {
            itemsIndexed(addedAudioList) { _, audio ->
                // Eklenen ses listesi için her bir ses öğesi bileşeni
                MuzikObjesi(
                    audio = audio,
                    onItemClick = { onItemClick(audioList.indexOf(audio)) },
                    onAddClick = { addedAudioList.remove(audio) },
                    icon = Icons.Default.Delete
                )
            }
        }
    }
}

@Composable
fun MuzikCalarKontrol(
    isAudioPlaying: Boolean, // Ses çalınıyor mu?
    onStart: () -> Unit, // Ses çalmayı başlatma işlevi
    onNext: () -> Unit, // Sonraki sese geçme işlevi
    onPrevious: () -> Unit // Önceki sese geçme işlevi
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(70.dp)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.SkipPrevious,
            modifier = Modifier.clickable {
                onPrevious()
            },
            contentDescription = null
        )
        Spacer(modifier = Modifier.size(8.dp))
        İconObjesi(
            icon = if (isAudioPlaying) Icons.Default.Pause
            else Icons.Default.PlayArrow
        ) {
            onStart()
        }
        Spacer(modifier = Modifier.size(8.dp))
        Icon(
            imageVector = Icons.Default.SkipNext,
            modifier = Modifier.clickable {
                onNext()
            },
            contentDescription = null
        )
    }
}

@Composable
fun MuzikCalarObjesi(
    progress: Float, // Ses çalma ilerlemesi
    onProgress: (Float) -> Unit, // İlerleme değişikliği gerçekleştiğinde çağrılan işlev
    audio: Sarki, // Şu anda çalınan ses
    isAudioPlaying: Boolean, // Ses çalınıyor mu?
    onStart: () -> Unit, // Ses çalmayı başlatma işlevi
    onNext: () -> Unit, // Sonraki sese geçme işlevi
    onPrevious: () -> Unit // Önceki sese geçme işlevi
) {

    BottomAppBar(
        modifier = Modifier.fillMaxHeight(0.25f),// Alt çubuğun yüksekliği
        content = {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sanatçı bilgisi bileşeni
                    SanatciBilgisi(
                        audio = audio,
                        modifier = Modifier.weight(1f)
                    )
                    MuzikCalarKontrol(
                        isAudioPlaying, onStart, onNext, onPrevious
                    )
                }
                Slider(
                    value = progress,
                    onValueChange = { onProgress(it) },
                    valueRange = 0f..100f
                )
            }
        }
    )
}






