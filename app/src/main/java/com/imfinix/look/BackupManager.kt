package com.imfinix.look

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject
import java.io.File

object BackupManager {

    private val PREFS_FILES = listOf(
        "imfinix_theme",
        "imfinix_wall",
        "imfinix_state",
        "sound_profiles",
        "imfinix_depth",
        "imfinix_glass",
        "imfinix_icons",
        "imfinix_clock",
        "imfinix_lock"
    )

    private fun backupFile(c: Context): File =
        File(c.getExternalFilesDir(null) ?: c.filesDir, "imfinix-backup.json")

    fun export(c: Context): String {
        return try {
            val root = JSONObject()
            var count = 0
            PREFS_FILES.forEach { name ->
                val sp: SharedPreferences = c.getSharedPreferences(name, Context.MODE_PRIVATE)
                val all = sp.all
                if (all.isNotEmpty()) {
                    val obj = JSONObject()
                    all.forEach { (k, v) ->
                        when (v) {
                            is Boolean -> obj.put(k, v)
                            is Int -> obj.put(k, v)
                            is Long -> obj.put(k, v)
                            is Float -> obj.put(k, v.toDouble())
                            is String -> obj.put(k, v)
                            else -> {}
                        }
                        count++
                    }
                    root.put(name, obj)
                }
            }
            root.put("_app", "imfinixlook")
            root.put("_version", 1)
            backupFile(c).writeText(root.toString(2))
            "Backup salvo em Android/data/com.imfinix.look/files/imfinix-backup.json ($count configurações) ✅"
        } catch (e: Exception) {
            "Falhou: ${e.message}"
        }
    }

    fun backupExists(c: Context): Boolean = backupFile(c).exists()

    fun restore(c: Context): String {
        return try {
            val f = backupFile(c)
            if (!f.exists()) return "Nenhum backup encontrado. Faça um backup primeiro."
            val root = JSONObject(f.readText())
            if (root.optString("_app") != "imfinixlook") return "Esse arquivo não é um backup do ImfinixLook."
            var count = 0
            PREFS_FILES.forEach { name ->
                val obj = root.optJSONObject(name) ?: return@forEach
                val editor = c.getSharedPreferences(name, Context.MODE_PRIVATE).edit()
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val v = obj.get(k)
                    when (v) {
                        is Boolean -> editor.putBoolean(k, v)
                        is Int -> editor.putInt(k, v)
                        is Long -> editor.putLong(k, v)
                        is Double -> editor.putFloat(k, v.toFloat())
                        is String -> editor.putString(k, v)
                        else -> {}
                    }
                    count++
                }
                editor.apply()
            }
            "Backup restaurado ($count configurações). Reabra as telas pra ver tudo aplicado ✅"
        } catch (e: Exception) {
            "Falhou ao restaurar: ${e.message}"
        }
    }
}
