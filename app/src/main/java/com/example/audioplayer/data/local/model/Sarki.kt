package com.example.audioplayer.data.local.model



import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
// müzikleri tuttuğu class.
// Parcelable class'ından inherit etmesinin sebebi uygulama
// içindeki diğer componentlerle
// rahatça veri alışverişi yapabilmek, veriyi saklayabilmek vb.
data class Sarki(
    val uri: Uri?,
    val İsim: String?,
    val id: Long,
    val Sanatcı: String?,
    val data: String?,
    val uzunluk: Int,
    val baslık: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Uri::class.java.classLoader),
        parcel.readString(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString()
    )
    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Sarki> {
        override fun createFromParcel(parcel: Parcel): Sarki {
            return Sarki(parcel)
        }

        override fun newArray(size: Int): Array<Sarki?> {
            return arrayOfNulls(size)
        }
    }
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(uri, flags)
        parcel.writeString(İsim)
        parcel.writeLong(id)
        parcel.writeString(Sanatcı)
        parcel.writeString(data)
        parcel.writeInt(uzunluk)
        parcel.writeString(baslık)
    }


}
