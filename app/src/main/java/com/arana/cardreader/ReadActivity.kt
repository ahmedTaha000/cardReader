package com.arana.cardreader

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.tech.MifareClassic
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.arana.cardreader.nfc.CardData
import com.arana.cardreader.nfc.NfcViewModel
import com.arana.cardreader.ui.theme.CardReaderTheme

class ReadActivity : ComponentActivity() {

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var viewModel: NfcViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this)[NfcViewModel::class.java]
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        setContent {
            CardReaderTheme {
                ReadScreen(viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        enableNfcForegroundDispatch()
    }

    override fun onPause() {
        super.onPause()
        disableNfcForegroundDispatch()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.onNfcIntent(intent)
    }

    private fun enableNfcForegroundDispatch() {
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val filter = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, arrayOf(filter), arrayOf(arrayOf(MifareClassic::class.java.name)))
    }

    private fun disableNfcForegroundDispatch() {
        nfcAdapter.disableForegroundDispatch(this)
    }
}

@Composable
fun ReadScreen(viewModel: NfcViewModel) {
    val state by viewModel.nfcState.observeAsState(NfcViewModel.NfcState.Idle)

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(title = { Text("Read Card Data") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state) {
                is NfcViewModel.NfcState.Idle -> {
                    StatusView("Scan your card to read the 9 slots")
                }
                is NfcViewModel.NfcState.Reading -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Reading card...")
                }
                is NfcViewModel.NfcState.Success -> {
                    val data = (state as NfcViewModel.NfcState.Success).cardData
                    DataDisplay(data)
                }
                is NfcViewModel.NfcState.Error -> {
                    Text(
                        text = (state as NfcViewModel.NfcState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                    Button(onClick = { viewModel.resetState() }) {
                        Text("Try Again")
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun StatusView(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun DataDisplay(data: CardData) {
    val fields = listOf(
        "Slot 1" to data.fieldOne,
        "Slot 2" to data.fieldTwo,
        "Slot 3" to data.fieldThree,
        "Slot 4" to data.fieldFour,
        "Slot 5" to data.fieldFive,
        "Slot 6" to data.fieldSix,
        "Slot 7" to data.fieldSeven,
        "Slot 8" to data.fieldEight,
        "Slot 9" to data.fieldNine
    )

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(fields) { (label, value) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(label, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(value ?: "Empty", fontSize = 16.sp)
                }
            }
        }
    }
}
