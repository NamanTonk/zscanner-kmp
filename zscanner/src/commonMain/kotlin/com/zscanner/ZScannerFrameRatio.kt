package com.zscanner

sealed interface ZScannerFrameRatio {
    val ratio: Float

    object Ratio_1_1 : ZScannerFrameRatio {
        override val ratio: Float = 1.0f
    }
    object Ratio_1_2 : ZScannerFrameRatio {
        override val ratio: Float = 0.5f
    }
    object Ratio_2_1 : ZScannerFrameRatio {
        override val ratio: Float = 2.0f
    }
    data class Custom(override val ratio: Float) : ZScannerFrameRatio
}
