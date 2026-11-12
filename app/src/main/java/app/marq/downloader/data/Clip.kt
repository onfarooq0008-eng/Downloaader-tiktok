package app.marq.downloader.data

import org.json.JSONArray
import org.json.JSONObject

enum class Variant { LOGO, CLEAN }

data class Clip(
    val id: String,
    val sourceUrl: String,
    val title: String,
    val authorName: String,
    val authorHandle: String,
    val avatar: String,
    val cover: String,
    val durationSec: Int,
    val likes: Long,
    val comments: Long,
    val plays: Long,
    val withLogoUrl: String?,
    val withoutLogoUrl: String?,
    val audioUrl: String?,
    val audioTitle: String?,
    val images: List<String>,
    val fetchedAt: Long,
) {
    val isPhoto: Boolean get() = images.isNotEmpty() && durationSec <= 0

    fun urlFor(variant: Variant): String? =
        if (variant == Variant.CLEAN) withoutLogoUrl else withLogoUrl

    fun availableVariant(prefer: Variant): Variant = when {
        prefer == Variant.CLEAN && withoutLogoUrl != null -> Variant.CLEAN
        prefer == Variant.LOGO && withLogoUrl != null -> Variant.LOGO
        withoutLogoUrl != null -> Variant.CLEAN
        else -> Variant.LOGO
    }

    fun fileBase(): String {
        val handle = authorHandle.replace(Regex("[^\\w.-]+"), "").take(40).ifEmpty { "post" }
        val safeId = id.replace(Regex("[^\\w.-]+"), "").take(24).ifEmpty { "clip" }
        return "marq-$handle-$safeId"
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("sourceUrl", sourceUrl)
        put("title", title)
        put("authorName", authorName)
        put("authorHandle", authorHandle)
        put("avatar", avatar)
        put("cover", cover)
        put("durationSec", durationSec)
        put("likes", likes)
        put("comments", comments)
        put("plays", plays)
        put("withLogoUrl", withLogoUrl ?: JSONObject.NULL)
        put("withoutLogoUrl", withoutLogoUrl ?: JSONObject.NULL)
        put("audioUrl", audioUrl ?: JSONObject.NULL)
        put("audioTitle", audioTitle ?: JSONObject.NULL)
        put("images", JSONArray(images))
        put("fetchedAt", fetchedAt)
    }

    companion object {
        fun fromJson(o: JSONObject): Clip {
            fun str(key: String): String? = if (o.isNull(key)) null else o.optString(key).ifEmpty { null }
            val imgs = o.optJSONArray("images")
            return Clip(
                id = o.optString("id"),
                sourceUrl = o.optString("sourceUrl"),
                title = o.optString("title"),
                authorName = o.optString("authorName"),
                authorHandle = o.optString("authorHandle"),
                avatar = o.optString("avatar"),
                cover = o.optString("cover"),
                durationSec = o.optInt("durationSec"),
                likes = o.optLong("likes"),
                comments = o.optLong("comments"),
                plays = o.optLong("plays"),
                withLogoUrl = str("withLogoUrl"),
                withoutLogoUrl = str("withoutLogoUrl"),
                audioUrl = str("audioUrl"),
                audioTitle = str("audioTitle"),
                images = (0 until (imgs?.length() ?: 0)).mapNotNull { imgs?.optString(it)?.ifEmpty { null } },
                fetchedAt = o.optLong("fetchedAt"),
            )
        }
    }
}

fun compactCount(n: Long): String {
    if (n < 0) return "0"
    if (n < 1000) return n.toString()
    if (n < 1_000_000) {
        val v = n / 1000.0
        val t = if (v < 10) String.format("%.1f", v) else String.format("%.0f", v)
        return t.removeSuffix(".0") + "K"
    }
    val v = n / 1_000_000.0
    val t = if (v < 10) String.format("%.1f", v) else String.format("%.0f", v)
    return t.removeSuffix(".0") + "M"
}

fun clock(sec: Int): String {
    val s = maxOf(0, sec)
    return "%d:%02d".format(s / 60, s % 60)
}

fun ago(ts: Long, now: Long = System.currentTimeMillis()): String {
    val min = maxOf(0L, now - ts) / 60000
    if (min < 1) return "Just now"
    if (min < 60) return "${min}m ago"
    val hr = min / 60
    if (hr < 24) return "${hr}h ago"
    return "${hr / 24}d ago"
}
