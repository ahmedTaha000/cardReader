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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.arana.cardreader.nfc.CardData
import com.arana.cardreader.nfc.NfcViewModel
import com.arana.cardreader.ui.theme.CardReaderTheme

class WriteActivity : ComponentActivity() {

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var viewModel: NfcViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this)[NfcViewModel::class.java]
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        setContent {
            CardReaderTheme {
                WriteScreen(viewModel)
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
fun WriteScreen(viewModel: NfcViewModel) {
    val state by viewModel.nfcState.observeAsState(NfcViewModel.NfcState.Idle)
    
    // State for the 9 fields
    var field1 by remember { mutableStateOf("") }
    var field2 by remember { mutableStateOf("") }
    var field3 by remember { mutableStateOf("") }
    var field4 by remember { mutableStateOf("") }
    var field5 by remember { mutableStateOf("") }
    var field6 by remember { mutableStateOf("") }
    var field7 by remember { mutableStateOf("") }
    var field8 by remember { mutableStateOf("") }
    var field9 by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(title = { Text("Write to Card") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state == NfcViewModel.NfcState.Idle) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item { FieldInput("Slot 1", field1) { field1 = it } }
                    item { FieldInput("Slot 2", field2) { field2 = it } }
                    item { FieldInput("Slot 3", field3) { field3 = it } }
                    item { FieldInput("Slot 4", field4) { field4 = it } }
                    item { FieldInput("Slot 5", field5) { field5 = it } }
                    item { FieldInput("Slot 6", field6) { field6 = it } }
                    item { FieldInput("Slot 7", field7) { field7 = it } }
                    item { FieldInput("Slot 8", field8) { field8 = it } }
                    item { FieldInput("Slot 9", field9) { field9 = it } }
                }

                Button(
                    onClick = {
                        val cardData = CardData(
                            fieldOne = field1, fieldTwo = field2, fieldThree = field3,
                            fieldFour = field4, fieldFive = field5, fieldSix = field6,
                            fieldSeven = field7, fieldEight = field8, fieldNine = field9
                        )
                        viewModel.startWrite(cardData)
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Text("Ready to Write")
                }
            } else {
                WritingStatus(state, viewModel)
            }
        }
    }
}

@Composable
fun FieldInput(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
    )
}

@Composable
fun WritingStatus(state: NfcViewModel.NfcState, viewModel: NfcViewModel) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when (state) {
                is NfcViewModel.NfcState.Writing -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Place card to write...", fontSize = 18.sp)
                    Button(onClick = { viewModel.cancelOperation() }, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Cancel")
                    }
                }
                is NfcViewModel.NfcState.SuccessWrite -> {
                    Text("Data written successfully!", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Button(onClick = { viewModel.resetState() }, modifier = Modifier.padding(top = 16.dp)) {
                        Text("Finish")
                    }
                }
                is NfcViewModel.NfcState.Error -> {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.resetState() }, modifier = Modifier.padding(top = 16.dp)) {
                        Text("Retry")
                    }
                }
                else -> {}
            }
        }
    }
}
