package br.com.tagueamento

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
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
                val eventName = FirebaseAnalytics.Event.BEGIN_CHECKOUT

                val bundle = Bundle()

                if (dataLayer.has("ecommerce")) {
                    val ecommerce = dataLayer.getJSONObject("ecommerce")
                    if (ecommerce.has("currency")) {
                        bundle.putString(FirebaseAnalytics.Param.CURRENCY, ecommerce.getString("currency"))
                    }
                    if (ecommerce.has("value")) {
                        bundle.putDouble(FirebaseAnalytics.Param.VALUE, ecommerce.getDouble("value"))
                    }
                    if (ecommerce.has("discount")) {
                        bundle.putDouble("discount", ecommerce.getDouble("discount"))
                    }
                    if (ecommerce.has("items")) {
                        val itemsArray = ecommerce.getJSONArray("items")
                        val itemsBundles = Array(itemsArray.length()) { i ->
                            val itemObj = itemsArray.getJSONObject(i)
                            Bundle().apply {
                                if (itemObj.has("item_id")) putString(FirebaseAnalytics.Param.ITEM_ID, itemObj.getString("item_id"))
                                if (itemObj.has("item_name")) putString(FirebaseAnalytics.Param.ITEM_NAME, itemObj.getString("item_name"))
                                if (itemObj.has("item_category")) putString(FirebaseAnalytics.Param.ITEM_CATEGORY, itemObj.getString("item_category"))
                                if (itemObj.has("price")) putDouble(FirebaseAnalytics.Param.PRICE, itemObj.getDouble("price"))
                                if (itemObj.has("quantity")) putLong(FirebaseAnalytics.Param.QUANTITY, itemObj.getLong("quantity"))
                            }
                        }
                        bundle.putParcelableArray(FirebaseAnalytics.Param.ITEMS, itemsBundles)
                    }
                }

                firebaseAnalytics.logEvent(eventName, bundle)

                // Roda na thread principal para mostrar o diálogo de confirmação
                runOnUiThread {
                    onMessageReceived("Evento enviado para o Firebase Analytics com os dados: $dataLayerString")
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
        val urlToLoad = intent.data?.toString() ?: "https://havaianas.com.br/checkouts/cn/hWN8lzqS5lEapNts4L4bzzST/pt-br/information?_r=AQAB8o-N2B9Qlxvi4iIyd4Sk2DohCFyuLJffu_d682roCF8&isApp=true"

        setContent {
            var showDialog by remember { mutableStateOf(false) }
            var dialogMessage by remember { mutableStateOf("") }

            val activity = (LocalContext.current as? Activity)
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Tagueamento WebAPP Hava BR") },
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
                
                // Adiciona a interface JavaScript ao WebView, nomeando-a "channelGTM"
                addJavascriptInterface(webAppInterface, "channelGTM")

                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setCookie(urlToLoad, "isApp=true")

                loadUrl(urlToLoad)
            }
        },
        modifier = modifier
    )
}