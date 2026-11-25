package com.imfinix.look

import android.content.Context
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object ShizukuShell {

    const val PKG = "moe.shizuku.privileged.api"

    fun installed(c: Context): Boolean = try {
        c.packageManager.getPackageInfo(PKG, 0)
        true
    } catch (e: Exception) {
        false
    }

    fun binderAlive(): Boolean = try {
        Shizuku.pingBinder()
    } catch (e: Exception) {
        false
    }

    fun permissionGranted(): Boolean = try {
        binderAlive() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    } catch (e: Exception) {
        false
    }

    fun requestPermission(code: Int = 700) {
        try {
            Shizuku.requestPermission(code)
        } catch (_: Exception) {
        }
    }

    /** Runs a shell command with Shizuku (shell uid) privileges. Returns exit code + output. */
    fun run(cmd: String): Pair<Int, String> {
        return try {
            val m = Shizuku::class.java.getDeclaredMethod(
                "newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java
            )
            m.isAccessible = true
            val p = m.invoke(null, arrayOf("sh", "-c", cmd), null, null) as Process
            val out = BufferedReader(InputStreamReader(p.inputStream)).readText()
            val err = BufferedReader(InputStreamReader(p.errorStream)).readText()
            val finished = p.waitFor(8, java.util.concurrent.TimeUnit.SECONDS)
            if (!finished) {
                p.destroy()
                return Pair(-2, "tempo esgotado")
            }
            Pair(p.exitValue(), (out + err).trim())
        } catch (e: Exception) {
            Pair(-1, e.message ?: "erro")
        }
    }

    fun hasSecureSettings(c: Context): Boolean =
        c.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == PackageManager.PERMISSION_GRANTED

    fun grantSecureSettings(c: Context): Boolean {
        val (code, out) = run("pm grant ${c.packageName} android.permission.WRITE_SECURE_SETTINGS")
        return code == 0 || hasSecureSettings(c)
    }
}
