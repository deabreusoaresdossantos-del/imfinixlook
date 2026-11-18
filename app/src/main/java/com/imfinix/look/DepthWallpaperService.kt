package com.imfinix.look

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import java.util.Calendar

class DepthWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = DepthEngineImpl()

    inner class DepthEngineImpl : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private var shown = false
        private var rev = -1
        private var bg: Bitmap? = null
        private var fg: Bitmap? = null

        private val tick = object : Runnable {
            override fun run() {
                drawFrame()
                if (shown) handler.postDelayed(this, if (DepthEngine.prefs(applicationContext).getInt("style", 0) == 8) 1000L else 20000L)
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            shown = visible
            handler.removeCallbacks(tick)
            if (visible) handler.post(tick)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            drawFrame()
        }

        override fun onDestroy() {
            handler.removeCallbacks(tick)
            super.onDestroy()
        }

        private fun reload() {
            val p = DepthEngine.prefs(applicationContext)
            val r = p.getInt("rev", 0)
            if (r == rev && (bg != null || !DepthEngine.hasPhoto(applicationContext))) return
            rev = r
            try {
                val raw = BitmapFactory.decodeFile(DepthEngine.bgFile(applicationContext).absolutePath)
                bg = if (raw != null) DepthEngine.blur(raw, p.getInt("blur", 0)) else null
                val f = DepthEngine.fgFile(applicationContext)
                fg = if (f.exists()) BitmapFactory.decodeFile(f.absolutePath) else null
            } catch (e: Throwable) {
                bg = null
                fg = null
            }
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            var cv: Canvas? = null
            try {
                cv = holder.lockCanvas()
                if (cv != null) {
                    reload()
                    DepthEngine.drawScene(applicationContext, cv, cv.width, cv.height, bg, fg, Calendar.getInstance())
                }
            } catch (e: Throwable) {
            } finally {
                if (cv != null) {
                    try {
                        holder.unlockCanvasAndPost(cv)
                    } catch (e: Throwable) {
                    }
                }
            }
        }
    }
}
