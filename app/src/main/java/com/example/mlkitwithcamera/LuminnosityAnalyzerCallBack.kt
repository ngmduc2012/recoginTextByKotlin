package com.example.mlkitwithcamera

import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.text.Text

interface LuminnosityAnalyzerCallBack {
    fun onChangeTextResult(imageProxy: ImageProxy)

}