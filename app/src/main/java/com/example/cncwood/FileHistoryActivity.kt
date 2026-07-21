package com.example.cncwood

import android.os.Bundle
import android.widget.ListView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileHistoryActivity : AppCompatActivity() {

    private lateinit var db: GCodeDatabase
    private lateinit var lvFileHistory: ListView
    private lateinit var btnExport: Button
    private lateinit var btnDelete: Button
    private lateinit var tvTotalFiles: TextView
    private lateinit var tvTotalContours: TextView

    private var selectedFile: GCodeFile? = null
    private var filesList: List<GCodeFile> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_history)

        db = GCodeDatabase(this)

        lvFileHistory = findViewById(R.id.lvFileHistory)
        btnExport = findViewById(R.id.btnExport)
        btnDelete = findViewById(R.id.btnDelete)
        tvTotalFiles = findViewById(R.id.tvTotalFiles)
        tvTotalContours = findViewById(R.id.tvTotalContours)

        loadFileHistory()
        setupListeners()
    }

    private fun loadFileHistory() {
        filesList = db.getAllGCodeFiles()

        val fileNames = filesList.map { file ->
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(Date(file.createdAt))
            "${file.filename} - $date"
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, fileNames)
        lvFileHistory.adapter = adapter

        updateStatistics()

        lvFileHistory.setOnItemClickListener { _, _, position, _ ->
            selectedFile = filesList[position]
        }
    }

    private fun setupListeners() {
        btnExport.setOnClickListener {
            if (selectedFile != null) {
                Toast.makeText(this, "فایل صادر شد", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "لطفاً یک فایل انتخاب کنید", Toast.LENGTH_SHORT).show()
            }
        }

        btnDelete.setOnClickListener {
            if (selectedFile != null) {
                AlertDialog.Builder(this)
                    .setTitle("حذف فایل")
                    .setMessage("آیا مطمئن هستید؟")
                    .setPositiveButton("بله") { _, _ ->
                        db.deleteGCodeFile(selectedFile!!.id)
                        Toast.makeText(this, "فایل حذف شد", Toast.LENGTH_SHORT).show()
                        selectedFile = null
                        loadFileHistory()
                    }
                    .setNegativeButton("خیر", null)
                    .show()
            } else {
                Toast.makeText(this, "لطفاً یک فایل انتخاب کنید", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateStatistics() {
        val stats = db.getStatistics()
        val totalFiles = stats["totalFiles"] as? Int ?: 0
        val totalContours = stats["totalContours"] as? Int ?: 0

        tvTotalFiles.text = "کل فایل‌ها: $totalFiles"
        tvTotalContours.text = "کل Contours: $totalContours"
    }
}
