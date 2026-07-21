package com.example.cncwood

import java.io.Serializable

data class GCodeFile(
    val id: String,
    val filename: String,
    val content: String,
    val imagePath: String,
    val width: Double,
    val height: Double,
    val depth: Double,
    val createdAt: Long,
    val contourCount: Int
) : Serializable

data class ContourPoint(
    val x: Double,
    val y: Double,
    val isSelected: Boolean = false
) : Serializable

data class GCodeFormat(
    val name: String,
    val description: String,
    val rapidCommand: String = "G00",
    val linearCommand: String = "G01",
    val shouldIncludeZMovement: Boolean = true,
    val shouldIncludeSpeedControl: Boolean = true,
    val shouldIncludeSpindleControl: Boolean = true,
    val feedRate: Int = 100,
    val spindleSpeed: Int = 1000
) : Serializable {
    companion object {
        fun getDefaultFormats(): List<GCodeFormat> {
            return listOf(
                GCodeFormat(
                    name = "Standard G-Code",
                    description = "G-Code استاندارد",
                    feedRate = 100,
                    spindleSpeed = 1000
                ),
                GCodeFormat(
                    name = "Mach3 Compatible",
                    description = "G-Code سازگار با Mach3",
                    feedRate = 80,
                    spindleSpeed = 800
                ),
                GCodeFormat(
                    name = "LinuxCNC Compatible",
                    description = "G-Code سازگار با LinuxCNC",
                    feedRate = 120,
                    spindleSpeed = 1200
                )
            )
        }
    }
}
