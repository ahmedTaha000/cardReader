package com.arana.cardreader.nfc

import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import kotlin.text.iterator

class NfcRepository {

    private val TAG = "NfcRepository"

    sealed class NfcResult {
        data class Success(val data: CardData) : NfcResult()
        data class Error(val message: String) : NfcResult()
        object IncompatibleCard : NfcResult()
        object ConnectionLost : NfcResult()
    }

    suspend fun readCard(tag: Tag): NfcResult = withContext(Dispatchers.IO) {
        val mTag = MifareClassic.get(tag) ?: return@withContext NfcResult.Error("Not a Mifare Classic tag")

        try {
            mTag.connect()

            // 1. Check Compatibility (Auth Sector 3 / Block 12)
            if (!authenticateSector(mTag, 12)) {
                // Original logic checks authenticity of block 12 (Sector 3)
                mTag.close()
                return@withContext NfcResult.IncompatibleCard
            }

            // 2. Read ID and Check Blockage (Field Seven)
            val idData = readTextFromBlocks(mTag, NfcConstants.BLOCKS_FIELD_SEVEN)
            val isBlocked = checkIsBlocked(idData)

            if (isBlocked) {
                // If blocked, we might still want to return the data but marked as blocked,
                // or return Error. Logic depends on requirements.
                // Original app marks it but continues or shows error dialog.
                // Here we return data with isBlocked = true
            }

            // 3. Read All Fields
            val fieldOne = readTextFromBlocks(mTag, NfcConstants.BLOCKS_FIELD_ONE)
            val fieldTwo = readTextFromBlocks(mTag, NfcConstants.BLOCKS_FIELD_TWO)
            val fieldThree = readTextFromBlocks(mTag, NfcConstants.BLOCKS_FIELD_THREE)
            val fieldFour = readTextFromBlocks(mTag, NfcConstants.BLOCKS_FIELD_FOUR)
            val fieldFive = readTextFromBlocks(mTag, NfcConstants.BLOCKS_FIELD_FIVE)
            val fieldSix = readTextFromBlocks(mTag, NfcConstants.BLOCKS_FIELD_SIX)
            val fieldSeven = idData // Already read

            // 4. Read Fingerprint (if available)
            // Check magic bytes in Block 1 (Field One[0])
            // Original logic checks block "one[0]" which is 1.
            val hasFingerprint = checkHasFingerprint(mTag)

            var fingerprintData: ByteArray? = null
            if (hasFingerprint) {
                fingerprintData = readBytesFromBlocks(mTag, NfcConstants.BLOCKS_FINGERPRINT)
            }

            mTag.close()

            return@withContext NfcResult.Success(
                CardData(
                    fieldOne = fieldOne,
                    fieldTwo = fieldTwo,
                    fieldThree = fieldThree,
                    fieldFour = fieldFour,
                    fieldFive = fieldFive,
                    fieldSix = fieldSix,
                    fieldSeven = fieldSeven,
                    fingerprintTemplate = fingerprintData,
                    isBlocked = isBlocked
                )
            )

        } catch (e: IOException) {
            e.printStackTrace()
            return@withContext NfcResult.ConnectionLost
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext NfcResult.Error(e.message ?: "Unknown error")
        } finally {
            try {
                if (mTag.isConnected) mTag.close()
            } catch (e: Exception) {
            }
        }
    }

    private fun authenticateSector(tag: MifareClassic, blockIndex: Int): Boolean {
        return try {
            val sector = tag.blockToSector(blockIndex)
            tag.authenticateSectorWithKeyA(sector, NfcConstants.AUTH_KEY)
        } catch (e: Exception) {
            false
        }
    }

    private fun readTextFromBlocks(tag: MifareClassic, blocks: IntArray): String {
        val bos = ByteArrayOutputStream()
        for (block in blocks) {
            if (authenticateSector(tag, block)) {
                try {
                    val data = tag.readBlock(block)
                    bos.write(data)
                } catch (e: IOException) {
                    Log.e(TAG, "Failed to read block $block")
                }
            } else {
                Log.e(TAG, "Auth failed for block $block")
            }
        }
        val rawString = String(bos.toByteArray(), StandardCharsets.UTF_8)
        return cleanString(rawString)
    }

    private fun readBytesFromBlocks(tag: MifareClassic, blocks: IntArray): ByteArray {
        val bos = ByteArrayOutputStream()
        for (block in blocks) {
            if (authenticateSector(tag, block)) {
                try {
                    val data = tag.readBlock(block)
                    bos.write(data)
                } catch (e: IOException) {
                    Log.e(TAG, "Failed to read block $block")
                }
            }
        }
        return bos.toByteArray()
    }

    private fun checkHasFingerprint(tag: MifareClassic): Boolean {
        val probeBlock = NfcConstants.BLOCKS_FIELD_ONE[0] // Block 1
        if (authenticateSector(tag, probeBlock)) {
            try {
                val data = tag.readBlock(probeBlock)
                // Check for FMR header 0x46, 0x4d, 0x52
                if (data.size >= 3 &&
                    data[0] == NfcConstants.FINGERPRINT_MAGIC_HEADER[0] &&
                    data[1] == NfcConstants.FINGERPRINT_MAGIC_HEADER[1] &&
                    data[2] == NfcConstants.FINGERPRINT_MAGIC_HEADER[2]) {
                    return true
                }
            } catch (e: Exception) {
                return false
            }
        }
        return false
    }

    private fun checkIsBlocked(idField: String): Boolean {
        if (idField.isEmpty()) return true

        // Decrypt ID
        val decryptedId = AESEncryption.encrypt(idField) // Note: The original logic ENCRYPTED the ID to send to the server/check.
        // Wait, looking at original code:
        // decId = AESEncryption2.INSTANCE.encrypt(id);
        // checkViewModel.checkRecord(..., decId, ...);
        // This implies the server expects the ENCRYPTED ID or the variable name `decId` is misleading (maybe 'decoratedId'?).
        // If the purpose is local 'decryption' usually we decrypt.
        // BUT `AESEncryption2.encrypt` is called in `checkRecordIsBlocked`.
        // So we should replicate that if we are calling the same API.
        // If this is purely local check, we might need more context.
        // However, the original code *calls an API* to check if blocked.
        // Since we are porting the NFC reading, we will return the ID.
        // The business logic of checking if blocked usually belongs in the ViewModel interacting with a Repository.
        // For now, let's just return false here and let the ViewModel handle the API check using the raw or encrypted ID from Field Seven.

        return false
    }

    // Helper to clean non-alphanumeric characters if necessary, based on original readTextFromCard
    private fun cleanString(input: String): String {
        val sb = StringBuilder()
        for (char in input) {
            if (char.isLetterOrDigit() || char == ' ') {
                sb.append(char)
            }
        }
        return sb.toString().trim()
    }
}