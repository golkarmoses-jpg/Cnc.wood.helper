package com.example.cncwood

import android.content.Context
import android.content.SharedPreferences
import java.io.File
import java.util.*

class GCodeDatabase(context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("gcode_history", Context.MODE_PRIVATE)
    
    private val filesDir = File(context.getExternalFilesDir(null), "gcode_history")
    
    init {
        if (!filesDir.exists()) {
            filesDir.mkdirs()
        }
    }

    fun saveGCodeFile(
        content: String,
        imagePath: String,
        width: Double,
        height: Double,
        depth: Double,
        contourCount: Int
    ): GCodeFile {
        val id = UUID.randomUUID().toString()
        val filename = "gcode_${System.currentTimeMillis()}.nc"
        val file = File(filesDir, filename)
        file.writeText(content)
        
        val gcodeFile = GCodeFile(
            id = id,
            filename = filename,
            content = content,
            imagePath = imagePath,
            width = width,
            height = height,
            depth = depth,
            createdAt = System.currentTimeMillis(),
            contourCount = contourCount
        )
        
        val editor = sharedPreferences.edit()
        val json = serializeGCodeFile(gcodeFile)
        editor.putString("gcode_$id", json)
        editor.apply()
        
        return gcodeFile
    }

    fun getAllGCodeFiles(): List<GCodeFile> {
        val files = mutableListOf<GCodeFile>()
        val allEntries = sharedPreferences.all
        
        for ((key, value) in allEntries) {
            if (key.startsWith("gcode_") && value is String) {
                try {
                    val file = deserializeGCodeFile(value)
                    files.add(file)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        
        return files.sortedByDescending { it.createdAt }
    }

    fun deleteGCodeFile(id: String): Boolean {
        val file = getGCodeFile(id) ?: return false
        val fileToDelete = File(filesDir, file.filename)
        val deleted = fileToDelete.delete()
        
        val editor = sharedPreferences.edit()
        editor.remove("gcode_$id")
        editor.apply()
        
        return deleted
    }

    fun getGCodeFile(id: String): GCodeFile? {
        val json = sharedPreferences.getString("gcode_$id", null) ?: return null
        return try {
            deserializeGCodeFile(json)
        } catch (e: Exception) {
            null
        }
    }

    fun getStatistics(): Map<String, Any> {
        val files = getAllGCodeFiles()
        return mapOf(
            "totalFiles" to files.size,
            "totalContours" to files.sumOf { it.contourCount },
            "averageWidth" to (files.map { it.width }.average().takeIf { it.isFinite() } ?: 0.0),
            "averageHeight" to (files.map { it.height }.average().takeIf { it.isFinite() } ?: 0.0),
            "averageDepth" to (files.map { it.depth }.average().takeIf { it.isFinite() } ?: 0.0)
        )
    }

    fun clearHistory() {
        filesDir.listFiles()?.forEach { it.delete() }
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    private fun serializeGCodeFile(file: GCodeFile): String {
        return "${file.id}|${file.filename}|${file.imagePath}|${file.width}|${file.height}|${file.depth}|${file.createdAt}|${file.contourCount}"
    }

    private fun deserializeGCodeFile(json: String): GCodeFile {
        val parts = json.split("|")
        val file = File(filesDir, parts[1])
        val content = if (file.exists()) file.readText() else ""
        
        return GCodeFile(
            id = parts[0],
            filename = parts[1],
            content = content,
            imagePath = parts[2],
            width = parts[3].toDouble(),
            height = parts[4].toDouble(),
            depth = parts[5].toDouble(),
            createdAt = parts[6].toLong(),
            contourCount = parts[7].toInt()
        )
    }
}
