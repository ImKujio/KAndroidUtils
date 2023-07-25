package me.kujio.android.kandroidutils.loading

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import me.kujio.android.kandroidutils.R
import me.kujio.android.kandroidutils.dp
import me.kujio.android.kandroidutils.primaryColor


abstract class Object(
    protected val size: Float,
    protected val center: PointF,
    protected val color: Int,
) {
    private lateinit var animator: Animator

    protected val paint = Paint().apply {
        isAntiAlias = true
        color = this@Object.color
        alpha = 125
    }

    fun setAnimator(block: Object.() -> Animator) {
        animator = block()
        animator.start()
    }

    fun setAlpha(value: Int) {
        paint.alpha = value
    }

    abstract fun draw(canvas: Canvas)

    fun recycle() {
        animator.cancel()
    }
}

class Circle(size: Float, center: PointF, color: Int) : Object(size, center, color) {
    private val radius = size / 10f

    override fun draw(canvas: Canvas) {
        canvas.drawCircle(center.x, radius, radius, paint)
    }
}

class Line(size: Float, center: PointF, color: Int) : Object(size, center, color) {
    private val lineWidth = size / 8.0f
    private val start = PointF(center.x, center.y - size / 2f)
    private val end = PointF(center.x, start.y + 2 * lineWidth)

    init {
        paint.strokeWidth = lineWidth
    }

    override fun draw(canvas: Canvas) {
        canvas.drawLine(start.x, start.y, end.x, end.y, paint)
    }

}

class KLoadingView : View {
    private val defSize = 20.dp
    private lateinit var objects: Array<Object>
    private val count = 8
    private val center: PointF get() = PointF(width / 2f, height / 2f)

    var color = context.primaryColor
    var type = Type.LINE

    enum class Type {
        LINE, CIRCLE
    }

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        if (attrs == null) return
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.KLoadingView)
        type = if (typedArray.getInt(R.styleable.KLoadingView_type, 1) == 1) Type.CIRCLE else Type.LINE
        color = typedArray.getColor(R.styleable.KLoadingView_color, context.primaryColor)
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredWidth = resolveSize(defSize, widthMeasureSpec)
        val measuredHeight = resolveSize(defSize, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        initializeObjects()
    }

    private fun initializeObjects() {
        val size = width.coerceAtMost(height).toFloat()
        objects = Array(count) {
            val obj = if (type == Type.CIRCLE) Circle(size, center, color)
            else Line(size, center, color)
            obj.setAnimator {
                ValueAnimator.ofInt(125, 255, 125).apply {
                    repeatCount = ValueAnimator.INFINITE
                    duration = 1000
                    startDelay = (it * 120).toLong()
                    addUpdateListener { animation ->
                        obj.setAlpha(animation.animatedValue as Int)
                        invalidate()
                    }
                }
            }
            obj
        }
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) return
        objects.forEachIndexed { i, obj ->
            canvas.save()
            canvas.rotate((360 / count * i).toFloat(), center.x, center.y)
            obj.draw(canvas)
            canvas.restore()
        }
    }

}

