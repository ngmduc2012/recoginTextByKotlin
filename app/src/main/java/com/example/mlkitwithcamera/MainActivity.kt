package com.example.mlkitwithcamera

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.Display
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias LumaListener = (luma: Double) -> Unit

class MainActivity : AppCompatActivity(), LuminnosityAnalyzerCallBack {
    private var mSelectedImage: Bitmap? = null
    private var resizedBitmap: Bitmap? = null

    // Max width (portrait mode)
    private var mImageMaxWidth: Int? = null

    // Max height (portrait mode)
    private var mImageMaxHeight: Int? = null

    private var imageCapture: ImageCapture? = null
    private var mGraphicOverlay: GraphicOverlay? = null
    var textTest: Text? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mGraphicOverlay = findViewById(R.id.graphic_overlay)


        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.createSurfaceProvider())
                }

            imageCapture = ImageCapture.Builder()
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, LuminosityAnalyzer(this))
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun onChangeTextResult(imageProxy: ImageProxy) {


        mSelectedImage = BitmapUtils.getBitmap(imageProxy)


        if(mSelectedImage?.width!!.toFloat()/mSelectedImage?.height!!.toFloat()<1){
            mSelectedImage = Bitmap.createBitmap(
                mSelectedImage!!,
                ((mSelectedImage?.width!!.toFloat()-(getImageMaxWidth().toFloat()/getImageMaxHeight().toFloat()*mSelectedImage?.height!!.toFloat()))/2).toInt(),
                0,
                ((getImageMaxWidth().toFloat()/getImageMaxHeight().toFloat()*mSelectedImage?.height!!.toFloat())).toInt(),
                (mSelectedImage?.height!!.toFloat()).toInt()
            )
        }
        else{
            mSelectedImage = Bitmap.createBitmap(
                mSelectedImage!!,
                0,
                ((mSelectedImage?.height!!.toFloat()-(mSelectedImage?.width!!.toFloat()*getImageMaxHeight().toFloat()/getImageMaxWidth().toFloat()))/2).toInt(),
                (mSelectedImage?.width!!.toFloat()).toInt(),
                (mSelectedImage?.width!!.toFloat()*getImageMaxHeight().toFloat()/getImageMaxWidth().toFloat()).toInt()
            )
        }


        resizedBitmap = Bitmap.createScaledBitmap(
            mSelectedImage!!,
            getImageMaxWidth(),
            getImageMaxHeight(),
            true
        )

        val matrix = Matrix()
        matrix.postRotate(270f)
        mSelectedImage = Bitmap.createBitmap(resizedBitmap!!, 0, 0, resizedBitmap!!.width, resizedBitmap!!.height, matrix, true)

        val image = InputImage.fromBitmap(mSelectedImage!!, 0)
        val recognizer = TextRecognition.getClient()
        recognizer.process(image)
            .addOnSuccessListener { texts ->
                processTextRecognitionResult(texts)
            }
            .addOnFailureListener { e -> // Task failed with an exception
                e.printStackTrace()
            }
            .addOnCompleteListener { imageProxy.close() }

    }
    var line1Result: String = ""
    var line2Result: String = ""
    var line3Result: String = ""
    var textMRZResult: String = ""

    private fun findTextMRZ(text: String): String {
        var line1: String = text.substring(0, 30)
        var line2: String = text.substring(30, 60)
        var line3: String = text.substring(60, 90)
        line1 = line1.substring(0, 5)+line1.substring(5).replace("O", "0")
        line2 = line2.substring(0, 29)+line2.substring(29).replace("O", "0")
        line1 = line1.replace("K<", "<<")
        line1 = line1.replace("S<", "<<")
        line1 = line1.replace("s<", "<<")
        line1 = line1.replace("k<", "<<")
        val pattern1 = "I[A-Z]{4}[0-9]{22}+<{2}[0-9]{1}".toRegex()
        val pattern2 = "[A-Z0-9]{2,30}+<{2,20}[0-9]{1}".toRegex()
        val pattern3 = "\\w+<<(\\w+<)+<{3,15}".toRegex()

        if (pattern1.matches(line1)) {
            line1Result = line1
        }
        if (pattern2.matches(line2)) {
            line2Result = line2
        }
        if (pattern3.matches(line3)) {
            line3Result = line3
        }
        line3Result = line3
        return line1Result + line2Result + line3Result
    }


    // Ham nay tra ve kq, lay ra text
    private fun processTextRecognitionResult(texts: Text) {
        textTest = texts
        val blocks = texts.textBlocks

        if (blocks.size == 0) {
            mGraphicOverlay!!.clear()
            return
        }
        mGraphicOverlay!!.clear()
        for (block in texts.textBlocks) {
            var textIndex = block.text
            if (textIndex.length >= 90 && textIndex.startsWith("I")) {
//                Log.d("ok", textIndex)
                textIndex = textIndex.replace(" ", "").trim()
                textIndex = textIndex.replace("\n", "")
                textIndex = textIndex.replace("<S<", "<<<")
                textIndex = textIndex.replace("<K<", "<<<")
                textIndex = textIndex.replace("<k<", "<<<")
                textIndex = textIndex.replace("<s<", "<<<")
                if (textIndex.length == 90) {
//                    Log.d("ok","=" + textIndex)
                    textMRZResult = findTextMRZ(textIndex)
                    if (textMRZResult.length == 90) {
                        Log.d("ok","=====" + textMRZResult)
                        line1Result = ""
                        line2Result = ""
                        line3Result = ""


                    }
                }
            }

        }
        for (i in blocks.indices) {
            val lines = blocks[i].lines
            for (j in lines.indices) {
                val elements = lines[j].elements
                for (k in elements.indices) {
//                    Log.d("ok", "================"+elements[k].text)
                    if(elements[k].text.length >= 29 && elements[k].text.contains("<")){
                        val textGraphic: GraphicOverlay.Graphic =
                            TextGraphic(mGraphicOverlay, elements[k], getImageMaxWidth(), getImageMaxHeight())
                        mGraphicOverlay?.add(textGraphic)
                    }
                }
            }
        }

    }


    private fun getImageMaxWidth(): Int {
        if (mImageMaxWidth == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxWidth = mGraphicOverlay?.width
        }
        return mImageMaxWidth as Int
    }

    // Returns max image height, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private fun getImageMaxHeight(): Int {
        if (mImageMaxHeight == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxHeight = mGraphicOverlay?.height
        }
        return mImageMaxHeight as Int
    }


}