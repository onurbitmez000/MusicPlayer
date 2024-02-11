package com.example.audioplayer.data.local

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.WorkerThread
import com.example.audioplayer.data.local.model.Sarki
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


// ContentResolverHelper --> Android uygulamasında yerel olarak depolanan müzik dosyalarını sorgulamak ve bir liste olarak döndürmek için kullanılıyor

class VeriCekme @Inject
constructor(@ApplicationContext val context: Context) {
    private var mCursor: Cursor? = null // Sorgu yapabilmek için gerekli olan cursor objesi

    private val projection: Array<String> = arrayOf( // Sorgu sonucu gelen müziklerin tutulduğu liste
        MediaStore.Audio.AudioColumns.DISPLAY_NAME,
        MediaStore.Audio.AudioColumns._ID,
        MediaStore.Audio.AudioColumns.ARTIST,
        MediaStore.Audio.AudioColumns.DATA,
        MediaStore.Audio.AudioColumns.DURATION,
        MediaStore.Audio.AudioColumns.TITLE,
    )


    private var selectionClause: String? = "${MediaStore.Audio.AudioColumns.IS_MUSIC} = ?" // Sorgu filtresi

    private var selectionArg = arrayOf("1") // Sorgu filtresi

    private val sortOrder = "${MediaStore.Audio.AudioColumns.TITLE} ASC" // Sorgunun sort düzeni

    private fun SarkilariCekCursor(): MutableList<Sarki> { // Telefondaki hafızaya erişip, varsa müzikleri çekip, hepsini tek bir listede toplayıp listeyi döndürüyor
        val sarkiListesi = mutableListOf<Sarki>()

        mCursor = context.contentResolver.query(  // Telefondaki hafızaya erişme
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selectionClause,
            selectionArg,
            sortOrder
        )


        // Hafızadaki müzikle ilgili verileri çekme
        mCursor?.use { cursor ->
            val idColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DISPLAY_NAME)
            val artistColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST)
            val dataColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATA)
            val durationColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION)
            val titleColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE)

            cursor.apply {
                if (count == 0) { // Boş veri gelirse
                    Log.e("Cursor", "getCursorData: Cursor is Empty")
                } else {
                    while (cursor.moveToNext()) {  // Müzik bulunursa
                        val isim = getString(displayNameColumn)
                        val id = getLong(idColumn)
                        val sanatci = getString(artistColumn)
                        val data = getString(dataColumn)
                        val uzunluk = getInt(durationColumn)
                        val baslik = getString(titleColumn)
                        val uri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                        sarkiListesi += Sarki(
                            uri, isim, id, sanatci, data, uzunluk, baslik
                        )
                    }
                }
            }
        }
        return sarkiListesi
    }


    @WorkerThread // --> Bu, fonksiyonun arka planda çalıştığını ve ana iş parçacığına erişmediğini gösterir. ??
    fun SarkilariCek(): List<Sarki> { // Ana fonksiyon
        return SarkilariCekCursor()
    }


}