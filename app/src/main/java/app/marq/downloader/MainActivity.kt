package app.marq.downloader

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import app.marq.downloader.ui.MarqApp
import app.marq.downloader.ui.MarqColors
import app.marq.downloader.ui.MarqTheme
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.background

class MainActivity : ComponentActivity() {
    private val vm: MarqViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(0xFF171411.toInt()),
            navigationBarStyle = SystemBarStyle.dark(0xFF171411.toInt()),
        )
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) handle(intent)
        setContent {
            MarqTheme {
                MarqApp(vm, modifier = Modifier.fillMaxSize().background(MarqColors.Bg).statusBarsPadding())
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handle(intent)
    }

    private fun handle(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)?.let { vm.fetchFrom(it) }
            Intent.ACTION_VIEW -> intent.dataString?.let { vm.fetchFrom(it) }
        }
    }
}
