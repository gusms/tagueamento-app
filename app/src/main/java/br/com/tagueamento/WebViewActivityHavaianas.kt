package br.com.tagueamento

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import org.json.JSONObject

class WebViewActivityHavaianas : ComponentActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    /**
     * Classe da interface que recebe a mensagem do JavaScript.
     * O método anotado com @JavascriptInterface pode ser chamado pelo JS.
     */
    private inner class WebAppInterface(
        private val firebaseAnalytics: FirebaseAnalytics,
        private val onMessageReceived: (String) -> Unit
    ) {
        @JavascriptInterface
        fun postMessage(dataLayerString: String) {
            Log.d("WebAppInterface", "DataLayer recebido: $dataLayerString")
            try {
                val dataLayer = JSONObject(dataLayerString)
                val eventName = FirebaseAnalytics.Event.SELECT_CONTENT

                val bundle = Bundle()

                // Mapeia os parâmetros padrão do evento 'select_content'
                if (dataLayer.has("content_type")) {
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, dataLayer.getString("content_type"))
                }
                if (dataLayer.has("item_id")) {
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, dataLayer.getString("item_id"))
                }

                firebaseAnalytics.logEvent(eventName, bundle)

                // Roda na thread principal para mostrar o diálogo de confirmação
                runOnUiThread {
                    onMessageReceived("Evento '$eventName' enviado para o Firebase Analytics com os dados: $dataLayerString")
                }
            } catch (e: Exception) {
                Log.e("WebAppInterface", "Erro ao processar o dataLayer", e)
                runOnUiThread {
                    onMessageReceived("Erro ao processar o dataLayer: ${e.message}")
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAnalytics = Firebase.analytics

        // Pega a URL do intent que abriu a activity
        val urlToLoad = intent.data?.toString() ?: "https://www.havaianas.com.br"

        setContent {
            var showDialog by remember { mutableStateOf(false) }
            var dialogMessage by remember { mutableStateOf("") }

            val activity = (LocalContext.current as? Activity)
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Tagueamento Web") },
                        navigationIcon = {
                            IconButton(onClick = { activity?.finish() }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = "Voltar"
                                )
                            }
                        }
                    )
                }
            ) { paddingValues ->
                WebViewContentb(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    urlToLoad = urlToLoad,
                    webAppInterface = WebAppInterface(
                        firebaseAnalytics = firebaseAnalytics
                    ) { message ->
                        dialogMessage = message
                        showDialog = true
                    }
                )

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Mensagem do WebView") },
                        text = { Text(dialogMessage) },
                        confirmButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("OK")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WebViewContentb(
    modifier: Modifier = Modifier,
    urlToLoad: String,
    webAppInterface: Any
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()

                // Limpa o cache antes de carregar a URL
                clearCache(true)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true

                // Define um User-Agent personalizado
                settings.userAgentString = "webview-app"

                // Adiciona a interface JavaScript ao WebView, nomeando-a "Android"
                addJavascriptInterface(webAppInterface, "Android")

                loadUrl(urlToLoad)
            }
        },
        modifier = modifier
    )
}