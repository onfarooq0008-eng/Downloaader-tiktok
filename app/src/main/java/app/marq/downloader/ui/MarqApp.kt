package app.marq.downloader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.marq.downloader.MarqViewModel
import app.marq.downloader.Tab

@Composable
fun MarqApp(vm: MarqViewModel, modifier: Modifier = Modifier) {
    val state by vm.state.collectAsStateWithLifecycle()

    Column(modifier) {
        Row(
            Modifier.padding(start = 20.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MarqMark(28.dp)
            Text("Marq", fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, color = MarqColors.Fg)
        }

        Box(Modifier.weight(1f).fillMaxWidth()) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                when (state.tab) {
                    Tab.SAVE -> SaveScreen(state, vm)
                    Tab.SHELF -> ShelfScreen(state, onOpen = { vm.fetchFrom(it.sourceUrl) }, onRemove = vm::removeFromShelf)
                    Tab.ABOUT -> AboutScreen(state.shelf.size, onClear = vm::clearShelf)
                }
            }
        }

        HorizontalDivider(color = MarqColors.Line)
        Row(
            Modifier.fillMaxWidth().background(MarqColors.Bg).navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            NavItem("Save", Icons.Outlined.Download, state.tab == Tab.SAVE, Modifier.weight(1f)) { vm.setTab(Tab.SAVE) }
            NavItem("Shelf", Icons.Outlined.Bookmark, state.tab == Tab.SHELF, Modifier.weight(1f)) { vm.setTab(Tab.SHELF) }
            NavItem("About", Icons.Outlined.HelpOutline, state.tab == Tab.ABOUT, Modifier.weight(1f)) { vm.setTab(Tab.ABOUT) }
        }
    }
}

@Composable
private fun NavItem(label: String, icon: ImageVector, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.heightIn(min = 52.dp).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.width(64.dp).height(32.dp).clip(CircleShape)
                .background(if (selected) MarqColors.Copper else androidx.compose.ui.graphics.Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = if (selected) MarqColors.OnCopper else MarqColors.Muted, modifier = Modifier.size(22.dp))
        }
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (selected) MarqColors.Fg else MarqColors.Muted)
    }
}
