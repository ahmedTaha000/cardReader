package com.arana.cardreader.nfc


import android.os.Parcelable
 /**
 * Data model representing the information read from the NFC Card.
 */
@Parcelize
data class CardData(
    val fieldOne: String? = null,
    val fieldTwo: String? = null,
    val fieldThree: String? = null,
    val fieldFour: String? = null,
    val fieldFive: String? = null,
    val fieldSix: String? = null,
    val fieldSeven: String? = null,
    val fingerprintTemplate: ByteArray? = null,
    val isBlocked: Boolean = false
) : Parcelable {
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
        if (fingerprintTemplate != null) {
            if (other.fingerprintTemplate == null) return false
            if (!fingerprintTemplate.contentEquals(other.fingerprintTemplate)) return false
        } else if (other.fingerprintTemplate != null) return false
        if (isBlocked != other.isBlocked) return false

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
        result = 31 * result + (fingerprintTemplate?.contentHashCode() ?: 0)
        result = 31 * result + isBlocked.hashCode()
        return result
    }
}

