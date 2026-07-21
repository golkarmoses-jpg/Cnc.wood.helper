package com.example.cncwood

import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefManager: PreferenceManager
    private lateinit var sbFeedRate: SeekBar
    private lateinit var sbSafeHeight: SeekBar
    private lateinit var sbSpindleSpeed: SeekBar
    private lateinit var tvFeedRateValue: TextView
    private lateinit var tvSafeHeightValue: TextView
    private lateinit var tvSpindleSpeedValue: TextView
    private lateinit var swAutoSave: Switch
    private lateinit var btnClearHistory: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefManager = PreferenceManager(this)

        sbFeedRate = findViewById(R.id.sbFeedRate)
        sbSafeHeight = findViewById(R.id.sbSafeHeight)
        sbSpindleSpeed = findViewById(R.id.sbSpindleSpeed)
        tvFeedRateValue = findViewById(R.id.tvFeedRateValue)
        tvSafeHeightValue = findViewById(R.id.tvSafeHeightValue)
        tvSpindleSpeedValue = findViewById(R.id.tvSpindleSpeedValue)
        swAutoSave = findViewById(R.id.swAutoSave)
        btnClearHistory = findViewById(R.id.btnClearHistory)

        loadSettings()
        setupListeners()
    }

    private fun loadSettings() {
        sbFeedRate.progress = prefManager.getFeedRate()
        sbSafeHeight.progress = prefManager.getSafeHeight().toInt()
        sbSpindleSpeed.progress = prefManager.getSpindleSpeed()
        swAutoSave.isChecked = prefManager.isAutoSaveEnabled()

        tvFeedRateValue.text = prefManager.getFeedRate().toString()
        tvSafeHeightValue.text = prefManager.getSafeHeight().toString()
        tvSpindleSpeedValue.text = prefManager.getSpindleSpeed().toString()
    }

    private fun setupListeners() {
        sbFeedRate.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    prefManager.setFeedRate(progress)
                    tvFeedRateValue.text = progress.toString()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        sbSafeHeight.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    prefManager.setSafeHeight(progress.toDouble())
                    tvSafeHeightValue.text = progress.toString()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        sbSpindleSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    prefManager.setSpindleSpeed(progress)
                    tvSpindleSpeedValue.text = progress.toString()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        swAutoSave.setOnCheckedChangeListener { _, isChecked ->
            prefManager.setAutoSave(isChecked)
        }

        btnClearHistory.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("پاک کردن تاریخچه")
                .setMessage("آیا مطمئن هستید؟")
                .setPositiveButton("بله") { _, _ ->
                    val db = GCodeDatabase(this)
                    db.clearHistory()
                    Toast.makeText(this, "تاریخچه پاک شد", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("خیر", null)
                .show()
        }
    }
}
