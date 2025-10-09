package br.com.tagueamento

import android.app.Activity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

class WebViewActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pega a URL do intent que abriu a activity
        val urlToLoad = intent.data?.toString() ?: "https://www.tagueamento.com.br"

        setContent {
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
                WebViewContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    urlToLoad = urlToLoad
                )
            }
        }
    }
}

@Composable
fun WebViewContent(modifier: Modifier = Modifier, urlToLoad: String) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                loadUrl(urlToLoad)
            }
        },
        modifier = modifier
    )
}