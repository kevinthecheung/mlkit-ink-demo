package com.example.digitalink

import android.graphics.*
import android.graphics.drawable.Drawable

class InkDrawable(private val paint: Paint) : Drawable() {

    private class InkPoint(val x: Float, val y: Float)
    private var strokes = mutableListOf(mutableListOf<InkPoint>())

    fun addPoint(x: Float, y: Float) {
        strokes.last().add(InkPoint(x, y))
        invalidateSelf()
    }

    fun newStroke() {
        strokes.add(mutableListOf())
    }

    fun reset() {
        strokes = mutableListOf(mutableListOf())
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        for (stroke in strokes) {
            if (stroke.size == 1) {
                canvas.drawPoint(stroke[0].x, stroke[0].y, paint)
            } else if (stroke.size > 1) {
                var lastPt = stroke[0]
                for (pt in stroke.slice(1 until stroke.size)) {
                    canvas.drawLine(lastPt.x, lastPt.y, pt.x, pt.y, paint)
                    lastPt = pt
                }
            }
        }
    }

    override fun getOpacity(): Int = PixelFormat.OPAQUE

    override fun setAlpha(alpha: Int) {
        throw IllegalStateException("No.")
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        throw IllegalStateException("No.")
    }

}