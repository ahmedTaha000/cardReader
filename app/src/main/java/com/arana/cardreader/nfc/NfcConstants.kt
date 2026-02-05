package com.arana.cardreader.nfc

import android.nfc.tech.MifareClassic

object NfcConstants {
    // Key used for authentication (Sector 3 / Block 12 check)
    // 0x0B, 0x03, 0x7A, 0x53, 0x2E, 0x9F
//    val AUTH_KEY = byteArrayOf(0x0B.toByte(), 0x03.toByte(), 0x7A.toByte(), 0x53.toByte(), 0x2E.toByte(), 0x9F.toByte())
      val AUTH_KEY = MifareClassic.KEY_DEFAULT
      val BLOCH_INDEX = 10



   /*
    | Sector       | Blocks in Sector | Sector Trailer (Stores Keys) |
    | :--- | :--- | :--- |
    | **Sector 0**     | 0, 1, 2 |            **3** |
    | **Sector 1**     | 4, 5, 6 |            **7** |
    | **Sector 2**     | 8, 9, 10 |          **11** |
    | **Sector 3**   | 12, 13, 14 |          **15**      |

can't contain any blocks from sector TRAILER 3, 7, 11, 15


    */




    // Magic bytes to check for fingerprint presence in Block 1 [0x46, 0x4d, 0x52] -> "FMR"
    val FINGERPRINT_MAGIC_HEADER = byteArrayOf(0x46.toByte(), 0x4d.toByte(), 0x52.toByte())

    // Block mappings
    val BLOCKS_FIELD_ONE = intArrayOf(1, 60, 26)
    val BLOCKS_FIELD_TWO = intArrayOf(10, 2, 6)
    val BLOCKS_FIELD_THREE = intArrayOf(28, 25, 61)
    val BLOCKS_FIELD_FOUR = intArrayOf(4, 9, 30)
    val BLOCKS_FIELD_FIVE = intArrayOf(34, 45, 56)
    val BLOCKS_FIELD_SIX = intArrayOf(22, 57, 54)    //36 not accepted
    val BLOCKS_FIELD_SEVEN = intArrayOf(8, 62) // Contains the ID to check blockage

    // Fingerprint data blocks
    // Field Eight (formerly Fingerprint)
    val BLOCKS_FIELD_EIGHT = intArrayOf(
        12, 13, 18, 40, 16, 17, 21, 5, 20, 46, 42, 48,
        29, 41, 37, 14, 38, 50, 44, 33, 49, 52, 53, 58, 24
    )

    val BLOCKS_FIELD_NINE = intArrayOf(20, 22, 60, 44)
//    val BLOCKS_FIELD_NINE = intArrayOf(15, 22, 39, 44)  //dead

    // All writable blocks for formatting the card
    val ALL_WRITABLE_BLOCKS = (BLOCKS_FIELD_ONE + BLOCKS_FIELD_TWO + BLOCKS_FIELD_THREE +
            BLOCKS_FIELD_FOUR + BLOCKS_FIELD_FIVE + BLOCKS_FIELD_SIX + BLOCKS_FIELD_SEVEN +
            BLOCKS_FIELD_EIGHT + BLOCKS_FIELD_NINE).distinct().sorted().toIntArray()
}
