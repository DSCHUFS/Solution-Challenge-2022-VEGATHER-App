package com.example.solution_challenge_2022_vegather_app

import android.R.attr.*
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View


class CustomCircleBar : View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    var numProgress : Float = 0.0f

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val paint = Paint()

        paint.color = Color.rgb(0xF3, 0xF3, 0xF3)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 40f

        val centerX = measuredWidth / 2
        val centerY = measuredHeight / 2
        val radius = Math.min(centerX, centerY)
        val startTop: Float = paint.strokeWidth / 2
        var mRect = RectF(
            paint.strokeWidth / 2, paint.strokeWidth / 2,
            (2 * radius - startTop).toFloat(), (2 * radius - startTop).toFloat()
        )

        //canvas?.drawArc(100f, 100f, 460f, 460f,  0f, 360f, false, paint)
        canvas?.drawArc(mRect,  0f, 360f, false, paint)

        paint.color = Color.rgb(0x7e, 0xdb, 0xb0)

        //canvas?.drawArc(100f, 100f, 460f, 460f, -90f, numProgress, false, paint)
        canvas?.drawArc(mRect, -90f, numProgress, false, paint)


    }

    fun setProgress (num: Float) {
        numProgress = num

        invalidate()
    }

}