package com.arana.cardreader.nfc.ui


import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.arana.cardreader.nfc.CardData
import com.arana.cardreader.nfc.NfcViewModel

class NfcActivity : AppCompatActivity() {

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var viewModel: NfcViewModel

    // UI Elements (assuming ViewBinding or findViewById)
    // private lateinit var binding: ActivityNfcBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_nfc)

        // 1. Initialize ViewModel
        viewModel = ViewModelProvider(this)[NfcViewModel::class.java]

        // 2. Initialize Adapter
        val adapter = NfcAdapter.getDefaultAdapter(this)
        if (adapter == null) {
            Toast.makeText(this, "NFC is not supported on this device.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        nfcAdapter = adapter

        // 3. Observe State
        observeNfcState()
    }

    private fun observeNfcState() {
        viewModel.nfcState.observe(this) { state ->
            when (state) {
                is NfcViewModel.NfcState.Idle -> {
                    // Update UI: "Scan a card..."
                    // binding.statusTextView.text = "Place card on sensor"
                }
                is NfcViewModel.NfcState.Reading -> {
                    // Update UI: Show Loading Dialog
                    // binding.progressBar.visibility = View.VISIBLE
                    Toast.makeText(this, "Reading card...", Toast.LENGTH_SHORT).show()
                }
                is NfcViewModel.NfcState.Success -> {
                    // Update UI: Show Data
                    // binding.progressBar.visibility = View.GONE
                    handleSuccess(state.cardData)
                }
                is NfcViewModel.NfcState.Error -> {
                    // Update UI: Show Error
                    // binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun handleSuccess(data: CardData) {
        // Display data
        Toast.makeText(this, "Card Read! Name: ${data.fieldOne}", Toast.LENGTH_LONG).show()

        if (data.isBlocked) {
            // Show Blocked Alert
            // showBlockedDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        // 4. Enable Foreground Dispatch
        enableNfcForegroundDispatch()
    }

    override fun onPause() {
        super.onPause()
        // 5. Disable Foreground Dispatch
        disableNfcForegroundDispatch()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // 6. Pass Intent to ViewModel
        viewModel.onNfcIntent(intent)
    }

    private fun enableNfcForegroundDispatch() {
        if (!nfcAdapter.isEnabled) {
            // Prompt user to enable NFC (optional)
            return
        }

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                this, 0,
                Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getActivity(
                this, 0,
                Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_IMMUTABLE // or 0 depending on requirement
            )
        }

        // Filters
        val tagMsg = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        val filters = arrayOf(tagMsg)

        // TechLists (Optional, null means all)
        val techLists = arrayOf(arrayOf(android.nfc.tech.MifareClassic::class.java.name))

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, techLists)
    }

    private fun disableNfcForegroundDispatch() {
        nfcAdapter.disableForegroundDispatch(this)
    }
}

