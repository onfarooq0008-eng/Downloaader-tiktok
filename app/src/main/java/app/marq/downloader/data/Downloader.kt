package app.marq.downloader.data

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.IOException

enum class Kind(val mime: String, val collection: String, val folder: String) {
    VIDEO("video/mp4", "video", Environment.DIRECTORY_MOVIES),
    AUDIO("audio/mpeg", "audio", Environment.DIRECTORY_MUSIC),
    IMAGE("image/jpeg", "image", Environment.DIRECTORY_PICTURES),
}

/** Streams a remote file into the public gallery/music library via MediaStore (no storage permission needed on API 29+). */
object Downloader {
    suspend fun save(
        context: Context,
        url: String,
        fileName: String,
        kind: Kind,
        onProgress: (Float?) -> Unit = {},
    ): String = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Referer", "https://www.tiktok.com/")
            .header("Accept", "*/*")
            .build()

        val resolver = context.contentResolver
        val collectionUri = when (kind) {
            Kind.VIDEO -> MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            Kind.AUDIO -> MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            Kind.IMAGE -> MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        }
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, kind.mime)
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${kind.folder}/Marq")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val target = resolver.insert(collectionUri, values)
            ?: throw MarqException("Couldn’t create the file on your phone.")

        try {
            TikTokApi.http.newCall(req).execute().use { r ->
                val body = r.body
                if (!r.isSuccessful || body == null) throw MarqException("The file host refused the download. Try again.")
                val type = r.header("Content-Type").orEmpty()
                if (type.contains("text/html") || type.contains("application/json")) {
                    throw MarqException("The file host refused the download. Try again.")
                }
                val total = body.contentLength()
                resolver.openOutputStream(target)?.use { out ->
                    body.byteStream().use { input ->
                        val buf = ByteArray(64 * 1024)
                        var done = 0L
                        while (true) {
                            val n = input.read(buf)
                            if (n < 0) break
                            out.write(buf, 0, n)
                            done += n
                            onProgress(if (total > 0) done.toFloat() / total else null)
                        }
                    }
                } ?: throw MarqException("Couldn’t write the file on your phone.")
            }
            val done = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
            resolver.update(target, done, null, null)
            "${kind.folder}/Marq/$fileName"
        } catch (e: Exception) {
            resolver.delete(target, null, null)
            when (e) {
                is MarqException -> throw e
                is IOException -> throw MarqException("The download was interrupted. Try again.")
                else -> throw MarqException("Couldn’t save that file. Try again.")
            }
        }
    }
}
