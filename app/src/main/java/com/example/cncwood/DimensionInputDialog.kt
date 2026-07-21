package com.example.cncwood

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import android.os.Bundle

class DimensionInputDialog(
    private val context: Context,
    private val onDimensionsConfirmed: (width: Double, height: Double, depth: Double) -> Unit
) : AppCompatDialogFragment() {

    private var etWidth: EditText? = null
    private var etHeight: EditText? = null
    private var etDepth: EditText? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): android.app.Dialog {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_dimensions, null)

        etWidth = view.findViewById(R.id.etWidth)
        etHeight = view.findViewById(R.id.etHeight)
        etDepth = view.findViewById(R.id.etDepth)

        etWidth?.setText("100")
        etHeight?.setText("100")
        etDepth?.setText("5")

        val dialog = AlertDialog.Builder(context)
            .setTitle("ابعاد قطعه را وارد کنید")
            .setView(view)
            .setPositiveButton("تایید") { _, _ ->
                validateAndConfirm()
            }
            .setNegativeButton("لغو") { dialog, _ ->
                dialog.cancel()
            }
            .create()

        return dialog
    }

    private fun validateAndConfirm() {
        val widthStr = etWidth?.text.toString().trim()
        val heightStr = etHeight?.text.toString().trim()
        val depthStr = etDepth?.text.toString().trim()

        if (widthStr.isEmpty() || heightStr.isEmpty() || depthStr.isEmpty()) {
            Toast.makeText(context, "لطفاً تمام فیلدها را پر کنید", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val width = widthStr.toDouble()
            val height = heightStr.toDouble()
            val depth = depthStr.toDouble()

            if (width <= 0 || height <= 0 || depth <= 0) {
                Toast.makeText(context, "مقادیر باید بزرگتر از صفر باشند", Toast.LENGTH_SHORT).show()
                return
            }

            onDimensionsConfirmed(width, height, depth)
            dismiss()
        } catch (e: NumberFormatException) {
            Toast.makeText(context, "لطفاً اعداد صحیح وارد کنید", Toast.LENGTH_SHORT).show()
        }
    }
}

class GCodeFormatDialog(
    private val formats: List<GCodeFormat>,
    private val onFormatSelected: (format: GCodeFormat) -> Unit
) : AppCompatDialogFragment() {

    private var selectedFormat: GCodeFormat = formats.first()

    override fun onCreateDialog(savedInstanceState: Bundle?): android.app.Dialog {
        val spinner = android.widget.Spinner(requireContext())
        
        val formatNames = formats.map { it.name }
        val adapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            formatNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        
        spinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedFormat = formats[position]
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })

        return AlertDialog.Builder(requireContext())
            .setTitle("فرمت G-Code را انتخاب کنید")
            .setView(spinner)
            .setPositiveButton("تایید") { _, _ ->
                onFormatSelected(selectedFormat)
                dismiss()
            }
            .setNegativeButton("لغو") { dialog, _ ->
                dialog.cancel()
            }
            .create()
    }
}
