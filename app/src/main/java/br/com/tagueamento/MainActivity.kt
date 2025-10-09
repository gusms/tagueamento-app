package br.com.tagueamento

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAnalytics = Firebase.analytics

        setContent {
            var appInstanceId by remember { mutableStateOf<String?>(null) }
            var sessionId by remember { mutableStateOf<Long?>(null) }

            // Recupera o App Instance ID e o Session ID uma vez ao iniciar a tela
            LaunchedEffect(Unit) {
                firebaseAnalytics.appInstanceId
                    .addOnCompleteListener(OnCompleteListener { task ->
                        if (task.isSuccessful) {
                            appInstanceId = task.result
                        } else {
                            appInstanceId = "Erro ao obter ID"
                        }
                    })

                firebaseAnalytics.sessionId
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            sessionId = task.result
                        }
                    }
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Tagueamento App") }
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            val bundle = Bundle().apply {
                                putString("origem", "main_activity")
                            }
                            firebaseAnalytics.logEvent("navegacao_segunda_tela", bundle)
                            startActivity(Intent(this@MainActivity, SegundaActivity::class.java))
                        }
                    ) {
                        Text("Ir para Segunda Tela")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val bundle = Bundle().apply {
                                putString("origem", "main_activity")
                                putString("destino", "webview_tagueamento")
                                putString("level_difficulty", "1");
                            }
                            firebaseAnalytics.logEvent("navegacao_webview", bundle)
                            startActivity(Intent(this@MainActivity, WebViewActivity::class.java))
                        }
                    ) {
                        Text("Abrir Tagueamento Web")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val bundle = Bundle().apply {
                                putString("origem", "main_activity")
                                putString("destino", "gabriel_activity")
                            }
                            firebaseAnalytics.logEvent("navegacao_gabriel", bundle)
                            startActivity(Intent(this@MainActivity, GabrielActivity::class.java))
                        }
                    ) {
                        Text("Ir para Tela do Gabriel")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Exibe o App Instance ID
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "App Instance ID: ${appInstanceId ?: "Carregando..."}",
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Session ID: ${sessionId?.toString() ?: "Carregando..."}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ComprarButton(firebaseAnalytics)
                }
            }
        }
    }
}

@Composable
fun ComprarButton(firebaseAnalytics: FirebaseAnalytics) {
    val context = LocalContext.current
    Button(
        onClick = {
            // Cria o bundle com os dados do item
            val itemBundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.ITEM_ID, "SKU_123")
                putString(FirebaseAnalytics.Param.ITEM_NAME, "Camiseta Preta")
                putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Vestuário")
                putDouble(FirebaseAnalytics.Param.PRICE, 99.90)
                putLong(FirebaseAnalytics.Param.QUANTITY, 1)
            }

            // Cria o bundle principal com os dados da compra
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.CURRENCY, "BRL")
                putDouble(FirebaseAnalytics.Param.VALUE, 99.90)
                putString(FirebaseAnalytics.Param.TRANSACTION_ID, "${System.currentTimeMillis()}")
                putString(FirebaseAnalytics.Param.PAYMENT_TYPE, "CREDIT_CARD")
                // Adiciona o array de itens
                putParcelableArray(FirebaseAnalytics.Param.ITEMS, arrayOf(itemBundle))
            }
            
            // Envia o evento de purchase
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE, bundle)
            
            Toast.makeText(context, "Pedido realizado com items", Toast.LENGTH_SHORT).show()
        }
    ) {
        Text("Comprar")
    }
}