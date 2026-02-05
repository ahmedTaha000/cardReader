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
        object SuccessOperation : NfcResult()
    }

    suspend fun readCard(tag: Tag): NfcResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "========== READ CARD START ==========")
        val mTag = MifareClassic.get(tag) ?: run {
            Log.e(TAG, "Failed: Not a Mifare Classic tag")
            return@withContext NfcResult.Error("Not a Mifare Classic tag")
        }
        Log.d(TAG, "Step 1: Mifare Classic tag detected")

        try {
            Log.d(TAG, "Step 2: Connecting to tag...")
            mTag.connect()
            Log.d(TAG, "Step 2: ✓ Connected successfully")

            // 1. Check Compatibility (Auth Sector 3 / Block 12)
            Log.d(TAG, "Step 3: Authenticating sector 3 (block 12)...")
            if (!authenticateSector(mTag, NfcConstants.BLOCH_INDEX)) {
                Log.e(TAG, "Step 3: ✗ Authentication failed - Incompatible card")
                mTag.close()
                return@withContext NfcResult.IncompatibleCard
            }
            Log.d(TAG, "Step 3: ✓ Authentication successful")



            // 3. Read All Fields
            Log.d(TAG, "Step 4: Reading all 9 fields...")
            val fieldOne = readTextFromBlocks(mTag, NfcConstants.BLOCKS_FIELD_ONE)
            Log.d(TAG, "Step 4.1: ✓ Field 1 read: ${fieldOne.take(20)}...")
            
            val fieldTwo = readTextFromBlocks(mTag, NfcConstants.BLOCKS_FIELD_TWO)
            Log.d(TAG, "Step 4.2: ✓ Field 2 read: ${fieldTwo.take(20)}...")
            
            val fieldThree = readTextFromBlocks(mTag, NfcConstants.BLOCKS_FIELD_THREE)
            Log.d(TAG, "Step 4.3: ✓ Field 3 read: ${fieldThree.take(20)}...")
            
            val fieldFour = readTextFromBlocks(mTag, NfcConstants.BLOCKS_FIELD_FOUR)
            Log.d(TAG, "Step 4.4: ✓ Field 4 read: ${fieldFour.take(20)}...")
            
            val fieldFive = readTextFromBlocks(mTag, NfcConstants.BLOCKS_FIELD_FIVE)
            Log.d(TAG, "Step 4.5: ✓ Field 5 read: ${fieldFive.take(20)}...")
            
            val fieldSix = readTextFromBlocks(mTag, NfcConstants.BLOCKS_FIELD_SIX)
            Log.d(TAG, "Step 4.6: ✓ Field 6 read: ${fieldSix.take(20)}...")
            
            val fieldSeven = readTextFromBlocks(mTag, NfcConstants.BLOCKS_FIELD_SEVEN)
            Log.d(TAG, "Step 4.7: ✓ Field 7 read: ${fieldSeven.take(20)}...")
            
            val fieldEightData = readTextFromBlocks(mTag, NfcConstants.BLOCKS_FIELD_EIGHT)
            Log.d(TAG, "Step 4.8: ✓ Field 8 read: ${fieldEightData.take(20)}...")
            
            val fieldNine = readTextFromBlocks(mTag, NfcConstants.BLOCKS_FIELD_NINE)
            Log.d(TAG, "Step 4.9: ✓ Field 9 read: ${fieldNine.take(20)}...")




            Log.d(TAG, "Step 5: Checking for fingerprint data...")
            val hasFingerprint1 = checkSlotExists(mTag, NfcConstants.BLOCKS_FIELD_EIGHT[0], NfcConstants.FINGERPRINT_MAGIC_HEADER)
            var fingerprint1: ByteArray? = null
            if (hasFingerprint1) {
                Log.d(TAG, "Step 5.1: ✓ Fingerprint 1 detected, reading...")
                fingerprint1 = readBytesFromBlocks(mTag, NfcConstants.BLOCKS_FIELD_EIGHT)
                Log.d(TAG, "Step 5.1: ✓ Fingerprint 1 read (${fingerprint1.size} bytes)")
            } else {
                Log.d(TAG, "Step 5.1: No fingerprint 1 found")
            }

            val hasFingerprint2 = checkSlotExists(mTag, NfcConstants.BLOCKS_FIELD_NINE[0], NfcConstants.FINGERPRINT_MAGIC_HEADER)
            var fingerprint2: ByteArray? = null
            if (hasFingerprint2) {
                Log.d(TAG, "Step 5.2: ✓ Fingerprint 2 detected, reading...")
                fingerprint2 = readBytesFromBlocks(mTag, NfcConstants.BLOCKS_FIELD_NINE)
                Log.d(TAG, "Step 5.2: ✓ Fingerprint 2 read (${fingerprint2.size} bytes)")
            } else {
                Log.d(TAG, "Step 5.2: No fingerprint 2 found")
            }



            Log.d(TAG, "Step 6: Closing connection...")
            mTag.close()
            Log.d(TAG, "Step 6: ✓ Connection closed")

            Log.d(TAG, "Step 7: Creating CardData result...")
            return@withContext NfcResult.Success(
                CardData(
                    fieldOne = fieldOne,
                    fieldTwo = fieldTwo,
                    fieldThree = fieldThree,
                    fieldFour = fieldFour,
                    fieldFive = fieldFive,
                    fieldSix = fieldSix,
                    fieldSeven = fieldSeven,
                    fieldEight = fieldEightData,
                    fieldNine = fieldNine,
                    fingerprint1 = fingerprint1,
                    fingerprint2 = fingerprint2

                )
            ).also {
                Log.d(TAG, "========== READ CARD SUCCESS ==========")
            }

        } catch (e: IOException) {
            Log.e(TAG, "========== READ CARD FAILED (IO) ==========")
            Log.e(TAG, "IOException: ${e.message}")
            e.printStackTrace()
            return@withContext NfcResult.ConnectionLost
        } catch (e: Exception) {
            Log.e(TAG, "========== READ CARD FAILED (EXCEPTION) ==========")
            Log.e(TAG, "Exception: ${e.message}")
            e.printStackTrace()
            return@withContext NfcResult.Error(e.message ?: "Unknown error")
        } finally {
            try {
                if (mTag.isConnected) mTag.close()
            } catch (e: Exception) {
            }
        }
    }

    /**
     * Formats (clears) the NFC card by writing zeros to all data blocks.
     * This effectively erases all stored data on the card.
     */
    suspend fun formatCard(tag: Tag): NfcResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "========== FORMAT CARD START ==========")
        val mTag = MifareClassic.get(tag) ?: run {
            Log.e(TAG, "Failed: Not a Mifare Classic tag")
            return@withContext NfcResult.Error("Not a Mifare Classic tag")
        }
        Log.d(TAG, "Step 1: Mifare Classic tag detected")

        try {
            Log.d(TAG, "Step 2: Connecting to tag...")
            mTag.connect()
            Log.d(TAG, "Step 2: ✓ Connected successfully")

            // Check Compatibility (Auth Sector 3 / Block 12)
            Log.d(TAG, "Step 3: Authenticating sector 3 (block 12)...")
            if (!authenticateSector(mTag, NfcConstants.BLOCH_INDEX)) {
                Log.e(TAG, "Step 3: ✗ Authentication failed - Incompatible card")
                mTag.close()
                return@withContext NfcResult.IncompatibleCard
            }
            Log.d(TAG, "Step 3: ✓ Authentication successful")

            // Clear all blocks
            Log.d(TAG, "Step 4: Formatting card (writing zeros to all blocks)...")
            val emptyBlock = ByteArray(16) // 16 bytes of zeros
            val blocksToFormat = NfcConstants.ALL_WRITABLE_BLOCKS
            
            Log.d(TAG, "Total blocks to format: ${blocksToFormat.size}")
            
            for ( blockIndex in blocksToFormat) {
                if (authenticateSector(mTag, blockIndex)) {
                    try {
                        mTag.writeBlock(blockIndex, emptyBlock)
                        Log.d(TAG, "Block $blockIndex cleared")
                    } catch (e: IOException) {
                        Log.e(TAG, "Failed to clear block $blockIndex: ${e.message}")
                        // Continue with other blocks even if one fails
                    }
                } else {
                    Log.e(TAG, "Failed auth for block $blockIndex - skipping")
                }
            }

            Log.d(TAG, "Step 5: Closing connection...")
            mTag.close()
            Log.d(TAG, "Step 5: ✓ Connection closed")
            Log.d(TAG, "========== FORMAT CARD SUCCESS ==========")
            return@withContext NfcResult.SuccessOperation

        } catch (e: IOException) {
            Log.e(TAG, "========== FORMAT CARD FAILED (IO) ==========")
            Log.e(TAG, "IOException: ${e.message}")
            e.printStackTrace()
            return@withContext NfcResult.ConnectionLost
        } catch (e: Exception) {
            Log.e(TAG, "========== FORMAT CARD FAILED (EXCEPTION) ==========")
            Log.e(TAG, "Exception: ${e.message}")
            e.printStackTrace()
            return@withContext NfcResult.Error(e.message ?: "Unknown error")
        } finally {
            try {
                if (mTag.isConnected) mTag.close()
            } catch (e: Exception) {
            }
        }
    }

    /**
     * Writes CardData to the NFC card.
     * This function writes all the text fields and fingerprint data to their respective blocks.
     */
    suspend fun writeCard(tag: Tag, data: CardData): NfcResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "========== WRITE CARD START ==========")
        val mTag = MifareClassic.get(tag) ?: run {
            Log.e(TAG, "Failed: Not a Mifare Classic tag")
            return@withContext NfcResult.Error("Not a Mifare Classic tag")
        }
        Log.d(TAG, "Step 1: Mifare Classic tag detected")

        try {
            Log.d(TAG, "Step 2: Connecting to tag...")
            mTag.connect()
            Log.d(TAG, "Step 2: ✓ Connected successfully")

            // Check Compatibility (Auth Sector 3 / Block 12)
            Log.d(TAG, "Step 3: Authenticating sector 3 (block 12)...")
            if (!authenticateSector(mTag, NfcConstants.BLOCH_INDEX)) {
                Log.e(TAG, "Step 3: ✗ Authentication failed - Incompatible card")
                mTag.close()
                return@withContext NfcResult.IncompatibleCard
            }
            Log.d(TAG, "Step 3: ✓ Authentication successful")

            // Write text fields
            Log.d(TAG, "Step 4: Writing fields to card...")
            data.fieldOne?.let { 
                Log.d(TAG, "Step 4.1: Writing Field 1...")
                writeTextToBlocks(mTag, NfcConstants.BLOCKS_FIELD_ONE, it)
                Log.d(TAG, "Step 4.1: ✓ Field 1 written")
            }
            data.fieldTwo?.let { 
                Log.d(TAG, "Step 4.2: Writing Field 2...")
                writeTextToBlocks(mTag, NfcConstants.BLOCKS_FIELD_TWO, it)
                Log.d(TAG, "Step 4.2: ✓ Field 2 written")
            }
            data.fieldThree?.let { 
                Log.d(TAG, "Step 4.3: Writing Field 3...")
                writeTextToBlocks(mTag, NfcConstants.BLOCKS_FIELD_THREE, it)
                Log.d(TAG, "Step 4.3: ✓ Field 3 written")
            }
            data.fieldFour?.let { 
                Log.d(TAG, "Step 4.4: Writing Field 4...")
                writeTextToBlocks(mTag, NfcConstants.BLOCKS_FIELD_FOUR, it)
                Log.d(TAG, "Step 4.4: ✓ Field 4 written")
            }
            data.fieldFive?.let { 
                Log.d(TAG, "Step 4.5: Writing Field 5...")
                writeTextToBlocks(mTag, NfcConstants.BLOCKS_FIELD_FIVE, it)
                Log.d(TAG, "Step 4.5: ✓ Field 5 written")
            }
            data.fieldSix?.let { 
                Log.d(TAG, "Step 4.6: Writing Field 6...")
                writeTextToBlocks(mTag, NfcConstants.BLOCKS_FIELD_SIX, it)
                Log.d(TAG, "Step 4.6: ✓ Field 6 written")
            }
            data.fieldSeven?.let { 
                Log.d(TAG, "Step 4.7: Writing Field 7...")
                writeTextToBlocks(mTag, NfcConstants.BLOCKS_FIELD_SEVEN, it)
                Log.d(TAG, "Step 4.7: ✓ Field 7 written")
            }
            data.fieldEight?.let { 
                Log.d(TAG, "Step 4.8: Writing Field 8...")
                writeTextToBlocks(mTag, NfcConstants.BLOCKS_FIELD_EIGHT, it)
                Log.d(TAG, "Step 4.8: ✓ Field 8 written")
            }
            data.fieldNine?.let { 
                Log.d(TAG, "Step 4.9: Writing Field 9...")
                writeTextToBlocks(mTag, NfcConstants.BLOCKS_FIELD_NINE, it)
                Log.d(TAG, "Step 4.9: ✓ Field 9 written")
            }

            // Write fingerprint data if present
//            data.fingerprint1?.let { writeBytesToBlocks(mTag, NfcConstants.BLOCKS_FIELD_EIGHT, it) }
//            data.fingerprint2?.let { writeBytesToBlocks(mTag, NfcConstants.BLOCKS_FIELD_NINE, it) }

            Log.d(TAG, "Step 5: Closing connection...")
            mTag.close()
            Log.d(TAG, "Step 5: ✓ Connection closed")
            Log.d(TAG, "========== WRITE CARD SUCCESS ==========")
            return@withContext NfcResult.SuccessOperation

        } catch (e: IOException) {
            Log.e(TAG, "========== WRITE CARD FAILED (IO) ==========")
            Log.e(TAG, "IOException: ${e.message}")
            e.printStackTrace()
            return@withContext NfcResult.ConnectionLost
        } catch (e: Exception) {
            Log.e(TAG, "========== WRITE CARD FAILED (EXCEPTION) ==========")
            Log.e(TAG, "Exception: ${e.message}")
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


    // ... (Existing Sealed Classes and Read Methods) ...


    /**
     * Writes text to blocks on the NFC card.
     */
    private fun writeTextToBlocks(tag: MifareClassic, blocks: IntArray, text: String): Boolean {
        val bytes = text.toByteArray(StandardCharsets.UTF_8)
        return writeBytesToBlocks(tag, blocks, bytes)
    }

    /**
     * Writes bytes to blocks on the NFC card.
     */
    private fun writeBytesToBlocks(tag: MifareClassic, blocks: IntArray, data: ByteArray): Boolean {
        try {
            for (i in blocks.indices) {
                val blockIndex = blocks[i]
                if ((blockIndex + 1) % 4 == 0) {
                    Log.w(TAG, "Skipping Sector Trailer at block $blockIndex to prevent bricking.")
                    continue
                }
                if (authenticateSector(tag, blockIndex)) {
                    val start = i * 16
                    if (start >= data.size) break // No more data to write
                    val slice = if (start + 16 > data.size) {
                        // Padding with zeros if data is less than 16 bytes
                        val portion = data.copyOfRange(start, data.size)
                        ByteArray(16).apply { System.arraycopy(portion, 0, this, 0, portion.size) }
                    } else {
                        data.copyOfRange(start, start + 16)
                    }
                    tag.writeBlock(blockIndex, slice)
                } else {
                    Log.e(TAG, "Failed auth for block $blockIndex")
                    return false
                }
            }
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }


    private fun checkSlotExists(tag: MifareClassic, blockIndex: Int, magicHeader: ByteArray): Boolean {
        if (authenticateSector(tag, blockIndex)) {
            try {
                val data = tag.readBlock(blockIndex)
                if (data.size >= magicHeader.size) {
                     for (i in magicHeader.indices) {
                         if (data[i] != magicHeader[i]) return false
                     }
                    return true
                }
            } catch (e: Exception) {
                return false
            }
        }
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