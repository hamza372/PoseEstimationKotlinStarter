package com.example.imageclassificationkotlin

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.View.OnLongClickListener
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val RESULT_LOAD_IMAGE = 123
    val IMAGE_CAPTURE_CODE = 654
    private val PERMISSION_CODE = 321
    var frame: ImageView? = null;
    var innerImage:ImageView? = null
    var resultTv: TextView? = null
    private var image_uri: Uri? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        innerImage = findViewById(R.id.imageView2)
        resultTv = findViewById(R.id.textView)
        innerImage?.setOnClickListener(View.OnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE)
        })

        frame?.setOnLongClickListener(OnLongClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED) {
                    val permission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestPermissions(permission, PERMISSION_CODE)
                } else {
                    openCamera()
                }
            } else {
                openCamera()
            }
            true
        })


       //TODO write pose estimation code


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
        ) {
            //TODO show live camera footage
            openCamera()
        } else {
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            image_uri = data.data
            doPoseEstimation()
        }
        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == Activity.RESULT_OK) {
            doPoseEstimation()
        }
    }

    //TODO perform image labeling
    fun doPoseEstimation() {
        val inputImage = uriToBitmap(image_uri!!)
        val rotated = rotateBitmap(inputImage!!)
        innerImage?.setImageBitmap(rotated)

    }

    //TODO takes URI of the image and returns bitmap
    private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
        try {
            val parcelFileDescriptor =
                contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return image
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    //TODO rotate image if image captured on sumsong devices
    //Most phone cameras are landscape, meaning if you take the photo in portrait, the resulting photos will be rotated 90 degrees.
    fun rotateBitmap(input: Bitmap): Bitmap? {
        val orientationColumn =
            arrayOf(MediaStore.Images.Media.ORIENTATION)
        val cur =
            contentResolver.query(image_uri!!, orientationColumn, null, null, null)
        var orientation = -1
        if (cur != null && cur.moveToFirst()) {
            orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]))
        }
        Log.d("tryOrientation", orientation.toString() + "")
        val rotationMatrix = Matrix()
        rotationMatrix.setRotate(orientation.toFloat())
        return Bitmap.createBitmap(
            input,
            0,
            0,
            input.width,
            input.height,
            rotationMatrix,
            true
        )
    }

    fun drawLine(canvas: Canvas, startLandmark: PoseLandmark, endLandmark: PoseLandmark, paint: Paint?) {
        val start = startLandmark.position
        val end = endLandmark.position
        canvas.drawLine(
                start.x, start.y, end.x, end.y, paint!!)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    //    val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
//    val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
//    val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
//    val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
//    val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
//    val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
//    val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
//    val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
//    val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
//    val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
//    val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
//    val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)
//    val leftPinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY)
//    val rightPinky = pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY)
//    val leftIndex = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX)
//    val rightIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX)
//    val leftThumb = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB)
//    val rightThumb = pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB)
//    val leftHeel = pose.getPoseLandmark(PoseLandmark.LEFT_HEEL)
//    val rightHeel = pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL)
//    val leftFootIndex = pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX)
//    val rightFootIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX)
//    drawLine(canvas, leftShoulder, rightShoulder, p)
//    drawLine(canvas, leftHip, rightHip, p)
//
//    // Left body
//    drawLine(canvas, leftShoulder, leftElbow, p)
//    drawLine(canvas, leftElbow, leftWrist, p)
//    drawLine(canvas, leftShoulder, leftHip, p)
//    drawLine(canvas, leftHip, leftKnee, p)
//    drawLine(canvas, leftKnee, leftAnkle, p)
//    drawLine(canvas, leftWrist, leftThumb, p)
//    drawLine(canvas, leftWrist, leftPinky, p)
//    drawLine(canvas, leftWrist, leftIndex, p)
//    drawLine(canvas, leftIndex, leftPinky, p)
//    drawLine(canvas, leftAnkle, leftHeel, p)
//    drawLine(canvas, leftHeel, leftFootIndex, p)
//
//    // Right body
//    drawLine(canvas, rightShoulder, rightElbow, p)
//    drawLine(canvas, rightElbow, rightWrist, p)
//    drawLine(canvas, rightShoulder, rightHip, p)
//    drawLine(canvas, rightHip, rightKnee, p)
//    drawLine(canvas, rightKnee, rightAnkle, p)
//    drawLine(canvas, rightWrist, rightThumb, p)
//    drawLine(canvas, rightWrist, rightPinky, p)
//    drawLine(canvas, rightWrist, rightIndex, p)
//    drawLine(canvas, rightIndex, rightPinky, p)
//    drawLine(canvas, rightAnkle, rightHeel, p)
//    drawLine(canvas, rightHeel, rightFootIndex, p)
}