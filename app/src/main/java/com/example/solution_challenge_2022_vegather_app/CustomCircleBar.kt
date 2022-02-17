package com.example.solution_challenge_2022_vegather_app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
        canvas?.drawArc(100f, 100f, 430f, 430f,  0f, 360f, false, paint)

        paint.color = Color.rgb(0x7e, 0xdb, 0xb0)
        canvas?.drawArc(100f, 100f, 430f, 430f, -90f, numProgress, false, paint)

    }

    fun setProgress (num: Float) {
        numProgress = num

        invalidate()
    }

}