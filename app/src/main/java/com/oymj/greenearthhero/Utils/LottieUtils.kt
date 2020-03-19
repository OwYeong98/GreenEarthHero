package com.oymj.greenearthhero.Utils

import android.os.Handler
import com.airbnb.lottie.LottieAnimationView

object LottieUtils {

    fun startAnimationWithDelay(view: LottieAnimationView, startDelayMillis: Long): Unit {
        Handler().postDelayed({
            view.playAnimation()
        }, startDelayMillis)
    }
}