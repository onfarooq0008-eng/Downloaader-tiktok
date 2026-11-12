package app.marq.downloader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.marq.downloader.Busy
import app.marq.downloader.MarqViewModel
import app.marq.downloader.UiState
import app.marq.downloader.data.Clip
import app.marq.downloader.data.Variant
import app.marq.downloader.data.clock
import app.marq.downloader.data.compactCount
import coil.compose.AsyncImage

private const val SAMPLE = "https://www.tiktok.com/@scout2015/video/6718335390845095173"

@Composable
fun SaveScreen(state: UiState, vm: MarqViewModel) {
    val clipboard = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current
    val clip = state.clip

    Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Column {
            Eyebrow("Save")
            Text("With the logo.\nOr without.", style = DisplayStyle, modifier = Modifier.padding(top = 4.dp))
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Public TikTok link", color = MarqColors.Muted, fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = state.url,
                    onValueChange = vm::setUrl,
                    placeholder = { Text("https://www.tiktok.com/...", color = MarqColors.Faint) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MarqColors.Surface,
                        unfocusedContainerColor = MarqColors.Surface,
                        focusedTextColor = MarqColors.Fg,
                        unfocusedTextColor = MarqColors.Fg,
                        cursorColor = MarqColors.Copper,
                        focusedIndicatorColor = MarqColors.Copper,
                        unfocusedIndicatorColor = MarqColors.Line,
                    ),
                    modifier = Modifier.weight(1f),
                )
                Box(
                    Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(MarqColors.Raised)
                        .clickable {
                            val text = clipboard.getText()?.text?.trim().orEmpty()
                            if (text.isNotEmpty()) vm.setUrl(text) else vm.setError("Clipboard is empty. Copy a TikTok link first.")
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.ContentPaste, contentDescription = "Paste link", tint = MarqColors.Fg)
                }
            }
            PrimaryButton(
                label = if (state.busy == Busy.FETCH) "Reading the post…" else "Fetch post",
                enabled = state.busy != Busy.FETCH && state.url.isNotBlank(),
                onClick = vm::fetch,
            )
            Box(
                Modifier.fillMaxWidth().heightIn(min = 44.dp).clickable {
                    vm.setUrl(SAMPLE); vm.fetch()
                },
                contentAlignment = Alignment.Center,
            ) {
                Text("Try a public sample", color = MarqColors.Muted, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }

        if (state.busy == Busy.FETCH) LinearProgressIndicator(Modifier.fillMaxWidth(), color = MarqColors.Copper, trackColor = MarqColors.Raised)

        if (state.error.isNotEmpty()) {
            Text(
                state.error, color = MarqColors.Fg, fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MarqColors.Surface).padding(16.dp, 12.dp),
            )
        }

        if (clip != null) {
            ClipCard(clip, state, vm, onOpenOriginal = { uriHandler.openUri(clip.sourceUrl) })
        } else {
            Text(
                "Paste a video or photo link — or share one to Marq straight from TikTok. Marq asks for the files TikTok already published: one with the moving logo, and the clean one when it exists.",
                color = MarqColors.Muted, fontSize = 14.sp,
            )
        }

        Text("Only save posts you’re allowed to keep. Private and friends-only posts won’t resolve.", color = MarqColors.Faint, fontSize = 12.sp)
    }
}

@Composable
private fun ClipCard(clip: Clip, state: UiState, vm: MarqViewModel, onOpenOriginal: () -> Unit) {
    val working = state.busy == Busy.FILE || state.busy == Busy.AUDIO
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(MarqColors.Surface).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (clip.avatar.isNotEmpty()) {
                AsyncImage(clip.avatar, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(44.dp).clip(CircleShape))
            } else {
                Box(Modifier.size(44.dp).clip(CircleShape).background(MarqColors.Raised))
            }
            Column(Modifier.weight(1f)) {
                Text(clip.authorName, color = MarqColors.Fg, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("@${clip.authorHandle}", color = MarqColors.Muted, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        Text(clip.title, color = MarqColors.Fg, fontSize = 14.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (clip.durationSec > 0) Text(clock(clip.durationSec), color = MarqColors.Muted, fontSize = 12.sp)
            Stat(Icons.Outlined.PlayArrow, compactCount(clip.plays))
            Stat(Icons.Outlined.FavoriteBorder, compactCount(clip.likes))
            Stat(Icons.Outlined.ChatBubbleOutline, compactCount(clip.comments))
        }

        if (clip.isPhoto) PhotoSet(clip, working) { url, i -> vm.downloadImage(url, i) }
        else VideoSet(clip, state, working, vm)

        if (clip.audioUrl != null) {
            SecondaryButton(Modifier.fillMaxWidth(), enabled = !working, onClick = vm::downloadAudio) {
                Icon(Icons.Outlined.MusicNote, null, tint = MarqColors.Fg, modifier = Modifier.size(16.dp))
                Text(
                    if (state.busy == Busy.AUDIO) "Saving sound…"
                    else clip.audioTitle?.let { "Save sound · $it" } ?: "Save sound",
                    color = MarqColors.Fg, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (working) {
            val p = state.progress
            if (p != null) LinearProgressIndicator(progress = { p }, modifier = Modifier.fillMaxWidth(), color = MarqColors.Copper, trackColor = MarqColors.Raised)
            else LinearProgressIndicator(Modifier.fillMaxWidth(), color = MarqColors.Copper, trackColor = MarqColors.Raised)
        }

        Box(Modifier.fillMaxWidth().heightIn(min = 40.dp).clickable(onClick = onOpenOriginal), contentAlignment = Alignment.Center) {
            Text("Open the original post", color = MarqColors.Muted, fontSize = 14.sp)
        }
        if (state.notice.isNotEmpty()) {
            Text(state.notice, color = MarqColors.Muted, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun Stat(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, null, tint = MarqColors.Muted, modifier = Modifier.size(14.dp))
        Text(value, color = MarqColors.Muted, fontSize = 12.sp)
    }
}

@Composable
private fun VideoSet(clip: Clip, state: UiState, working: Boolean, vm: MarqViewModel) {
    val variant = state.variant
    val active = clip.urlFor(variant)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (clip.cover.isNotEmpty()) {
            AsyncImage(
                clip.cover, contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(260.dp).clip(RoundedCornerShape(16.dp)).background(MarqColors.Ink),
            )
        }
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MarqColors.Ink).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            VariantButton("With logo", "Stamped file", variant == Variant.LOGO, clip.withLogoUrl != null, Modifier.weight(1f)) { vm.choose(Variant.LOGO) }
            VariantButton("Without logo", "Clean file", variant == Variant.CLEAN, clip.withoutLogoUrl != null, Modifier.weight(1f)) { vm.choose(Variant.CLEAN) }
        }
        Text(
            if (clip.withLogoUrl == null || clip.withoutLogoUrl == null)
                "This post only has one video file" + if (clip.withoutLogoUrl != null) " — the clean one, without the logo." else " — the one with the logo still on it."
            else if (variant == Variant.CLEAN) "Clean playback file. The moving TikTok logo is not burned in."
            else "The file TikTok stamps with its moving logo and username.",
            color = MarqColors.Muted, fontSize = 12.sp,
        )
        PrimaryButton(
            label = if (state.busy == Busy.FILE) "Downloading…" else if (variant == Variant.CLEAN) "Download without logo" else "Download with logo",
            enabled = active != null && !working,
            onClick = vm::downloadVideo,
        )
    }
}

@Composable
private fun VariantButton(label: String, hint: String, selected: Boolean, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(12.dp))
            .background(if (selected) MarqColors.Copper else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick).padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val alpha = if (enabled) 1f else 0.4f
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = (if (selected) MarqColors.OnCopper else MarqColors.Fg).copy(alpha = alpha))
        Text(hint, fontSize = 12.sp, color = (if (selected) MarqColors.OnCopper.copy(alpha = 0.8f) else MarqColors.Muted).copy(alpha = alpha))
    }
}

@Composable
private fun PhotoSet(clip: Clip, working: Boolean, onSave: (String, Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Photo post. These slides don’t carry the moving logo.", color = MarqColors.Muted, fontSize = 12.sp)
        clip.images.chunked(2).forEachIndexed { row, pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEachIndexed { col, image ->
                    val index = row * 2 + col
                    Column(Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(MarqColors.Ink)) {
                        AsyncImage(image, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().aspectRatio(3f / 4f))
                        Box(
                            Modifier.fillMaxWidth().heightIn(min = 44.dp).clickable(enabled = !working) { onSave(image, index) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                if (clip.images.size > 1) "Save slide ${index + 1}" else "Download photo",
                                color = MarqColors.Fg.copy(alpha = if (working) 0.5f else 1f), fontSize = 14.sp, fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
                if (pair.size == 1 && clip.images.size > 1) Box(Modifier.weight(1f))
            }
        }
    }
}
