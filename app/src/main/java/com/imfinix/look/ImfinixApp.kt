package com.imfinix.look

import android.app.Application
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImfinixApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashLogger.install(this)
    }
}

object CrashLogger {

    private const val FILE = "crash.log"

    fun install(app: Application) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                val stamp = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US).format(Date())
                val entry = "===== $stamp =====\nThread: ${thread.name}\n${sw}\n\n"
                File(app.filesDir, FILE).appendText(entry)
            } catch (_: Exception) {
            }
            previous?.uncaughtException(thread, throwable)
        }
    }

    fun hasLog(app: Application): Boolean {
        val f = File(app.filesDir, FILE)
        return f.exists() && f.length() > 0
    }

    fun read(app: Application): String {
        val f = File(app.filesDir, FILE)
        if (!f.exists()) return ""
        val text = f.readText()
        return if (text.length > 3500) "..." + text.takeLast(3500) else text
    }

    fun clear(app: Application) {
        try { File(app.filesDir, FILE).delete() } catch (_: Exception) {}
    }
}
