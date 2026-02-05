package com.arana.cardreader.nfc

object NfcConstants {
    // Key used for authentication (Sector 3 / Block 12 check)
    // 0x0B, 0x03, 0x7A, 0x53, 0x2E, 0x9F
    val AUTH_KEY = byteArrayOf(
        0x0B.toByte(), 0x03.toByte(), 0x7A.toByte(),
        0x53.toByte(), 0x2E.toByte(), 0x9F.toByte()
    )

    // Magic bytes to check for fingerprint presence in Block 1 [0x46, 0x4d, 0x52] -> "FMR"
    val FINGERPRINT_MAGIC_HEADER = byteArrayOf(0x46.toByte(), 0x4d.toByte(), 0x52.toByte())

    // Block mappings
    val BLOCKS_FIELD_ONE = intArrayOf(1, 60, 26)
    val BLOCKS_FIELD_TWO = intArrayOf(10, 2, 6)
    val BLOCKS_FIELD_THREE = intArrayOf(28, 25, 61)
    val BLOCKS_FIELD_FOUR = intArrayOf(4, 9, 30)
    val BLOCKS_FIELD_FIVE = intArrayOf(34, 45, 56)
    val BLOCKS_FIELD_SIX = intArrayOf(36, 57, 54)
    val BLOCKS_FIELD_SEVEN = intArrayOf(8, 62) // Contains the ID to check blockage

    // Fingerprint data blocks
    val BLOCKS_FINGERPRINT = intArrayOf(
        12, 13, 18, 40, 16, 17, 21, 5, 20, 46, 42, 48,
        29, 41, 37, 14, 38, 50, 44, 33, 49, 52, 53, 58, 24
    )
}