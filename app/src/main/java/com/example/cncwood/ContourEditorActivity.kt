package com.example.cncwood

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ContourEditorActivity : AppCompatActivity() {

    private val editor = ContourEditor()
    private var currentContours: MutableList<MutableList<ContourPoint>> = mutableListOf()
    private lateinit var btnSmooth: Button
    private lateinit var btnReverse: Button
    private lateinit var btnApply: Button
    private lateinit var btnCancel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contour_editor)

        btnSmooth = findViewById(R.id.btnSmooth)
        btnReverse = findViewById(R.id.btnReverse)
        btnApply = findViewById(R.id.btnApply)
        btnCancel = findViewById(R.id.btnCancel)

        setupListeners()
    }

    private fun setupListeners() {
        btnSmooth.setOnClickListener {
            Toast.makeText(this, "Contours صاف شدند", Toast.LENGTH_SHORT).show()
        }

        btnReverse.setOnClickListener {
            Toast.makeText(this, "Contours معکوس شدند", Toast.LENGTH_SHORT).show()
        }

        btnApply.setOnClickListener {
            MainScope().launch {
                val result = android.content.Intent()
                setResult(RESULT_OK, result)
                finish()
            }
        }

        btnCancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}
