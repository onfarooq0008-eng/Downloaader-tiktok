package app.marq.downloader.data

import android.content.Context
import org.json.JSONArray

class ShelfStore(context: Context) {
    private val prefs = context.getSharedPreferences("marq-shelf", Context.MODE_PRIVATE)

    fun loadItems(): List<Clip> = try {
        val arr = JSONArray(prefs.getString("items", "[]"))
        (0 until arr.length()).map { Clip.fromJson(arr.getJSONObject(it)) }
    } catch (e: Exception) {
        emptyList()
    }

    fun saveItems(items: List<Clip>) {
        val arr = JSONArray()
        items.forEach { arr.put(it.toJson()) }
        prefs.edit().putString("items", arr.toString()).apply()
    }

    fun loadPrefer(): Variant =
        if (prefs.getString("prefer", "clean") == "logo") Variant.LOGO else Variant.CLEAN

    fun savePrefer(v: Variant) {
        prefs.edit().putString("prefer", if (v == Variant.LOGO) "logo" else "clean").apply()
    }
}
