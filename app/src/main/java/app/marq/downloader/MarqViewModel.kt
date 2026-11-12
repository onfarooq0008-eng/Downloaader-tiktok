package app.marq.downloader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.marq.downloader.data.Clip
import app.marq.downloader.data.Downloader
import app.marq.downloader.data.Kind
import app.marq.downloader.data.MarqException
import app.marq.downloader.data.ShelfStore
import app.marq.downloader.data.TikTokApi
import app.marq.downloader.data.Variant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Busy { NONE, FETCH, FILE, AUDIO }

enum class Tab { SAVE, SHELF, ABOUT }

data class UiState(
    val url: String = "",
    val clip: Clip? = null,
    val variant: Variant = Variant.CLEAN,
    val busy: Busy = Busy.NONE,
    val progress: Float? = null,
    val error: String = "",
    val notice: String = "",
    val shelf: List<Clip> = emptyList(),
    val tab: Tab = Tab.SAVE,
)

class MarqViewModel(app: Application) : AndroidViewModel(app) {
    private val store = ShelfStore(app)
    private val _state = MutableStateFlow(
        UiState(shelf = store.loadItems(), variant = store.loadPrefer()),
    )
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun setTab(tab: Tab) = _state.update { it.copy(tab = tab) }
    fun setUrl(value: String) = _state.update { it.copy(url = value) }
    fun setError(message: String) = _state.update { it.copy(error = message) }
    fun dismissNotice() = _state.update { it.copy(notice = "") }

    fun choose(variant: Variant) {
        store.savePrefer(variant)
        _state.update { it.copy(variant = variant) }
    }

    /** Called when a link is shared into the app or opened from the shelf. */
    fun fetchFrom(text: String) {
        val link = TikTokApi.extractUrl(text) ?: text.trim()
        _state.update { it.copy(tab = Tab.SAVE, url = link) }
        fetch()
    }

    fun fetch() {
        val input = _state.value.url
        if (input.isBlank() || _state.value.busy == Busy.FETCH) return
        _state.update { it.copy(busy = Busy.FETCH, error = "", notice = "") }
        viewModelScope.launch {
            try {
                val clip = TikTokApi.resolve(input)
                val prefer = store.loadPrefer()
                val shelf = (listOf(clip) + _state.value.shelf.filter { it.id != clip.id }).take(30)
                store.saveItems(shelf)
                _state.update {
                    it.copy(
                        clip = clip,
                        variant = clip.availableVariant(prefer),
                        shelf = shelf,
                        busy = Busy.NONE,
                        notice = "Saved to your shelf on this device.",
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(busy = Busy.NONE, error = messageOf(e)) }
            }
        }
    }

    fun downloadVideo() {
        val s = _state.value
        val clip = s.clip ?: return
        val url = clip.urlFor(s.variant) ?: return
        val tag = if (s.variant == Variant.CLEAN) "no-logo" else "with-logo"
        save(url, "${clip.fileBase()}-$tag.mp4", Kind.VIDEO, Busy.FILE)
    }

    fun downloadAudio() {
        val clip = _state.value.clip ?: return
        val url = clip.audioUrl ?: return
        save(url, "${clip.fileBase()}-sound.mp3", Kind.AUDIO, Busy.AUDIO)
    }

    fun downloadImage(url: String, index: Int) {
        val clip = _state.value.clip ?: return
        save(url, "${clip.fileBase()}-photo-${index + 1}.jpg", Kind.IMAGE, Busy.FILE)
    }

    private fun save(url: String, name: String, kind: Kind, busy: Busy) {
        if (_state.value.busy != Busy.NONE) return
        _state.update { it.copy(busy = busy, progress = null, error = "", notice = "") }
        viewModelScope.launch {
            try {
                val where = Downloader.save(getApplication(), url, name, kind) { p ->
                    _state.update { it.copy(progress = p) }
                }
                _state.update { it.copy(busy = Busy.NONE, progress = null, notice = "Saved to $where") }
            } catch (e: Exception) {
                _state.update { it.copy(busy = Busy.NONE, progress = null, error = messageOf(e)) }
            }
        }
    }

    fun removeFromShelf(id: String) {
        val shelf = _state.value.shelf.filter { it.id != id }
        store.saveItems(shelf)
        _state.update { it.copy(shelf = shelf) }
    }

    fun clearShelf() {
        store.saveItems(emptyList())
        _state.update { it.copy(shelf = emptyList()) }
    }

    private fun messageOf(e: Exception) =
        if (e is MarqException && e.message != null) e.message!! else "Something went wrong. Try that link again."
}
