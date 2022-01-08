package pt.isec.kotlin.reversi.atividades


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_fotografia.*
import pt.isec.kotlin.reversi.R
import pt.isec.kotlin.reversi.Utils
import pt.isec.kotlin.reversi.databinding.ActivityFotografiaBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

//https://developer.android.com/codelabs/camerax-getting-started#1

class FotografiaActivity : AppCompatActivity() {


    lateinit var b: ActivityFotografiaBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var data: Intent
    var tema: Int = 1

    private lateinit var cameraSelector: CameraSelector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setFullscreen(this)

        b = ActivityFotografiaBinding.inflate(layoutInflater)

        // Pedir permissões
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        tema = Utils.setTema(this)
        setContentView(b.root)
        checkTheme()


        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA


        Btn_tirarFotografia.setOnClickListener {
            takePhoto()
        }
    }

    private fun takePhoto() {

        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT,
                Locale.US).format(System.currentTimeMillis()) + ".jpg")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        var savedUri: Uri? = null


        Toast.makeText(baseContext, getString(R.string.previewphoto), Toast.LENGTH_SHORT)
            .show()
        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"

                    Log.d(TAG, msg)
                    val imagem: ImageView = findViewById(R.id.previewFoto)
                    imagem.setImageURI(Uri.parse(savedUri.toString()))
                }
            })

        data = Intent()
        data.data = Uri.fromFile(photoFile)


        Btn_SubmeterFotografia.isEnabled = true
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
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))

    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults:
        IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, getString(R.string.permissionsdenied), Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }


    fun onAlterarCamara(view: View) {
        if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        }
        startCamera()
    }

    fun onSubmeterFotografia(view: View) {

        setResult(Activity.RESULT_OK, data)
        finish()

    }


    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    fun checkTheme() {

        when (tema) {
            1 -> {
                b.BtnTirarFotografia.setBackgroundColor(Color.parseColor("#7f5539"))
                b.BtnSubmeterFotografia.setBackgroundColor(Color.parseColor("#7f5539"))
                b.AlterarCamara.setBackgroundColor(Color.parseColor("#7f5539"))
                b.AlterarCamaraTextID.setBackgroundColor(Color.parseColor("#7f5539"))
            }
            2 -> {
                b.BtnTirarFotografia.setBackgroundColor(Color.parseColor("#133A49"))
                b.BtnSubmeterFotografia.setBackgroundColor(Color.parseColor("#133A49"))
                b.AlterarCamara.setBackgroundColor(Color.parseColor("#133A49"))
                b.AlterarCamaraTextID.setBackgroundColor(Color.parseColor("#133A49"))
            }
            3 -> {
                b.BtnTirarFotografia.setBackgroundColor(Color.parseColor("#1E1015"))
                b.BtnSubmeterFotografia.setBackgroundColor(Color.parseColor("#1E1015"))
                b.AlterarCamara.setBackgroundColor(Color.parseColor("#1E1015"))
                b.AlterarCamaraTextID.setBackgroundColor(Color.parseColor("#1E1015"))
            }
        }
    }


}