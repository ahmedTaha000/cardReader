package com.arana.cardreader.nfc


import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NfcViewModel : ViewModel() {

    private val TAG = "NfcViewModel"
    private val repository = NfcRepository()

    private val _nfcState = MutableLiveData<NfcState>()
    val nfcState: LiveData<NfcState> = _nfcState

    sealed class NfcState {
        object Idle : NfcState()
        object Reading : NfcState()
        object Writing : NfcState()
        object Formatting : NfcState()
        data class Success(val cardData: CardData) : NfcState()
        object SuccessWrite : NfcState()
        object SuccessFormat : NfcState()
        data class Error(val message: String) : NfcState()
    }

    private var pendingWriteData: CardData? = null
    private var pendingFormat: Boolean = false

    fun onNfcIntent(intent: Intent) {
        Log.d(TAG, "========== NFC INTENT RECEIVED ==========")
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: run {
            Log.e(TAG, "No tag found in intent")
            return
        }
        Log.d(TAG, "Tag ID: ${tag.id.joinToString("") { "%02x".format(it) }}")
        
        when {
            pendingFormat -> {
                Log.d(TAG, "Mode: FORMAT (pending format requested)")
                processFormat(tag)
            }
            pendingWriteData != null -> {
                Log.d(TAG, "Mode: WRITE (pending write data exists)")
                processWrite(tag, pendingWriteData!!)
            }
            else -> {
                Log.d(TAG, "Mode: READ (no pending operations)")
                processRead(tag)
            }
        }
    }

    fun startWrite(data: CardData) {
        Log.d(TAG, "startWrite called - Setting pending write data")
        pendingWriteData = data
        pendingFormat = false
        _nfcState.value = NfcState.Writing
        Log.d(TAG, "State changed to: Writing")
    }

    fun startFormat() {
        Log.d(TAG, "startFormat called - Setting pending format flag")
        pendingFormat = true
        pendingWriteData = null
        _nfcState.value = NfcState.Formatting
        Log.d(TAG, "State changed to: Formatting")
    }

    fun cancelOperation() {
        Log.d(TAG, "cancelOperation called - Clearing pending operations")
        pendingWriteData = null
        pendingFormat = false
        _nfcState.value = NfcState.Idle
        Log.d(TAG, "State changed to: Idle")
    }

    private fun processRead(tag: Tag) {
        Log.d(TAG, ">>> processRead started")
        _nfcState.value = NfcState.Reading
        Log.d(TAG, "State changed to: Reading")
        
        viewModelScope.launch {
            when (val result = repository.readCard(tag)) {
                is NfcRepository.NfcResult.Success -> {
                    Log.d(TAG, "Read result: Success")
                    _nfcState.value = NfcState.Success(result.data)
                    Log.d(TAG, "State changed to: Success")
                }
                is NfcRepository.NfcResult.Error -> {
                    Log.e(TAG, "Read result: Error - ${result.message}")
                    _nfcState.value = NfcState.Error(result.message)
                    Log.d(TAG, "State changed to: Error")
                }
                is NfcRepository.NfcResult.IncompatibleCard -> {
                    Log.e(TAG, "Read result: IncompatibleCard")
                    _nfcState.value = NfcState.Error("Card is not compatible.")
                    Log.d(TAG, "State changed to: Error (incompatible)")
                }
                is NfcRepository.NfcResult.ConnectionLost -> {
                    Log.e(TAG, "Read result: ConnectionLost")
                    _nfcState.value = NfcState.Error("Connection lost.")
                    Log.d(TAG, "State changed to: Error (connection lost)")
                }
                else -> {
                    Log.w(TAG, "Read result: Unknown result type")
                }
            }
        }
    }

    private fun processWrite(tag: Tag, data: CardData) {
        Log.d(TAG, ">>> processWrite started")
        _nfcState.value = NfcState.Writing
        Log.d(TAG, "State changed to: Writing")
        
        viewModelScope.launch {
            val result = repository.writeCard(tag, data)
            pendingWriteData = null // Clear after attempt
            Log.d(TAG, "Pending write data cleared")
            
            when (result) {
                is NfcRepository.NfcResult.SuccessOperation -> {
                    Log.d(TAG, "Write result: Success")
                    _nfcState.value = NfcState.SuccessWrite
                    Log.d(TAG, "State changed to: SuccessWrite")
                }
                is NfcRepository.NfcResult.Error -> {
                    Log.e(TAG, "Write result: Error - ${result.message}")
                    _nfcState.value = NfcState.Error(result.message)
                    Log.d(TAG, "State changed to: Error")
                }
                is NfcRepository.NfcResult.IncompatibleCard -> {
                    Log.e(TAG, "Write result: IncompatibleCard")
                    _nfcState.value = NfcState.Error("Card is not compatible.")
                    Log.d(TAG, "State changed to: Error (incompatible)")
                }
                is NfcRepository.NfcResult.ConnectionLost -> {
                    Log.e(TAG, "Write result: ConnectionLost")
                    _nfcState.value = NfcState.Error("Connection lost.")
                    Log.d(TAG, "State changed to: Error (connection lost)")
                }
                else -> {
                    Log.w(TAG, "Write result: Unknown result type")
                }
            }
        }
    }

    private fun processFormat(tag: Tag) {
        Log.d(TAG, ">>> processFormat started")
        _nfcState.value = NfcState.Formatting
        Log.d(TAG, "State changed to: Formatting")
        
        viewModelScope.launch {
            val result = repository.formatCard(tag)
            pendingFormat = false // Clear after attempt
            Log.d(TAG, "Pending format flag cleared")
            
            when (result) {
                is NfcRepository.NfcResult.SuccessOperation -> {
                    Log.d(TAG, "Format result: Success")
                    _nfcState.value = NfcState.SuccessFormat
                    Log.d(TAG, "State changed to: SuccessFormat")
                }
                is NfcRepository.NfcResult.Error -> {
                    Log.e(TAG, "Format result: Error - ${result.message}")
                    _nfcState.value = NfcState.Error(result.message)
                    Log.d(TAG, "State changed to: Error")
                }
                is NfcRepository.NfcResult.IncompatibleCard -> {
                    Log.e(TAG, "Format result: IncompatibleCard")
                    _nfcState.value = NfcState.Error("Card is not compatible.")
                    Log.d(TAG, "State changed to: Error (incompatible)")
                }
                is NfcRepository.NfcResult.ConnectionLost -> {
                    Log.e(TAG, "Format result: ConnectionLost")
                    _nfcState.value = NfcState.Error("Connection lost.")
                    Log.d(TAG, "State changed to: Error (connection lost)")
                }
                else -> {
                    Log.w(TAG, "Format result: Unknown result type")
                }
            }
        }
    }

    fun resetState() {
        Log.d(TAG, "resetState called")
        _nfcState.value = NfcState.Idle
        pendingWriteData = null
        pendingFormat = false
        Log.d(TAG, "State changed to: Idle (reset)")
    }
}

