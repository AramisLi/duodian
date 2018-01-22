package com.afc.arkotlin

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import org.jetbrains.anko.dip

/**
 * Loading
 * Created by admin on 2018/1/22.
 */
class ArLoadingView : View {

    constructor(context: Context?) : super(context) {
        init(null, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
    }

//    private var tag = "===ArLoadingView"
    private var strokeWidth = 20f
    private var startAngle = 0f
    private var _startAngle = 0f
    private var _endAngle = 0f
    private var sweepAngle = 0f
    private var _sweepAngle = 0f

    private val loadingPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val loadingRectF = RectF()

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var li: LinearGradient? = null
    //动画值
    private var value = 0f
    private val defaultValue = 4f

    private val loadingColors = arrayOf(LoadingColor(0xff51a2ef.toInt(), 0xff50d8ee.toInt()), LoadingColor(0xffFFC125.toInt(), 0xffFFA500.toInt()),
            LoadingColor(0xffF08080.toInt(), 0xffEE5C42.toInt()), LoadingColor(0xffFFDEAD.toInt(), 0xffFF7F50.toInt()))
    private var colorTimes = 0

    private var text = "Loading"
    private val textRect = Rect()
    private var textSize = dip(15).toFloat()
    //动画
    private val startAnimator = ValueAnimator.ofFloat(0f, defaultValue)
    private val textColorAnimator = ValueAnimator.ofFloat(0f, 1f)
    private var argbEvaluator = ArgbEvaluator()

    private fun initType(attrs: AttributeSet?, defStyleAttr: Int) {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.ArLoadingView, defStyleAttr, 0)
        strokeWidth = typedArray.getDimensionPixelSize(R.styleable.ArLoadingView_strokeWidth, strokeWidth.toInt()).toFloat()
        textSize = typedArray.getDimensionPixelSize(R.styleable.ArLoadingView_textSize, textSize.toInt()).toFloat()
        text = typedArray.getString(R.styleable.ArLoadingView_text) ?: text
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int) {
//        Log.e(tag, "init")
        initType(attrs, defStyleAttr)
        loadingPaint.style = Paint.Style.STROKE
        loadingPaint.strokeWidth = this.strokeWidth
//        Log.e(tag, "width:$width,height:$height")
        _startAngle = startAngle
        _sweepAngle = sweepAngle

        textPaint.color = loadingColors[0].endColor
        textPaint.textSize = textSize
        textPaint.getTextBounds(text, 0, text.length, textRect)
        initAnimator()
    }

    private fun initAnimator() {
        textColorAnimator.addUpdateListener {
            val startColor = if (colorTimes == 0) loadingColors[loadingColors.size - 1].endColor else loadingColors[colorTimes - 1].endColor
            val endColor = loadingColors[colorTimes].endColor
            textPaint.color = argbEvaluator.evaluate(it.animatedFraction, startColor, endColor) as Int
        }
        textColorAnimator.duration = 800

        startAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
                colorTimes++
                if (colorTimes >= loadingColors.size) colorTimes = 0
                initLinearGradient(colorTimes)
                textColorAnim()
            }

            override fun onAnimationEnd(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })
        startAnimator.interpolator = LinearInterpolator()
        startAnimator.duration = 4000
        startAnimator.repeatCount = ValueAnimator.INFINITE
        startAnimator.repeatMode = ValueAnimator.RESTART
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawLoading(canvas)
        drawText(canvas)

//        canvas?.drawLine(0f, height / 2f, width.toFloat(), height / 2f, textPaint)
//        canvas?.drawLine(width / 2f, 0f, width / 2f, height.toFloat(), textPaint)
    }

    private fun drawText(canvas: Canvas?) {
        val x = width / 2f - (textRect.width() / 2f)
        val fontMetrics = textPaint.fontMetrics
        val y = height / 2f + (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
        canvas?.drawText(text, x, y, textPaint)
    }

    private fun drawLoading(canvas: Canvas?) {
        if (li == null) initLinearGradient()
        loadingPaint.shader = li
        val length = if (width > height) height else width
        loadingRectF.set(strokeWidth / 2, strokeWidth / 2, length - strokeWidth / 2, length - strokeWidth / 2)
        canvas?.drawArc(loadingRectF, _startAngle, _sweepAngle, false, loadingPaint)
    }

    private fun startAnim(listener: (v: ValueAnimator) -> Unit) {
        startAnimator.addUpdateListener {
            listener.invoke(it)
        }
        startAnimator.start()
    }


    private fun textColorAnim() {
        textColorAnimator.start()
    }

    fun startAnim() {
        startAnim {
            value = it.animatedValue as Float
            if (value == 0f) {
                _endAngle = 0f
                _startAngle = 0f

            }
            val fraction = it.animatedFraction
            _startAngle = when {
                fraction <= 0.25f -> 180f * (value)
                fraction <= 0.50f -> 180f + 360f * (value - defaultValue * 0.25f)
                fraction <= 0.75f -> 540f + 180f * (value - defaultValue * 0.50f)
                else -> 720f + 360f * (value - defaultValue * 0.75f)

            }

            _endAngle = when {
                fraction <= 0.25f -> 360f * value
                fraction <= 0.50f -> 360f + 180f * (value - defaultValue * 0.25f)
                fraction <= 0.75f -> 540f + 360f * (value - defaultValue * 0.50f)
                else -> 900f + 180f * (value - defaultValue * 0.75f)

            }
            _sweepAngle = _endAngle - _startAngle
            invalidate()
        }

    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        startAnim()
    }

    private fun initLinearGradient(position: Int = 0) {
        li = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(),
                loadingColors[position].startColor, loadingColors[position].endColor, Shader.TileMode.CLAMP)
    }

    private inner class LoadingColor(val startColor: Int, val endColor: Int)

}