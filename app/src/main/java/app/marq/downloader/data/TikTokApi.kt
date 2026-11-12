package app.marq.downloader.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.net.URI
import java.util.concurrent.TimeUnit

class MarqException(message: String) : Exception(message)

const val USER_AGENT =
    "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"

object TikTokApi {
    val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val urlRegex = Regex("https?://[^\\s]+")

    /** Pulls the first link out of pasted/shared text ("Check this out https://vm.tiktok.com/abc"). */
    fun extractUrl(text: String): String? = urlRegex.find(text)?.value?.trimEnd('.', ',', ')', '"')

    fun assertTikTokUrl(input: String): String {
        val candidate = extractUrl(input.trim()) ?: input.trim()
        val uri = try {
            URI(candidate)
        } catch (e: Exception) {
            throw MarqException("Paste a full link, including https://")
        }
        val scheme = uri.scheme?.lowercase()
        if (scheme != "https" && scheme != "http") {
            throw MarqException("That link needs to start with https://")
        }
        val host = (uri.host ?: "").lowercase().removePrefix("www.")
        if (!(host == "tiktok.com" || host.endsWith(".tiktok.com"))) {
            throw MarqException("That doesn’t look like a TikTok link.")
        }
        return candidate.replaceFirst(Regex("^http://"), "https://")
    }

    private fun asUrl(o: JSONObject, key: String): String? {
        if (o.isNull(key)) return null
        val v = o.optString(key).trim()
        return if (v.startsWith("http")) v else null
    }

    private suspend fun lookup(pageUrl: String, attempt: Int = 0): JSONObject {
        val endpoint = "https://www.tikwm.com/api/?hd=1&url=" + java.net.URLEncoder.encode(pageUrl, "UTF-8")
        val req = Request.Builder()
            .url(endpoint)
            .header("Accept", "application/json")
            .header("User-Agent", USER_AGENT)
            .build()
        val body = withContext(Dispatchers.IO) {
            try {
                http.newCall(req).execute().use { r ->
                    if (!r.isSuccessful) throw MarqException("The lookup didn’t answer. Try again in a moment.")
                    r.body?.string().orEmpty()
                }
            } catch (e: MarqException) {
                throw e
            } catch (e: IOException) {
                throw MarqException("No connection, or the lookup didn’t answer. Try again in a moment.")
            }
        }
        val json = try {
            JSONObject(body)
        } catch (e: Exception) {
            throw MarqException("The lookup returned something unexpected. Try again.")
        }
        val data = json.optJSONObject("data")
        if (json.optInt("code", -1) != 0 || data == null) {
            val msg = json.optString("msg")
            if (Regex("limit|too many|request/second", RegexOption.IGNORE_CASE).containsMatchIn(msg) && attempt < 2) {
                delay(1200)
                return lookup(pageUrl, attempt + 1)
            }
            if (Regex("parse|url|private|not found", RegexOption.IGNORE_CASE).containsMatchIn(msg)) {
                throw MarqException("Couldn’t read that link. Use a public video or photo post.")
            }
            throw MarqException("Couldn’t read that TikTok. Check that the post is public.")
        }
        return data
    }

    suspend fun resolve(input: String): Clip {
        val sourceUrl = assertTikTokUrl(input)
        val d = lookup(sourceUrl)

        val wm = asUrl(d, "wmplay")
        val play = asUrl(d, "play")
        val hd = asUrl(d, "hdplay")
        val clean = when {
            hd != null && hd != wm -> hd
            play != null && play != wm -> play
            else -> null
        }
        var withLogo = wm
        var withoutLogo = clean
        if (withLogo == null && withoutLogo == null && play != null) withoutLogo = play

        val images = mutableListOf<String>()
        d.optJSONArray("images")?.let { arr ->
            for (i in 0 until arr.length()) {
                val item = arr.opt(i)
                val u = when (item) {
                    is String -> item
                    is JSONObject -> item.optString("url")
                    else -> ""
                }
                if (u.startsWith("http")) images += u
            }
        }

        val music = d.optJSONObject("music_info")
        val audioUrl = music?.let { asUrl(it, "play") } ?: asUrl(d, "music")

        if (withLogo == null && withoutLogo == null && images.isEmpty()) {
            throw MarqException("No downloadable file on that post. It may be private or a live.")
        }

        val author = d.optJSONObject("author")
        val handle = author?.optString("unique_id")?.trim().orEmpty().ifEmpty { "tiktok" }
        val id = d.opt("id")?.toString().orEmpty()
        val title = d.optString("title").trim().ifEmpty { "Untitled post" }

        return Clip(
            id = id.ifEmpty { sourceUrl },
            sourceUrl = sourceUrl,
            title = title,
            authorName = author?.optString("nickname")?.trim().orEmpty().ifEmpty { handle },
            authorHandle = handle,
            avatar = author?.let { asUrl(it, "avatar") }.orEmpty(),
            cover = asUrl(d, "origin_cover") ?: asUrl(d, "cover") ?: images.firstOrNull().orEmpty(),
            durationSec = d.optInt("duration"),
            likes = d.optLong("digg_count"),
            comments = d.optLong("comment_count"),
            plays = d.optLong("play_count"),
            withLogoUrl = withLogo,
            withoutLogoUrl = withoutLogo,
            audioUrl = audioUrl,
            audioTitle = music?.optString("title")?.trim()?.ifEmpty { null },
            images = images,
            fetchedAt = System.currentTimeMillis(),
        )
    }
}
