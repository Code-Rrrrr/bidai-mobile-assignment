
package com.bid.ai.ui.login

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable

class LoginBackgroundDrawable : Drawable() {

    private val blue = Color.rgb(0, 99, 230)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = blue
        style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        val b = bounds
        val w = b.width().toFloat()
        val h = b.height().toFloat()

        canvas.drawColor(Color.WHITE)

        // Blue top-right wave.
        val top = Path().apply {
            moveTo(w * 0.20f, 0f)
            lineTo(w, 0f)
            lineTo(w, h * 0.28f)
            cubicTo(w * 0.88f, h * 0.27f, w * 0.80f, h * 0.12f, w * 0.55f, h * 0.07f)
            cubicTo(w * 0.37f, h * 0.04f, w * 0.23f, h * 0.02f, w * 0.20f, 0f)
            close()
        }
        canvas.drawPath(top, paint)

        // Blue bottom-left wave.
        val bottom = Path().apply {
            moveTo(0f, h * 0.75f)
            cubicTo(w * 0.08f, h * 0.78f, w * 0.10f, h * 0.88f, w * 0.33f, h * 0.94f)
            lineTo(w * 0.62f, h)
            lineTo(0f, h)
            close()
        }
        canvas.drawPath(bottom, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = android.graphics.PixelFormat.OPAQUE
}
