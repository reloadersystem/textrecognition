package com.reloader.recognitiontext

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var textRecognizer: TextRecognizer
    private val tag: String? = "MainActivity"

    private val PERMISSION_REQUEST_CAMERA: Int = 101

    private lateinit var mCameraSource: CameraSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        requestForPermission()

        textRecognizer = TextRecognizer.Builder(this).build()

        if (!textRecognizer.isOperational) {
            Toast.makeText(
                this,
                "Dependencies are not loaded yet...please try after few moment!!",
                Toast.LENGTH_SHORT
            ).show()
            Log.d(tag, "Dependencies are downloading....try after few moment")
            return
        }

        mCameraSource = CameraSource.Builder(applicationContext, textRecognizer)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(1280, 1024)
            .setAutoFocusEnabled(true)
            .setRequestedFps(2.0f)
            .build()



        surface_camera_preview.holder.addCallback(object : SurfaceHolder.Callback {


            override fun surfaceDestroyed(p0: SurfaceHolder) {

                mCameraSource.stop()
            }

            override fun surfaceCreated(p0: SurfaceHolder) {

                try {
                    if (isCameraPermissionGranted()) {
                        mCameraSource.start(surface_camera_preview.holder)
                    } else {
                        requestForPermission()
                    }
                } catch (e: Exception) {

                    Toast.makeText(applicationContext, "Error:  ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }


            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {

            }

        })


        textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
            override fun release() {}

            override fun receiveDetections(detections: Detector.Detections<TextBlock>) {
                val items = detections.detectedItems

                if (items.size() <= 0) {
                    return
                }

                tv_result.post {
                    val stringBuilder = StringBuilder()
                    for (i in 0 until items.size()) {
                        val item = items.valueAt(i)
                        stringBuilder.append(item.value)
                        stringBuilder.append("\n")
                    }
                    tv_result.text = stringBuilder.toString()
                }
            }
        })

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {


        when (requestCode) {
            PERMISSION_REQUEST_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                } else {
                    requestForPermission()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
              // requestForPermission()
            }

        }


    }

    private fun requestForPermission() {

        if (ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.CAMERA
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CAMERA
                )
            ) {

            } else {
                // You can directly ask for the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    PERMISSION_REQUEST_CAMERA
                )

            }
        } else {

        }
    }

    private fun isCameraPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
    }


}
