package com.example.cncwood

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val REQUEST_PERMISSION_CAMERA = 1001
    private val REQUEST_IMAGE_CAPTURE = 1003
    private val REQUEST_IMAGE_PICK = 1004
    private val REQUEST_PREVIEW_3D = 1005
    private val REQUEST_CONTOUR_EDIT = 1006

    private var currentImageBitmap: Bitmap? = null
    private var currentContours: List<List<Pair<Double, Double>>>? = null
    private lateinit var db: GCodeDatabase
    private lateinit var prefManager: PreferenceManager

    private lateinit var ivPreview: ImageView
    private lateinit var btnCamera: Button
    private lateinit var btnGallery: Button
    private lateinit var btnGenerateGCode: Button
    private lateinit var btnClear: Button
    private lateinit var btnSettings: Button
    private lateinit var btnHistory: Button
    private lateinit var btnEditContour: Button
    private lateinit var btnPreview3D: Button
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = GCodeDatabase(this)
        prefManager = PreferenceManager(this)

        ivPreview = findViewById(R.id.ivPreview)
        btnCamera = findViewById(R.id.btnCamera)
        btnGallery = findViewById(R.id.btnGallery)
        btnGenerateGCode = findViewById(R.id.btnGenerateGCode)
        btnClear = findViewById(R.id.btnClear)
        btnSettings = findViewById(R.id.btnSettings)
        btnHistory = findViewById(R.id.btnHistory)
        btnEditContour = findViewById(R.id.btnEditContour)
        btnPreview3D = findViewById(R.id.btnPreview3D)
        tvStatus = findViewById(R.id.tvStatus)

        if (!hasAllPermissions()) {
            requestPermissions()
        }

        btnCamera.setOnClickListener { captureImage() }
        btnGallery.setOnClickListener { pickImageFromGallery() }
        btnGenerateGCode.setOnClickListener { showGenerateOptions() }
        btnClear.setOnClickListener { clearImage() }
        btnSettings.setOnClickListener { openSettings() }
        btnHistory.setOnClickListener { openFileHistory() }
        btnEditContour.setOnClickListener { editContours() }
        btnPreview3D.setOnClickListener { preview3D() }
    }

    private fun hasAllPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), REQUEST_PERMISSION_CAMERA)
    }

    private fun captureImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_PERMISSION_CAMERA)
            return
        }

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            val photoFile = createImageFile()
            val photoURI: Uri = FileProvider.getUriForFile(this, "com.example.cncwood.fileprovider", photoFile)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        } catch (e: Exception) {
            Toast.makeText(this, "خطا: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = BitmapFactory.decodeFile(getLastPhotoPath())
                    currentImageBitmap = imageBitmap
                    ivPreview.setImageBitmap(imageBitmap)
                    tvStatus.text = "عکس با موفقیت گرفته شد"
                    currentContours = null
                }
                REQUEST_IMAGE_PICK -> {
                    data?.data?.let { uri ->
                        val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                        currentImageBitmap = imageBitmap
                        ivPreview.setImageBitmap(imageBitmap)
                        tvStatus.text = "عکس با موفقیت انتخاب شد"
                        currentContours = null
                    }
                }
                REQUEST_CONTOUR_EDIT -> {
                    @Suppress("UNCHECKED_CAST")
                    currentContours = data?.getSerializableExtra("edited_contours") as? List<List<Pair<Double, Double>>>
                    tvStatus.text = "Contours ویرایش شدند"
                }
            }
        }
    }

    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}_",
            ".jpg",
            storageDir
        )
    }

    private fun getLastPhotoPath(): String {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val files = storageDir?.listFiles() ?: arrayOf()
        return if (files.isNotEmpty()) {
            files.maxByOrNull { it.lastModified() }?.absolutePath ?: ""
        } else {
            ""
        }
    }

    private fun showGenerateOptions() {
        if (currentImageBitmap == null) {
            Toast.makeText(this, "لطفاً ابتدا یک عکس انتخاب کنید", Toast.LENGTH_SHORT).show()
            return
        }

        val formats = GCodeFormat.getDefaultFormats()
        val dialog = GCodeFormatDialog(formats) { selectedFormat ->
            showDimensionDialog(selectedFormat)
        }
        dialog.show(supportFragmentManager, "FormatDialog")
    }

    private fun showDimensionDialog(format: GCodeFormat) {
        val dialog = DimensionInputDialog(this) { width, height, depth ->
            generateGCode(width, height, depth, format)
        }
        dialog.show(supportFragmentManager, "DimensionDialog")
    }

    private fun generateGCode(width: Double, height: Double, depth: Double, format: GCodeFormat) {
        currentImageBitmap?.let { bitmap ->
            MainScope().launch {
                try {
                    tvStatus.text = "در حال تولید G-Code..."

                    val gcode = withContext(Dispatchers.Default) {
                        val processor = ImageProcessor()
                        val edges = processor.detectEdges(bitmap)
                        val contours = mutableListOf<List<Pair<Double, Double>>>()

                        var y = 0
                        val visited = Array(edges.size) { BooleanArray(edges[0].size) }
                        
                        for (row in 0 until edges.size) {
                            for (col in 0 until edges[0].size) {
                                if (edges[row][col] > 0 && !visited[row][col]) {
                                    val contour = traceContour(edges, visited, col, row)
                                    if (contour.size > 10) {
                                        val scaledContour = scaleContour(contour, edges, width, height)
                                        contours.add(scaledContour)
                                    }
                                }
                            }
                        }

                        currentContours = contours

                        val generator = AdvancedGCodeGenerator()
                        generator.generateGCodeWithFormat(contours, width, height, depth, format)
                    }

                    saveGCode(gcode, currentContours?.size ?: 0)

                    tvStatus.text = "G-Code با موفقیت تولید شد!"
                    Toast.makeText(this@MainActivity, "G-Code ذخیره شد", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    tvStatus.text = "خطا: ${e.message}"
                    Toast.makeText(this@MainActivity, "خطا: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun traceContour(edges: Array<IntArray>, visited: Array<BooleanArray>, startX: Int, startY: Int): List<Pair<Int, Int>> {
        val contour = mutableListOf<Pair<Int, Int>>()
        val queue = mutableListOf(Pair(startX, startY))
        visited[startY][startX] = true

        while (queue.isNotEmpty()) {
            val (x, y) = queue.removeAt(0)
            contour.add(Pair(x, y))

            for (dy in -1..1) {
                for (dx in -1..1) {
                    if (dx == 0 && dy == 0) continue
                    val nx = x + dx
                    val ny = y + dy
                    if (nx in edges[0].indices && ny in edges.indices && edges[ny][nx] > 0 && !visited[ny][nx]) {
                        visited[ny][nx] = true
                        queue.add(Pair(nx, ny))
                    }
                }
            }
        }

        return contour
    }

    private fun scaleContour(contour: List<Pair<Int, Int>>, edges: Array<IntArray>, physicalWidth: Double, physicalHeight: Double): List<Pair<Double, Double>> {
        val imageHeight = edges.size
        val imageWidth = edges[0].size
        val scaleX = physicalWidth / imageWidth
        val scaleY = physicalHeight / imageHeight

        return contour.map { (x, y) -> Pair(x * scaleX, y * scaleY) }
    }

    private fun saveGCode(gcode: String, contourCount: Int) {
        val filename = "gcode_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.nc"
        val file = File(getExternalFilesDir(null), filename)
        file.writeText(gcode)

        db.saveGCodeFile(
            content = gcode,
            imagePath = "",
            width = 100.0,
            height = 100.0,
            depth = 5.0,
            contourCount = contourCount
        )
    }

    private fun clearImage() {
        currentImageBitmap = null
        currentContours = null
        ivPreview.setImageBitmap(null)
        tvStatus.text = "آماده برای شروع"
    }

    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun openFileHistory() {
        startActivity(Intent(this, FileHistoryActivity::class.java))
    }

    private fun editContours() {
        if (currentContours == null) {
            Toast.makeText(this, "لطفاً ابتدا G-Code را تولید کنید", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, ContourEditorActivity::class.java)
        intent.putExtra("contours", currentContours as java.io.Serializable)
        startActivityForResult(intent, REQUEST_CONTOUR_EDIT)
    }

    private fun preview3D() {
        if (currentContours == null) {
            Toast.makeText(this, "لطفاً ابتدا G-Code را تولید کنید", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, Preview3DActivity::class.java)
        intent.putExtra("contours", currentContours as java.io.Serializable)
        intent.putExtra("width", 100.0)
        intent.putExtra("height", 100.0)
        intent.putExtra("depth", 5.0)
        startActivity(intent)
    }
}
