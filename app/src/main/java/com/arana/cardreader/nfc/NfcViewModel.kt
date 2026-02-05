package com.arana.cardreader.nfc


import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
  import kotlinx.coroutines.launch

class NfcViewModel : ViewModel() {

    private val repository = NfcRepository()

    private val _nfcState = MutableLiveData<NfcState>()
    val nfcState: LiveData<NfcState> = _nfcState

    sealed class NfcState {
        object Idle : NfcState()
        object Reading : NfcState()
        data class Success(val cardData: CardData) : NfcState()
        data class Error(val message: String) : NfcState()
    }

    fun onNfcIntent(intent: Intent) {
        val action = intent.action
        if (NfcAdapter.ACTION_TAG_DISCOVERED == action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == action ||
            NfcAdapter.ACTION_NDEF_DISCOVERED == action) {

            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            if (tag != null) {
                processTag(tag)
            }
        }
    }

    private fun processTag(tag: Tag) {
        _nfcState.value = NfcState.Reading
        viewModelScope.launch {
            when (val result = repository.readCard(tag)) {
                is NfcRepository.NfcResult.Success -> {
                    _nfcState.value = NfcState.Success(result.data)
                }
                is NfcRepository.NfcResult.Error -> {
                    _nfcState.value = NfcState.Error(result.message)
                }
                is NfcRepository.NfcResult.IncompatibleCard -> {
                    _nfcState.value = NfcState.Error("Card is not compatible (Authentication failed).")
                }
                is NfcRepository.NfcResult.ConnectionLost -> {
                    _nfcState.value = NfcState.Error("Connection lost. Please hold the card steadily.")
                }
            }
        }
    }
}

