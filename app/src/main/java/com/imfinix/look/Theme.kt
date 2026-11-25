package com.imfinix.look

import android.content.Context
import android.graphics.Color

object Theme {

    data class Palette(val id: String, val name: String, val emoji: String, val a: Int, val b: Int)

    val palettes = listOf(
        Palette("indigo", "Índigo", "💠", Color.parseColor("#6C8CFF"), Color.parseColor("#9D6CFF")),
        Palette("esmeralda", "Esmeralda", "🌿", Color.parseColor("#34D399"), Color.parseColor("#22D3EE")),
        Palette("ambar", "Âmbar", "🌅", Color.parseColor("#FBBF24"), Color.parseColor("#F97316")),
        Palette("rosa", "Rosa", "🌸", Color.parseColor("#F472B6"), Color.parseColor("#A78BFA")),
        Palette("ciano", "Ciano", "🧊", Color.parseColor("#22D3EE"), Color.parseColor("#60A5FA")),
        Palette("rubi", "Rubi", "🔥", Color.parseColor("#F87171"), Color.parseColor("#FB7185")),
        Palette("oceano", "Oceano", "🌊", Color.parseColor("#38BDF8"), Color.parseColor("#34D399")),
        Palette("aurora2", "Aurora", "🌌", Color.parseColor("#67E8F9"), Color.parseColor("#C084FC")),
        Palette("cafe", "Café", "☕", Color.parseColor("#E0B589"), Color.parseColor("#A98467"))
    )

    private const val PREFS = "imfinix_theme"
    private const val KEY = "palette"

    fun current(c: Context): Palette {
        val id = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "indigo")
        return palettes.firstOrNull { it.id == id } ?: palettes[0]
    }

    fun set(c: Context, id: String) {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, id).apply()
    }

    fun liquid(c: Context): Boolean = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("liquid", false)

    fun setLiquid(c: Context, v: Boolean) {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("liquid", v).apply()
    }
}
