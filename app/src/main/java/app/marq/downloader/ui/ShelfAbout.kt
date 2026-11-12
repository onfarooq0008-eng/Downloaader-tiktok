package app.marq.downloader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.marq.downloader.UiState
import app.marq.downloader.data.Clip
import app.marq.downloader.data.ago
import coil.compose.AsyncImage

@Composable
fun ShelfScreen(state: UiState, onOpen: (Clip) -> Unit, onRemove: (String) -> Unit) {
    Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column {
            Eyebrow("Shelf")
            Text("Recent posts", style = DisplayStyle, modifier = Modifier.padding(top = 4.dp))
        }
        if (state.shelf.isEmpty()) {
            Text("Nothing here yet. Fetch a link on Save and it stays on this device, ready to download again.", color = MarqColors.Muted, fontSize = 14.sp)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.shelf.forEach { clip ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            Modifier.weight(1f).heightIn(min = 64.dp).clip(RoundedCornerShape(16.dp)).background(MarqColors.Surface)
                                .clickable { onOpen(clip) }.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            if (clip.cover.isNotEmpty()) {
                                AsyncImage(clip.cover, null, contentScale = ContentScale.Crop, modifier = Modifier.width(48.dp).height(64.dp).clip(RoundedCornerShape(12.dp)))
                            } else {
                                Box(Modifier.width(48.dp).height(64.dp).clip(RoundedCornerShape(12.dp)).background(MarqColors.Raised))
                            }
                            Column(Modifier.padding(vertical = 4.dp)) {
                                Text(clip.title, color = MarqColors.Fg, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text("@${clip.authorHandle} · ${ago(clip.fetchedAt)}", color = MarqColors.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                        Box(
                            Modifier.width(48.dp).heightIn(min = 64.dp).clip(RoundedCornerShape(16.dp)).background(MarqColors.Surface)
                                .clickable { onRemove(clip.id) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Remove ${clip.authorHandle}", tint = MarqColors.Muted, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AboutScreen(count: Int, onClear: () -> Unit) {
    Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Column {
            Eyebrow("About")
            Text("How Marq saves", style = DisplayStyle, modifier = Modifier.padding(top = 4.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Step("1 · Paste", "A public video or photo link from TikTok, including short vm.tiktok.com links. Or use Share → Marq inside the TikTok app.")
            Step("2 · Choose", "With logo keeps the moving TikTok mark. Without logo uses the clean playback file when the post has one.")
            Step("3 · Download", "Videos land in Movies/Marq, sounds in Music/Marq and photos in Pictures/Marq on your phone.")
        }
        Text("Photo posts come through as slides, not a logo choice. Lives, private posts, and friends-only posts can’t be fetched.", color = MarqColors.Muted, fontSize = 14.sp)
        Text("Your shelf stays on this device only. Links expire, so opening an old row fetches the post again.", color = MarqColors.Muted, fontSize = 14.sp)
        Box(
            Modifier.fillMaxWidth().heightIn(min = 48.dp).clip(RoundedCornerShape(16.dp)).background(MarqColors.Raised.copy(alpha = if (count > 0) 1f else 0.4f))
                .clickable(enabled = count > 0, onClick = onClear),
            contentAlignment = Alignment.Center,
        ) {
            Text(if (count > 0) "Clear shelf · $count" else "Clear shelf", color = MarqColors.Fg, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun Step(title: String, body: String) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MarqColors.Surface).padding(16.dp, 12.dp)) {
        Text(title, color = MarqColors.Copper, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Text(body, color = MarqColors.Muted, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
    }
}
