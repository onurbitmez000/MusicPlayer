package com.example.audioplayer.data.repository

import com.example.audioplayer.data.local.VeriCekme
import com.example.audioplayer.data.local.model.Sarki
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SarkiReposu @Inject constructor(
    private  val VeriCekme : VeriCekme
) {
    suspend fun SarkilariCek():List<Sarki> = withContext(Dispatchers.IO){
        VeriCekme.SarkilariCek()
    }
}




/*


Kod, AudioRepository adında bir sınıf tanımlıyor.
Bu sınıf, @Inject anotasyonu ile Dagger Hilt tarafından bağımlılık enjeksiyonu için kullanılıyor.
Bu, sınıfın ContentResolverHelper sınıfından bir nesne almasını sağlar. ContentResolverHelper sınıfı, önceki kodda tanımlanmıştır
ve yerel olarak depolanan müzik dosyalarını sorgulamak için kullanılır.
Sınıfın içinde, getAudioData() adında bir fonksiyon var. Bu fonksiyon, suspend anahtar kelimesi ile işaretlenmiştir.
 Bu, fonksiyonun Kotlin coroutines1 ile asenkron olarak çalıştığını gösterir.
Coroutines, arka planda çalışan işleri kolaylaştırmak için kullanılan bir programlama tekniğidir2.
Fonksiyon, withContext(Dispatchers.IO) fonksiyonunu kullanarak, contentResolver.getAudioData() fonksiyonunu çağırır
ve bir List<Audio> döndürürwithContext fonksiyonu, coroutine’in çalışacağı bağlamı belirler3Dispatchers.IO ise,
 giriş/çıkış işlemleri için optimize edilmiş bir bağlamdır4. contentResolver.getAudioData() fonksiyonu ise,
 önceki kodda tanımlanmıştır ve yerel olarak depolanan müzik dosyalarını sorgulayarak bir List<Audio> oluşturur.
Böylece, bu kod, ContentResolverHelper sınıfından aldığı nesne ile yerel olarak depolanan müzik dosyalarını sorgulayarak,
 bir List<Audio> oluşturur. Bu liste, uygulamada müzik oynatmak için kullanılabilir.



 */