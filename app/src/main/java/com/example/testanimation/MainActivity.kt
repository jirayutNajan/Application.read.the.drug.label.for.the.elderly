package com.example.testanimation

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.testanimation.ml.Newmedicines
import com.google.android.material.imageview.ShapeableImageView
import com.lyrebirdstudio.croppylib.Croppy
import com.lyrebirdstudio.croppylib.main.CropRequest
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder


class MainActivity : AppCompatActivity() {
    var imageSize = 224
    lateinit var bitmap: Bitmap
    lateinit var imageView: ShapeableImageView
    lateinit var findDrug: Button
    lateinit var file: File
    lateinit var uri: Uri
    lateinit var currentPhotoPath: String
    private var photoIsTaken: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.drug_image)
        findDrug = findViewById(R.id.findDrug)

        enableRuntimePermission()

        val captureButton: Button = findViewById(R.id.capture)
        captureButton.setOnClickListener {

            val fileName: String = "photo"
            val storageDirectory: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

            val imageFile: File = File.createTempFile(fileName, ".jpg", storageDirectory)
            currentPhotoPath = imageFile.absolutePath

            uri = FileProvider.getUriForFile(this, "com.example.testanimation.provider", imageFile )

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)

            startActivityForResult(takePictureIntent, 1)

        }

        findDrug.setOnClickListener {
            if(photoIsTaken){

//                val model = Medicines.newInstance(this)
//
//                // Creates inputs for reference.
//                val image = TensorImage.fromBitmap(bitmap)
//
//                // Runs model inference and gets result.
//                val outputs = model.process(image)
//                val probability = outputs.probabilityAsCategoryList
//                probability.sortBy { it.score }
//
//                val medName = probability.elementAt(7).label.toString()
//                // Releases model resources if no longer used.
//                model.close()
//
//                val viewInfo: Intent = Intent(this, Information::class.java)
//                viewInfo.putExtra("drugname", medName)
//                startActivity(viewInfo)

                val model: Newmedicines = Newmedicines.newInstance(applicationContext)

                // Creates inputs for reference.

                // Creates inputs for reference.
                val inputFeature0 =
                    TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
                val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
                byteBuffer.order(ByteOrder.nativeOrder())

                // get 1D array of 224 * 224 pixels in image

                // get 1D array of 224 * 224 pixels in image
                val intValues = IntArray(imageSize * imageSize)
                bitmap.getPixels(
                    intValues,
                    0,
                    bitmap.getWidth(),
                    0,
                    0,
                    bitmap.getWidth(),
                    bitmap.getHeight()
                )

                // iterate over pixels and extract R, G, and B values. Add to bytebuffer.

                // iterate over pixels and extract R, G, and B values. Add to bytebuffer.
                var pixel = 0
                for (i in 0 until imageSize) {
                    for (j in 0 until imageSize) {
                        val `val` = intValues[pixel++] // RGB
                        byteBuffer.putFloat((`val` shr 16 and 0xFF) * (1f / 255f))
                        byteBuffer.putFloat((`val` shr 8 and 0xFF) * (1f / 255f))
                        byteBuffer.putFloat((`val` and 0xFF) * (1f / 255f))
                    }
                }

                inputFeature0.loadBuffer(byteBuffer)

                // Runs model inference and gets result.

                // Runs model inference and gets result.
                val outputs: Newmedicines.Outputs = model.process(inputFeature0)
                val outputFeature0: TensorBuffer = outputs.getOutputFeature0AsTensorBuffer()

                val confidences = outputFeature0.floatArray
                // find the index of the class with the biggest confidence.
                // find the index of the class with the biggest confidence.
                var maxPos = 0
                var maxConfidence = 0f
                for (i in confidences.indices) {
                    if (confidences[i] > maxConfidence) {
                        maxConfidence = confidences[i]
                        maxPos = i
                    }
                }
                val classes = arrayOf("COUNTERPAIN",
                    "TYLENOL 500",
                    "SARA",
                    "FLYING RABBIT MIST SALOL ET MENTHOL",
                    "STREPSILS THROAT IRRITATION & COUGH",
                    "SOLMAX CAPSULE",
                    "COUGHING MIXTURE",
                    "AIR - X"
                )



                // Releases model resources if no longer used.


                // Releases model resources if no longer used.
                model.close()
                val viewInfo: Intent = Intent(this, Information::class.java)
                viewInfo.putExtra("drugname",classes[maxPos])
                startActivity(viewInfo)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1){
            val cropRequest = CropRequest.Auto(sourceUri = uri, requestCode = 101)
            Croppy.start(this, cropRequest)
        }

        if(requestCode == 100){
            uri = data?.data!!
            val cropRequest = CropRequest.Auto(sourceUri = uri, requestCode = 101)
            Croppy.start(this, cropRequest)
        }

        if(requestCode == 101){
            if (data != null) {
                bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, data.data)
                bitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, false)
                imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 3000, 4300,false))
                photoIsTaken = true
                findDrug.background = ContextCompat.getDrawable(this, R.drawable.capture_button)
            }
        }
    }

    private fun enableRuntimePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this@MainActivity,android.Manifest.permission.CAMERA
            )){
            Toast.makeText(this@MainActivity,
                "Camera Permission allows us to Camera App",
                Toast.LENGTH_SHORT).show()
        }
        else{
            ActivityCompat.requestPermissions(this@MainActivity,
                arrayOf(android.Manifest.permission.CAMERA),RequestPermissionCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RequestPermissionCode-> if (grantResults.size>0
                && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this@MainActivity,
                    "Permission Granted , Now your application can access Camera",
                    Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this@MainActivity,
                    "Permission Granted , Now your application can not  access Camera",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object{
        const val RequestPermissionCode = 111
    }
}