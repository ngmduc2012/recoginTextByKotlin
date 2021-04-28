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
//        val source = BitmapUtils.getBitmap(imageProxy)!!
//        val matrix = Matrix()
//        matrix.postRotate(270f)
//        mSelectedImage = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
//        val cropRect = Rect(0, 0, 500, 500)
//        imageProxy.setCropRect(cropRect)
        mSelectedImage = BitmapUtils.getBitmap(imageProxy)

        //        Spinner dropdown = findViewById(R.id.spinner);
//        mImageView.setImageBitmap(mSelectedImage);
//        val targetWidth = getImageMaxWidth()
//        val maxHeight = getImageMaxHeight()

        // Determine how much to scale down the image
//        val scaleFactor = Math.max(
//            mSelectedImage!!.width.toFloat() / getImageMaxWidth().toFloat() ,
//            mSelectedImage!!.height.toFloat() / getImageMaxHeight().toFloat()
//        )
//        val scaleShape = Math.max(
//            mSelectedImage!!.width.toFloat() / mSelectedImage!!.height.toFloat() ,
//            getImageMaxWidth().toFloat() / getImageMaxHeight().toFloat()
//        )
//        Log.d("ok", "mSelectedImage!!.width:  " + mSelectedImage!!.width.toFloat().toString())
//        Log.d("ok", "mSelectedImage!!.height:  " + mSelectedImage!!.height.toFloat().toString())
//        Log.d(
//            "ok",
//            "x = " + (((mSelectedImage?.width!!.toFloat() / mSelectedImage?.height!!.toFloat() * getImageMaxHeight().toFloat()) - getImageMaxWidth().toFloat()) / 2 * mSelectedImage?.width!!.toFloat() / mSelectedImage?.height!!.toFloat()).toString()
//        )
//        Log.d(
//            "ok",
//            "x = " + (((mSelectedImage?.width!!.toFloat() / mSelectedImage?.height!!.toFloat() * getImageMaxHeight().toFloat()) - getImageMaxWidth().toFloat())).toString()
//        )
//        Log.d(
//            "ok",
//            "x = " + (((mSelectedImage?.width!!.toFloat() / mSelectedImage?.height!!.toFloat() * getImageMaxHeight().toFloat()))).toString()
//        )
//        Log.d(
//            "ok",
//            "x = " + (((mSelectedImage?.width!!.toFloat() / mSelectedImage?.height!!.toFloat())).toString())
//        )
//        Log.d(
//            "ok",
//            "width = " + ((getImageMaxWidth().toFloat() * mSelectedImage?.width!!.toFloat() / mSelectedImage?.height!!.toFloat()).toInt()).toString())
//
//        Log.d(
//            "ok",
//            "hight = " + ((getImageMaxHeight().toFloat() * mSelectedImage?.width!!.toFloat() / mSelectedImage?.height!!.toFloat()).toInt()).toString())
//

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
//        Log.d("ok", "imageProxy!!.width:  " + imageProxy.width.toFloat().toString())
//        Log.d("ok", "imageProxy!!.height:  " + imageProxy.height.toFloat().toString())
//        Log.d("ok", "cropRect:  $cropRect")

//        Log.d("ok","scaleFactor:  " + scaleFactor.toInt().toString())
//        Log.d("ok","mSelectedImage!!.width / scaleFactor:  " + (mSelectedImage!!.width / scaleFactor).toInt().toString() )
//        Log.d("ok","mSelectedImage!!.height / scaleFactor:  " + (mSelectedImage!!.height / scaleFactor).toInt().toString() )

        mSelectedImage = resizedBitmap
//

//        Log.d("ok", "targetWidth:  " + getImageMaxWidth().toFloat().toString())
//        Log.d("ok", "maxHeight:  " + getImageMaxHeight().toFloat().toString())
//        Log.d(
//            "ok",
//            "mSelectedImage!!.width / targetWidth:  " + (mSelectedImage!!.width.toFloat() / getImageMaxWidth().toFloat()).toString()
//        )
//        Log.d(
//            "ok",
//            "mSelectedImage!!.height / maxHeight:  " + (mSelectedImage!!.height.toFloat() / getImageMaxHeight().toFloat()).toString()
//        )
//        Log.d("ok","mSelectedImage!!.width:  " + mSelectedImage!!.width.toFloat().toString() )
//        Log.d("ok","mSelectedImage!!.height:  " + mSelectedImage!!.height.toFloat().toString() )
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
            Log.d("ok", block.text)

        }
        for (i in blocks.indices) {
            val lines = blocks[i].lines
            for (j in lines.indices) {
                val elements = lines[j].elements
                for (k in elements.indices) {
                    val textGraphic: GraphicOverlay.Graphic =
                        TextGraphic(mGraphicOverlay, elements[k])
                    mGraphicOverlay?.add(textGraphic)
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