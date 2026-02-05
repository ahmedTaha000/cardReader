package com.arana.cardreader

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.arana.cardreader.ui.theme.CardReaderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CardReaderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val context = LocalContext.current
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Button(
                                onClick = { context.startActivity(Intent(context, ReadActivity::class.java)) },
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Text("Read Card Data")
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { context.startActivity(Intent(context, WriteActivity::class.java)) },
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Text("Write Card Data")
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { context.startActivity(Intent(context, FormatActivity::class.java)) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Format Card (Erase All)")
                            }
                        }
                    }
                }
            }
        }
    }
}