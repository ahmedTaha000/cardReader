package com.arana.cardreader.nfc


import android.os.Parcel
import android.os.Parcelable
 /**
 * Data model representing the information read from the NFC Card.
 */

data class CardData(
    val fieldOne: String? = null,
    val fieldTwo: String? = null,
    val fieldThree: String? = null,
    val fieldFour: String? = null,
    val fieldFive: String? = null,
    val fieldSix: String? = null,
    val fieldSeven: String? = null,
    val fieldEight: String? = null,
    val fieldNine: String? = null,
    val fingerprint1: ByteArray? = null,
    val fingerprint2: ByteArray? = null
  ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.createByteArray(),
        parcel.createByteArray(),
      )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(fieldOne)
        parcel.writeString(fieldTwo)
        parcel.writeString(fieldThree)
        parcel.writeString(fieldFour)
        parcel.writeString(fieldFive)
        parcel.writeString(fieldSix)
        parcel.writeString(fieldSeven)
        parcel.writeString(fieldEight)
        parcel.writeString(fieldNine)
        parcel.writeByteArray(fingerprint1)
        parcel.writeByteArray(fingerprint2)
      }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CardData> {
        override fun createFromParcel(parcel: Parcel): CardData {
            return CardData(parcel)
        }

        override fun newArray(size: Int): Array<CardData?> {
            return arrayOfNulls(size)
        }
    }

     override fun equals(other: Any?): Boolean {
         if (this === other) return true
         if (javaClass != other?.javaClass) return false

         other as CardData

         if (fieldOne != other.fieldOne) return false
         if (fieldTwo != other.fieldTwo) return false
         if (fieldThree != other.fieldThree) return false
         if (fieldFour != other.fieldFour) return false
         if (fieldFive != other.fieldFive) return false
         if (fieldSix != other.fieldSix) return false
         if (fieldSeven != other.fieldSeven) return false
         if (fieldEight != other.fieldEight) return false
         if (fieldNine != other.fieldNine) return false
         if (!fingerprint1.contentEquals(other.fingerprint1)) return false
         if (!fingerprint2.contentEquals(other.fingerprint2)) return false

         return true
     }

     override fun hashCode(): Int {
         var result = fieldOne?.hashCode() ?: 0
         result = 31 * result + (fieldTwo?.hashCode() ?: 0)
         result = 31 * result + (fieldThree?.hashCode() ?: 0)
         result = 31 * result + (fieldFour?.hashCode() ?: 0)
         result = 31 * result + (fieldFive?.hashCode() ?: 0)
         result = 31 * result + (fieldSix?.hashCode() ?: 0)
         result = 31 * result + (fieldSeven?.hashCode() ?: 0)
         result = 31 * result + (fieldEight?.hashCode() ?: 0)
         result = 31 * result + (fieldNine?.hashCode() ?: 0)
         result = 31 * result + (fingerprint1?.contentHashCode() ?: 0)
         result = 31 * result + (fingerprint2?.contentHashCode() ?: 0)
         return result
     }


 }

