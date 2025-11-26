package com.hqumath.demo.utils

import android.hardware.camera2.params.RggbChannelVector
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class CameraUtil {
    companion object {
        fun KelvinToRggb(kelvin: Int): RggbChannelVector {
            val temperature = kelvin / 100.0

            val red = when {
                temperature <= 66 -> 255.0
                else -> {
                    val t = temperature - 60
                    329.698727446 * t.pow(-0.1332047592)
                }
            }

            val green = when {
                temperature <= 66 -> {
                    99.4708025861 * ln(temperature) - 161.1195681661
                }

                else -> {
                    val t = temperature - 60
                    288.1221695283 * t.pow(-0.0755148492)
                }
            }

            val blue = when {
                temperature >= 66 -> 255.0
                else -> {
                    if (temperature <= 19) 0.0
                    else 138.5177312231 * ln(temperature - 10) - 305.0447927307
                }
            }

            fun clamp(x: Double) = max(0.0, min(255.0, x)) / 255.0

            return RggbChannelVector(
                clamp(red).toFloat(),
                clamp(green).toFloat(),
                clamp(green).toFloat(),
                clamp(blue).toFloat()
            )
        }
    }
}