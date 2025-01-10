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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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