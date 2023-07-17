package me.kujio.android.kandroidutils

import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator

fun animatorDecelerate(start:Float = 0f, end:Float = 1f, duration : Long = DisPlay.animationDuration, update: (value:Float) -> Unit){
    val animator = ValueAnimator.ofFloat(start,end)
    animator.duration = duration
    animator.interpolator = DecelerateInterpolator()
    animator.addUpdateListener { value ->
        update(value.animatedValue as Float)
    }
    animator.start()
}