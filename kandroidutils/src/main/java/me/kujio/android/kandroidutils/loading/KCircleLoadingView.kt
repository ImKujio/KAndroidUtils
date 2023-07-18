package me.kujio.android.kandroidutils.loading

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import me.kujio.android.kandroidutils.dp
import me.kujio.android.kandroidutils.primaryColor


class Circle(
    private val center: PointF,
    private val radius: Float,
    private val color: Int,
    block: Circle.() -> Animator
) {

    private val paint = Paint().apply {
        isAntiAlias = true
        color = this@Circle.color
        alpha = 125
    }

    private val animator = block().apply { start() }

    fun setAlpha(value: Int) {
        paint.alpha = value
    }

    fun draw(canvas: Canvas) {
        canvas.drawCircle(center.x, center.y, radius, paint)
    }

    fun recycle() {
        animator.cancel()
    }
}

/**
 * Created by Tuyen Nguyen on 2/10/17.
 */
class KCircleLoadingView : View {
    private var color = context.primaryColor
    private val desiredSize = 40.dp
    private lateinit var circles: Array<Circle>
    private val count = 8
    private val center: PointF get() = PointF(width / 2f, height / 2f)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredWidth = resolveSize(desiredSize, widthMeasureSpec)
        val measuredHeight = resolveSize(desiredSize, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        initializeObjects()
    }

    private fun initializeObjects() {
        val size = width.coerceAtMost(height).toFloat()
        val circleRadius = size / 10.0f
        circles = Array(count) {
            Circle(PointF(center.x, circleRadius), circleRadius, color) {
                ValueAnimator.ofInt(125, 255, 125).apply {
                    repeatCount = ValueAnimator.INFINITE
                    duration = 1000
                    startDelay = (it * 120).toLong()
                    addUpdateListener { animation ->
                        this@Circle.setAlpha(animation.animatedValue as Int)
                        invalidate()
                    }
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) return
        circles.forEachIndexed { i, circle ->
            canvas.save()
            canvas.rotate((45 * i).toFloat(), center.x, center.y)
            circle.draw(canvas)
            canvas.restore()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        for (circle in circles) {
            circle.recycle()
        }
    }
}